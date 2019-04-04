package org.ops4j.kaiserkai.rest.api.impl;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ops4j.kaiserkai.core.api.lock.RequiresLock;
import org.ops4j.kaiserkai.core.api.model.GarbageCollectionResult;
import org.ops4j.kaiserkai.core.api.model.JobStatus;
import org.ops4j.kaiserkai.core.api.storage.file.GarbageCollectionService;
import org.ops4j.kaiserkai.rest.exc.ResourceNotFoundException;
import org.ops4j.kaiserkai.rest.model.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Path("_gc")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class GarbageCollectionResource {

    private static Logger log = LoggerFactory.getLogger(GarbageCollectionResource.class);

    @Inject
    GarbageCollectionService gc;

    @Context
    private UriInfo uriInfo;

    private String currentJob;

    private Future<GarbageCollectionResult> futureResult;

    @POST
    @RequiresLock
    public Response collectGarbage() throws IOException {
        currentJob = UUID.randomUUID().toString();
        futureResult = CompletableFuture.supplyAsync(() -> gc.collectGarbage(currentJob)).whenComplete(this::onComplete);
        URI uri = uriInfo.getRequestUriBuilder().path(currentJob).build();
        return Response.accepted().location(uri).build();
    }

    private void onComplete(GarbageCollectionResult result, Throwable exc) {
        if (exc != null) {
            log.error("Garbage collection terminated abnormally", exc);
        }
    }

    @GET
    @Path("{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public GarbageCollectionResult getGarbageCollectionStatus(@PathParam("uuid") String uuid) {
        if (uuid.equals(currentJob)  && !futureResult.isDone()) {
            GarbageCollectionResult result = new GarbageCollectionResult();
            result.setId(uuid);
            result.setStatus(JobStatus.RUNNING);
            return result;
        }
        GarbageCollectionResult result = gc.readJobResult(uuid);
        if (result == null) {
            throw new ResourceNotFoundException(ErrorCode.NAME_UNKNOWN, "job not found");
        }
        return result;
    }

}
