package com.meong9.backend.domain.pension.entity;

import com.meong9.backend.domain.pension.entity.id.PensionFileKey;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PensionFile {

    @EmbeddedId
    private PensionFileKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pensionId")
    @JoinColumn(name = "pension_id",nullable = false)
    private Pension pension;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fileId")
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

}
