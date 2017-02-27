# koleshop
Location based android marketplace for sellers and nearby customers

Here's a demo for the application: http://bit.ly/koleshopDemo

## Technology Stack:
• Google Cloud Platform
 - Google Cloud Sql (MySQL db)
 - Google Cloud Storage (Used for storing images)
 - Google App Engine (Java Backend)
 - Google Cloud Endpoints v1 (Alternative of web services)
• Native Android App

## How to run the app:

1. Clone the project
2. Build using Android Studio (Sign in to the google account on Android Studio to enable the google cloud functionality like deploying the backend to app engine from inside the studio)
3. Create a google cloud project.
4. Create new google maps api key in the project and configure the key inside the build.gradle (app) inside the buildTypes section.
5. Create a new google cloud sql instance in the project and use [this database dump file](http://bit.ly/koleshop_db_dump) to create the database.
6. Now update the [config-production.properties](https://github.com/gndps/koleshop/blob/master/backend/src/main/webapp/config-production.properties) or the [config-development.properties](https://github.com/gndps/koleshop/blob/master/backend/src/main/webapp/config-development.properties) file. Update all the parameters related to Google Cloud Sql
7. Enable Cloud messaging in your google project and Create a GCM key (now called FCM) to enable push notifications in the app. And update the GCM Api key inside the same config file as in previous step.
8. Create a new bucket in google cloud storage. Create a folder named `<your-bucket-name>/profile`, `<your-bucket-name>/profile_thumb`, `<your-bucket-name>/category-pics` and `<your-bucket-name>/product_images/s` and update the path `com.koleshop.koleshopbackend.common.Constants#PUBLIC_PROFILE_IMAGE_FOLDER` and other paths in the same section of this file.
9. Now upload the category pics from [here](http://bit.ly/koleshopCategoryImages) to your google cloud storage directory & product images from [here](http://bit.ly/koleshopProductImagesSmall) to `<your-bucket-name>/product_images/s`. Update the category-pics url in mysql db table `ProductCategory`, column `image_url`. Also update the product images url in Table `InventoryVariety` and 'ProductVariety'
10. Uncomment the line `compile project(path: ':backend', configuration: 'android-endpoints')` in build.gradle(app) to compile the endpoints libs from the backend project. (Other way is to generate the jar libraries using gradlew export)
11. `Build>Deploy Module to app engine` inside Android Studio. This project uses Google Cloud Endpoints v1 and this step deploys the backend cloud endpoints to the app engine. Clicking on deploy will ask you to choose the google cloud project.
12. Build & Run the Android app on phone.

## Caveats:
- Only activated shops are shown in the nearby shops list. You must go to the mysql db and activate any new shop manually.
- You must configure the SMS gateway in the backend `common.Constants.SMS_GATEWAY_URL`, `common.Constants.USE_GATEWAY_NUMBER` and the sms sending json in `services.SessionService#requestOneTimePassword` according to your own sms gateway.

