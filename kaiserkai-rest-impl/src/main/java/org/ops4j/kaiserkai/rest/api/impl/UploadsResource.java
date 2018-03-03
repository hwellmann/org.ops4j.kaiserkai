package org.ops4j.kaiserkai.rest.api.impl;

import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.createDataFile;
import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.createLinkFile;
import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.createTimestampFile;
import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.deleteTree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ops4j.kaiserkai.core.api.authz.PermissionsAllowed;
import org.ops4j.kaiserkai.core.api.storage.file.DigestBuilder;
import org.ops4j.kaiserkai.core.api.storage.file.StoragePaths;
import org.ops4j.kaiserkai.rest.exc.ResourceNotFoundException;
import org.ops4j.kaiserkai.rest.model.ErrorCode;

@ApplicationScoped
@Path("{repository}/blobs/uploads")
@Produces(MediaType.APPLICATION_JSON)
@PermissionsAllowed("USER")
public class UploadsResource {

    private static final String DOCKER_UPLOAD_UUID = "Docker-Upload-UUID";

    @Inject
    private StoragePaths paths;

    @Context
    private UriInfo uriInfo;

    @POST
    public Response initiateUpload(@PathParam("repository") String repository, @QueryParam("digest") String digest)
            throws IOException {
        if (digest == null) {
            return initiateResumableUpload(repository);
        } else {
            return initiateMonolithicUpload(repository, digest);
        }
    }

    private Response initiateResumableUpload(String repository) throws IOException {
        String uuid = UUID.randomUUID().toString();
        File uploadDir = paths.getUploadDir(repository, uuid);
        createTimestampFile(uploadDir);
        createDataFile(uploadDir);

        URI uri = uriInfo.getBaseUriBuilder().path(repository).path("blobs/uploads").path(uuid).build();
        return Response.accepted().location(uri).header("Range", "0-0").header(DOCKER_UPLOAD_UUID, uuid).build();
    }

    private Response initiateMonolithicUpload(String repository, String digest) {
        throw new BadRequestException("monolithic upload is not implemented");
    }

    @GET
    @Path("{uuid}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response findUpload(@PathParam("repository") String repository, @PathParam("uuid") String uuid)  {
        File uploadDir = paths.getUploadDir(repository, uuid);
        if (!uploadDir.exists()) {
            throw new ResourceNotFoundException(ErrorCode.NAME_UNKNOWN, "upload not found");
        }
        File dataFile = new File(uploadDir, "data");


        URI uri = uriInfo.getBaseUriBuilder().path(repository).path("blobs/uploads").path(uuid).build();
        long offset = Math.max(0, dataFile.length() - 1);
        return Response.noContent().location(uri).header("Range", "0-" + offset)
                .header(DOCKER_UPLOAD_UUID, uuid).build();
    }

    @DELETE
    @Path("{uuid}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteUpload(@PathParam("repository") String repository, @PathParam("uuid") String uuid)  {
        File uploadDir = paths.getUploadDir(repository, uuid);
        if (!uploadDir.exists()) {
            throw new ResourceNotFoundException(ErrorCode.NAME_UNKNOWN, "upload not found");
        }
        deleteTree(uploadDir);
        return Response.noContent().build();
    }

    @PUT
    @Path("{uuid}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response completeUpload(@PathParam("repository") String repository, @PathParam("uuid") String uuid,
            @QueryParam("digest") String digest, InputStream is) throws IOException {
        if (digest == null) {
            throw new BadRequestException();
        }
        File dataFile = appendData(repository, uuid, is);
        String actualDigest = DigestBuilder.computeDigest(dataFile);
        if (!actualDigest.equals(digest)) {
            throw new BadRequestException("invalid digest");
        }

        File blobDir = paths.getBlobDir(digest);
        blobDir.mkdirs();
        File finalDataFile = new File(blobDir, "data");

        if (!finalDataFile.exists()) {
            dataFile.renameTo(finalDataFile);
        }

        File linkDir = paths.getLayerDir(repository, digest);
        createLinkFile(linkDir, digest);
        deleteTree(dataFile.getParentFile());

        URI uri = uriInfo.getBaseUriBuilder().path(repository).path("blobs").path(digest).build();

        return Response.created(uri).entity("").header("Docker-Content-Digest", digest).build();
    }

    @PATCH
    @Path("{uuid}")
    public Response appendToUpload(@PathParam("repository") String repository, @PathParam("uuid") String uuid,
            @HeaderParam("Content-Range") String contentRange, InputStream is) throws IOException {

        File dataFile = appendData(repository, uuid, is);

        URI uri = uriInfo.getBaseUriBuilder().path(repository).path("blobs/uploads").path(uuid).build();
        return Response.accepted().location(uri).header("Range", "0-" + (dataFile.length() - 1))
                .header(DOCKER_UPLOAD_UUID, uuid).build();
    }

    private File appendData(String repository, String uuid, InputStream is) throws IOException {
        File uploadDir = paths.getUploadDir(repository, uuid);
        uploadDir.mkdirs();
        java.nio.file.Path dataPath = uploadDir.toPath().resolve("data");
        dataPath.toFile().createNewFile();
        byte[] buffer = new byte[8192];
        try (OutputStream os = new FileOutputStream(dataPath.toFile(), true)) {
            while (true) {
                int numBytes = is.read(buffer);
                if (numBytes < 0) {
                    break;
                }
                os.write(buffer, 0, numBytes);
            }
        }
        return dataPath.toFile();
    }
}
