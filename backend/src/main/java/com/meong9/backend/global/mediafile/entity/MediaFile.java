package com.meong9.backend.global.mediafile.entity;

import com.meong9.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MediaFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mediaFileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FileType fileType; // IMAGE 또는 VIDEO

    @Column(nullable = false)
    private Integer fileSize;

    @Column(nullable = false)
    private String fileName;

    private String fileUrl;

    private Double height;

    private Double width;

    @Column
    private String fileKey;

    @Builder
    public MediaFile(FileType fileType, Integer fileSize, String fileName, String fileUrl, Double height, Double width, String fileKey) {
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.height = height;
        this.width = width;
        this.fileKey = fileKey;
    }
}
