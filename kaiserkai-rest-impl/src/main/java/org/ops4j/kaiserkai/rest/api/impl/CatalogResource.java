package org.ops4j.kaiserkai.rest.api.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ops4j.kaiserkai.core.api.storage.file.StoragePaths;
import org.ops4j.kaiserkai.rest.model.Catalog;

@ApplicationScoped
@Path("_catalog")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogResource {

    @Inject
    private StoragePaths paths;

    @GET
    public Catalog findRepositories() {
        Catalog catalog = new Catalog();
        String[] subdirs = paths.getRepositoriesDir().list();
        List<String> repos = (subdirs == null) ? Collections.emptyList() : Arrays.asList(subdirs);
        Collections.sort(repos);
        catalog.setRepositories(repos);
        return catalog;
    }
}
