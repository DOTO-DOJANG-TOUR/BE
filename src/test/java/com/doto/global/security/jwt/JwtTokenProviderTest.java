package com.doto.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.global.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    private final JwtProperties jwtProperties =
            new JwtProperties("test-jwt-secret-key-must-be-long-enough-32bytes", 3600, 1209600);
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtProperties);

    @Nested
    class 토큰_발급과_해석 {

        @Test
        void 발급한_토큰에서_사용자_ID를_다시_꺼낼_수_있다() {
            String token = jwtTokenProvider.createAccessToken(123456789L);

            assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(123456789L);
        }

        @Test
        void 발급한_토큰은_유효하다고_검증된다() {
            String token = jwtTokenProvider.createAccessToken(1L);

            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        @Test
        void 만료_시간_설정값을_그대로_돌려준다() {
            assertThat(jwtTokenProvider.getExpirationSeconds()).isEqualTo(3600);
        }
    }

    @Nested
    class 토큰_검증_실패 {

        @Test
        void 형식이_깨진_토큰은_유효하지_않다() {
            assertThat(jwtTokenProvider.validateToken("not-a-jwt")).isFalse();
        }

        @Test
        void 다른_비밀키로_서명된_토큰은_유효하지_않다() {
            JwtTokenProvider otherProvider = new JwtTokenProvider(
                    new JwtProperties("other-jwt-secret-key-must-be-long-enough-32b", 3600, 1209600)
            );
            String token = otherProvider.createAccessToken(1L);

            assertThat(jwtTokenProvider.validateToken(token)).isFalse();
        }

        @Test
        void 타입_claim이_access가_아니면_서명이_유효해도_거부된다() {
            SecretKey key = (SecretKey) ReflectionTestUtils.getField(jwtTokenProvider, "key");
            String token = Jwts.builder()
                    .subject("1")
                    .claim("type", "refresh")
                    .signWith(key)
                    .compact();

            assertThat(jwtTokenProvider.validateToken(token)).isFalse();
        }
    }

    @Nested
    class 헤더에서_토큰_추출 {

        @Test
        void Bearer_헤더에서_토큰만_추출한다() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer abc.def.ghi");

            assertThat(JwtTokenProvider.resolveToken(request)).isEqualTo("abc.def.ghi");
        }

        @Test
        void 헤더가_없으면_null이다() {
            MockHttpServletRequest request = new MockHttpServletRequest();

            assertThat(JwtTokenProvider.resolveToken(request)).isNull();
        }

        @Test
        void Bearer_형식이_아니면_null이다() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Basic abcdef");

            assertThat(JwtTokenProvider.resolveToken(request)).isNull();
        }
    }
}
