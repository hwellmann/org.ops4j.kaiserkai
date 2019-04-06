/*
 * Copyright 2017 OPS4J Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.kaiserkai.rest;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.BasicAuthentication;
import org.ops4j.kaiserkai.core.api.model.GarbageCollectionResult;
import org.ops4j.kaiserkai.rest.model.Catalog;
import org.ops4j.kaiserkai.rest.model.Tags;

/**
 * @author Harald Wellmann
 *
 */
public class RegistryClient {

    private Client client;
    private WebTarget entryPoint;

    public RegistryClient(String registryUrl, String username, String password) {
        client = ((ResteasyClientBuilder) ClientBuilder.newBuilder()).connectionPoolSize(20).build();
        client.register(LoggingResponseFilter.class);
        client.register(new BasicAuthentication(username, password));

        entryPoint = client.target(registryUrl).path("v2");
    }

    public List<String> getRepositories() {
        Response response = entryPoint.path("_catalog").request(MediaType.APPLICATION_JSON).get();
        Catalog catalog = response.readEntity(Catalog.class);
        return catalog.getRepositories();

    }

    public List<String> getTags(String repository) {
        Response response = entryPoint.path(repository).path("tags/list").request(MediaType.APPLICATION_JSON).get();
        Tags tags = response.readEntity(Tags.class);
        return tags.getTags();
    }

    public void deleteTag(String repository, String tag) {
        Response response = entryPoint.path(repository).path("manifests").path(tag).request("application/vnd.docker.distribution.manifest.v2+json").head();
        String digest = response.getHeaderString("Docker-Content-Digest");
        assertThat(digest).isNotNull();

        response = entryPoint.path(repository).path("manifests").path(digest).request().delete();
    }

    public String collectGarbage() {
        Response response = entryPoint.path("_gc").request().post(null);
        String path = response.getLocation().getPath();
        int slash = path.lastIndexOf('/');
        return path.substring(slash + 1);
    }

    public GarbageCollectionResult getGarbageCollectionResult(String jobId) {
        Response response = entryPoint.path("_gc").path(jobId).request().get();
        if (! response.getStatusInfo().equals(Status.OK)) {
            return null;
        }
        return response.readEntity(GarbageCollectionResult.class);
    }
}
