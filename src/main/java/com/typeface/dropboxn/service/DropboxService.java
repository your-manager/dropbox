package com.typeface.dropboxn.service;

import com.typeface.dropboxn.entities.FileMetadata;
import com.typeface.dropboxn.repositories.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DropboxService {

    private static final Logger logger = LoggerFactory.getLogger(DropboxService.class);

    @Autowired
    private FileRepository fileRepository;

    private final String uploadDir = "uploads";

    public FileMetadata saveFile(MultipartFile file) throws IOException {
        try {
            Path filePath = Paths.get(uploadDir, file.getOriginalFilename());
            Files.write(filePath, file.getBytes());

            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(file.getOriginalFilename());
            metadata.setFileType(file.getContentType());
            metadata.setFilePath(filePath.toString());
            metadata.setFileSize(file.getSize());

            logger.info("File saved successfully: {}", file.getOriginalFilename());
            return fileRepository.save(metadata);
        } catch (IOException e) {
            logger.error("Error saving file: {}", file.getOriginalFilename(), e);
            throw new IOException("Failed to save file", e);
        }
    }

    public List<FileMetadata> getAllFiles() {
        logger.info("Fetching all files");
        return fileRepository.findAll();
    }

    public FileMetadata getFileMetadata(Long id) {
        logger.info("Fetching metadata for file with ID: {}", id);
        return fileRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("File not found with ID: {}", id);
                    return new RuntimeException("File not found with ID: " + id);
                });
    }

    public Resource getFileAsResource(Long id) {
        FileMetadata metadata = getFileMetadata(id);
        Path filePath = Paths.get(metadata.getFilePath());

        if (!Files.exists(filePath)) {
            logger.error("File not found on disk: {}", metadata.getFilePath());
            throw new RuntimeException("File not found on disk: " + metadata.getFilePath());
        }

        logger.info("File retrieved successfully: {}", metadata.getFileName());
        return new FileSystemResource(filePath);
    }
}