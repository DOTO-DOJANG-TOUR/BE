package com.doto.global.health;

import com.doto.global.api.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthApi {

    @Override
    public ResponseEntity<CommonResponse<String>> getHealth() {
        return ResponseEntity.ok(CommonResponse.success("UP"));
    }
}
