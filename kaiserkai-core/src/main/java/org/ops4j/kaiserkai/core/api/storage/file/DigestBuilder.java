package org.ops4j.kaiserkai.core.api.storage.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class DigestBuilder {

    public static String computeDigest(File file) {
        try (InputStream fis = new FileInputStream(file)) {
            return computeDigest(fis);
        } catch (IOException exc) {
            throw new IllegalStateException(exc);
        }
    }

    public static String computeDigest(String text) {
        InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        return computeDigest(is);
    }

    private static String computeDigest(InputStream fis) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] data = new byte[1024];
            int numBytes = 0;
            while ((numBytes = fis.read(data)) != -1) {
                digest.update(data, 0, numBytes);
            }
            byte[] hashBytes = digest.digest();
            return "sha256:" + DatatypeConverter.printHexBinary(hashBytes).toLowerCase();
        } catch (NoSuchAlgorithmException | IOException exc) {
            throw new IllegalStateException(exc);
        }
    }
}
