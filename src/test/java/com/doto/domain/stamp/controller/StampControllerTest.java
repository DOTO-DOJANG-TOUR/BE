package com.doto.domain.stamp.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doto.domain.member.entity.Member;
import com.doto.domain.stamp.dto.CurrentVisitTourSpotResponseDTO;
import com.doto.domain.stamp.service.StampService;
import com.doto.fixture.MemberFixture;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class StampControllerTest {

    @Mock
    private StampService stampService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StampController(stampService))
                .setCustomArgumentResolvers(new CurrentMemberArgumentResolver())
                .build();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class 관광지_방문_시작 {

        @Test
        void 인증된_사용자는_201을_반환한다() throws Exception {
            authenticateAs(1L);
            given(stampService.startTourSpotVisit(1L, 10L, 100L))
                    .willReturn(new CurrentVisitTourSpotResponseDTO("100", "관광지", Instant.now().plusSeconds(1)));

            mockMvc.perform(post("/api/v1/festival/{festivalId}/tour-spots/{tourSpotId}/visit", 10L, 100L))
                    .andExpect(status().isCreated());

            then(stampService).should().startTourSpotVisit(1L, 10L, 100L);
        }
    }

    @Nested
    class 관광지_방문_중단 {

        @Test
        void 인증된_사용자는_204를_반환한다() throws Exception {
            authenticateAs(1L);

            mockMvc.perform(delete("/api/v1/festival/{festivalId}/tour-spots/{tourSpotId}/visit", 10L, 100L))
                    .andExpect(status().isNoContent());

            then(stampService).should().stopTourSpotVisit(1L, 10L, 100L);
        }
    }

    private void authenticateAs(Long memberId) {
        Member member = MemberFixture.create(memberId);
        CustomMemberDetails principal = new CustomMemberDetails(member);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
