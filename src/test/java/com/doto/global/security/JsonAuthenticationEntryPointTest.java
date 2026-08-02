package com.doto.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.doto.domain.auth.exception.AuthErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

class JsonAuthenticationEntryPointTest {

    private final JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();

    @Test
    void 인증되지_않은_요청은_CommonResponse_형식의_401_JSON을_반환한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("인증 실패"));

        assertThat(response.getStatus()).isEqualTo(AuthErrorCode.UNAUTHENTICATED.getStatus().value());
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getContentAsString()).contains(AuthErrorCode.UNAUTHENTICATED.getCode());
    }
}
