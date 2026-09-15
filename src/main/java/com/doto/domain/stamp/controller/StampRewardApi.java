package com.doto.domain.stamp.controller;

import com.doto.domain.stamp.dto.StampTourRewardPreviewResponseDTO;
import com.doto.domain.stamp.dto.StampTourRewardRequestDTO;
import com.doto.domain.stamp.dto.StampTourRewardResponseDTO;
import com.doto.domain.stamp.exception.StampTourErrorCode;
import com.doto.global.api.CommonResponse;
import com.doto.global.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

// QR을 스캔한 방문객이 로그인 없이 바로 여는 보상 처리 화면에서 호출하는 API라 인증 없이 공개됨
@Tag(name = "Stamp Reward", description = "QR코드 기반 스탬프 투어 보상 처리 API (인증 불필요)")
@ApiErrorCodeExamples({StampTourErrorCode.class})
public interface StampRewardApi {

    @Operation(
            summary = "QR코드로 스탬프 투어 보상 미리보기 조회",
            description = """
                    QR코드(보상 처리 화면 URL)에 담긴 보상 코드로 누구의 투어인지 미리 조회합니다.
                    - 화면에 회원 정보를 보여주고, 실제 보상 지급은 이 조회 결과를 확인한 뒤 별도로 보상 처리 API를 호출해야 합니다.
                    - 이 호출만으로는 투어 상태가 바뀌지 않습니다.
                    - 모든 도장을 완료(COMPLETED)하지 않았거나 이미 보상을 받은(REWARDED) 투어면 보상 처리와 동일하게 각각 409로 실패합니다.
                    - 보상 코드에 해당하는 스탬프 투어가 없으면 404로 실패합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "스탬프 투어 보상 미리보기 조회 성공")
    @GetMapping("/api/v1/stamp-tours/reward")
    ResponseEntity<CommonResponse<StampTourRewardPreviewResponseDTO>> previewStampTourReward(
            @Parameter(description = "QR코드에 담긴 6자리 보상 코드", example = "048213")
            @RequestParam String rewardCode
    );

    @Operation(
            summary = "QR코드로 스탬프 투어 보상 처리",
            description = """
                    미리보기 화면에서 확인한 뒤 "확인하기"를 누르면 호출되는, 실제 보상 지급을 처리하는 API입니다.
                    - 모든 도장을 완료(COMPLETED)한 투어만 보상 처리할 수 있습니다.
                    - 이미 보상을 받은(REWARDED) 투어는 409로 실패합니다.
                    - 보상 코드에 해당하는 스탬프 투어가 없으면 404로 실패합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "스탬프 투어 보상 처리 성공")
    @PostMapping("/api/v1/stamp-tours/reward")
    ResponseEntity<CommonResponse<StampTourRewardResponseDTO>> rewardStampTour(
            @RequestBody StampTourRewardRequestDTO request
    );
}
