package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import de.taimos.totp.TOTP;

class Utils2FATest {

	@BeforeEach
	void setUp() {
		// Any setup if necessary
	}

	@Test
	void testGenerateSecretKey_returnsValidSecretKey() {
		String secretKey = Utils2FA.generateSecretKey();
		assertNotNull(secretKey);
		assertTrue(secretKey.length() > 0);
	}

	@Test
	void testGetTOTPCode_returnsCode() {
		String mockSecretKey = "mockSecretKey";
		byte[] decodedBytes = new Base32().decode(mockSecretKey);
		String hexKey = Hex.encodeHexString(decodedBytes);

		try (MockedStatic<TOTP> mockedTOTP = mockStatic(TOTP.class)) {
			mockedTOTP.when(() -> TOTP.getOTP(hexKey)).thenReturn("123456");

			String totpCode = Utils2FA.getTOTPCode(mockSecretKey);
			assertEquals("123456", totpCode);
			mockedTOTP.verify(() -> TOTP.getOTP(hexKey));
		}
	}

	@Test
	void testGetGoogleAuthenticatorBarCode_returnsUrl() throws UnsupportedEncodingException {
		String secretKey = "mockSecretKey";
		String account = "testAccount";
		String issuer = "testIssuer";
		String expectedUrl = "otpauth://totp/testIssuer:testAccount?secret=mockSecretKey&issuer=testIssuer";

		try (MockedStatic<URLEncoder> mockedURLEncoder = mockStatic(URLEncoder.class)) {
			mockedURLEncoder.when(() -> URLEncoder.encode(issuer + ":" + account, "UTF-8")).thenReturn("testIssuer:testAccount");
			mockedURLEncoder.when(() -> URLEncoder.encode(secretKey, "UTF-8")).thenReturn("mockSecretKey");
			mockedURLEncoder.when(() -> URLEncoder.encode(issuer, "UTF-8")).thenReturn("testIssuer");

			String url = Utils2FA.getGoogleAuthenticatorBarCode(secretKey, account, issuer);
			assertEquals(expectedUrl, url);

			mockedURLEncoder.verify(() -> URLEncoder.encode(issuer + ":" + account, "UTF-8"));
			mockedURLEncoder.verify(() -> URLEncoder.encode(secretKey, "UTF-8"));
			mockedURLEncoder.verify(() -> URLEncoder.encode(issuer, "UTF-8"));
		}
	}

	@Test
	void testCreateQRCode_createsQRCode() throws IOException, WriterException {
		String barCodeData = "mockBarcodeData";
		String filePath = "/tmp/mockQRCode.png";
		Path expectedPath = Paths.get(filePath);

		try (MockedStatic<MatrixToImageWriter> mockedMatrixWriter = mockStatic(MatrixToImageWriter.class)) {
			mockedMatrixWriter.when(() -> MatrixToImageWriter.writeToPath(any(), eq("png"), any(Path.class))).thenAnswer(invocation -> null);

			// Call the method under test
			Utils2FA.createQRCode(barCodeData, filePath, 300, 300);

			// Verify that the method was called with the correct parameters
			mockedMatrixWriter.verify(() -> MatrixToImageWriter.writeToPath(any(), eq("png"), eq(expectedPath)));
		}
	}

	@Test
	void testObtenerSecretKey_generatesQrAndReturnsSecretKey() {
		String user = "user@example.com";
		String secretKey = "mockSecret";
		String barcode = "mockBarcode";
		String qrPath = "/tmp/mockSecret.jpg";

		try (MockedStatic<Utils2FA> utilsMock = mockStatic(Utils2FA.class, Mockito.CALLS_REAL_METHODS); MockedStatic<UtilsFiles> filesMock = mockStatic(UtilsFiles.class)) {
			utilsMock.when(Utils2FA::generateSecretKey).thenReturn(secretKey);
			utilsMock.when(() -> Utils2FA.getGoogleAuthenticatorBarCode(secretKey, user, "LACNIC (PAI)")).thenReturn(barcode);
			filesMock.when(() -> UtilsFiles.calcularRutaImgQR(secretKey)).thenReturn(qrPath);
			utilsMock.when(() -> Utils2FA.createQRCode(barcode, qrPath, 300, 300)).thenAnswer(invocation -> null);

			String result = Utils2FA.obtenerSecretKey(user);

			assertEquals(secretKey, result);
			utilsMock.verify(() -> Utils2FA.createQRCode(barcode, qrPath, 300, 300));
			filesMock.verify(() -> UtilsFiles.calcularRutaImgQR(secretKey));
		}
	}

	@Test
	void testObtenerSecretKey_returnsNullWhenQRCodeCreationFails() {
		String user = "user@example.com";
		String secretKey = "mockSecret";
		String barcode = "mockBarcode";
		String qrPath = "/tmp/mockSecret.jpg";

		try (MockedStatic<Utils2FA> utilsMock = mockStatic(Utils2FA.class, Mockito.CALLS_REAL_METHODS); MockedStatic<UtilsFiles> filesMock = mockStatic(UtilsFiles.class)) {
			utilsMock.when(Utils2FA::generateSecretKey).thenReturn(secretKey);
			utilsMock.when(() -> Utils2FA.getGoogleAuthenticatorBarCode(secretKey, user, "LACNIC (PAI)")).thenReturn(barcode);
			filesMock.when(() -> UtilsFiles.calcularRutaImgQR(secretKey)).thenReturn(qrPath);
			utilsMock.when(() -> Utils2FA.createQRCode(barcode, qrPath, 300, 300)).thenThrow(new IOException("fail"));

			String result = Utils2FA.obtenerSecretKey(user);

			assertNull(result);
		}
	}

}
