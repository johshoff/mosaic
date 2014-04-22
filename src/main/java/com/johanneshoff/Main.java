package com.johanneshoff;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.RiakFactory;
import com.basho.riak.client.bucket.Bucket;
import com.basho.riak.client.builders.RiakObjectBuilder;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Main {

    public static void addImage(IRiakClient riakClient, String filename) throws Exception {
        ImageReader reader = new ImageReader();

        Bucket images     = riakClient.createBucket("image_data").execute();
        Bucket thumbnails = riakClient.createBucket("thumbnails").execute();

        ImageDataVerbose image = reader.readImage(filename);

        String key = image.md5;

        images.delete(key).rw(3).execute();
        thumbnails.delete(key).rw(3).execute();

        Process process = Runtime.getRuntime().exec(new String[]{"convert", filename, "-resize", "640x480>", "png:-"});
        process.waitFor();
        byte[] thumbnailContent = ByteStreams.toByteArray(process.getInputStream());

        IRiakObject thumbnail = RiakObjectBuilder.newBuilder("thumbnails", key)
                .withContentType("image/png")
                .withValue(thumbnailContent)
                .build();

        thumbnails.store(key, thumbnail).execute();

        images.store(key, image.basicData).execute();
    }

    public static void main(String[] args) throws Exception {
        IRiakClient riakClient = RiakFactory.pbcClient();

        List<String> files = new ArrayList<>();

        File folder = new File(".");
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".png")) {
                files.add(file.getName());
            }
        }

        System.out.println("Adding " + files.size() + " files.");

        ExecutorService workers = Executors.newFixedThreadPool(32);

        for (String filename : files) {
            workers.submit(() -> {
                System.out.println(Thread.currentThread().getId()+": Adding "+filename);
                addImage(riakClient, filename);
                return null;
            });
        }

        System.out.println("Waiting for workers to complete");
        workers.shutdown();
        workers.awaitTermination(60, TimeUnit.SECONDS);

        riakClient.shutdown();
    }
}
