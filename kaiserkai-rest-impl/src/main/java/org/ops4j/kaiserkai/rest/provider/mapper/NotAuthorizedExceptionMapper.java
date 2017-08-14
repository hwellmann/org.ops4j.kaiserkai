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
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import javax.enterprise.context.Dependent;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ops4j.kaiserkai.rest.model.ApiError;
import org.ops4j.kaiserkai.rest.model.ApiErrors;
import org.ops4j.kaiserkai.rest.model.ErrorCode;


@Provider
@Dependent
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

    @Override
    public Response toResponse(NotAuthorizedException exception) {
        ApiError error = new ApiError();
        error.setCode(ErrorCode.UNAUTHORIZED);
        error.setMessage("authentication required");
        ApiErrors errors = new ApiErrors(error);

        return Response.status(UNAUTHORIZED).type(APPLICATION_JSON).
                header(HttpHeaders.WWW_AUTHENTICATE, exception.getChallenges().get(0)).
                entity(errors).build();
    }

}
