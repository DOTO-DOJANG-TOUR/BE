package com.doto.domain.admin.controller;

import com.doto.domain.stamp.dto.StampTourRewardRequestDTO;
import com.doto.domain.stamp.dto.StampTourRewardResponseDTO;
import com.doto.domain.stamp.exception.StampTourErrorCode;
import com.doto.global.api.CommonResponse;
import com.doto.global.config.SwaggerConfig;
import com.doto.global.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// 백엔드 전용(관리자) API, SecurityConfig 참고
@Tag(name = "Admin - Stamp Reward", description = "백엔드 전용 스탬프 투어 보상 처리 API")
@ApiErrorCodeExamples({StampTourErrorCode.class})
public interface StampRewardAdminApi {

    @Operation(
            summary = "QR코드로 스탬프 투어 보상 처리",
            description = """
                    사용자가 제시한 스탬프 투어 QR코드를 스캔(또는 6자리 코드를 직접 입력)해 보상 지급을 처리합니다.
                    - QR코드에는 6자리 보상 코드가 그대로 인코딩되어 있습니다.
                    - 모든 도장을 완료(COMPLETED)한 투어만 보상 처리할 수 있습니다.
                    - 이미 보상을 받은(REWARDED) 투어는 409로 실패합니다.
                    - 보상 코드에 해당하는 스탬프 투어가 없으면 404로 실패합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "스탬프 투어 보상 처리 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PostMapping("/api/v1/admin/stamp-tours/reward")
    ResponseEntity<CommonResponse<StampTourRewardResponseDTO>> rewardStampTour(
            @RequestBody StampTourRewardRequestDTO request
    );
}
