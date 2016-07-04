package io.ignitr.springboot.common.tracing;

import org.slf4j.MDC;
import org.springframework.cloud.sleuth.Span;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A {@link HandlerInterceptorAdapter} that sets the distributed tracing information on asynchronous requests.
 */
@Component
public class TracingHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Make sure that the current trace id and span id are passed down to asynchronous handlers
        response.setHeader(Span.TRACE_ID_NAME, MDC.get(Span.TRACE_ID_NAME));
        response.setHeader(Span.SPAN_ID_NAME, MDC.get(Span.SPAN_ID_NAME));

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}
