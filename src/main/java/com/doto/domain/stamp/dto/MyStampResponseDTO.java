package com.doto.domain.stamp.dto;

import com.doto.domain.stamp.entity.enums.StampTourStatus;

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
    public record StampResponseDTO(
            String tourSpotName,
            Instant stampedAt
    ){}
}
