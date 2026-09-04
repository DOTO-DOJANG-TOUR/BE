package com.doto.domain.festival.controller;

import com.doto.domain.festival.dto.FestivalDetailResponseDTO;
import com.doto.domain.festival.dto.FestivalPageResponseDTO;
import com.doto.domain.festival.dto.FestivalRegionPageResponseDTO;
import com.doto.domain.festival.entity.enums.FestivalSort;
import com.doto.domain.festival.entity.enums.RegionGroup;
import com.doto.domain.festival.service.FestivalRecommendationService;
import com.doto.global.api.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FestivalController implements FestivalApi {

    private final FestivalRecommendationService festivalRecommendationService;

    @Override
    public ResponseEntity<CommonResponse<FestivalPageResponseDTO>> getTodayFestivals(String cursor, int size) {
        return ResponseEntity.ok(
                CommonResponse.success(festivalRecommendationService.getTodayFestivals(cursor, size))
        );
    }

    @Override
    public ResponseEntity<CommonResponse<FestivalPageResponseDTO>> getUpcomingFestivals(String cursor, int size) {
        return ResponseEntity.ok(
                CommonResponse.success(festivalRecommendationService.getUpcomingFestivals(cursor, size))
        );
    }

    @Override
    public ResponseEntity<CommonResponse<FestivalRegionPageResponseDTO>> searchFestivals(String query, String cursor, int size) {
        return ResponseEntity.ok(
                CommonResponse.success(festivalRecommendationService.searchFestivals(query, cursor, size))
        );
    }

    @Override
    public ResponseEntity<CommonResponse<FestivalRegionPageResponseDTO>> getFestivalsByRegion(
            RegionGroup regionGroup, FestivalSort sort, String cursor, int size
    ) {
        return ResponseEntity.ok(
                CommonResponse.success(festivalRecommendationService.getFestivalsByRegion(regionGroup, sort, cursor, size))
        );
    }

    @Override
    public ResponseEntity<CommonResponse<FestivalDetailResponseDTO>> getFestivalDetail(Long festivalId) {
        return ResponseEntity.ok(
                CommonResponse.success(festivalRecommendationService.getFestivalDetail(festivalId))
        );
    }

}
