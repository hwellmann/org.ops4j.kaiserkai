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

import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

/**
 * @author Harald Wellmann
 *
 */
public class LocalAuthSupplier implements RegistryAuthSupplier {

    @Override
    public RegistryAuth authFor(String imageName) throws DockerException {
        if (imageName.startsWith("127.0.0.1")) {
            RegistryAuth auth = RegistryAuth.builder().username("admin").password("admin").build();
            return auth;
        }
        return null;
    }

    @Override
    public RegistryAuth authForSwarm() throws DockerException {
        return null;
    }

    @Override
    public RegistryConfigs authForBuild() throws DockerException {
        return null;
    }
}
