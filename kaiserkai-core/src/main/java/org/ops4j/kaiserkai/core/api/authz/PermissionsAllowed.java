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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

/**
 * A security binding annotation for classes or methods, listing one or more permissions. Methods
 * with this binding will be intercepted by DeltaSpike Security. The invoker is required to have at
 * least one of the listed permissions.
 *
 * @author hwellmann
 *
 */
@Retention(value = RUNTIME)
@Target({ TYPE, METHOD })
@Documented
//@SecurityBindingType
public @interface PermissionsAllowed {

    /**
     * Permissions allowing the invoker to execute the current method.
     *
     * @return list of allowed permissions
     */
    @Nonbinding
    String[] value() default {};
}
