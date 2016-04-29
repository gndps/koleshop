package com.koleshop.koleshopbackend.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.storage.StorageScopes;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.gcloud.storage.Acl;
import com.google.gcloud.storage.BlobInfo;
import com.google.gcloud.storage.Bucket;
import com.google.gson.Gson;
import com.google.gson.internal.bind.TypeAdapters;
import com.koleshop.koleshopbackend.common.Constants;
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
import com.koleshop.koleshopbackend.utils.ImageUtils;
import com.koleshop.koleshopbackend.utils.PropertiesCache;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by Gundeep on 04/12/15.
 */
public class KoleshopCloudStorageService {

    private static Storage storageService;
    private static final int IMAGE_THUMBNAIL_SIZE = 120;

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

        Storage storage = StorageOptions.defaultInstance().service();
        BlobId blobId = BlobId.of("koleshop-bucket", filename);
        Blob blob = Blob.load(storage, blobId);
        if (blob == null) {
            BlobInfo blobInfo = BlobInfo.builder(blobId).contentType("text/plain").build();
            storage.create(blobInfo, ("Hello, Cloud Storage! its gndp test here\n" + blobContent).getBytes(UTF_8));
        } else {
            byte[] prevContent = blob.content();
            System.out.println(new String(prevContent, UTF_8));
            WritableByteChannel channel = blob.writer();
            try {
                channel.write(ByteBuffer.wrap("Updated content".getBytes(UTF_8)));
                channel.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean uploadProfilePicture(ImageUploadRequest imageUploadRequest) {
        return uploadImage(imageUploadRequest, Constants.PUBLIC_PROFILE_IMAGE_FOLDER);
    }

    public static boolean uploadProfilePictureThumbnail(ImageUploadRequest imageUploadRequest) {
        ImagesService imagesService = ImagesServiceFactory.getImagesService();

        Image oldImage = ImagesServiceFactory.makeImage(imageUploadRequest.getImageData());
        Transform resize = ImagesServiceFactory.makeResize(IMAGE_THUMBNAIL_SIZE, IMAGE_THUMBNAIL_SIZE);

        Image newImage = imagesService.applyTransform(resize, oldImage);

        byte[] thumbnailImageData = newImage.getImageData();

        imageUploadRequest.setImageData(thumbnailImageData);
        return uploadImage(imageUploadRequest, Constants.PUBLIC_PROFILE_IMAGE_THUMBNAIL_FOLDER);

    }

    public static boolean uploadProductImage(ImageUploadRequest imageUploadRequest) {
        boolean largeUploaded = uploadImage(imageUploadRequest,
                Constants.PUBLIC_PRODUCT_IMAGE_FOLDER_LARGE);
        boolean mediumUploaded = uploadImage(scaleImageUploadRequest(imageUploadRequest, Constants.PRODUCT_IMAGE_MEDIUM_SIZE)
                , Constants.PUBLIC_PRODUCT_IMAGE_FOLDER_MEDIUM);
        boolean smallUploaded = uploadImage(scaleImageUploadRequest(imageUploadRequest, Constants.PRODUCT_IMAGE_SMALL_SIZE)
                , Constants.PUBLIC_PRODUCT_IMAGE_FOLDER_LARGE);
        return largeUploaded && mediumUploaded && smallUploaded;
    }

    private static ImageUploadRequest scaleImageUploadRequest(ImageUploadRequest imageUploadRequest, int scaleSize) {
        byte[] imageDataMedium = ImageUtils.scale(imageUploadRequest.getImageData(), scaleSize, scaleSize);
        ImageUploadRequest imageUploadRequestScaled = new ImageUploadRequest();
        imageUploadRequestScaled.setFileName(imageUploadRequest.getFileName());
        imageUploadRequestScaled.setImageData(imageDataMedium);
        imageUploadRequestScaled.setMimeType(imageUploadRequest.getMimeType());
        return imageUploadRequestScaled;
    }

    public static boolean uploadImage(ImageUploadRequest imageUploadRequest, String bucketUrl) {

        Storage storage = StorageOptions.defaultInstance().service();
        BlobId blobId = BlobId.of(PropertiesCache.getProp("PUBLIC_IMAGES_BUCKET_NAME"), bucketUrl + imageUploadRequest.getFileName());
        Blob blob = Blob.load(storage, blobId);

        if (blob == null) {
            //create new blob
            //create blob info(public access for all users)
            List<Acl> aclList = new ArrayList<>();
            Acl.User cloudUser = Acl.User.ofAllUsers();
            Acl.Role cloudUserRole = Acl.Role.READER;
            aclList.add(new Acl(cloudUser, cloudUserRole));
            Acl.User ownerUser = Acl.User.ofAllAuthenticatedUsers();
            Acl.Role roleOwner = Acl.Role.OWNER;
            aclList.add(new Acl(ownerUser, roleOwner));
            BlobInfo blobInfo = BlobInfo.builder(blobId).
                    contentType(imageUploadRequest.getMimeType())
                    .acl(aclList)
                    .build();
            storage.create(blobInfo, imageUploadRequest.getImageData());
            return true;
        } else {
            byte[] prevContent = blob.content();
            System.out.println(new String(prevContent, UTF_8));
            logger.info("updating the blob:" + imageUploadRequest.getFileName());
            WritableByteChannel channel = blob.writer();
            try {
                channel.write(ByteBuffer.wrap(imageUploadRequest.getImageData()));
                channel.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public static void justALoggingTest(String blobContent) {

        logger.log(Level.INFO, "logging info 1");
        logger.log(Level.WARNING, "logging warning 2");
        logger.log(Level.SEVERE, "logging severe 3");

    }

}
