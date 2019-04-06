package org.ops4j.kaiserkai.core.api.storage.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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

    public static String computeDigest(char[] text) {
        InputStream is = new ByteArrayInputStream(toBytes(text));
        return computeDigest(is);
    }

    private static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000');
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
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
