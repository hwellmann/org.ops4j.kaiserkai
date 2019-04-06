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

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@PreMatching
public class LoggingRequestFilter implements ContainerRequestFilter {

    private Logger log = LoggerFactory.getLogger(LoggingRequestFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {

        log.debug("Request = {} {}", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());
        for (String header : requestContext.getHeaders().keySet()) {
            log.debug(">> {} = {}", header, requestContext.getHeaderString(header));
        }
    }
}