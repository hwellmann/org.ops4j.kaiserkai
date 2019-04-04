package org.ops4j.kaiserkai.rest.api.impl;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Entry point for REST API.
 *
 * @author hwellmann
 *
 */
@ApplicationPath(RegistryApplication.ENTRY_POINT)
//@BasicAuthenticationMechanismDefinition(realmName = "kaiserkai")
//@DeclareRoles({"USER", "ADMIN" })
//@ApplicationScoped
public class RegistryApplication extends Application {

    /**
     * Relative path entry point.
     */
    public static final String ENTRY_POINT = "/v2";

}
