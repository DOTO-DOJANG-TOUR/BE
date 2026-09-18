package com.doto.domain.tourspot.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doto.domain.member.entity.Member;
import com.doto.domain.stamp.dto.StampTourSpotItemResponseDTO;
import com.doto.domain.tourspot.dto.TourSpotDetailResponseDTO;
import com.doto.domain.tourspot.entity.enums.TourSpotCategory;
import com.doto.domain.tourspot.service.TourSpotQueryService;
import com.doto.fixture.MemberFixture;
import com.doto.global.security.CurrentMemberArgumentResolver;
import com.doto.global.security.CustomMemberDetails;
import java.util.List;
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
class TourSpotControllerTest {

    @Mock
    private TourSpotQueryService tourSpotQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TourSpotController(tourSpotQueryService))
                .setCustomArgumentResolvers(new CurrentMemberArgumentResolver())
                .build();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long memberId) {
        Member member = MemberFixture.create(memberId);
        CustomMemberDetails principal = new CustomMemberDetails(member);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Nested
    class 축제_관광지_검색 {

        @Test
        void 키워드와_함께_목록을_반환한다() throws Exception {
            given(tourSpotQueryService.searchTourSpots(10L, "해수욕장"))
                    .willReturn(List.of(new StampTourSpotItemResponseDTO(
                            "100", "대천해수욕장", null, "충남 보령시", "126.5", "36.3", "자연", "1.2km"
                    )));

            mockMvc.perform(get("/api/v1/festival/{festivalId}/tour-spots", 10L).param("keyword", "해수욕장"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result[0].tourSpotId").value("100"));

            then(tourSpotQueryService).should().searchTourSpots(10L, "해수욕장");
        }
    }

    @Nested
    class 축제_관광지_상세_조회 {

        @Test
        void 상세_정보를_반환한다() throws Exception {
            authenticateAs(1L);
            given(tourSpotQueryService.getTourSpotDetail(1L, 10L, 100L))
                    .willReturn(new TourSpotDetailResponseDTO(
                            "100", "대천해수욕장", null, List.of(), "충남 보령시", "126.5", "36.3",
                            TourSpotCategory.자연, "451", null, null, null, true
                    ));

            mockMvc.perform(get("/api/v1/festival/{festivalId}/tour-spots/{tourSpotId}", 10L, 100L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.tourSpotId").value("100"))
                    .andExpect(jsonPath("$.result.isVisited").value(true));

            then(tourSpotQueryService).should().getTourSpotDetail(1L, 10L, 100L);
        }
    }
}
