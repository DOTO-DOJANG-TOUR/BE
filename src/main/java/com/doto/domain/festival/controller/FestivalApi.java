package com.doto.domain.festival.controller;

import com.doto.domain.festival.dto.FestivalPageResponseDTO;
import com.doto.global.api.CommonResponse;
import com.doto.global.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Festival", description = "축제 API")
public interface FestivalApi {

    @Operation(
            summary = "오늘의 축제 조회",
            description = "지금 도장 투어가 가능한, 오늘 진행 중인 축제 목록을 커서 기반으로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "오늘의 축제 조회 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/festival/today")
    ResponseEntity<CommonResponse<FestivalPageResponseDTO>> getTodayFestivals(
            @Parameter(description = "이전 응답의 nextCursor, 첫 페이지면 생략")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "한 페이지에 조회할 개수")
            @RequestParam(defaultValue = "5") int size
    );

    @Operation(
            summary = "앞으로의 축제 조회",
            description = "아직 시작하지 않은 개최 예정 축제 목록을 커서 기반으로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "앞으로의 축제 조회 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/festival/upcoming")
    ResponseEntity<CommonResponse<FestivalPageResponseDTO>> getUpcomingFestivals(
            @Parameter(description = "이전 응답의 nextCursor, 첫 페이지면 생략")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "한 페이지에 조회할 개수")
            @RequestParam(defaultValue = "5") int size
    );

}
