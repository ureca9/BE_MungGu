package com.meong9.backend.domain.pension.entity;

import com.meong9.backend.domain.pension.entity.id.PensionFileId;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PensionFile {
    @EmbeddedId
    private PensionFileId pensionFileId;

    @MapsId("pensionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pension_id")
    private Pension pension;

    @MapsId("mediaFileId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_file_id")
    private MediaFile mediaFile;
}
