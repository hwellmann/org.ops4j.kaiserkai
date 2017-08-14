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
package org.ops4j.kaiserkai.core.api.model;

/**
 * @author Harald Wellmann
 *
 */
public class Digest {

    private String algorithm;
    private String checksum;

    public Digest() {
    }

    public Digest(String algorithm, String checksum) {
        this.algorithm = algorithm;
        this.checksum = checksum;
    }

    /**
     * @return the algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }
    /**
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    /**
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }
    /**
     * @param checksum the checksum to set
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }



}
