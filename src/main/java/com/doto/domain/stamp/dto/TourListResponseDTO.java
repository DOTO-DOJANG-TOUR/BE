package com.doto.domain.stamp.dto;

import java.util.List;

public record TourListResponseDTO(
        Long stampCount,
        List<TourSpotItemResponseDTO> items
){}