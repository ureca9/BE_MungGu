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
    private PlaceFileId placeFileId; // 복합 키 객체

    @MapsId("placeId") // PlaceFileId의 placeId와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @MapsId("mediaFileId") // PlaceFileId의 mediaFileId와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

    @Builder
    public PlaceFile(Place place, MediaFile mediaFile) {
        this.placeFileId = new PlaceFileId(place.getPlaceId(), mediaFile.getMediaFileId());
        this.place = place;
        this.mediaFile = mediaFile;
    }
}
