package com.doto.domain.stamp.controller;

import com.doto.domain.stamp.dto.StampTourDetailResponseDTO;
import com.doto.domain.stamp.dto.StampTourStatusResponseDTO;
import com.doto.domain.stamp.dto.StampTourViewStatus;
import com.doto.domain.stamp.service.StampTourService;
import com.doto.domain.tourspot.entity.enums.TourSpotCategoryFilter;
import com.doto.global.api.CommonResponse;
import com.doto.global.api.CommonSuccessCode;
import com.doto.global.security.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StampTourController implements StampTourApi {

    private final StampTourService stampTourService;

    @Override
    public ResponseEntity<Void> startStampTour(
            CustomMemberDetails memberDetails, Long festivalId
    ) {
        stampTourService.startStampTour(memberDetails.getMemberId(), festivalId);
        return ResponseEntity.status(CommonSuccessCode.CREATED.getStatus())
                .build();
    }

    @Override
    public ResponseEntity<Void> endStampTour(CustomMemberDetails memberDetails, Long festivalId) {
        stampTourService.endStampTour(memberDetails.getMemberId(), festivalId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CommonResponse<StampTourStatusResponseDTO>> getStampTourStatus(
            CustomMemberDetails memberDetails, Long festivalId
    ) {
        StampTourViewStatus status = stampTourService.getStampTourStatus(memberDetails.getMemberId(), festivalId);
        return ResponseEntity.ok(CommonResponse.success(new StampTourStatusResponseDTO(status)));
    }

    @Override
    public ResponseEntity<CommonResponse<StampTourDetailResponseDTO>> getCurrentStampTour(
            CustomMemberDetails memberDetails, TourSpotCategoryFilter category
    ) {
        StampTourDetailResponseDTO result = stampTourService.getCurrentStampTour(
                memberDetails.getMemberId(),
                category
        );
        return ResponseEntity.ok(CommonResponse.success(result));
    }
}
