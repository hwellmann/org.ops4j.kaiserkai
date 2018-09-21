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

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.docker.dsl.EventListener;

public class DockerClientListener implements EventListener {

    private static Logger log = LoggerFactory.getLogger(DockerClientListener.class);

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onSuccess(String message) {
        log.debug(message);
        latch.countDown();
    }

    @Override
    public void onError(String message) {
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

    @Override
    public void onError(Throwable exc) {
        log.error(exc.getMessage());
        latch.countDown();
    }
}