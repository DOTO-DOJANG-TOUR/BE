package com.doto.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.doto.domain.user.entity.User;
import com.doto.domain.user.repository.UserRepository;
import com.doto.global.config.JwtProperties;
import com.doto.global.security.CustomUserDetails;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private UserRepository userRepository;

    private JwtTokenProvider jwtTokenProvider;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                new JwtProperties("test-jwt-secret-key-must-be-long-enough-32bytes", 3600, 1209600)
        );
        filter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class 유효한_토큰 {

        @Test
        void 인증_정보를_SecurityContext에_채운다() throws Exception {
            User user = User.register("홍길동");
            ReflectionTestUtils.setField(user, "id", 1L);
            String token = jwtTokenProvider.createAccessToken(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(((CustomUserDetails) authentication.getPrincipal()).getUserId()).isEqualTo(1L);
        }
    }

    @Nested
    class 비활성_사용자 {

        @Test
        void 토큰은_유효해도_계정이_비활성화됐으면_인증_정보를_채우지_않는다() throws Exception {
            User user = User.register("홍길동");
            ReflectionTestUtils.setField(user, "id", 1L);
            user.deactivate();
            String token = jwtTokenProvider.createAccessToken(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    class 토큰이_없거나_유효하지_않음 {

        @Test
        void 헤더가_없으면_인증_정보를_채우지_않는다() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verifyNoInteractions(userRepository);
        }

        @Test
        void 유효하지_않은_토큰이면_인증_정보를_채우지_않는다() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer invalid-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}
