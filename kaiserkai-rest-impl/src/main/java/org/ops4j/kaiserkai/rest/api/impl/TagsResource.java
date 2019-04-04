package org.ops4j.kaiserkai.rest.api.impl;

import static java.util.stream.Collectors.toList;

import java.io.File;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ops4j.kaiserkai.core.api.storage.file.FileOperations;
import org.ops4j.kaiserkai.core.api.storage.file.StoragePaths;
import org.ops4j.kaiserkai.rest.model.Tags;

@ApplicationScoped
@Path("{repository}/tags/list")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class TagsResource {

    @Inject
    StoragePaths paths;

    @GET
    public Tags findTags(@PathParam("repository") String repository) {
        File repoDir = paths.getRepositoryDir(repository);
        if (!repoDir.exists()) {
            throw new NotFoundException();
        }
        File tagsDir = paths.getTagsDir(repository);

        Tags tags = new Tags();
        tags.setName(repository);
        tags.setTags(FileOperations.toSubDirs(tagsDir).map(File::getName).sorted().collect(toList()));
        return tags;
    }
}
