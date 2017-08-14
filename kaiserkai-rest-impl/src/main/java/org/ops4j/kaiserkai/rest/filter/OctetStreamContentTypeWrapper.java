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
package org.ops4j.kaiserkai.rest.filter;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author Harald Wellmann
 *
 */
class OctetStreamContentTypeWrapper extends HttpServletRequestWrapper {

    public OctetStreamContentTypeWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getHeader(String name) {
        if (name.equals(CONTENT_TYPE)) {
            return APPLICATION_OCTET_STREAM;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (name.equals(CONTENT_TYPE)) {
            Vector<String> types = new Vector<>();
            types.add(APPLICATION_OCTET_STREAM);
            return types.elements();
        }
        return super.getHeaders(name);
    }

    @Override
    public String getContentType() {
        String type = super.getContentType();
        if (type.isEmpty()) {
            return null;
        }
        return type;
    }
}
