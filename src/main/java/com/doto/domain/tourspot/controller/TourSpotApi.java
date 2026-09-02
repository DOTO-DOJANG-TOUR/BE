package com.doto.domain.tourspot.controller;

import com.doto.domain.stamp.dto.StampTourSpotItemResponseDTO;
import com.doto.domain.tourspot.dto.TourSpotDetailResponseDTO;
import com.doto.domain.tourspot.exception.TourErrorCode;
import com.doto.global.api.CommonResponse;
import com.doto.global.config.SwaggerConfig;
import com.doto.global.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "TourSpot", description = "관광지 API")
@ApiErrorCodeExamples({TourErrorCode.class})
public interface TourSpotApi {

    @Operation(
            summary = "축제 관광지 검색",
            description = """
                    festivalId에 속한 관광지를 제목 키워드로 검색합니다.
                    - keyword를 생략하거나 빈 값으로 전달하면 해당 축제의 전체 관광지를 반환합니다.
                    - 결과는 축제 위치 기준 거리순으로 반환합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "관광지 검색 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/festival/{festivalId}/tour-spots")
    ResponseEntity<CommonResponse<List<StampTourSpotItemResponseDTO>>> searchTourSpots(
            @Parameter(description = "축제 ID") @PathVariable Long festivalId,
            @Parameter(description = "관광지명 검색 키워드. 생략 시 전체 조회", example = "해수욕장")
            @RequestParam(required = false) String keyword
    );

    @Operation(summary = "축제 관광지 상세 조회", description = "축제에 연결된 관광지의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "관광지 상세 조회 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/festival/{festivalId}/tour-spots/{tourSpotId}")
    ResponseEntity<CommonResponse<TourSpotDetailResponseDTO>> getTourSpotDetail(
            @Parameter(description = "축제 ID") @PathVariable Long festivalId,
            @Parameter(description = "관광지 ID") @PathVariable Long tourSpotId
    );
}
