package com.meong9.backend.domain.place.entity;

import com.meong9.backend.domain.place.entity.id.PlaceFileId;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PlaceFile {

    @EmbeddedId
    private PlaceFileId id; // 복합 키 객체

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("placeId") // PlaceFileId의 placeId와 매핑
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fileId") // PlaceFileId의 fileId와 매핑
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

}
