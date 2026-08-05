package com.doto.global.security;

import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.global.api.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** 인증되지 않은 요청을 CommonResponse 형식의 JSON으로 응답하는 진입점 */
@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Spring Boot 4/Jackson 3는 com.fasterxml.jackson.databind.ObjectMapper(Jackson 2)가 아니라
    // tools.jackson.databind.json.JsonMapper(Jackson 3)를 기본 Bean으로 자동 설정한다.
    private final JsonMapper jsonMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        AuthErrorCode errorCode = AuthErrorCode.UNAUTHENTICATED;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                jsonMapper.writeValueAsString(CommonResponse.error(errorCode))
        );
    }

}
