package org.ops4j.kaiserkai.rest.provider.filter;

import javax.security.enterprise.SecurityContext;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

// @Provider
// @RequestScoped
public class BasicAuthenticationRequestFilter implements ContainerRequestFilter {

    private static final Object BASIC_CHALLENGE = "Basic realm=\"kaiserkai\"";

    // @Inject
    private SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (securityContext.getCallerPrincipal() == null) {
            throw new NotAuthorizedException(BASIC_CHALLENGE);
        }
    }
}
