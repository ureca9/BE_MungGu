package com.meong9.backend.domain.member.entity;

import com.meong9.backend.global.entity.BaseTimeEntity;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    private String provider;

    private String phone;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(length = 50, unique = true)
    private String providerId;

    @OneToOne(cascade = CascadeType.ALL)
    private MediaFile profileImage;

    private String roleCode = "010";

    @Builder
    public Member (String email, String name, String nickname, String provider, String providerId, MediaFile profileImage) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImage = profileImage;
    }
}
