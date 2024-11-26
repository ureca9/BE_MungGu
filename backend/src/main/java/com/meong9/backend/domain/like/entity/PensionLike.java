package com.meong9.backend.domain.like.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("Pension")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PensionLike extends Like{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pension_id")
    private Pension pension;

    public PensionLike(Member member, Pension pension) {
        super(null, member);
        this.pension = pension;
    }
}
