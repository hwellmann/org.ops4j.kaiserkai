/*
 * Copyright 2019 OPS4J Contributors
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
package org.ops4j.kaiserkai.rest.provider.filter;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class BasicAuthenticationRequestFilter implements ContainerRequestFilter {

    private static final Object BASIC_CHALLENGE = "Basic realm=\"kaiserkai\"";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (requestContext.getSecurityContext().getUserPrincipal() == null) {
            throw new NotAuthorizedException(BASIC_CHALLENGE);
        }
    }
}
