package com.koleshop.koleshopbackend.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.storage.StorageScopes;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.gcloud.storage.BlobInfo;
import com.google.gson.Gson;
import com.google.gson.internal.bind.TypeAdapters;
import com.koleshop.koleshopbackend.db.models.ImageUploadRequest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.appengine.api.blobstore.BlobstoreService;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gcloud.storage.Blob;
import com.google.gcloud.storage.BlobId;
import com.google.gcloud.storage.Storage;
import com.google.gcloud.storage.StorageOptions;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Gundeep on 04/12/15.
 */
public class KoleshopCloudStorageService {

    private static Storage storageService;

    private static final Logger logger = Logger.getLogger(KoleshopCloudStorageService.class.getName());

    public static void saveImageForUser(ImageUploadRequest imageRequest) {

        // create blob url
        BlobstoreService blobService = BlobstoreServiceFactory.getBlobstoreService();
        String uploadUrl = blobService.createUploadUrl("/blob/upload");

        // create multipart body containing file
        HttpEntity requestEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", imageRequest.getImageData(),
                        ContentType.create(imageRequest.getMimeType()), imageRequest.getFileName())
                .build();

        // Post request to BlobstorageService
        // Note: We cannot use Apache HttpClient, since AppEngine only supports Url-Fetch
        //  See: https://cloud.google.com/appengine/docs/java/sockets/
        URL url = null;
        try {
            url = new URL(uploadUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-length", requestEntity.getContentLength() + "");
            connection.addRequestProperty(requestEntity.getContentType().getName(), requestEntity.getContentType().getValue());
            requestEntity.writeTo(connection.getOutputStream());

            // BlobstorageService will forward to /blob/upload, which returns our json
            String responseBody = IOUtils.toString(connection.getInputStream());

            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                throw new IOException("HTTP Status " + connection.getResponseCode() + ": " + connection.getHeaderFields() + "\n" + responseBody);
            }

            // parse BlopUploadServlet's Json response
            //ImageUploadResponse response = new Gson().fromJson(responseBody, ImageUploadResponse.class);

            // save blobkey and serving url ...
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean doThisShit(String blobContent, String filename) {

        logger.log(Level.INFO, "----------- start logging 1 ---------");
        Storage storage = StorageOptions.defaultInstance().service();
        BlobId blobId = BlobId.of("koleshop-bucket", filename);
        if(blobId!=null) {
            logger.log(Level.INFO, "----------- start logging blob id is not null ---------");
        } else {
            logger.log(Level.INFO, "----------- start logging blod id is NULL ---------");
        }
        Blob blob = Blob.load(storage, blobId);
        if(blob!=null) {
            logger.log(Level.INFO, "----------- blob is NOT null ---------");
        } else {
            logger.log(Level.INFO, "----------- blob is NULL ---------");
        }
        if (blob == null) {
            logger.log(Level.INFO, "----------- creating new blob here ---------");
            BlobInfo blobInfo = BlobInfo.builder(blobId).contentType("text/plain").build();
            logger.log(Level.INFO, "----------- kickkkkiieeee ---------" + blobInfo.toString());
            storage.create(blobInfo, ("Hello, Cloud Storage! its gndp test here\n" + blobContent).getBytes(UTF_8));
            logger.log(Level.INFO, "----------- just said hello---------" + blobInfo.toString());
        } else {
            logger.log(Level.INFO, "----------- poing 1 ---------");
            logger.log(Level.INFO, "Updating content for " + blobId.name());
            byte[] prevContent = blob.content();
            logger.log(Level.INFO, "----------- poing 2 ---------");
            System.out.println(new String(prevContent, UTF_8));
            logger.log(Level.INFO, "----------- poing 3 ---------");
            WritableByteChannel channel = blob.writer();
            logger.log(Level.INFO, "----------- poing 4 ---------");
            try {
                logger.log(Level.INFO, "----------- writing some shit ---------");
                channel.write(ByteBuffer.wrap("Updated content".getBytes(UTF_8)));
                logger.log(Level.INFO, "----------- writing some shit 2 ---------");
                channel.close();
                logger.log(Level.INFO, "----------- wrote that shit bitch ---------");
                return true;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "omg why", e);
                return false;
            }
        }
        return false;
    }

    public static void justALoggingTest(String blobContent) {

        logger.log(Level.INFO, "logging info 1");
        logger.log(Level.WARNING, "logging warning 2");
        logger.log(Level.SEVERE, "logging severe 3");

    }

}
