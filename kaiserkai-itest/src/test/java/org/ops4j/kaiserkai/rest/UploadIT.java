/*
 * Copyright 2018 OPS4J Contributors
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

import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.PARTIAL_CONTENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.kaiserkai.core.api.storage.file.DigestBuilder.computeDigest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.BasicAuthentication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadIT {

    private static Logger log = LoggerFactory.getLogger(UploadIT.class);

    private static final String LOCAL_JAR = Paths.get(System.getProperty("user.dir"), "target", "dependency", "logback-classic-1.0.7.jar").toString();

    private static final String REGISTRY_URL = "http://localhost:" + System.getProperty("kaiserkai.http.port", "8080");

    private static final int CHUNK_SIZE = 100_000;

    private Client client;

    private WebTarget entryPoint;

    @BeforeEach
    public void init() {
        log.debug("registry URL = {}", REGISTRY_URL);

        client = ((ResteasyClientBuilder) ClientBuilder.newBuilder()).connectionPoolSize(20).build();
        client.register(LoggingResponseFilter.class);
        client.register(new BasicAuthentication("operator", "operator"));

        entryPoint = client.target(REGISTRY_URL).path("v2");
    }

    @AfterEach
    public void after() {
        client.close();
    }

    @Test
    public void shouldUploadBlob() throws IOException {

        log.debug("POST");
        WebTarget target = entryPoint.path("foobar").path("blobs/uploads/");
        Response response = target.request().post(null);
        assertThat(response.getStatusInfo()).isEqualTo(ACCEPTED);

        String uploadLocation = response.getHeaderString("Location");
        response.close();

        log.debug("GET");
        Response getResponse = client.target(uploadLocation).request().get();
        assertThat(getResponse.getStatusInfo()).isEqualTo(NO_CONTENT);

        log.debug("PUT");
        File file = new File(LOCAL_JAR);
        byte[] content = Files.readAllBytes(file.toPath());
        String digest = computeDigest(file);
        WebTarget putTarget = client.target(uploadLocation).queryParam("digest", digest);
        Response putResponse = putTarget.request().
                header("Content-Range", "0-" + file.length()).
                put(Entity.entity(file, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        assertThat(putResponse.getStatusInfo()).isEqualTo(CREATED);

        String blobLocation = putResponse.getHeaderString("Location");
        assertThat(blobLocation).isEqualTo(UriBuilder.fromUri(REGISTRY_URL).path("v2/foobar/blobs").path(digest).build().toString());

        log.debug("GET upload");
        getResponse = client.target(uploadLocation).request().get();
        assertThat(getResponse.getStatusInfo()).isEqualTo(NOT_FOUND);

        log.debug("GET blob");
        getResponse = client.target(blobLocation).request().get();
        assertThat(getResponse.getStatusInfo()).isEqualTo(OK);

        byte[] responseChunk = getResponse.readEntity(byte[].class);
        assertThat(responseChunk).isEqualTo(content);
    }

    @Test
    public void shouldDeleteEmptyUpload() {

        log.debug("POST");
        WebTarget target = entryPoint.path("foobar").path("blobs/uploads/");
        Response response = target.request().post(null);
        assertThat(response.getStatusInfo()).isEqualTo(ACCEPTED);

        String location = response.getHeaderString("Location");

        log.debug("HEAD");
        response = client.target(location).request().head();
        assertThat(response.getStatusInfo()).isEqualTo(NO_CONTENT);

        log.debug("DELETE");
        response = client.target(location).request().delete();
        assertThat(response.getStatusInfo()).isEqualTo(NO_CONTENT);

        log.debug("HEAD");
        response = client.target(location).request().head();
        assertThat(response.getStatusInfo()).isEqualTo(NOT_FOUND);
    }


    @Test
    public void shouldUploadBlobInChunks() throws IOException {

        log.debug("POST");
        WebTarget target = entryPoint.path("foobar").path("blobs/uploads/");
        Response response = target.request().post(null);
        assertThat(response.getStatusInfo()).isEqualTo(ACCEPTED);

        String uploadLocation = response.getHeaderString(HttpHeaders.LOCATION);

        File file = new File(LOCAL_JAR);
        int length = (int) file.length();
        int numBytes = 0;

        byte[] content = Files.readAllBytes(file.toPath());
        while (numBytes < length) {
            int from = numBytes;
            int to = numBytes + CHUNK_SIZE;
            if (to > length) {
                to = length;
            }

            byte[] chunk = Arrays.copyOfRange(content, from, to);

            log.debug("PATCH");
            WebTarget putTarget = client.target(uploadLocation);
            response = putTarget.request().
                    header("Content-Range", from + "-" + (to-1)).
                    method("PATCH", Entity.entity(chunk, MediaType.APPLICATION_OCTET_STREAM_TYPE));
            assertThat(response.getStatusInfo()).isEqualTo(ACCEPTED);
            uploadLocation = response.getHeaderString("Location");
            numBytes += CHUNK_SIZE;
        }


        log.debug("PUT");
        String digest = computeDigest(file);
        WebTarget putTarget = client.target(uploadLocation).queryParam("digest", digest);
        response = putTarget.request().
                put(null);
        assertThat(response.getStatusInfo()).isEqualTo(CREATED);


        String blobLocation = response.getHeaderString("Location");
        assertThat(blobLocation).isEqualTo(UriBuilder.fromUri(REGISTRY_URL).path("v2/foobar/blobs").path(digest).build().toString());

        log.debug("GET upload");
        response = client.target(uploadLocation).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(NOT_FOUND);




        log.debug("HEAD blob");
        target = entryPoint.path("foobar").path("blobs").path(digest);
        response = target.request().head();
        assertThat(response.getStatusInfo()).isEqualTo(OK);
        assertThat(response.hasEntity()).isFalse();

        log.debug("GET blob range");
        target = entryPoint.path("foobar").path("blobs").path(digest);
        response = target.request().header("Range", "bytes=50000-149999").get();
        assertThat(response.getStatusInfo()).isEqualTo(PARTIAL_CONTENT);
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_LENGTH)).isEqualTo("100000");
        assertThat(response.getHeaderString("Content-Range")).isEqualTo("bytes 50000-149999/251679");
        byte[] responseChunk = response.readEntity(byte[].class);
        assertThat(responseChunk.length).isEqualTo(CHUNK_SIZE);
        assertThat(responseChunk).isEqualTo(Arrays.copyOfRange(content, 50_000, 150_000));
    }
}
