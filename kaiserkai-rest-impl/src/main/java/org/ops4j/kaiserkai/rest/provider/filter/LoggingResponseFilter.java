package org.ops4j.kaiserkai.rest.provider.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class LoggingResponseFilter implements ContainerResponseFilter {

    private Logger log = LoggerFactory.getLogger(LoggingResponseFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        log.debug("Status = {}", responseContext.getStatus());
        for (String header : responseContext.getHeaders().keySet()) {
            log.debug("<< {} = {}", header, responseContext.getHeaderString(header));
        }
    }
}