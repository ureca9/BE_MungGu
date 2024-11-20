package com.meong9.backend.global.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MediaFile {

    @Id
    @Column(nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FileType fileType; // IMAGE 또는 VIDEO

    @Column(nullable = false)
    private Integer fileSize;

    @Column(nullable = false)
    private String fileName;

    private Double height;

    private Double width;

    @Column
    private String fileKey;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 등록날짜
}
