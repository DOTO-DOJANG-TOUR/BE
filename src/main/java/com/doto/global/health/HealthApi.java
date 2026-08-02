package com.doto.global.health;

import com.doto.global.api.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Health", description = "애플리케이션 상태 확인 API")
public interface HealthApi {

    @Operation(summary = "애플리케이션 상태 확인", description = "애플리케이션이 요청에 응답할 수 있는지 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "애플리케이션 정상")
    })
    @GetMapping("/health")
    ResponseEntity<CommonResponse<String>> getHealth();
}
