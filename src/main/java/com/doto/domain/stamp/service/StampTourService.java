package com.doto.domain.stamp.service;

import com.doto.domain.tourspot.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StampTourService {
    private final TourSpotRepository tourSpotRepository;

}
