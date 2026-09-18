package com.doto.domain.tourspot.controller;

import com.doto.domain.stamp.dto.StampTourSpotItemResponseDTO;
import com.doto.domain.tourspot.dto.TourSpotDetailResponseDTO;
import com.doto.domain.tourspot.service.TourSpotQueryService;
import com.doto.global.api.CommonResponse;
import com.doto.global.security.CustomMemberDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TourSpotController implements TourSpotApi {

    private final TourSpotQueryService tourSpotQueryService;

    @Override
    public ResponseEntity<CommonResponse<List<StampTourSpotItemResponseDTO>>> searchTourSpots(
            Long festivalId, String keyword
    ) {
        return ResponseEntity.ok(CommonResponse.success(tourSpotQueryService.searchTourSpots(festivalId, keyword)));
    }

    @Override
    public ResponseEntity<CommonResponse<TourSpotDetailResponseDTO>> getTourSpotDetail(
            CustomMemberDetails memberDetails, Long festivalId, Long tourSpotId
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                tourSpotQueryService.getTourSpotDetail(memberDetails.getMemberId(), festivalId, tourSpotId)
        ));
    }
}
