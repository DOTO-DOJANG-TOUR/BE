package com.doto.domain.stamp.controller;

import com.doto.domain.stamp.dto.CurrentVisitTourSpotResponseDTO;
import com.doto.domain.stamp.dto.MyStampResponseDTO;
import com.doto.domain.stamp.dto.MyStampTourResponseDTO;
import com.doto.domain.stamp.dto.StampLocationRequestDTO;
import com.doto.domain.stamp.dto.StampResponseDTO;
import com.doto.domain.stamp.dto.TourQRCodeResponseDTO;
import com.doto.domain.stamp.service.StampService;
import com.doto.global.api.CommonResponse;
import com.doto.global.api.CommonSuccessCode;
import com.doto.global.security.CustomMemberDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StampController implements StampApi {

    private final StampService stampService;

    @Override
    public ResponseEntity<CommonResponse<CurrentVisitTourSpotResponseDTO>> startTourSpotVisit(
            CustomMemberDetails memberDetails,
            Long festivalId,
            Long tourSpotId
    ) {
        CurrentVisitTourSpotResponseDTO result = stampService.startTourSpotVisit(
                memberDetails.getMemberId(), festivalId, tourSpotId
        );
        return ResponseEntity.status(CommonSuccessCode.CREATED.getStatus())
                .body(CommonResponse.success(CommonSuccessCode.CREATED, result));
    }

    @Override
    public ResponseEntity<Void> stopTourSpotVisit(
            CustomMemberDetails memberDetails,
            Long festivalId,
            Long tourSpotId
    ) {
        stampService.stopTourSpotVisit(memberDetails.getMemberId(), festivalId, tourSpotId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CommonResponse<StampResponseDTO>> completeStamp(
            CustomMemberDetails memberDetails,
            Long festivalId,
            Long tourSpotId,
            @Valid StampLocationRequestDTO request
    ) {
        return ResponseEntity.ok(CommonResponse.success(stampService.completeStamp(
                memberDetails.getMemberId(),
                festivalId,
                tourSpotId,
                request
        )));
    }

    @Override
    public ResponseEntity<CommonResponse<CurrentVisitTourSpotResponseDTO>> getCurrentVisitTourSpot(
            CustomMemberDetails memberDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(stampService.getCurrentVisitTourSpot(memberDetails.getMemberId())));
    }

    @Override
    public ResponseEntity<CommonResponse<MyStampTourResponseDTO>> getMyStampTours(
            CustomMemberDetails memberDetails
    ) {
        return ResponseEntity.ok(CommonResponse.success(stampService.getMyStampTours(memberDetails.getMemberId())));
    }

    @Override
    public ResponseEntity<CommonResponse<MyStampResponseDTO>> getMyStamp(
            CustomMemberDetails memberDetails,
            Long festivalId
    ) {
        return ResponseEntity.ok(CommonResponse.success(stampService.getMyStamp(memberDetails.getMemberId(), festivalId)));
    }

    @Override
    public ResponseEntity<CommonResponse<TourQRCodeResponseDTO>> getTourQRCode(
            CustomMemberDetails memberDetails,
            Long festivalId
    ) {
        return ResponseEntity.ok(CommonResponse.success(stampService.getTourQRCode(memberDetails.getMemberId(), festivalId)));
    }
}
