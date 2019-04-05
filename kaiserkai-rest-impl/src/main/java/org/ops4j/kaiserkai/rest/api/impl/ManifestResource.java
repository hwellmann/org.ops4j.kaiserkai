package org.ops4j.kaiserkai.rest.api.impl;

import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.createLinkFile;
import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.deleteTree;
import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.readLinkFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ops4j.kaiserkai.core.api.lock.DenyIfLocked;
import org.ops4j.kaiserkai.core.api.storage.file.DigestBuilder;
import org.ops4j.kaiserkai.core.api.storage.file.FileOperations;
import org.ops4j.kaiserkai.core.api.storage.file.StoragePaths;
import org.ops4j.kaiserkai.rest.exc.ResourceNotFoundException;
import org.ops4j.kaiserkai.rest.model.ErrorCode;

@ApplicationScoped
@Path("{repository}/manifests/{reference}")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class ManifestResource {

    public static final String MEDIA_TYPE_MANIFEST = "application/vnd.docker.distribution.manifest.v2+json";

    @Inject
    StoragePaths paths;

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces(MEDIA_TYPE_MANIFEST)
    public Response findManifest(@PathParam("repository") String repository, @PathParam("reference") String reference) {

        String digest = reference;
        if (isTag(reference)) {
            File tagDir = paths.getCurrentTagDir(repository, reference);
            digest = FileOperations.readLinkFile(tagDir).orElseThrow(this::manifestUnknown);
        } else {
            File revisionDir = paths.getRevisionDir(repository, reference);
            File link = new File(revisionDir, "link");
            if (!link.exists()) {
                throw manifestUnknown();
            }
        }

        File data = new File(paths.getBlobDir(digest), "data");
        return Response.ok(data).header("Docker-Content-Digest", digest).build();
    }

    private RuntimeException manifestUnknown() {
        return new ResourceNotFoundException(ErrorCode.MANIFEST_UNKNOWN, "manifest unknown");
    }

    @DELETE
    @RolesAllowed("ADMIN")
    @DenyIfLocked
    public Response deleteManifest(@PathParam("repository") String repository, @PathParam("reference") String reference) {

        if (isTag(reference)) {
            throw new BadRequestException();
        }

        File revisionDir = paths.getRevisionDir(repository, reference);
        File link = new File(revisionDir, "link");
        if (!link.exists()) {
            throw manifestUnknown();
        }

        deleteTree(revisionDir);

        File tagsDir = paths.getTagsDir(repository);
        Optional<String> tag = findTagForDigest(tagsDir, reference);
        if (tag.isPresent()) {
            deleteTree(new File(tagsDir, tag.get()));
        }

        return Response.accepted().build();
    }

    private Optional<String> findTagForDigest(File tagsDir, String digest) {
        String[] tags = tagsDir.list();
        if (tags == null) {
            return Optional.empty();
        }
        return Stream.of(tags).filter(tag -> matchesDigest(tagsDir, tag, digest)).findFirst();
    }

    private boolean matchesDigest(File tagsDir, String tag, String digest) {
        File tagDir = new File(tagsDir, tag);
        File linkDir = new File(tagDir, "current");
        return readLinkFile(linkDir).map(l -> l.equals(digest)).orElse(false);
    }

    @PUT
    @Consumes(MEDIA_TYPE_MANIFEST)
    @DenyIfLocked
    public Response createManifest(@PathParam("repository") String repository, @PathParam("reference") String reference,
            InputStream manifest) throws IOException {

        String uuid = UUID.randomUUID().toString();
        File uploadDir = paths.getUploadDir(repository, uuid);
        uploadDir.mkdirs();
        File dataFile = new File(uploadDir, "data");
        Files.copy(manifest, dataFile.toPath());
        String digest = DigestBuilder.computeDigest(dataFile);

        File blobDir = paths.getBlobDir(digest);
        blobDir.mkdirs();
        File dataTarget = new File(blobDir, "data");
        dataFile.renameTo(dataTarget);

        File revisionDir = paths.getRevisionDir(repository, digest);
        createLinkFile(revisionDir, digest);

        File currentTagDir = paths.getCurrentTagDir(repository, reference);
        createLinkFile(currentTagDir, digest);

        File checksumDir = paths.getTagIndexDir(repository, reference, digest);
        createLinkFile(checksumDir, digest);

        URI uri = uriInfo.getBaseUriBuilder().path(repository).path("manifests").path(digest).build();
        return Response.created(uri).header("Docker-Content-Digest", digest).build();
    }

    private boolean isTag(String reference) {
        return !reference.contains(":");
    }
}
