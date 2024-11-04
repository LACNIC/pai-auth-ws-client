package net.lacnic.portal.auth.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsFiles {
	private static final Logger logger = LoggerFactory.getLogger(UtilsFiles.class);

	public static byte[] getBytesFromFile(String fileString) throws IOException {
		return getBytesFromFile(new File(fileString));

	}

	public static String calcularRutaImgQR(String secretKey) {
		return System.getProperty("jboss.server.temp.dir", "/tmp/").concat("/") + secretKey + ".jpg";
	}

	public static byte[] calcularBytesImgQR(String secretKey) {
		try {
			return getBytesFromFile(calcularRutaImgQR(secretKey));
		} catch (IOException e) {
			logger.error("An error occurred: {}", e.getMessage(), e);
			return null;
		}
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		try (InputStream is = new FileInputStream(file)) {

			long length = file.length();
			if (length > Integer.MAX_VALUE) {
				throw new IOException("File is too large to process");
			}

			byte[] bytes = new byte[(int) length];

			int offset = 0;
			int numRead;
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + file.getName());
			}

			return bytes;
		}
	}

	public static File obtenerFile(byte[] bytes, String extension) {
		try {
			String uuid = UUID.randomUUID().toString();
			File file = new File(System.getProperty("jboss.server.temp.dir").concat("/") + uuid + "." + extension);
			file.createNewFile();

			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(bytes);
				fos.flush();
			}

			return file;
		} catch (FileNotFoundException e) {
			logger.error("An error occurred: {}", e.getMessage(), e);
		} catch (Exception e) {
			logger.error("An error occurred: {}", e.getMessage(), e);
		}
		return null;
	}
}
