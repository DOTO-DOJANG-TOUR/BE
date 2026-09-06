package com.doto.domain.tourspot.service;

import com.doto.domain.stamp.dto.StampTourSpotItemResponseDTO;
import com.doto.domain.tourspot.dto.TourSpotDetailResponseDTO;
import com.doto.domain.tourspot.entity.FestivalTourSpot;
import com.doto.domain.tourspot.entity.TourSpot;
import com.doto.domain.tourspot.entity.enums.TourSpotCategory;
import com.doto.domain.tourspot.entity.TourSpotImage;
import com.doto.domain.tourspot.exception.TourErrorCode;
import com.doto.domain.tourspot.exception.TourException;
import com.doto.domain.tourspot.repository.FestivalTourSpotRepository;
import com.doto.domain.tourspot.repository.TourSpotImageRepository;
import com.doto.domain.tourspot.repository.TourSpotRepository;
import com.doto.global.util.DistanceUtils;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourSpotQueryService {

    // 상세 조회 응답의 이미지 개수(대표 이미지 포함 최대 4장)
    private static final int IMAGE_LIST_SIZE = 4;

    private final FestivalTourSpotRepository festivalTourSpotRepository;
    private final TourSpotRepository tourSpotRepository;
    private final TourSpotImageRepository tourSpotImageRepository;

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
        List<String> imageList = buildImageList(tourSpot);
        return new TourSpotDetailResponseDTO(
                String.valueOf(tourSpot.getId()),
                tourSpot.getTitle(),
                tourSpot.getImageUrl(),
                imageList,
                tourSpot.getAddress(),
                String.valueOf(tourSpot.getLocation().getX()),
                String.valueOf(tourSpot.getLocation().getY()),
                TourSpotCategory.valueOf(tourSpot.getCategory()),
                tourSpot.getLegalDongSigunguCode(),
                tourSpot.getPhone(),
                tourSpot.getApiModifiedAt()
        );
    }

    // 대표 이미지가 있으면 대표 이미지 1장 + 세부 이미지 3장, 없으면 세부 이미지 4장을 응답 이미지 목록으로 구성
    // TourAPI 갤러리에 대표 이미지가 그대로 포함된 경우가 있어 세부 이미지에서는 대표 이미지를 제외한다
    private List<String> buildImageList(TourSpot tourSpot) {
        String representativeImageUrl = tourSpot.getImageUrl();
        int detailImageLimit = representativeImageUrl == null ? IMAGE_LIST_SIZE : IMAGE_LIST_SIZE - 1;

        Stream<TourSpotImage> detailImages = tourSpotImageRepository
                .findAllByTourSpot_IdOrderBySerialNumberAsc(tourSpot.getId())
                .stream();
        if (representativeImageUrl != null) {
            detailImages = detailImages.filter(image -> !representativeImageUrl.equals(image.getImageUrl()));
        }
        Stream<String> detailImageUrls = detailImages.map(TourSpotImage::getImageUrl).limit(detailImageLimit);

        if (representativeImageUrl == null) {
            return detailImageUrls.toList();
        }
        return Stream.concat(Stream.of(representativeImageUrl), detailImageUrls).toList();
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
