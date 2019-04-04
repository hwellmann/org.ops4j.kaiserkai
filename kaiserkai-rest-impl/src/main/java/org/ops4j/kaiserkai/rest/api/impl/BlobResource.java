package org.ops4j.kaiserkai.rest.api.impl;

import java.io.File;
import java.io.IOException;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ops4j.kaiserkai.core.api.storage.file.StoragePaths;

@ApplicationScoped
@Path("{repository}/blobs/{digest}")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class BlobResource {

    @Inject
    StoragePaths paths;

    @GET
    public Response findBlob(@PathParam("repository") String repository, @PathParam("digest") String digest) throws IOException {
        File blobDir = paths.getBlobDir(digest);
        File data = new File(blobDir, "data");
        if (!data.exists()) {
            throw new NotFoundException();
        }
        return Response.ok(data, MediaType.APPLICATION_OCTET_STREAM_TYPE).
                header(HttpHeaders.CONTENT_LENGTH, data.length()).
                header("Docker-Content-Digest", digest).build();
    }
}
