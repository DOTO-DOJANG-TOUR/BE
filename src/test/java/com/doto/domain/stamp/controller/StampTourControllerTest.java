package com.doto.domain.stamp.controller;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doto.domain.member.entity.Member;
import com.doto.domain.stamp.service.StampTourService;
import com.doto.fixture.MemberFixture;
import com.doto.global.security.CurrentMemberArgumentResolver;
import com.doto.global.security.CustomMemberDetails;
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
class StampTourControllerTest {

    @Mock
    private StampTourService stampTourService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StampTourController(stampTourService))
                .setCustomArgumentResolvers(new CurrentMemberArgumentResolver())
                .build();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class 스탬프_투어_시작 {

        @Test
        void 인증된_사용자는_201을_반환한다() throws Exception {
            authenticateAs(1L);

            mockMvc.perform(post("/api/v1/festival/{festivalId}/stamp-tour", 10L))
                    .andExpect(status().isCreated());

            then(stampTourService).should().startStampTour(1L, 10L);
        }
    }

    @Nested
    class 스탬프_투어_중단 {

        @Test
        void 인증된_사용자는_204를_반환한다() throws Exception {
            authenticateAs(1L);

            mockMvc.perform(delete("/api/v1/festival/{festivalId}/stamp-tour", 10L))
                    .andExpect(status().isNoContent());

            then(stampTourService).should().endStampTour(1L, 10L);
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
