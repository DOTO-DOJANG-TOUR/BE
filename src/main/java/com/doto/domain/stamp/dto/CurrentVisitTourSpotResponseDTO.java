package com.doto.domain.stamp.dto;

import java.time.Instant;

public record CurrentVisitTourSpotResponseDTO(
        String tourSpotId,
        String tourSpotName,
        Instant expiresAt
) {
}
