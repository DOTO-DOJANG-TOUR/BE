package com.doto.domain.tourspot.service;

import com.doto.domain.stamp.dto.StampTourSpotItemResponseDTO;
import com.doto.domain.tourspot.dto.TourSpotDetailResponseDTO;
import com.doto.domain.tourspot.entity.FestivalTourSpot;
import com.doto.domain.tourspot.entity.TourSpot;
import com.doto.domain.tourspot.entity.enums.TourSpotCategory;
import com.doto.domain.tourspot.exception.TourErrorCode;
import com.doto.domain.tourspot.exception.TourException;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import com.doto.domain.tourspot.repository.TourSpotRepository;
import com.doto.global.util.DistanceUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourSpotQueryService {

    private final FestivalTourSpotRepository festivalTourSpotRepository;
    private final TourSpotRepository tourSpotRepository;

    public List<TourSpot> getTourSpotsByFestivalId(Long festivalId) {
        return festivalTourSpotRepository.findAllWithTourSpotByFestivalId(festivalId)
                .stream()
                .map(FestivalTourSpot::getTourSpot)
                .toList();
    }

    public TourSpotDetailResponseDTO getTourSpotDetail(Long festivalId, Long tourSpotId) {
        if (!festivalTourSpotRepository.existsByFestival_IdAndTourSpot_Id(festivalId, tourSpotId)) {
            throw new TourException(TourErrorCode.TOUR_SPOT_NOT_FOUND);
        }

        TourSpot tourSpot = tourSpotRepository.findById(tourSpotId)
                .orElseThrow(() -> new TourException(TourErrorCode.TOUR_SPOT_NOT_FOUND));
        return new TourSpotDetailResponseDTO(
                String.valueOf(tourSpot.getId()),
                tourSpot.getTitle(),
                tourSpot.getImageUrl(),
                tourSpot.getAddress(),
                String.valueOf(tourSpot.getLocation().getX()),
                String.valueOf(tourSpot.getLocation().getY()),
                TourSpotCategory.valueOf(tourSpot.getCategory()),
                tourSpot.getLegalDongRegionCode(),
                tourSpot.getLegalDongSigunguCode(),
                tourSpot.getPhone(),
                tourSpot.getApiModifiedAt()
        );
    }

    // festivalId,키워드로 관광지 검색
    public List<StampTourSpotItemResponseDTO> searchTourSpots(Long festivalId, String keyword) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        List<FestivalTourSpot> festivalTourSpots = normalizedKeyword == null
                ? festivalTourSpotRepository.findAllWithTourSpotByFestivalId(festivalId)
                : festivalTourSpotRepository.searchAllWithTourSpotByFestivalIdAndKeyword(festivalId, normalizedKeyword);
        return festivalTourSpots
                .stream()
                .map(this::toStampTourSpotItem)
                .toList();
    }

    private StampTourSpotItemResponseDTO toStampTourSpotItem(FestivalTourSpot festivalTourSpot) {
        TourSpot tourSpot = festivalTourSpot.getTourSpot();
        return new StampTourSpotItemResponseDTO(
                String.valueOf(tourSpot.getId()),
                tourSpot.getTitle(),
                tourSpot.getImageUrl(),
                tourSpot.getAddress(),
                String.valueOf(tourSpot.getLocation().getX()),
                String.valueOf(tourSpot.getLocation().getY()),
                tourSpot.getCategory(),
                DistanceUtils.format(festivalTourSpot.getDistanceMeters())
        );
    }

}
