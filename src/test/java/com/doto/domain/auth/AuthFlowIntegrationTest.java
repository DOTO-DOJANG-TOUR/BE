package com.doto.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doto.global.config.TestcontainersConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

/** 회원가입 → 로그인 → 재발급 → 로그아웃 전체 흐름을 실제 DB(Flyway 스키마)와 Security 필터 체인까지 통과시켜 검증한다 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 회원가입_로그인_재발급_로그아웃_전체_흐름이_정상적으로_동작한다() throws Exception {
        String email = "flow-user@example.com";
        String password = "stringst";

        String signUpBody = objectMapper.writeValueAsString(new SignUpRequest(email, password, "홍길동"));

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.result.refreshToken").isNotEmpty());

        String signInBody = objectMapper.writeValueAsString(new SignInRequest(email, password));

        MvcResult signInResult = mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signInBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").isNotEmpty())
                .andReturn();

        String accessToken = readResultField(signInResult, "accessToken");
        String refreshToken = readResultField(signInResult, "refreshToken");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.nickname").value("홍길동"));

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\": \"수정된닉네임\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.nickname").value("수정된닉네임"));

        String refreshBody = objectMapper.writeValueAsString(new RefreshRequest(refreshToken));

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").isNotEmpty())
                .andReturn();

        String rotatedRefreshToken = readResultField(refreshResult, "refreshToken");
        assertThat(rotatedRefreshToken).isNotEqualTo(refreshToken);

        // 로그인 시 발급받은 refreshToken은 재발급 과정에서 즉시 폐기되어 재사용할 수 없다
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-401-003"));

        String signOutBody = objectMapper.writeValueAsString(new RefreshRequest(rotatedRefreshToken));

        mockMvc.perform(post("/api/v1/auth/sign-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signOutBody))
                .andExpect(status().isNoContent());

        // 로그아웃으로 폐기된 refreshToken은 더 이상 재발급에 쓸 수 없다
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signOutBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-401-003"));
    }

    @Test
    void 인증_없이_내_정보_조회를_시도하면_401이_난다() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 이미_가입된_이메일로_회원가입하면_409가_난다() throws Exception {
        String email = "duplicate-user@example.com";
        String signUpBody = objectMapper.writeValueAsString(new SignUpRequest(email, "stringst", "중복유저"));

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH-409-001"));
    }

    @Test
    void 잘못된_비밀번호로_로그인하면_401이_난다() throws Exception {
        String email = "wrong-password-user@example.com";
        String signUpBody = objectMapper.writeValueAsString(new SignUpRequest(email, "stringst", "테스트유저"));

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody))
                .andExpect(status().isCreated());

        String signInBody = objectMapper.writeValueAsString(new SignInRequest(email, "wrongpassword"));

        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signInBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-401-001"));
    }

    private String readResultField(MvcResult result, String field) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("result").path(field).asText();
    }

    private record SignUpRequest(String email, String password, String nickname) {
    }

    private record SignInRequest(String email, String password) {
    }

    private record RefreshRequest(String refreshToken) {
    }
}
