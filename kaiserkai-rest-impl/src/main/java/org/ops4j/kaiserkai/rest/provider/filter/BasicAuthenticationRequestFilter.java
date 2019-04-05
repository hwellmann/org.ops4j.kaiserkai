package org.ops4j.kaiserkai.rest.provider.filter;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class BasicAuthenticationRequestFilter implements ContainerRequestFilter {

    private static final Object BASIC_CHALLENGE = "Basic realm=\"kaiserkai\"";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (requestContext.getSecurityContext().getUserPrincipal() == null) {
            throw new NotAuthorizedException(BASIC_CHALLENGE);
        }
    }
}
