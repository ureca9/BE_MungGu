package com.meong9.backend.domain.member.entity;

import com.meong9.backend.global.entity.BaseEntity;
import com.meong9.backend.global.entity.MediaFile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    private String provider;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(length = 50, unique = true)
    private String providerId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MediaFile profileImageUrl;
}
