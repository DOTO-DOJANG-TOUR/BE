package com.doto.domain.festival.controller;

import com.doto.domain.festival.dto.FestivalDetailResponseDTO;
import com.doto.domain.festival.dto.FestivalPageResponseDTO;
import com.doto.domain.festival.dto.FestivalRegionPageResponseDTO;
import com.doto.domain.festival.entity.enums.FestivalSort;
import com.doto.domain.festival.entity.enums.RegionGroup;
import com.doto.global.api.CommonResponse;
import com.doto.global.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            @Parameter(description = """
                    첫 페이지 조회 시에는 생략합니다.
                    이전 응답의 nextCursor 값을 다음 요청에 그대로 넣습니다.
                    nextCursor가 null이면 마지막 페이지입니다.""")
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
    @GetMapping("/api/v1/festival/future")
    ResponseEntity<CommonResponse<FestivalPageResponseDTO>> getUpcomingFestivals(
            @Parameter(description = """
                    첫 페이지 조회 시에는 생략합니다.
                    이전 응답의 nextCursor 값을 다음 요청에 그대로 넣습니다.
                    nextCursor가 null이면 마지막 페이지입니다.""")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "한 페이지에 조회할 개수")
            @RequestParam(defaultValue = "5") int size
    );

    @Operation(
            summary = "축제 검색",
            description = """
                    축제 제목을 검색합니다.
                    query에 "진행" 또는 "오늘"이 포함되면 진행 중인 축제만, "예정" 또는 "내일"이 포함되면 개최 예정 축제만 필터링하고, 둘 다 없으면 진행중+개최예정 전체를 대상으로 합니다.
                    남은 검색어와 제목의 유사도가 높은 순으로 보여줍니다."""
    )
    @ApiResponse(responseCode = "200", description = "축제 검색 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/festival")
    ResponseEntity<CommonResponse<FestivalRegionPageResponseDTO>> searchFestivals(
            @Parameter(description = "검색어. \"오늘\"/\"진행\", \"내일\"/\"예정\" 같은 상태 키워드와 제목 텍스트를 함께 넣을 수 있습니다.", example = "오늘 국가유산야행")
            @RequestParam String query,
            @Parameter(description = """
                    첫 페이지 조회 시에는 생략합니다.
                    이전 응답의 nextCursor 값을 다음 요청에 그대로 넣습니다.
                    nextCursor가 null이면 마지막 페이지입니다.""")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "한 페이지에 조회할 개수")
            @RequestParam(defaultValue = "5") int size
    );

    @Operation(
            summary = "지역별 축제 조회",
            description = "선택한 지역 그룹의 개최중/개최전 축제 목록을 종료임박순/개최임박순 중 선택한 기준으로 커서 기반 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "지역별 축제 조회 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/festival/region")
    ResponseEntity<CommonResponse<FestivalRegionPageResponseDTO>> getFestivalsByRegion(
            @Parameter(description = "지역 그룹")
            @RequestParam RegionGroup regionGroup,
            @Parameter(description = "정렬 기준")
            @RequestParam(defaultValue = "END_DATE") FestivalSort sort,
            @Parameter(description = """
                    첫 페이지 조회 시에는 생략합니다.
                    이전 응답의 nextCursor 값을 다음 요청에 그대로 넣습니다.
                    nextCursor가 null이면 마지막 페이지입니다.""")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "한 페이지에 조회할 개수")
            @RequestParam(defaultValue = "5") int size
    );

    @Operation(
            summary = "축제 상세 조회",
            description = "festivalId로 축제 상세 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "축제 상세 조회 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/festival/{festivalId}")
    ResponseEntity<CommonResponse<FestivalDetailResponseDTO>> getFestivalDetail(
            @Parameter(description = "축제 ID")
            @PathVariable Long festivalId
    );

}
