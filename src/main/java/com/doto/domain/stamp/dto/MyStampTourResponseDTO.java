package com.doto.domain.stamp.dto;

import java.time.LocalDate;
import java.util.List;

public record MyStampTourResponseDTO(
        Integer rewardedTourCount,
        List<TourResponseDTO> tours
) {
    public record TourResponseDTO(
            String festivalId,
            String title,
            String imageUrl,
            Integer stampCount,
            LocalDate eventEndDate,
            StampTourViewStatus status
    ){}
}
