package com.doto.domain.festival.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FestivalPageResponseDTO(
        @Schema(description = "축제 목록")
        List<FestivalShortResponseDTO> festivals,

        @Schema(description = "다음 페이지 조회용 커서, 다음 페이지가 없으면 null", example = "MjAyNi0wOC0yNFQxNTowMDowMFp8...")
        String nextCursor
) {
}
