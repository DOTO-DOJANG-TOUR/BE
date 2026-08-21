package com.doto.domain.festival.controller;

import com.doto.domain.festival.dto.FestivalTodayResponseDTO;
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
    public ResponseEntity<CommonResponse<FestivalTodayResponseDTO>> getTodayFestivals(String cursor, int size) {
        return ResponseEntity.ok(
                CommonResponse.success(festivalRecommendationService.getTodayFestivals(cursor, size))
        );
    }

}
