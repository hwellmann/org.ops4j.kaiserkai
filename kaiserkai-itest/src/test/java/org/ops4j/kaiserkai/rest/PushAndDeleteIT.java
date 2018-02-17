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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.kaiserkai.core.api.model.GarbageCollectionResult;
import org.ops4j.kaiserkai.core.api.model.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;

/**
 * @author Harald Wellmann
 *
 */
public class PushAndDeleteIT {

    private static Logger log = LoggerFactory.getLogger(PushAndDeleteIT.class);

    // Note the use of 127.0.0.1 instead of localhost.
    // With localhost, dockerd on MacOS tries to use https.
    private static final String REGISTRY = "127.0.0.1:" + System.getProperty("kaiserkai.http.port", "8080");

    private static final String REGISTRY_URL = "http://" + REGISTRY;

    private DockerClient dockerClient;

    private RegistryClient registryClient;

    @Before
    public void before() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        AuthConfig authConfig = new AuthConfig();
        authConfig.setServeraddress(REGISTRY);
        authConfig.setUsername("admin");
        authConfig.setPassword("admin");
        configBuilder.addToAuthConfigs(REGISTRY, authConfig);
        dockerClient = new DefaultDockerClient(configBuilder.build());

        log.debug("registry URL = {}", REGISTRY_URL);

        registryClient = new RegistryClient(REGISTRY_URL, "admin", "admin");
    }

    @After
    public void after() throws IOException {
        dockerClient.close();
    }

    @Test
    public void copyImages() throws InterruptedException, IOException {
        assertThat(getRepositories(), is(empty()));

        copyImage("postgres", "9.6.2", REGISTRY);
        assertThat(getRepositories(), contains("postgres"));
        assertThat(getTags("postgres"), contains("9.6.2"));

        copyImage("postgres", "9.6.3", REGISTRY);
        assertThat(getRepositories(), contains("postgres"));
        assertThat(getTags("postgres"), contains("9.6.2", "9.6.3"));

        copyImage("postgres", "9.6.4", REGISTRY);
        assertThat(getRepositories(), contains("postgres"));
        assertThat(getTags("postgres"), contains("9.6.2", "9.6.3", "9.6.4"));

        registryClient.deleteTag("postgres", "9.6.2");
        assertThat(getTags("postgres"), contains("9.6.3", "9.6.4"));

        registryClient.deleteTag("postgres", "9.6.3");
        assertThat(getTags("postgres"), contains("9.6.4"));

        registryClient.deleteTag("postgres", "9.6.4");
        assertThat(getTags("postgres"), is(empty()));

        String jobId = registryClient.collectGarbage();
        JobStatus status = JobStatus.RUNNING;
        while (status == JobStatus.RUNNING) {
            GarbageCollectionResult result = registryClient.getGarbageCollectionResult(jobId);
            status = result.getStatus();
            log.info("job {} is {}", jobId, status);
            if (status == JobStatus.RUNNING) {
                TimeUnit.SECONDS.sleep(1);
            }
        }

        assertThat(getRepositories(), is(empty()));
    }

    public void copyImage(String repository, String tag, String registry) {
        log.info("copying image {}:{}", repository, tag);
        DockerClientListener listener = new DockerClientListener();
        dockerClient.image().withName(repository).pull().usingListener(listener).withTag(tag).fromRegistry();
        listener.await();

        dockerClient.image().withName(String.format("%s:%s", repository, tag)).tag().
            inRepository(String.format("%s/%s", registry, repository)).withTagName(tag);

        dockerClient.image().withName(String.format("%s/%s:%s", registry, repository, tag)).push().usingListener(listener).toRegistry();
        listener.await();
    }

    private List<String> getRepositories() {
        return registryClient.getRepositories();
    }

    private List<String> getTags(String repository) {
        return registryClient.getTags(repository);
    }
}
