package com.doto.domain.stamp.controller;

import com.doto.domain.festival.exception.FestivalErrorCode;
import com.doto.domain.member.exception.MemberErrorCode;
import com.doto.domain.stamp.dto.StampTourDetailResponseDTO;
import com.doto.domain.stamp.dto.StampTourStatusResponseDTO;
import com.doto.domain.stamp.exception.StampTourErrorCode;
import com.doto.domain.tourspot.entity.enums.TourSpotCategoryFilter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "StampTour", description = "스탬프 투어 API")
public interface StampTourApi {

    @Operation(
            summary = "스탬프 투어 시작",
            description = """
                    로그인한 사용자가 선택한 축제에서 새 스탬프 투어를 시작합니다.
                    - 같은 축제에 이미 진행 중(PROGRESS)인 스탬프 투어가 있으면 409로 실패합니다.
                    - festivalId에 해당하는 축제가 없으면 404로 실패합니다.
                    """
    )
    @ApiResponse(responseCode = "201", description = "스탬프 투어 시작 성공")
    @ApiErrorCodeExamples({StampTourErrorCode.class, FestivalErrorCode.class, MemberErrorCode.class})
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PostMapping("/api/v1/festival/{festivalId}/stamp-tour")
    ResponseEntity<Void> startStampTour(
            @CurrentMember CustomMemberDetails memberDetails,
            @Parameter(description = "축제 ID") @PathVariable Long festivalId
    );

    @Operation(
            summary = "스탬프 투어 중단",
            description = """
                    로그인한 사용자가 선택한 축제에서 진행 중인 스탬프 투어를 중단하고 삭제합니다.
                    - 해당 축제에 진행 중(PROGRESS)인 스탬프 투어가 없으면 404로 실패합니다.
                    - 성공 응답은 204 No Content이며 body가 없습니다.
                    """
    )
    @ApiResponse(responseCode = "204", description = "스탬프 투어 중단 성공 (정상 처리, body 없음)")
    @ApiErrorCodeExamples({StampTourErrorCode.class})
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @DeleteMapping("/api/v1/festival/{festivalId}/stamp-tour")
    ResponseEntity<Void> endStampTour(
            @CurrentMember CustomMemberDetails memberDetails,
            @Parameter(description = "축제 ID") @PathVariable Long festivalId
    );

    @Operation(
            summary = "스탬프 투어 상태 조회",
            description = """
                    로그인한 사용자의 선택한 축제에 대한 스탬프 투어 상태를 조회합니다.
                    - 스탬프 투어를 시작한 적이 없으면 NOT_STARTED를 반환합니다.
                    - 진행 중이면 PROGRESS, 모든 스팟을 방문 완료했으면 COMPLETED, 보상까지 수령했으면 REWARDED를 반환합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "스탬프 투어 상태 조회 성공")
    @ApiErrorCodeExamples({})
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/festival/{festivalId}/stamp-tour")
    ResponseEntity<CommonResponse<StampTourStatusResponseDTO>> getStampTourStatus(
            @CurrentMember CustomMemberDetails memberDetails,
            @Parameter(description = "축제 ID") @PathVariable Long festivalId
    );

    @Operation(
            summary = "현재 방문 중인 스탬프 투어 조회",
            description = """
                    로그인한 사용자가 현재 방문 중인 축제의 스탬프 투어 상세를 조회합니다.
                    - category를 생략하면 기본값 ALL로 전체 관광지를 거리순으로 반환합니다.
                    - category는 문화, 역사, 자연, 체험 중 하나로 필터링할 수 있습니다.
                    - 현재 방문 중인 축제가 없으면 result는 null입니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "현재 스탬프 투어 조회 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/api/v1/stamp-tour")
    ResponseEntity<CommonResponse<StampTourDetailResponseDTO>> getCurrentStampTour(
            @CurrentMember CustomMemberDetails memberDetails,
            @Parameter(description = "관광지 카테고리 필터. 기본값 ALL", example = "ALL")
            @RequestParam(defaultValue = "ALL") TourSpotCategoryFilter category
    );

}
