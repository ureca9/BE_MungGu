package com.meong9.backend.domain.member.entity.id;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class PlcFavCategoryId implements Serializable {

    private Long memberId;
    private Long plcCategoryId;
}
