package com.sopan.util;

import com.sopan.exception.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileStorage {

    private final Path baseStorageDir;

    public FileStorage(String storagePath) {
        this.baseStorageDir = Paths.get(storagePath != null ? storagePath : System.getProperty("user.home") + "/.sopan/uploads");
        try {
            Files.createDirectories(this.baseStorageDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize file storage directory: " + this.baseStorageDir, e);
        }
    }

    /**
     * Verifies that the uploaded stream begins with the PDF magic header "%PDF"
     * and saves it with a randomized UUID filename.
     */
    public String savePdf(InputStream inputStream, String originalFilename) throws ValidationException, IOException {
        if (inputStream == null) {
            throw new ValidationException("No file uploaded.");
        }

        // Buffer the first 4 bytes to verify magic header
        byte[] magic = new byte[4];
        int bytesRead = inputStream.readNBytes(magic, 0, 4);
        if (bytesRead < 4 || magic[0] != '%' || magic[1] != 'P' || magic[2] != 'D' || magic[3] != 'F') {
            throw new ValidationException("Invalid file format. Only valid PDF files are permitted.");
        }

        String filename = UUID.randomUUID() + ".pdf";
        Path target = baseStorageDir.resolve(filename);

        // Prepend magic bytes and write remainder
        try (var out = Files.newOutputStream(target)) {
            out.write(magic);
            inputStream.transferTo(out);
        }

        return filename;
    }

    public Path resolve(String filename) {
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename path traversal attempt");
        }
        return baseStorageDir.resolve(filename);
    }
}
