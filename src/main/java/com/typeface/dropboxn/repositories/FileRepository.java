package com.typeface.dropboxn.repositories;


import com.typeface.dropboxn.entities.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileMetadata, Long> {
}