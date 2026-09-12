package com.doto.domain.admin.service;

import com.doto.domain.stamp.dto.StampTourRewardResponseDTO;
import com.doto.domain.stamp.service.StampTourService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 관리자가 사용자의 스탬프 투어 QR코드를 스캔해 보상을 처리
@Service
@RequiredArgsConstructor
public class StampRewardAdminService {

    private final StampTourService stampTourService;

    public StampTourRewardResponseDTO rewardStampTour(String rewardCode) {
        return stampTourService.rewardStampTourByRewardCode(rewardCode);
    }
}
