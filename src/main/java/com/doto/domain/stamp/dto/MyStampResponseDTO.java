package com.doto.domain.stamp.dto;

import com.doto.domain.stamp.entity.enums.StampTourStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record MyStampResponseDTO(
        String festivalId,
        String festivalImgUrl,
        String tourName,
        Integer stampCount,
        List<StampResponseDTO> stamps,
        StampTourStatus status
) {
    // 최상위 com.doto.domain.stamp.dto.StampResponseDTO와 단순 클래스명이 같아
    // springdoc이 OpenAPI 컴포넌트 스키마를 같은 이름으로 등록해 덮어쓰던 문제 방지용 스키마명 지정
    @Schema(name = "MyStampResponseDTO.StampResponseDTO")
    public record StampResponseDTO(
            String tourSpotName,
            Instant stampedAt
    ){}
}
