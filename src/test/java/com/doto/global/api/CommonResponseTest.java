package com.doto.global.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.global.error.CommonErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CommonResponseTest {

    @Nested
    class CreateResponse {

        @Test
        void 성공_응답은_결과를_포함한다() {
            CommonResponse<String> response = CommonResponse.success("result");

            assertThat(response.success()).isTrue();
            assertThat(response.code()).isEqualTo("SUCCESS");
            assertThat(response.result()).isEqualTo("result");
        }

        @Test
        void 실패_응답은_ErrorCode를_사용한다() {
            CommonResponse<Void> response = CommonResponse.error(CommonErrorCode.INVALID_INPUT);

            assertThat(response.success()).isFalse();
            assertThat(response.code()).isEqualTo("COMMON-400-001");
            assertThat(response.result()).isNull();
        }
    }
}
