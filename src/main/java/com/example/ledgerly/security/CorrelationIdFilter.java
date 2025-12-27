package com.example.ledgerly.security;

import com.example.ledgerly.observability.Correlation;
import com.example.ledgerly.observability.LogFields;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HDR_CORR = "X-Correlation-Id";
    public static final String HDR_IDEMP = "Idempotency-Key";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        UUID correlationId = Correlation.parseOrNew(request.getHeader(HDR_CORR));
        String idempotencyKey = request.getHeader(HDR_IDEMP);

        MDC.put(LogFields.CORRELATION_ID, correlationId.toString());
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            MDC.put(LogFields.IDEMPOTENCY_KEY, idempotencyKey);
        }

        response.setHeader(HDR_CORR, correlationId.toString());

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
