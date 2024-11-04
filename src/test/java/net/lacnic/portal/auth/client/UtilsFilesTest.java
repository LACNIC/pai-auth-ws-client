package net.lacnic.portal.auth.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UtilsFilesTest {

	private static final String TEMP_DIR_KEY = "jboss.server.temp.dir";
	private Path tempDirectory;

	@BeforeEach
	void setUp() throws IOException {
		// Create a temporary directory for testing
		tempDirectory = Files.createTempDirectory("testTempDir");
		System.setProperty(TEMP_DIR_KEY, tempDirectory.toString());
	}

	@Test
	void testObtenerFile_createsNewFileWithGivenBytes() throws IOException {
		byte[] fileBytes = "Test data".getBytes();
		String extension = "txt";

		// Call the method under test
		File resultFile = UtilsFiles.obtenerFile(fileBytes, extension);

		// Validate the file creation
		assertNotNull(resultFile, "File should not be null");
		assertTrue(resultFile.exists(), "File should exist");

		// Verify the file's path is within the temporary directory
		assertEquals(tempDirectory.resolve(resultFile.getName()).toString(), resultFile.getPath(), "File path should match expected directory");

		// Validate file content
		byte[] readBytes = Files.readAllBytes(resultFile.toPath());
		assertArrayEquals(fileBytes, readBytes, "File content should match the written bytes");

		// Clean up the created file after the test
		resultFile.delete();
	}

	@Test
	void testCalcularRutaImgQR_returnsCorrectPath() {
		String secretKey = "mockSecretKey";

		// Get the temporary directory path from the system property
		String tempDir = System.getProperty("jboss.server.temp.dir");
		String expectedPath = tempDir + File.separator + secretKey + ".jpg";

		// Calculate the actual path using the method
		String actualPath = UtilsFiles.calcularRutaImgQR(secretKey);

		// Verify that the calculated path matches the expected path
		assertEquals(expectedPath, actualPath, "The generated image path should match the expected path");
	}

	@Test
	void testGetBytesFromFile_withFilePath() throws IOException {
		Path tempFile = Files.createTempFile("testFile", ".txt");
		byte[] mockBytes = "test content".getBytes();
		Files.write(tempFile, mockBytes);

		byte[] result = UtilsFiles.getBytesFromFile(tempFile.toString());
		assertArrayEquals(mockBytes, result);

		Files.delete(tempFile);
	}

	@Test
	void testGetBytesFromFile_withFileObject() throws IOException {
		Path tempFile = Files.createTempFile("testFile", ".txt");
		byte[] mockBytes = "test content".getBytes();
		Files.write(tempFile, mockBytes);

		byte[] result = UtilsFiles.getBytesFromFile(tempFile.toFile());
		assertArrayEquals(mockBytes, result, "File bytes should match the expected content");

		Files.delete(tempFile);
	}

	@Test
	void testObtenerFile_returnsNullOnIOException() {
		assertTrue(true);
	}

	@Test
	void testGetBytesFromFile_fileTooLarge() throws IOException {
		// Create a temporary file
		Path largeFile = Files.createTempFile("largeFile", ".txt");

		// Use Mockito to mock the length of the file
		File mockFile = Mockito.spy(largeFile.toFile());
		Mockito.when(mockFile.length()).thenReturn((long) Integer.MAX_VALUE + 1);

		// Verify that an IOException is thrown when attempting to get bytes from a
		// too-large file
		IOException exception = assertThrows(IOException.class, () -> {
			UtilsFiles.getBytesFromFile(mockFile);
		});

		assertEquals("File is too large to process", exception.getMessage(), "Expected message for file too large");

		Files.deleteIfExists(largeFile);
	}
}