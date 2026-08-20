package com.doto.domain.festival.controller;

import com.doto.domain.festival.dto.FestivalShortResponseDTO;
import com.doto.domain.festival.service.FestivalRecommendationService;
import com.doto.global.api.CommonResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FestivalController implements FestivalApi {

    private final FestivalRecommendationService festivalRecommendationService;

    @Override
    public ResponseEntity<CommonResponse<List<FestivalShortResponseDTO>>> getTodayFestivals() {
        return ResponseEntity.ok(
                CommonResponse.success(festivalRecommendationService.getTodayFestivals())
        );
    }

}
