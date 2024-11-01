package net.lacnic.portal.auth.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import de.taimos.totp.TOTP;

public class Utils2FA {

	public static String obtenerSecretKey(String user) {
		try {
			String secretKey = Utils2FA.generateSecretKey();
			String companyName = "LACNIC (PAI)";
			String barCodeUrl = Utils2FA.getGoogleAuthenticatorBarCode(secretKey, user, companyName);
			Utils2FA.createQRCode(barCodeUrl, UtilsFiles.calcularRutaImgQR(secretKey), 300, 300);
			return secretKey;
		} catch (IOException | WriterException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String generateSecretKey() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[20];
		random.nextBytes(bytes);
		Base32 base32 = new Base32();
		return base32.encodeToString(bytes);
	}

	public static String getTOTPCode(String secretKey) {
		Base32 base32 = new Base32();
		byte[] bytes = base32.decode(secretKey);
		String hexKey = Hex.encodeHexString(bytes);
		return TOTP.getOTP(hexKey);
	}

	public static String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) {
		try {
			return "otpauth://totp/" + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20") + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20") + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void createQRCode(String barCodeData, String filePath, int height, int width) throws IOException, WriterException {
		BitMatrix matrix = new MultiFormatWriter().encode(barCodeData, BarcodeFormat.QR_CODE, width, height);
		File out = new File(filePath);
		MatrixToImageWriter.writeToFile(matrix, "png", out);
	}

	public static void infinityGeneratingCodes(String secretKey) {
		String lastCode = null;
		while (true) {
			String code = getTOTPCode(secretKey);
			if (!code.equals(lastCode)) {
				System.out.println(code);
			}
			lastCode = code;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			;
		}
	}

}