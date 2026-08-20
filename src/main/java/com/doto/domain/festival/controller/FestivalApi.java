package com.doto.domain.festival.controller;

import com.doto.domain.festival.dto.FestivalShortResponseDTO;
import com.doto.global.api.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Festival", description = "축제 API")
public interface FestivalApi {

    @Operation(
            summary = "오늘의 축제 조회",
            description = "지금 도장 투어가 가능한, 오늘 진행 중인 축제 목록을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "오늘의 축제 조회 성공")
    @GetMapping("/api/v1/festival/today")
    ResponseEntity<CommonResponse<List<FestivalShortResponseDTO>>> getTodayFestivals();

}
