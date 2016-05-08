package com.koleshop.koleshopbackend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.koleshop.koleshopbackend.models.db.ImageUploadRequest;
import com.koleshop.koleshopbackend.models.db.KoleResponse;
import com.koleshop.koleshopbackend.services.KoleshopCloudStorageService;
import com.koleshop.koleshopbackend.services.SessionService;
import com.koleshop.koleshopbackend.test.LocalExample;

import java.io.IOException;

/**
 * Created by Gundeep on 04/12/15.
 */

@Api(name = "blobstoreEndpoint",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "api.koleshop.com", ownerName = "koleshopserver", packagePath = ""),
        scopes={"https://www.googleapis.com/auth/userinfo.email"
                ,"https://www.googleapis.com/auth/cloud-platform"})
public class BlobstoreEndpoint {


    @ApiMethod(
            name = "setImage",
            path = "setImage",
            httpMethod = ApiMethod.HttpMethod.POST
    )
     public KoleResponse saveImageForUser(@Named("userId") Long userId, @Named("sessionId") String sessionId, ImageUploadRequest imageRequest) throws IOException {

        KoleResponse koleResponse = new KoleResponse();

        try {
            if (SessionService.verifyUserAuthenticity(userId, sessionId) && imageRequest!=null) {
                KoleshopCloudStorageService.saveImageForUser(imageRequest);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    @ApiMethod(
            name = "doThisShit",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public KoleResponse doThisShit(@Named("testString") String storageString) throws IOException {
        KoleResponse koleResponse = new KoleResponse();
        try {
            new LocalExample().runTest();
            koleResponse.setData("ok done");
            koleResponse.setSuccess(true);
        } catch (ClassNotFoundException e) {
            koleResponse.setData("oopsy shit");
            koleResponse.setSuccess(false);
        }
        return koleResponse;
    }

    @ApiMethod(
            name = "doCloudyShit",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public KoleResponse doCloudyShit(@Named("testString") String testString, @Named("filename") String filename) throws IOException {
        KoleResponse koleResponse = new KoleResponse();
        try {
            KoleshopCloudStorageService.doThisShit(testString, filename);
            koleResponse.setData("ok done");
            koleResponse.setSuccess(true);
        } catch (Exception e) {
            koleResponse.setData("oopsy shit");
            koleResponse.setSuccess(false);
        }
        return koleResponse;
    }


    @ApiMethod(
            name = "loggingTest",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public KoleResponse loggingTest() {
        KoleshopCloudStorageService.justALoggingTest("anything");
        return new KoleResponse(KoleResponse.STATUS_KOLE_RESPONSE_SUCCESS, "lol daya");
    }

}
