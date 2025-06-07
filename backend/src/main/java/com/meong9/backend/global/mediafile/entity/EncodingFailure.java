package com.meong9.backend.global.mediafile.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EncodingFailure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reviewId;

    @Column(nullable = false)
    private String filename;

    /** 실패 발생 시각 */
    @Column(nullable = false)
    private LocalDateTime occurredAt;

    /** 실패 사유 메시지 */
    @Column(nullable = false, length = 1000)
    private String reason;
}
