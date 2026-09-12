
# PAI Authentication Web Service Client

**PAI Authentication Web Service Client** es un cliente Java para consumir el servicio web de autenticación de PAI. Este cliente permite realizar autenticaciones y gestionar accesos a través de la API de autenticación de PAI.

## Características

- Conexión sencilla al servicio web de autenticación de PAI.
- Métodos para realizar autenticaciones y gestionar sesiones.
- Cliente del gateway IA de PAI: `GET /ai/resolve/{use}` y `POST /ai/chat`.
- Configurable y fácil de integrar en aplicaciones Java.

## Requisitos

- Java 8 o superior.
- Maven para gestionar las dependencias.

## Instalación
[![](https://jitpack.io/v/LACNIC/pai-auth-ws-client.svg)](https://jitpack.io/#LACNIC/pai-auth-ws-client)

Puedes agregar esta biblioteca a tu proyecto utilizando [JitPack](https://jitpack.io/). Asegúrate de que tienes JitPack configurado como repositorio en tu proyecto Maven.

### Paso 1: Agregar el repositorio de JitPack
Agrega el siguiente repositorio en el archivo `pom.xml` de tu proyecto:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
Luego, agrega la dependencia del cliente PAI:
<dependency>
  <groupId>com.github.LACNIC</groupId>
  <artifactId>pai-auth-ws-client</artifactId>
  <version>1.6.0</version>
</dependency>
```

### Luego, agrega la dependencia del cliente PAI:

``` xml
<dependency>
  <groupId>com.github.LACNIC</groupId>
  <artifactId>pai-auth-ws-client</artifactId>
  <version>1.6.0</version>
</dependency>
```

## Gateway IA

Usa `URL_PORTAL_WS` de `pai.properties` y un Bearer PAI con rol `portal-ai-gateway`. La IP del caller debe estar en `AI_LLM_WS_IP_WHITELIST`.

```java
AiResolveData resolved = PortalAiClient.resolve(token, use);
AiChatData chat = PortalAiClient.chat(token, use, "resumí este texto");
AiChatData withRoles = PortalAiClient.chat(token, use,
    List.of(AiChatMessage.system("sos un asistente"), AiChatMessage.user("hola")));
```

También: `PortalWSClient.resolveAi(...)` y `PortalWSClient.chatAi(...)`.
El `use` lo define la aplicación que llama; el cliente solo lo reenvía al gateway.
