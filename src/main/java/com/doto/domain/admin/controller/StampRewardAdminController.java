package com.doto.domain.admin.controller;

import com.doto.domain.admin.service.StampRewardAdminService;
import com.doto.domain.stamp.dto.StampTourRewardRequestDTO;
import com.doto.domain.stamp.dto.StampTourRewardResponseDTO;
import com.doto.global.api.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StampRewardAdminController implements StampRewardAdminApi {

    private final StampRewardAdminService stampRewardAdminService;

    @Override
    public ResponseEntity<CommonResponse<StampTourRewardResponseDTO>> rewardStampTour(
            @Valid StampTourRewardRequestDTO request
    ) {
        StampTourRewardResponseDTO result = stampRewardAdminService.rewardStampTour(request.rewardCode());
        return ResponseEntity.ok(CommonResponse.success(result));
    }
}
