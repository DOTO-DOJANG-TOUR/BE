package com.doto.domain.stamp.controller;

import com.doto.domain.stamp.dto.StampTourRewardPreviewResponseDTO;
import com.doto.domain.stamp.dto.StampTourRewardRequestDTO;
import com.doto.domain.stamp.dto.StampTourRewardResponseDTO;
import com.doto.domain.stamp.service.StampTourService;
import com.doto.global.api.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StampRewardController implements StampRewardApi {

    private final StampTourService stampTourService;

    @Override
    public ResponseEntity<CommonResponse<StampTourRewardPreviewResponseDTO>> previewStampTourReward(
            String rewardCode
    ) {
        StampTourRewardPreviewResponseDTO result = stampTourService.previewStampTourReward(rewardCode);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @Override
    public ResponseEntity<CommonResponse<StampTourRewardResponseDTO>> rewardStampTour(
            @Valid StampTourRewardRequestDTO request
    ) {
        StampTourRewardResponseDTO result = stampTourService.rewardStampTourByRewardCode(request.rewardCode());
        return ResponseEntity.ok(CommonResponse.success(result));
    }
}
