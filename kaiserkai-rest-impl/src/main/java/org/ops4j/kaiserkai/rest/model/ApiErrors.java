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
package org.ops4j.kaiserkai.rest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Harald Wellmann
 *
 */
public class ApiErrors {

    private List<ApiError> errors = new ArrayList<>();

    public ApiErrors() {
    }

    public ApiErrors(ApiError error) {
        errors.add(error);
    }

    /**
     * @return the errors
     */
    public List<ApiError> getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(List<ApiError> errors) {
        this.errors = errors;
    }



}
