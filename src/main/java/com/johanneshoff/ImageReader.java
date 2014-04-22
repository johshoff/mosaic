package com.johanneshoff;

import com.google.common.io.ByteStreams;
import org.apache.commons.codec.binary.Hex;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jhoff on 4/21/14.
 */
public class ImageReader {
    private MessageDigest md5;

    public static byte[] loadFileContent(String filename) throws IOException {
            return ByteStreams.toByteArray(new BufferedInputStream(new FileInputStream(filename)));
    }

    public ImageReader() throws NoSuchAlgorithmException {
        md5 = MessageDigest.getInstance("MD5");
    }

    public ImageDataVerbose readImage(String filename) throws IOException {
        ImageDataVerbose image = new ImageDataVerbose();

        image.content = loadFileContent(filename);
        image.contentType = "image/png"; // TODO: Find correct content type
        image.md5 = Hex.encodeHexString(md5.digest(image.content));

        ImageData basicData = new ImageData();
        basicData.filename = filename; // TODO: Split name and path
        basicData.filepath = null;
        basicData.host     = null; // TODO: Supply host value somehow

        image.basicData = basicData;

        return image;
    }
}
