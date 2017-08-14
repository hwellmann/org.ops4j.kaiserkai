package org.ops4j.kaiserkai.rest.filter;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * Workaround for Docker daemon sending requests where Content-Type is an empty string.
 * Implementing this as a pre-matching JAX-RS ContainerRequestFilter does not help.
 *
 * @author Harald Wellmann
 *
 */
@WebFilter(urlPatterns = "/*")
public class EmptyContentTypeFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // not used
    }

    /**
     * If content type header is an empty string, the request is wrapped so that the wrapper
     * returns a content type of application/octet-stream.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getHeader(CONTENT_TYPE);

        if (contentType != null && contentType.isEmpty()) {
            chain.doFilter(new OctetStreamContentTypeWrapper(httpRequest), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // not used
    }
}
