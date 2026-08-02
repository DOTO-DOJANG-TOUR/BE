package com.doto.global.health;

import com.doto.global.api.CommonResponse;
import com.doto.global.api.CommonSuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthApi {

    @Override
    public ResponseEntity<CommonResponse<String>> getHealth() {
        CommonSuccessCode successCode = CommonSuccessCode.OK;
        return ResponseEntity.status(successCode.getStatus())
                .body(CommonResponse.success(successCode, "UP"));
    }
}
