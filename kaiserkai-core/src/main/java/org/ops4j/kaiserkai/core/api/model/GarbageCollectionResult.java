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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Harald Wellmann
 *
 */
public class GarbageCollectionResult {

    private String id;

    private JobStatus status;

    private List<Blob> blobs = new ArrayList<>();



    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the status
     */
    public JobStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(JobStatus status) {
        this.status = status;
    }

    /**
     * @return the blobs
     */
    public List<Blob> getBlobs() {
        return blobs;
    }

    /**
     * @param blobs the blobs to set
     */
    public void setBlobs(List<Blob> blobs) {
        this.blobs = blobs;
    }

}
