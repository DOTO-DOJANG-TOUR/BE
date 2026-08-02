package com.doto.global.logging;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @Nested
    class TraceIdPropagation {

        @Test
        void 요청의_TraceId를_응답에_전달한다() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/places");
            request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "client-trace-1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = (req, res) -> assertThat(MDC.get(TraceIdFilter.TRACE_ID))
                    .isEqualTo("client-trace-1");

            filter.doFilter(request, response, chain);

            assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo("client-trace-1");
            assertThat(MDC.get(TraceIdFilter.TRACE_ID)).isNull();
        }

        @Test
        void 안전하지_않은_TraceId는_새로_생성한다() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
            request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "invalid trace id");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> { });

            assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER))
                    .isNotBlank()
                    .isNotEqualTo("invalid trace id");
        }
    }
}
