package org.ops4j.kaiserkai.rest.provider.filter;

import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.BasicAuthHelper;
import org.ops4j.kaiserkai.core.api.authz.Authorization;
import org.ops4j.kaiserkai.core.api.config.RegistryConfiguration;
import org.ops4j.kaiserkai.core.api.storage.file.DigestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@RequestScoped
public class BasicAuthenticationRequestFilter implements ContainerRequestFilter {

    private static final Object BASIC_CHALLENGE = "Basic realm=\"kaiserkai\"";

    private Logger log = LoggerFactory.getLogger(BasicAuthenticationRequestFilter.class);

    @Inject
    private Authorization authorization;

    @Inject
    private RegistryConfiguration config;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            throw new NotAuthorizedException(BASIC_CHALLENGE);
        }
        String[] parts = BasicAuthHelper.parseHeader(authHeader);
        String user = parts[0];
        String password = parts[1];

        if (user.equals(config.getOperatorName()) && matchesDigest(user, password, config.getOperatorDigest())) {
            authorization.setPrincipal(config.getOperatorName());
            authorization.setPermission("USER");
        }
        else if (user.equals(config.getAdminName()) && matchesDigest(user, password, config.getAdminDigest())) {
            authorization.setPrincipal(config.getAdminName());
            authorization.setPermission("ADMIN");
        }
        else {
            log.debug("Access denied for user = {}", user);
            throw new NotAuthorizedException(BASIC_CHALLENGE);
        }
    }

    private boolean matchesDigest(String user, String password, String digest) {
        String actualDigest = DigestBuilder.computeDigest(String.format("%s:%s", user, password));
        return actualDigest.equals(digest);
    }
}
