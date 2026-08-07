package com.doto.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doto.domain.member.dto.UserResponseDTO;
import com.doto.domain.member.entity.Member;
import com.doto.domain.member.service.MemberService;
import com.doto.global.error.GlobalExceptionHandler;
import com.doto.global.security.CurrentMemberArgumentResolver;
import com.doto.global.security.CustomMemberDetails;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MemberController(memberService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentMemberArgumentResolver())
                .build();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(long memberId) {
        Member member = Member.register("홍길동");
        ReflectionTestUtils.setField(member, "id", memberId);
        CustomMemberDetails principal = new CustomMemberDetails(member);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Nested
    class 내_정보_조회 {

        @Test
        void 인증된_사용자면_200과_내_정보를_반환한다() throws Exception {
            authenticateAs(1L);
            UserResponseDTO response = new UserResponseDTO(
                    "1", "member@example.com", "홍길동", "ACTIVE", Instant.parse("2026-08-02T00:00:00Z")
            );
            when(memberService.getMyInfo(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/members/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.nickname").value("홍길동"))
                    .andExpect(jsonPath("$.result.email").value("member@example.com"));
        }

        @Test
        void 인증되지_않으면_401을_반환한다() throws Exception {
            mockMvc.perform(get("/api/v1/members/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 내_정보_수정 {

        @Test
        void 닉네임을_보내면_수정_후_최신_정보를_반환한다() throws Exception {
            authenticateAs(1L);
            UserResponseDTO response = new UserResponseDTO(
                    "1", "member@example.com", "김철수", "ACTIVE", Instant.parse("2026-08-02T00:00:00Z")
            );
            when(memberService.getMyInfo(1L)).thenReturn(response);

            mockMvc.perform(patch("/api/v1/members/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nickname":"김철수"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.nickname").value("김철수"));

            verify(memberService).updateMyInfo(eq(1L), any());
        }

        @Test
        void 닉네임_길이가_짧으면_400을_반환한다() throws Exception {
            authenticateAs(1L);

            mockMvc.perform(patch("/api/v1/members/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nickname":"a"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 인증되지_않으면_401을_반환한다() throws Exception {
            mockMvc.perform(patch("/api/v1/members/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nickname":"홍길동"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }
}
