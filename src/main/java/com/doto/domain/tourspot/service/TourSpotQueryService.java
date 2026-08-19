package com.doto.domain.tourspot.service;

import com.doto.domain.tourspot.entity.FestivalTourSpot;
import com.doto.domain.tourspot.entity.TourSpot;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourSpotQueryService {

    private final FestivalTourSpotRepository festivalTourSpotRepository;

    public List<TourSpot> getTourSpotsByFestivalId(Long festivalId) {
        return festivalTourSpotRepository.findAllWithTourSpotByFestivalId(festivalId)
                .stream()
                .map(FestivalTourSpot::getTourSpot)
                .toList();
    }
}
