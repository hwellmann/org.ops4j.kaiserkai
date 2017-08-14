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

/**
 * @author Harald Wellmann
 *
 */
public class ApiError {

    private ErrorCode code;

    private String message;

    private Object detail;

    public ApiError() {
    }


    public ApiError(ErrorCode code, String message) {
        this.code = code;
        this.message = message;
    }



    /**
     * @return the code
     */
    public ErrorCode getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(ErrorCode code) {
        this.code = code;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the detail
     */
    public Object getDetail() {
        return detail;
    }

    /**
     * @param detail the detail to set
     */
    public void setDetail(Object detail) {
        this.detail = detail;
    }

}
