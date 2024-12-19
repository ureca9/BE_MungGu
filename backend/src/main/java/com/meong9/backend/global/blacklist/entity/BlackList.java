package com.meong9.backend.global.blacklist.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BlackList extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blackListId;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "reason")
    private String reason;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Builder
    public BlackList(Member member, String reason, LocalDateTime lockedUntil) {
        this.member = member;
        this.reason = reason;
        this.lockedUntil = lockedUntil;
    }
}
