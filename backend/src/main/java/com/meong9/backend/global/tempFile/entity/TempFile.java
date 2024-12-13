package com.meong9.backend.global.tempFile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TempFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tempFileId;

    private String serviceUrl;
    private String fileName;
    private String contentType;
    private Long ownerId;
    private String ownerType;

    @Lob
    private byte[] fileData;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public TempFile(String serviceUrl, String fileName, String contentType, byte[] fileData, Long id, String type) {
        this.serviceUrl = serviceUrl;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileData = fileData;
        this.ownerId = id;
        this.ownerType = type;
    }
}

