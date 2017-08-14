package org.ops4j.kaiserkai.rest.provider.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@PreMatching
public class LoggingRequestFilter implements ContainerRequestFilter {

    private Logger log = LoggerFactory.getLogger(LoggingRequestFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {

        log.debug("Request = {} {}", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());
        for (String header : requestContext.getHeaders().keySet()) {
            log.debug(">> {} = {}", header, requestContext.getHeaderString(header));
        }
    }
}