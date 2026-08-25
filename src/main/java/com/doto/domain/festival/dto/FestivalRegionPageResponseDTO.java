package com.doto.domain.festival.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FestivalRegionPageResponseDTO(
        @Schema(description = "축제 목록")
        List<FestivalRegionResponseDTO> festivals,

        @Schema(description = "다음 페이지 조회용 커서, 다음 페이지가 없으면 null")
        String nextCursor
) {
}
