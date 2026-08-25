package com.doto.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

// 관리자 수동 동기화 결과, TourSyncAdminService 참고
@Schema(description = "관리자 축제·관광지 동기화 결과")
public record TourSyncResultDTO(
        @Schema(description = "동기화 기준일(eventStartDate)", example = "2026-08-19")
        LocalDate eventStartDate,

        @Schema(description = "동기화 대상 축제 수", example = "23")
        int requestedCount,

        @Schema(description = "배치 처리 단위", example = "10")
        int batchSize,

        @Schema(description = "동기화 성공 건수", example = "21")
        int successCount,

        @Schema(description = "동기화 실패 건수", example = "2")
        int failedCount,

        @Schema(description = "동기화에 실패한 축제의 contentId 목록")
        List<Long> failedFestivalContentIds,

        @Schema(description = "배치별 처리 결과")
        List<BatchResultDTO> batches
) {

    @Schema(description = "배치 단위 처리 결과")
    public record BatchResultDTO(
            @Schema(description = "배치 순번(1부터 시작)", example = "1")
            int batchIndex,

            @Schema(description = "배치 크기(마지막 배치는 batchSize보다 작을 수 있음)", example = "10")
            int batchSize,

            @Schema(description = "배치 내 성공 건수", example = "10")
            int successCount,

            @Schema(description = "배치 내 실패 건수", example = "0")
            int failedCount
    ) {
    }
}
