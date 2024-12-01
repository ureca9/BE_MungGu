package com.meong9.backend.domain.review.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.review.dto.ReviewRequestDto;
import com.meong9.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Review extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId; // 후기 아이디

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(columnDefinition = "TEXT")
    private String content; // 내용

    @Column(name = "m_nickname", length = 20)
    private String nickname; // 작성자 닉네임

    @Column(name = "score")
    private Float score; // 별점

    @Temporal(TemporalType.DATE)
    private Date visitDate; // 방문일

    @Column(name = "type", length = 3)
    private String type; // 시설 or 펜션 구분

    @Column(name = "plc_pen_id")
    private Long placePensionId; // 시설 또는 펜션 아이디

    @Setter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewFile> reviewFiles = Collections.emptyList(); // 후기 파일 리스트

    @Builder
    public Review(Member member, String content, String nickname, Float score, Date visitDate, String type, Long placePensionId, List<ReviewFile> reviewFiles) {
        this.member = member;
        this.content = content;
        this.nickname = nickname;
        this.score = score;
        this.visitDate = visitDate;
        this.type = type;
        this.placePensionId = placePensionId;
        this.reviewFiles = reviewFiles;
    }

    public void update(ReviewRequestDto reviewRequestDto) {
        this.content = reviewRequestDto.getContent();
        this.score = reviewRequestDto.getScore();
        this.visitDate = reviewRequestDto.getVisitDate();
        this.type = reviewRequestDto.getType();
        this.placePensionId = reviewRequestDto.getPlcPenId();
    }
}