package com.studymate.ai.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path uploadDirectory;

    public FileStorageService(
            @Value("${file.upload-dir}") String uploadDir
    ) {
        this.uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create upload directory",
                    e
            );
        }
    }

    public String storeFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("Cannot store an empty file");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new RuntimeException("Invalid file name");
        }

        String extension = "";

        int dotIndex = originalName.lastIndexOf('.');

        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }

        String storedFileName =
                UUID.randomUUID() + extension;

        Path targetPath =
                uploadDirectory.resolve(storedFileName)
                        .normalize();

        if (!targetPath.startsWith(uploadDirectory)) {
            throw new RuntimeException(
                    "Cannot store file outside upload directory"
            );
        }

        try {

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to store file",
                    e
            );
        }

        return targetPath.toString();
    }

    public void deleteFile(String filePath) {

        try {
            Files.deleteIfExists(
                    Paths.get(filePath)
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to delete file",
                    e
            );
        }
    }
}
