package com.typeface.dropboxn.controller;

import com.typeface.dropboxn.entities.FileMetadata;
import com.typeface.dropboxn.service.DropboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", exposedHeaders = "Content-Disposition")
@RequestMapping("/api/files")
public class DropboxController {

    @Autowired
    private DropboxService dropboxService;

    private static final Logger logger = LoggerFactory.getLogger(DropboxController.class);

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            FileMetadata metadata = dropboxService.saveFile(file);
            logger.info("File uploaded successfully: {}", file.getOriginalFilename());
            return ResponseEntity.status(HttpStatus.CREATED).body(metadata);
        } catch (IOException e) {
            logger.error("Error uploading file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> listFiles() {
        try {
            List<FileMetadata> files = dropboxService.getAllFiles();
            logger.info("Retrieved list of files");
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            logger.error("Error fetching file list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable Long id) {
        try {
            FileMetadata fileMetadata = dropboxService.getFileMetadata(id);
            Resource fileResource = dropboxService.getFileAsResource(id);

            String contentDisposition = "attachment; filename=\"" + fileMetadata.getFileName() + "\"";

            logger.info("File download initiated: {}", fileMetadata.getFileName());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileMetadata.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(fileResource);
        } catch (RuntimeException e) {
            logger.error("Error downloading file with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        } catch (Exception e) {
            logger.error("Unexpected error while downloading file with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to download file");
        }
    }
}
