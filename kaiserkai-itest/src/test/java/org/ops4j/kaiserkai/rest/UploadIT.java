package org.ops4j.kaiserkai.rest;

import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.PARTIAL_CONTENT;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.ops4j.kaiserkai.core.api.storage.file.DigestBuilder.computeDigest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadIT {

    private static Logger log = LoggerFactory.getLogger(UploadIT.class);

    private static final String LOCAL_JAR = Paths.get(System.getProperty("user.dir"), "target", "dependency", "junit-4.12.jar").toString();

    private static final String REGISTRY_URL = "http://localhost:" + System.getProperty("kaiserkai.http.port", "8080");

    private static final int CHUNK_SIZE = 100_000;

    private Client client;

    private WebTarget entryPoint;

    @Before
    public void init() {
        log.debug("registry URL = {}", REGISTRY_URL);

        client = new ResteasyClientBuilder().connectionPoolSize(20).build();
        client.register(LoggingResponseFilter.class);
        client.register(new BasicAuthentication("operator", "operator"));

        entryPoint = client.target(REGISTRY_URL).path("v2");
    }

    @After
    public void after() {
        client.close();
    }

    @Test
    public void shouldUploadBlob() {

        log.debug("POST");
        WebTarget target = entryPoint.path("foobar").path("blobs/uploads/");
        Response response = target.request().post(null);
        assertThat(response.getStatusInfo(), is(ACCEPTED));

        String location = response.getHeaderString("Location");
        response.close();

        log.debug("GET");
        Response getResponse = client.target(location).request().get();
        assertThat(getResponse.getStatusInfo(), is(NO_CONTENT));

        log.debug("PUT");
        File file = new File(LOCAL_JAR);
        String digest = computeDigest(file);
        WebTarget putTarget = client.target(location).queryParam("digest", digest);
        Response putResponse = putTarget.request().
                header("Content-Range", "0-" + file.length()).
                put(Entity.entity(file, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        assertThat(putResponse.getStatusInfo(), is(CREATED));

        log.debug("GET");
        getResponse = client.target(location).request().get();
        log.debug("Body = {}", getResponse.readEntity(String.class));

    }

    @Test
    public void shouldDeleteEmptyUpload() {

        log.debug("POST");
        WebTarget target = entryPoint.path("foobar").path("blobs/uploads/");
        Response response = target.request().post(null);
        assertThat(response.getStatusInfo(), is(ACCEPTED));

        String location = response.getHeaderString("Location");

        log.debug("HEAD");
        response = client.target(location).request().head();
        assertThat(response.getStatusInfo(), is(NO_CONTENT));

        log.debug("DELETE");
        response = client.target(location).request().delete();
        assertThat(response.getStatusInfo(), is(NO_CONTENT));

        log.debug("HEAD");
        response = client.target(location).request().head();
        assertThat(response.getStatusInfo(), is(NOT_FOUND));
    }





    @Test
    public void shouldUploadBlobInChunks() throws IOException {

        log.debug("POST");
        WebTarget target = entryPoint.path("foobar").path("blobs/uploads/");
        Response response = target.request().post(null);
        assertThat(response.getStatusInfo(), is(ACCEPTED));

        String location = response.getHeaderString(HttpHeaders.LOCATION);

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
            WebTarget putTarget = client.target(location);
            response = putTarget.request().
                    header("Content-Range", from + "-" + (to-1)).
                    method("PATCH", Entity.entity(chunk, MediaType.APPLICATION_OCTET_STREAM_TYPE));
            assertThat(response.getStatusInfo(), is(ACCEPTED));
            location = response.getHeaderString("Location");
            numBytes += CHUNK_SIZE;
        }


        log.debug("PUT");
        String digest = computeDigest(file);
        WebTarget putTarget = client.target(location).queryParam("digest", digest);
        response = putTarget.request().
                put(null);
        assertThat(response.getStatusInfo(), is(CREATED));


        log.debug("HEAD blob");
        target = entryPoint.path("foobar").path("blobs").path(digest);
        response = target.request().head();
        assertThat(response.getStatusInfo(), is(OK));

        log.debug("GET blob range");
        target = entryPoint.path("foobar").path("blobs").path(digest);
        response = target.request().header("Range", "bytes=100000-199999").get();
        assertThat(response.getStatusInfo(), is(PARTIAL_CONTENT));
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_LENGTH), is("100000"));
        assertThat(response.getHeaderString("Content-Range"), is("bytes 100000-199999/314932"));
        byte[] responseChunk = response.readEntity(byte[].class);
        assertThat(responseChunk.length, is(CHUNK_SIZE));
        assertThat(responseChunk, is(Arrays.copyOfRange(content, 100000, 200000)));
    }
}
