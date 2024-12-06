package com.meong9.backend.domain.member.entity;

import com.meong9.backend.domain.puppy.entity.Puppy;
import com.meong9.backend.global.entity.BaseTimeEntity;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // 특정 필드만 비교
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // memberId만 비교 기준으로 사용
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    private String name;

    @Column(length = 20, unique = true)
    @Setter
    private String nickname;

    private String provider;

    @Setter
    private String phone;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(length = 50, unique = true)
    private String providerId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Setter
    private MediaFile profileImage;

    private String roleCode = "010";

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Puppy> puppies;

    @Setter
    private LocalDateTime lastActivity = LocalDateTime.now();

    @Builder
    public Member (String email, String name, String provider, String providerId, MediaFile profileImage) {
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImage = profileImage;
    }
}
