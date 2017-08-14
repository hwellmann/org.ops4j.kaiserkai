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
package org.ops4j.kaiserkai.core.api.authz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;

/**
 * @author Harald Wellmann
 *
 */
@RequestScoped
public class Authorization {

    public static final String ANONYMOUS = "anonymous";

    private String principal = ANONYMOUS;

    private List<String> permissions = new ArrayList<>();

    public boolean isAuthenticated() {
        return !ANONYMOUS.equals(principal);
    }

    public boolean isAnonymous() {
        return ANONYMOUS.equals(principal);
    }

    public boolean hasPermission(String role) {
        return permissions.contains(role);
    }

    public void setPrincipal(String principal) {
        if (principal == null || principal.trim().isEmpty()) {
            throw new IllegalArgumentException("Principal must not be empty");
        }
        this.principal = principal;
    }

    public void setPermission(String permission) {
        permissions = Collections.singletonList(permission);
    }
}
