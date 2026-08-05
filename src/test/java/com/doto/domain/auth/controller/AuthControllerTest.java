package com.doto.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doto.domain.auth.dto.AuthResponseDTO;
import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.auth.service.AuthUseCase;
import com.doto.global.api.CommonResponse;
import com.doto.global.api.CommonSuccessCode;
import com.doto.global.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 응답 본문은 {@code jsonPath}로 필드를 하나씩 확인하는 대신, 기대하는 {@link CommonResponse}를 직접
 * 만들어 직렬화한 뒤 {@code content().json(...)}(lenient 모드)으로 비교한다. DTO에 필드가 추가돼도
 * 기대 객체 생성 코드만 따라가면 되고, 테스트에서 필드를 하나씩 추가로 검증할 필요가 없다.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthUseCase authUseCase;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    class 회원가입 {

        @Test
        void 성공하면_201과_토큰을_반환한다() throws Exception {
            AuthResponseDTO response = new AuthResponseDTO("1", "홍길동", "access-token", "refresh-token");
            when(authUseCase.signUp(any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"user@example.com","password":"password1","nickname":"홍길동"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            CommonResponse.success(CommonSuccessCode.CREATED, response)
                    )));
        }

        @Test
        void 이메일_형식이_잘못되면_400을_반환한다() throws Exception {
            mockMvc.perform(post("/api/v1/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"not-an-email","password":"password1","nickname":"홍길동"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 이메일이_중복되면_409를_반환한다() throws Exception {
            when(authUseCase.signUp(any())).thenThrow(new AuthException(AuthErrorCode.DUPLICATE_EMAIL));

            mockMvc.perform(post("/api/v1/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"user@example.com","password":"password1","nickname":"홍길동"}
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            CommonResponse.error(AuthErrorCode.DUPLICATE_EMAIL)
                    )));
        }
    }

    @Nested
    class 로그인 {

        @Test
        void 성공하면_200과_토큰을_반환한다() throws Exception {
            AuthResponseDTO response = new AuthResponseDTO("1", "홍길동", "access-token", "refresh-token");
            when(authUseCase.signIn(any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"user@example.com","password":"password1"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            CommonResponse.success(response)
                    )));
        }

        @Test
        void 자격_증명이_틀리면_401을_반환한다() throws Exception {
            when(authUseCase.signIn(any())).thenThrow(new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

            mockMvc.perform(post("/api/v1/auth/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"user@example.com","password":"wrong"}
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            CommonResponse.error(AuthErrorCode.INVALID_CREDENTIALS)
                    )));
        }
    }

    @Nested
    class 재발급 {

        @Test
        void 성공하면_200과_새_토큰을_반환한다() throws Exception {
            AuthResponseDTO response = new AuthResponseDTO("1", "홍길동", "new-access", "new-refresh");
            when(authUseCase.refresh(any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"refreshToken":"raw-refresh-token"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            CommonResponse.success(response)
                    )));
        }

        @Test
        void 유효하지_않은_토큰이면_401을_반환한다() throws Exception {
            when(authUseCase.refresh(any())).thenThrow(new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"refreshToken":"invalid"}
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            CommonResponse.error(AuthErrorCode.INVALID_REFRESH_TOKEN)
                    )));
        }
    }

    @Nested
    class 로그아웃 {

        @Test
        void 성공하면_204를_반환한다() throws Exception {
            mockMvc.perform(post("/api/v1/auth/sign-out")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"refreshToken":"raw-refresh-token"}
                                    """))
                    .andExpect(status().isNoContent());
        }

        @Test
        void 유효하지_않은_토큰이면_401을_반환한다() throws Exception {
            doThrow(new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN))
                    .when(authUseCase).signOut(any());

            mockMvc.perform(post("/api/v1/auth/sign-out")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"refreshToken":"invalid"}
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            CommonResponse.error(AuthErrorCode.INVALID_REFRESH_TOKEN)
                    )));
        }
    }
}
