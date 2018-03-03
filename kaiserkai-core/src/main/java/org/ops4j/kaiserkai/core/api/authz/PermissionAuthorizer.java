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

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.security.enterprise.SecurityContext;

import org.apache.deltaspike.core.util.AnnotationUtils;
import org.apache.deltaspike.security.api.authorization.Secures;

/**
 * Authorizer for the {@link PermissionsAllowed} security binding.
 *
 * @author hwellmann
 *
 */
@ApplicationScoped
public class PermissionAuthorizer {

    @Inject
    private SecurityContext securityContext;

    @Inject
    private BeanManager beanManager;

    /**
     * Permits the current invocation if the permission holder for the current request holds at
     * least one of the permissions listed in the {@code @PermissionsAllowed} annotation of the
     * invoked method.
     *
     * @param context
     *            invocation context of secured method
     * @return true if the invocation is permitted
     */
    @Secures
    @PermissionsAllowed
    public boolean doSecured(InvocationContext context) {
        PermissionsAllowed annotation = getAnnotation(context, PermissionsAllowed.class);
        String[] permissions = annotation.value();
        for (String methodPermission : permissions) {
            if (securityContext.isCallerInRole(methodPermission)) {
                return true;
            }
        }
        return false;
    }

    private <T extends Annotation> T getAnnotation(InvocationContext context, Class<T> klass) {
        T annotation = AnnotationUtils.extractAnnotationFromMethodOrClass(beanManager,
            context.getMethod(), context.getTarget().getClass(), klass);
        return annotation;
    }
}
