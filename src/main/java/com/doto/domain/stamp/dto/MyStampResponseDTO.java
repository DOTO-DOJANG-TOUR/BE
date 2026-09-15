package com.doto.domain.stamp.dto;

import java.time.Instant;
import java.util.List;

public record MyStampResponseDTO(
        String festivalId,
        String festivalImgUrl,
        String tourName,
        Integer stampCount,
        List<StampItemResponseDTO> stamps,
        StampTourViewStatus status
) {
    public record StampItemResponseDTO(
            String tourSpotName,
            Instant stampedAt
    ){}
}
