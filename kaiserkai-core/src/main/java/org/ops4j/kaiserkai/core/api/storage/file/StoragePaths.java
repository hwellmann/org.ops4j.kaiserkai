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
package org.ops4j.kaiserkai.core.api.storage.file;

import java.io.File;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ops4j.kaiserkai.core.api.config.RegistryConfiguration;
import org.ops4j.kaiserkai.core.api.model.Digest;

/**
 * @author Harald Wellmann
 *
 */
@ApplicationScoped
public class StoragePaths {

    @Inject
    private RegistryConfiguration config;

    public File getVersionDir() {
        return new File(config.getRegistryRoot(), "docker/registry/v2");
    }

    public File getBlobsDir() {
        return new File(getVersionDir(), "blobs");
    }

    public File getBlobDir(String digestString) {
        Digest digest = toDigest(digestString);
        File algoDir = new File(getBlobsDir(), digest.getAlgorithm());
        File blobsSubdir = new File(algoDir, digest.getChecksum().substring(0, 2));
        return new File(blobsSubdir, digest.getChecksum());
    }

    public File getJobsDir() {
        return new File(getVersionDir(), "jobs");
    }

    public File getRepositoriesDir() {
        return new File(getVersionDir(), "repositories");
    }

    public File getRepositoryDir(String repository) {
        return new File(getRepositoriesDir(), repository);
    }

    public File getLayerDir(String repository, String digest) {
        File layersDir = new File(getRepositoryDir(repository), "_layers");
        return getDigestDir(layersDir, digest);
    }

    public File getManifestsDir(String repository) {
        return new File(getRepositoryDir(repository), "_manifests");
    }

    public File getRevisionDir(String repository, String digest) {
        File revisionsDir = new File(getManifestsDir(repository), "revisions");
        return getDigestDir(revisionsDir, digest);
    }

    public File getTagsDir(String repository) {
        return new File(getManifestsDir(repository), "tags");
    }

    public File getTagDir(String repository, String tag) {
        return new File(getTagsDir(repository), tag);
    }

    public File getCurrentTagDir(String repository, String tag) {
        return new File(getTagDir(repository, tag), "current");
    }

    public File getTagIndexDir(String repository, String tag, String digest) {
        File indexDir = new File(getTagDir(repository, tag), "index");
        return getDigestDir(indexDir, digest);
    }

    public  File getUploadsDir(String repository) {
        File repoDir = getRepositoryDir(repository);
        return new File(repoDir, "_uploads");
    }

    public  File getUploadDir(String repository, String uuid) {
        return new File(getUploadsDir(repository), uuid);
    }

    public File getBlobData(String digest) {
        return new File(getBlobDir(digest), "data");
    }

    public File getDigestDir(File parentDir, String digestString) {
        Digest digest = toDigest(digestString);
        File algoDir = new File(parentDir, digest.getAlgorithm());
        return new File(algoDir, digest.getChecksum());
    }

    private Digest toDigest(String digest) {
        int colon = digest.indexOf(':');
        if (colon == -1) {
            throw new IllegalArgumentException("Invalid digest: " + digest);
        }

        String algo = digest.substring(0, colon);
        String checksum = digest.substring(colon + 1);
        return new Digest(algo, checksum);
    }
}
