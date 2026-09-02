package com.doto.domain.stamp.controller;

import com.doto.domain.stamp.dto.CurrentVisitTourSpotResponseDTO;
import com.doto.domain.stamp.dto.StampLocationRequestDTO;
import com.doto.domain.stamp.dto.StampResponseDTO;
import com.doto.domain.stamp.exception.StampErrorCode;
import com.doto.domain.stamp.exception.StampTourErrorCode;
import com.doto.domain.stamp.exception.TourSpotVisitErrorCode;
import com.doto.global.api.CommonResponse;
import com.doto.global.config.SwaggerConfig;
import com.doto.global.security.CurrentMember;
import com.doto.global.security.CustomMemberDetails;
import com.doto.global.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Stamp", description = "스탬프 API")
public interface StampApi {

    @Operation(
            summary = "관광지 방문 시작",
            description = """
                    진행 중인 스탬프 투어에서 축제 소속 관광지 방문을 시작합니다.
                    - 한 회원은 동시에 하나의 관광지만 방문할 수 있습니다.
                    - 방문은 시작 시점부터 7시간 후 만료됩니다.
                    """
    )
    @ApiResponse(responseCode = "201", description = "관광지 방문 시작 성공")
    @ApiErrorCodeExamples({StampErrorCode.class, StampTourErrorCode.class, TourSpotVisitErrorCode.class})
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PostMapping("/api/v1/festival/{festivalId}/tour-spots/{tourSpotId}/visit")
    ResponseEntity<CommonResponse<CurrentVisitTourSpotResponseDTO>> startTourSpotVisit(
            @CurrentMember CustomMemberDetails memberDetails,
            @Parameter(description = "축제 ID") @PathVariable Long festivalId,
            @Parameter(description = "관광지 ID") @PathVariable Long tourSpotId
    );

    @Operation(
            summary = "관광지 방문 중단",
            description = "진행 중인 축제 소속 관광지 방문을 종료합니다. 성공 응답은 204 No Content이며 body가 없습니다."
    )
    @ApiResponse(responseCode = "204", description = "관광지 방문 중단 성공")
    @ApiErrorCodeExamples({StampErrorCode.class, TourSpotVisitErrorCode.class})
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @DeleteMapping("/api/v1/festival/{festivalId}/tour-spots/{tourSpotId}/visit")
    ResponseEntity<Void> stopTourSpotVisit(
            @CurrentMember CustomMemberDetails memberDetails,
            @Parameter(description = "축제 ID") @PathVariable Long festivalId,
            @Parameter(description = "관광지 ID") @PathVariable Long tourSpotId
    );

    @Operation(
            summary = "관광지 스탬프 획득",
            description = """
                    현재 사용자 위치가 관광지 반경 300m 이내일 때 스탬프를 획득합니다.
                    - 한 스탬프 투어에서는 최대 3개의 스탬프만 획득할 수 있습니다.
                    - 같은 관광지의 스탬프는 한 번만 완료할 수 있습니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "스탬프 획득 성공")
    @ApiErrorCodeExamples({StampErrorCode.class, StampTourErrorCode.class, TourSpotVisitErrorCode.class})
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PostMapping("/api/v1/festival/{festivalId}/tour-spots/{tourSpotId}/stamps")
    ResponseEntity<CommonResponse<StampResponseDTO>> completeStamp(
            @CurrentMember CustomMemberDetails memberDetails,
            @Parameter(description = "축제 ID") @PathVariable Long festivalId,
            @Parameter(description = "관광지 ID") @PathVariable Long tourSpotId,
            @RequestBody StampLocationRequestDTO request
    );

    @Operation(
            summary = "현재 방문 중인 관광지 조회",
            description = "현재 진행 중인 관광지 방문과 만료 시각을 조회합니다. 방문 중인 관광지가 없으면 result는 null입니다."
    )
    @ApiResponse(responseCode = "200", description = "현재 방문 관광지 조회 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/stamps/current-visit-tour-spot")
    ResponseEntity<CommonResponse<CurrentVisitTourSpotResponseDTO>> getCurrentVisitTourSpot(
            @CurrentMember CustomMemberDetails memberDetails
    );
}
