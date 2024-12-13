package com.meong9.backend.domain.meongPhoto.entity;

import com.meong9.backend.domain.member.entity.Member;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meongPhotoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

    public static MeongPhoto createMeongPhoto(Member member, MediaFile mediaFile) {
        MeongPhoto meongPhoto = new MeongPhoto();
        meongPhoto.member = member;
        meongPhoto.mediaFile = mediaFile;
        return meongPhoto;
    }
}
