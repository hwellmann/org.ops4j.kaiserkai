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

import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.deleteTree;
import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.isEmptyOrMissing;
import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.readTimestampFile;
import static org.ops4j.kaiserkai.core.api.storage.file.FileOperations.toSubDirs;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ops4j.kaiserkai.core.api.lock.LockManager;
import org.ops4j.kaiserkai.core.api.model.Layer;
import org.ops4j.kaiserkai.core.api.model.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * @author Harald Wellmann
 *
 */
@ApplicationScoped
public class GarbageCollectionService {

    private static Logger log = LoggerFactory.getLogger(GarbageCollectionService.class);

    private static final String UNUSED = "_unused";

    @Inject
    private LockManager lockManager;

    @Inject
    private StoragePaths paths;

    /**
     * Maps blob digests to repository tags.
     * <p>
     * Each digest is initially mapped to {@link GarbageCollectionService#UNUSED}. The value is replaced by a qualified
     * repository tag (e.g. {@code httpd:latest}) to mark an image version that uses this blob.
     */
    private static class BlobToManifestMap extends HashMap<String, String> {

        private static final long serialVersionUID = 1L;
    }

    public void collectGarbage() {
        try {
            BlobToManifestMap blobToManifestMap = findAllBlobs();
            markReferencedBlobs(blobToManifestMap);
            deleteUnusedBlobs(blobToManifestMap);
            removeUnusedLayers(blobToManifestMap);
            removeStaleUploads();
            log.info("Garbage collection completed");
        } finally {
            lockManager.unlock();
        }
    }

    /**
     * @param blobToManifestMap
     */
    private void deleteUnusedBlobs(BlobToManifestMap blobToManifestMap) {
        blobToManifestMap.forEach((k, v) -> deleteBlobIfUnused(k, v));
    }

    /**
     * @param blob
     * @param manifest
     */
    private void deleteBlobIfUnused(String blob, String manifest) {
        if (manifest.equals(UNUSED)) {
            log.debug("Deleting unused blob: {}", blob);
            File blobDir = paths.getBlobDir(blob);
            deleteTree(blobDir);
        }
    }

    /**
     * @param blobToManifestMap
     */
    private void removeUnusedLayers(BlobToManifestMap blobToManifestMap) {
        toSubDirs(paths.getRepositoriesDir()).forEach(repo -> removeUnusedLayers(blobToManifestMap, repo));
    }

    /**
     * @param blobToManifestMap
     * @param repo
     * @return
     */
    private void removeUnusedLayers(BlobToManifestMap blobToManifestMap, File repo) {
        File layersDir = new File(repo, "_layers/sha256");
        toSubDirs(layersDir).forEach(layerDir -> removeLayerIfUnused(blobToManifestMap, layerDir));
        String repository = repo.getName();
        File tagsDir = paths.getTagsDir(repository);
        if (isEmptyOrMissing(tagsDir)) {
            log.debug("Deleting empty repository: {}", repository);
            deleteTree(repo);
            return;
        }
        toSubDirs(paths.getTagsDir(repo.getName())).forEach(tagDir -> removeUnusedIndexes(blobToManifestMap, tagDir));

        toSubDirs(new File(repo, "_manifests/revisions/sha256")).forEach(dir -> removeLayerIfUnused(blobToManifestMap, dir));
    }

    private void removeUnusedIndexes(BlobToManifestMap blobToManifestMap, File tagDir) {
        toSubDirs(new File(tagDir, "index/sha256")).forEach(dir -> removeLayerIfUnused(blobToManifestMap, dir));
    }

    /**
     * @param blobToManifestMap
     * @param layerDir
     * @return
     */
    private void removeLayerIfUnused(BlobToManifestMap blobToManifestMap, File layerDir) {
        String digest = "sha256:" + layerDir.getName();
        String reference = blobToManifestMap.get(digest);
        if (reference == null || UNUSED.equals(reference)) {
            log.debug("Deleting unused layer {}", digest);
            deleteTree(layerDir);
        }
    }

    private void removeStaleUploads() {
        toSubDirs(paths.getRepositoriesDir()).forEach(repo -> removeStaleUploads(repo));
    }

    /**
     * @param repo
     * @return
     */
    private void removeStaleUploads(File repo) {
        toSubDirs(paths.getUploadsDir(repo.getName())).forEach(upload -> removeStaleUpload(upload));
    }

    /**
     * @param upload
     * @return
     * @throws IOException
     */
    private void removeStaleUpload(File uploadDir) {
        Optional<Instant> timestamp = readTimestampFile(uploadDir);
        if (timestamp.isPresent()) {
            if (timestamp.get().isAfter(Instant.now().minus(1, ChronoUnit.DAYS))) {
                return;
            }
        }

        deleteTree(uploadDir);
    }

    /**
     * @param blobToManifestMap
     */
    private void markReferencedBlobs(BlobToManifestMap blobToManifestMap) {
        toSubDirs(paths.getRepositoriesDir()).forEach(repo -> markReferencedBlobs(blobToManifestMap, repo));
    }

    /**
     * @param blobToManifestMap
     * @param repo
     * @return
     * @throws IOException
     */
    private void markReferencedBlobs(BlobToManifestMap blobToManifestMap, File repo) {
        File tagsDir = paths.getTagsDir(repo.getName());
        File[] tags = tagsDir.listFiles();
        if (tags == null) {
            return;
        }
        for (File tagDir : tags) {
            File currentTagDir = paths.getCurrentTagDir(repo.getName(), tagDir.getName());
            String digest = FileOperations.readLinkFile(currentTagDir).get();
            File blobData = paths.getBlobData(digest);
            String repoTag = String.format("%s:%s", repo.getName(), tagDir.getName());
            blobToManifestMap.put(digest, repoTag);
            log.debug("Blob {} used by image {}", digest, repoTag);
            markReferencedBlobsFromManifest(blobToManifestMap, repoTag, blobData);
        }
    }

    /**
     * @param blobToManifestMap
     * @param blobData
     * @throws IOException
     * @throws JsonProcessingException
     */
    private void markReferencedBlobsFromManifest(BlobToManifestMap blobToManifestMap, String repoTag, File blobData) {
        if (!blobData.exists()) {
            return;
        }
        ObjectReader reader = new ObjectMapper().readerFor(Manifest.class);
        try {
            Manifest manifest = reader.readValue(blobData);
            log.debug("Blob {} used by manifest of {}", manifest.getConfig().getDigest(), repoTag);
            manifest.getLayers().forEach(l -> log.debug("Blob {} used by layer of {}", l.getDigest(), repoTag));

            blobToManifestMap.put(manifest.getConfig().getDigest(), repoTag);
            for (Layer layer : manifest.getLayers()) {
                blobToManifestMap.put(layer.getDigest(), repoTag);
            }
        } catch (IOException exc) {
            log.error("Cannot parse manifest " + blobData, exc);
        }
    }

    /**
     * @return
     */
    private BlobToManifestMap findAllBlobs() {
        BlobToManifestMap map = new BlobToManifestMap();
        File algoDir = new File(paths.getBlobsDir(), "sha256");
        toSubDirs(algoDir).flatMap(FileOperations::toSubDirs).forEach(dir -> map.put("sha256:" + dir.getName(), UNUSED));
        return map;
    }
}
