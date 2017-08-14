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
package org.ops4j.kaiserkai.rest.provider.mapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ops4j.kaiserkai.rest.exc.ResourceNotFoundException;
import org.ops4j.kaiserkai.rest.model.ApiError;
import org.ops4j.kaiserkai.rest.model.ApiErrors;


@Provider
@Dependent
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        ApiError error = new ApiError();
        error.setCode(exception.getErrorCode());
        error.setMessage(exception.getMessage());
        ApiErrors errors = new ApiErrors(error);

        return Response.status(NOT_FOUND).type(APPLICATION_JSON).entity(errors).build();
    }
}
