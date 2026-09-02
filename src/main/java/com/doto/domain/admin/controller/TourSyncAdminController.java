package com.doto.domain.admin.controller;

import com.doto.domain.admin.dto.TourSyncResultDTO;
import com.doto.domain.admin.service.TourSyncAdminService;
import com.doto.global.api.CommonResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TourSyncAdminController implements TourSyncAdminApi {

    private final TourSyncAdminService tourSyncAdminService;

    @Override
    public ResponseEntity<CommonResponse<TourSyncResultDTO>> synchronizeFestivals(
            LocalDate eventStartDate,
            LocalDate eventEndDate
    ) {
        TourSyncResultDTO result = tourSyncAdminService.synchronizeFestivals(eventStartDate, eventEndDate);
        return ResponseEntity.ok(CommonResponse.success(result));
    }
}
