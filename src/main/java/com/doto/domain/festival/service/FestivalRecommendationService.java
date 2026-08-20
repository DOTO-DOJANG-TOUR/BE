package com.doto.domain.festival.service;

import java.util.List;

import com.doto.domain.festival.dto.FestivalShortResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalRecommendationService {

    // TODO: TourApiClient 연동, 이미지 필터, 점수 계산(마감임박/다양성), 페이지네이션
    public List<FestivalShortResponseDTO> getTodayFestivals() {
        return List.of();
    }

}
