package com.doto.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.global.error.ErrorCode;
import com.doto.global.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.lang.reflect.Method;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

class SwaggerConfigTest {

    private final SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Test
    @DisplayName("공통 응답 스키마와 JWT 인증 방식을 등록한다")
    void registersCommonResponseAndBearerAuth() {
        OpenAPI openAPI = swaggerConfig.dotoOpenApi();

        assertThat(openAPI.getComponents().getSchemas()).containsKey("CommonResponse");
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey(SwaggerConfig.BEARER_AUTH);
        assertThat(openAPI.getSecurity()).isNull();
    }

    @Test
    @DisplayName("공통 오류와 도메인 오류 예시를 HTTP 상태별로 문서화한다")
    void documentsCommonAndDomainErrors() throws NoSuchMethodException {
        Operation operation = new Operation().responses(new ApiResponses());
        Method method = SampleController.class.getMethod("getPlace");
        HandlerMethod handlerMethod = new HandlerMethod(new SampleController(), method);

        swaggerConfig.errorResponseCustomizer().customize(operation, handlerMethod);

        ApiResponse badRequest = operation.getResponses().get("400");
        ApiResponse notFound = operation.getResponses().get("404");
        assertThat(badRequest.getContent().get("application/json").getExamples())
                .containsKeys("COMMON-400-001", "COMMON-400-002");
        assertThat(notFound.getContent().get("application/json").getExamples())
                .containsKey("PLACE-404-001");
        assertThat(operation.getResponses()).containsKey("500");
    }

    @ApiErrorCodeExamples(PlaceErrorCode.class)
    private interface SampleApi {
    }

    private static final class SampleController implements SampleApi {
        public void getPlace() {
        }
    }

    @Getter
    private enum PlaceErrorCode implements ErrorCode {
        NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE-404-001", "장소를 찾을 수 없습니다.");

        private final HttpStatus status;
        private final String code;
        private final String message;

        PlaceErrorCode(HttpStatus status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }
    }
}
