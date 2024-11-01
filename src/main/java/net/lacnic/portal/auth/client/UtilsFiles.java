package net.lacnic.portal.auth.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class UtilsFiles {

	public static byte[] getBytesFromFile(String fileString) throws IOException {
		return getBytesFromFile(new File(fileString));

	}

	public static String calcularRutaImgQR(String secretKey) {
		return System.getProperty("jboss.server.temp.dir").concat("/") + secretKey + ".jpg";
	}

	public static byte[] calcularBytesImgQR(String secretKey) {
		try {
			return getBytesFromFile(calcularRutaImgQR(secretKey));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		byte[] bytes = new byte[(int) length];

		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		is.close();

		return bytes;
	}

	public static File obtenerFile(byte[] bytes, String extension) {
		try {
			String uuid = UUID.randomUUID().toString();
			File file = new File(System.getProperty("jboss.server.temp.dir").concat("/") + uuid + "." + extension);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.flush();
			fos.close();
			return file;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
