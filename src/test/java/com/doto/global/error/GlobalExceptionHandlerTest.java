package com.doto.global.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doto.global.api.CommonResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(handler)
                .build();
    }

    @Nested
    class 애플리케이션_예외 {

        @Test
        void 도메인_예외를_ErrorCode의_HTTP_응답으로_변환한다() {
            TestDomainException exception = new TestDomainException(CommonErrorCode.INVALID_INPUT);

            ResponseEntity<CommonResponse<Void>> response = handler.handleDomainException(exception);

            assertThat(response.getStatusCode()).isEqualTo(CommonErrorCode.INVALID_INPUT.getStatus());
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo(CommonErrorCode.INVALID_INPUT.getCode());
        }
    }

    @Nested
    class Spring_MVC_예외 {

        @Test
        void 요청_본문을_읽을_수_없으면_400을_반환한다() throws Exception {
            mockMvc.perform(post("/test/body")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(CommonErrorCode.MALFORMED_REQUEST.getCode()));
        }

        @Test
        void RequestBody_검증에_실패하면_필드_오류와_400을_반환한다() throws Exception {
            mockMvc.perform(post("/test/body")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(CommonErrorCode.INVALID_INPUT.getCode()))
                    .andExpect(jsonPath("$.result.name").value("이름은 필수입니다."));
        }

        @Test
        void 필수_요청_파라미터가_누락되면_400을_반환한다() throws Exception {
            mockMvc.perform(get("/test/parameter"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        void 경로_변수의_타입이_맞지_않으면_400을_반환한다() throws Exception {
            mockMvc.perform(get("/test/items/not-a-number"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(CommonErrorCode.INVALID_INPUT.getCode()));
        }

        @Test
        void 지원하지_않는_HTTP_메서드이면_405를_반환한다() throws Exception {
            mockMvc.perform(patch("/test/parameter"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.code").value(CommonErrorCode.METHOD_NOT_ALLOWED.getCode()));
        }

        @Test
        void 지원하지_않는_ContentType이면_415를_반환한다() throws Exception {
            mockMvc.perform(post("/test/body")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("name"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.code").value(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode()));
        }
    }

    @RestController
    private static final class TestController {

        @GetMapping("/test/parameter")
        String parameter(@RequestParam String query) {
            return query;
        }

        @GetMapping("/test/items/{id}")
        Long item(@PathVariable Long id) {
            return id;
        }

        @PostMapping(value = "/test/body", consumes = MediaType.APPLICATION_JSON_VALUE)
        String body(@Valid @RequestBody TestRequest request) {
            return request.name();
        }
    }

    private record TestRequest(
            @NotBlank(message = "이름은 필수입니다.") String name
    ) {
    }

    private static final class TestDomainException extends DomainException {

        private TestDomainException(ErrorCode errorCode) {
            super(errorCode);
        }
    }
}
