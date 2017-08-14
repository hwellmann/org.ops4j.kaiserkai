package org.ops4j.kaiserkai.rest.api.impl;

import java.io.IOException;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedExecutors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ops4j.kaiserkai.core.api.authz.PermissionsAllowed;
import org.ops4j.kaiserkai.core.api.lock.RequiresLock;
import org.ops4j.kaiserkai.core.api.storage.file.GarbageCollectionListener;
import org.ops4j.kaiserkai.core.api.storage.file.GarbageCollectionService;

@ApplicationScoped
@Path("_gc")
@Produces(MediaType.APPLICATION_JSON)
public class GarbageCollectionResource {

    @Inject
    private GarbageCollectionService gc;

    @Resource
    private ManagedExecutorService executor;

    @POST
    @PermissionsAllowed("ADMIN")
    @RequiresLock
    public Response collectGarbage() throws IOException {
        executor.submit(ManagedExecutors.managedTask(gc::collectGarbage, new GarbageCollectionListener()));

        return Response.ok().build();
    }

}
