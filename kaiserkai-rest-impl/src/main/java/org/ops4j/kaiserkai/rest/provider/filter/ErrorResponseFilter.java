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

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.ops4j.kaiserkai.rest.model.ApiError;
import org.ops4j.kaiserkai.rest.model.ApiErrors;
import org.ops4j.kaiserkai.rest.model.ErrorCode;

/**
 * Overrides error responses with suitable JSON entities according to the API specification.
 *
 * @author Harald Wellmann
 *
 */
@Provider
public class ErrorResponseFilter implements ContainerResponseFilter {

    private static final String BASIC_CHALLENGE = "Basic realm=\"kaiserkai\"";

    @Override
    public void filter(ContainerRequestContext requestContext,
        ContainerResponseContext responseContext) throws IOException {
        if (responseContext.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
            responseContext.getHeaders().putSingle(HttpHeaders.WWW_AUTHENTICATE, BASIC_CHALLENGE);
            overrideResponse(responseContext, ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        else if (responseContext.getStatus() == Status.FORBIDDEN.getStatusCode()) {
            overrideResponse(responseContext, ErrorCode.DENIED,
                "Requested access to the resource is denied");
        }
        else if (responseContext.getStatus() == Status.METHOD_NOT_ALLOWED.getStatusCode()) {
            overrideResponse(responseContext, ErrorCode.UNSUPPORTED,
                "The operation is unsupported");
        }
    }

    private void overrideResponse(ContainerResponseContext responseContext, ErrorCode errorCode,
        String message) {
        ApiError error = new ApiError();
        error.setCode(errorCode);
        error.setMessage(message);
        ApiErrors errors = new ApiErrors(error);

        responseContext.setEntity(errors, null, MediaType.APPLICATION_JSON_TYPE);
    }
}
