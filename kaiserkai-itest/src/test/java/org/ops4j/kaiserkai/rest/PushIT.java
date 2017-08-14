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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.docker.api.model.AuthConfig;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.dsl.EventListener;

/**
 * @author Harald Wellmann
 *
 */
public class PushIT {

    private static Logger log = LoggerFactory.getLogger(UploadIT.class);

    // Note the use of 127.0.0.1 instead of localhost.
    // With localhost, dockerd on MacOS tries to use https.
    private static final String REGISTRY = "127.0.0.1:" + System.getProperty("kaiserkai.http.port", "8080");

    private DockerClient client;

    @Before
    public void before() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        AuthConfig authConfig = new AuthConfig();
        authConfig.setServeraddress(REGISTRY);
        authConfig.setUsername("operator");
        authConfig.setPassword("operator");
        configBuilder.addToAuthConfigs(REGISTRY, authConfig);
        client = new DefaultDockerClient(configBuilder.build());
    }

    @After
    public void after() throws IOException {
        client.close();
    }

    public static class MyEventListener implements EventListener {

        private CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void onSuccess(String message) {
            log.debug(message);
            latch.countDown();
        }

        @Override
        public void onError(String message) {
            log.error(message);
            latch.countDown();
        }

        @Override
        public void onEvent(String event) {
            log.debug(event);
        }

        public void await() {
            try {
                latch.await();
                latch = new CountDownLatch(1);
            } catch (InterruptedException exc) {
                log.error("Interrupted", exc);
            }
        }
    }

    @Test
    public void copyImages() throws InterruptedException, IOException {

        copyImage("postgres", "9.6.2", REGISTRY);
        copyImage("postgres", "9.6.3", REGISTRY);
        copyImage("postgres", "9.6.4", REGISTRY);
    }

    public void copyImage(String repository, String tag, String registry) {
        MyEventListener listener = new MyEventListener();
        client.image().withName(repository).pull().usingListener(listener).withTag(tag).fromRegistry();
        listener.await();

        client.image().withName(String.format("%s:%s", repository, tag)).tag().
            inRepository(String.format("%s/%s", registry, repository)).withTagName(tag);

        client.image().withName(String.format("%s/%s:%s", registry, repository, tag)).push().usingListener(listener).toRegistry();
        listener.await();
    }

}
