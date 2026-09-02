package com.doto.domain.admin.controller;

import com.doto.domain.admin.dto.TourSyncResultDTO;
import com.doto.domain.tourex.exception.TourApiErrorCode;
import com.doto.global.api.CommonResponse;
import com.doto.global.config.SwaggerConfig;
import com.doto.global.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 백엔드 전용(관리자) API, SecurityConfig 참고
@Tag(name = "Admin - Tour Sync", description = "백엔드 전용 축제·관광지 수동 동기화 API")
@ApiErrorCodeExamples({TourApiErrorCode.class})
public interface TourSyncAdminApi {

    @Operation(
            summary = "축제·관광지 수동 동기화",
            description = """
                    한국관광공사 TourAPI에서 축제 목록을 조회한 뒤 축제와 주변 관광지를 저장합니다.
                    - 매일 새벽 자동 실행되는 배치와 동일한 로직을 관리자가 즉시 수동으로 실행할 때 사용합니다.
                    - 대상 축제를 10건씩 배치로 나누어 순차 저장합니다.
                    - 날짜를 입력하지 않으면 오늘부터 30일 뒤까지의 축제를 동기화합니다.
                    - eventStartDate와 eventEndDate를 함께 입력하면 해당 축제 시작일 범위를 동기화합니다.
                    - 개별 축제 동기화가 실패해도 나머지 건은 계속 진행하며, 실패 건은 응답의 failedFestivalContentIds에서 확인할 수 있습니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "축제·관광지 동기화 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PostMapping("/api/v1/admin/tour-sync")
    ResponseEntity<CommonResponse<TourSyncResultDTO>> synchronizeFestivals(
            @Parameter(description = "동기화 대상 축제 시작일(YYYY-MM-DD), eventEndDate와 함께 입력")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate eventStartDate,

            @Parameter(description = "동기화 대상 축제 종료일(YYYY-MM-DD), eventStartDate와 함께 입력")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate eventEndDate
    );
}
