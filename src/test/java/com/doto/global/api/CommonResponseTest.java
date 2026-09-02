package com.doto.global.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.global.error.CommonErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CommonResponseTest {

    @Nested
    class CreateResponse {

        @Test
        void 성공_응답은_결과를_포함한다() {
            CommonResponse<String> response = CommonResponse.success("result");

            assertThat(response.success()).isTrue();
            assertThat(response.code()).isEqualTo(CommonSuccessCode.OK.getCode());
            assertThat(response.message()).isEqualTo(CommonSuccessCode.OK.getMessage());
            assertThat(response.result()).isEqualTo("result");
        }

        @Test
        void 생성_응답은_CREATED_성공_코드를_사용한다() {
            CommonResponse<String> response = CommonResponse.success(CommonSuccessCode.CREATED, "result");

            assertThat(CommonSuccessCode.CREATED.getStatus()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.code()).isEqualTo("SUCCESS-201");
            assertThat(response.message()).isEqualTo("리소스가 생성되었습니다.");
        }

        @Test
        void 실패_응답은_ErrorCode를_사용한다() {
            CommonResponse<Void> response = CommonResponse.error(CommonErrorCode.INVALID_INPUT);

            assertThat(response.success()).isFalse();
            assertThat(response.code()).isEqualTo("COMMON-400-001");
            assertThat(response.result()).isNull();
        }

        @Test
        void result가_null이어도_JSON_응답에_포함된다() throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();

            String json = objectMapper.writeValueAsString(CommonResponse.success(null));

            assertThat(json).contains("\"result\":null");
        }
    }
}
