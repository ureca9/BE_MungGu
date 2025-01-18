package com.meong9.backend.global.mediafile.entity;

import com.meong9.backend.domain.place.entity.PlaceFile;
import com.meong9.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Setter
    private String fileUrl;

    private Double height;

    private Double width;

    private String fileKey;

    @OneToMany(mappedBy = "mediaFile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceFile> placeFiles = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isDeleted = false; // 소프트 삭제 여부 (기본값: false)

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

    public void delete(){
        this.isDeleted = true;
    }

}
