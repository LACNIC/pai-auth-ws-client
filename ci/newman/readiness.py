"""Wait for the standard authenticated WS monitor before running Newman."""

from __future__ import annotations

import json
import re
import signal
import time
from contextlib import contextmanager
from urllib.error import HTTPError, URLError
from urllib.parse import urlsplit
from urllib.request import HTTPRedirectHandler, Request, build_opener


class NoRedirect(HTTPRedirectHandler):
    def redirect_request(self, request, fp, code, msg, headers, newurl):
        return None


BEARER_TOKEN = re.compile(r"[A-Za-z0-9._~+/-]+=*")


class ProbeDeadline(TimeoutError):
    pass


@contextmanager
def hard_timeout(seconds: float):
    """Cap the whole POSIX HTTP exchange, including trickling headers or JSON."""
    def expired(_signum, _frame):
        raise ProbeDeadline()

    previous_handler = signal.signal(signal.SIGALRM, expired)
    previous_timer = signal.setitimer(signal.ITIMER_REAL, seconds)
    try:
        yield
    finally:
        signal.setitimer(signal.ITIMER_REAL, 0)
        signal.signal(signal.SIGALRM, previous_handler)
        if previous_timer[0]:
            signal.setitimer(signal.ITIMER_REAL, *previous_timer)


def probe_status(url: str, token: str, timeout: float,
                 expected_version: str | None) -> tuple[bool, str, str | None]:
    request = Request(url, headers={"Authorization": f"Bearer {token}",
                                    "Accept": "application/json"}, method="GET")
    try:
        with hard_timeout(timeout):
            with build_opener(NoRedirect).open(request, timeout=timeout) as response:
                status = response.status
                if status != 200:
                    return False, f"HTTP {status}", None
                try:
                    payload = json.load(response)
                except (ValueError, UnicodeError):
                    return False, "invalid JSON", None
    except HTTPError as error:
        try:
            if error.code in (401, 403):
                raise ValueError(f"monitor access denied (HTTP {error.code})") from None
            if 300 <= error.code < 500:
                raise ValueError(f"monitor rejected request (HTTP {error.code})") from None
            return False, f"HTTP {error.code}", None
        finally:
            error.close()
    except (OSError, URLError, ProbeDeadline):
        return False, "connection unavailable", None

    service = payload.get("service") if isinstance(payload, dict) else None
    if not isinstance(service, dict) or service.get("availability") != "UP":
        return False, "service availability is not UP", None
    version = service.get("deployedVersion")
    if not isinstance(version, str) or not version.strip():
        return False, "service deployedVersion is missing", None
    if expected_version is not None and version != expected_version:
        return False, "service deployedVersion differs from expected version", None
    return True, "ready", version


def wait_for_readiness(base_url: str, token: str, expected_version: str | None = None,
                       timeout_seconds: float = 180, interval_seconds: float = 5
                       ) -> tuple[float, str]:
    if not token or not token.strip() or token.strip().startswith("{{"):
        raise ValueError("monitor_token is missing from the selected Newman profile")
    if not BEARER_TOKEN.fullmatch(token):
        raise ValueError("monitor_token contains invalid bearer characters")
    target = urlsplit(base_url)
    if target.scheme != "https" and target.hostname not in {"localhost", "127.0.0.1", "::1"}:
        raise ValueError("monitor_token requires HTTPS outside loopback")
    if expected_version is not None and not expected_version.strip():
        raise ValueError("expected version must not be empty")
    url = base_url.rstrip("/") + "/status/items"
    started = time.monotonic()
    deadline = started + timeout_seconds
    consecutive = 0
    last_reason = "no response"
    while True:
        remaining = deadline - time.monotonic()
        if remaining <= 0:
            raise ValueError(f"monitor readiness timed out after {timeout_seconds:g}s: {last_reason}")
        ready, last_reason, version = probe_status(url, token, min(5, remaining), expected_version)
        consecutive = consecutive + 1 if ready else 0
        if consecutive == 2:
            return round(time.monotonic() - started, 3), version
        remaining = deadline - time.monotonic()
        if remaining > 0:
            time.sleep(min(interval_seconds, remaining))
