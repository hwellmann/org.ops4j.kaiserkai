package org.ops4j.kaiserkai.rest.api.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ops4j.kaiserkai.core.api.authz.PermissionsAllowed;

@ApplicationScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@PermissionsAllowed("USER")
public class VersionResource {

    @GET
    public Response version() {
        return Response.ok().build();
    }
}
