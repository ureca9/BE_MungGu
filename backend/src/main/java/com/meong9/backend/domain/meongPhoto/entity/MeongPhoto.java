package com.meong9.backend.domain.meongPhoto.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.meongPhoto.entity.id.MeongPhotoId;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MeongPhoto {
    @EmbeddedId
    private MeongPhotoId meongPhotoId;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = false)
    private Member member;

    @MapsId("mediaFileId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

}
