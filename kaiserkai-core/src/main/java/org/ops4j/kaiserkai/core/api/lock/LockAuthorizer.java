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
package org.ops4j.kaiserkai.core.api.lock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

/**
 * Authorizer for the {@link DenyIfLocked} and {@link RequiresLock} security bindings.
 *
 * @author Harald Wellmann
 *
 */
@ApplicationScoped
public class LockAuthorizer {

    @Inject
    LockManager lockManager;

    //@Secures
    @DenyIfLocked
    public boolean doUnlessLocked(InvocationContext context) {
        return !lockManager.isLocked();
    }

    //@Secures
    @RequiresLock
    public boolean lockAndDo(InvocationContext context) {
        return lockManager.lock();
    }
}
