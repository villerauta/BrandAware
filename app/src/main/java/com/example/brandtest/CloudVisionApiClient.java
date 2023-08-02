package com.example.brandtest;

import com.example.brandtest.BuildConfig;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

//import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
//import com.google.j2objc.annotations.Weak;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/*
import com.google.api.gax.core.GoogleCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;

import com.google.protobuf.ByteString;
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudVisionApiClient {

    private MainActivity mainActivity = null;
    private final String API_KEY = BuildConfig.API_KEY;
    private final String CLOUD_VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate?key=" + API_KEY;

    public void detectLogos(Bitmap bitmap, MainActivity activity) throws IOException {

        mainActivity = activity;
        LogoDetectAsync(bitmap,activity);
        /*try{
            AsyncTask<Object, Void, String> logoDetectionTask = new LogoDetectionTask(activity, prepareAnnotationRequest(bitmap));
            logoDetectionTask.execute();
        } catch (IOException e) {
            Log.d(MainActivity.TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }

        */

    }
    
    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        //HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        NetHttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer = new VisionRequestInitializer(API_KEY)
        {
            @Override
            protected void initializeVisionRequest(VisionRequest<?> visionRequest) throws IOException
            {
                super.initializeVisionRequest(visionRequest);

            }
        };
        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);
        builder.setApplicationName("BrandAware");

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();

        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            //Add image
            Image base64Image = new Image();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            base64Image.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64Image);

            //mainActivity.previewAnalyzedBitmap(BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length));

            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature logoDetection = new Feature();
                logoDetection.setType("LABEL_DETECTION");
                logoDetection.setMaxResults(10);
                add(logoDetection);
            }});

            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
        annotateRequest.setDisableGZipContent(true);

        return annotateRequest;
    }

    void LogoDetectAsync(Bitmap bitmap, MainActivity mainActivity)
    {
        ExecutorService exe = Executors.newSingleThreadExecutor();
        exe.execute(new Runnable() {
            @Override
            public void run() {
                Vision.Images.Annotate annotate = null;
                try {
                    annotate = prepareAnnotationRequest(bitmap);
                    doInBackground(mainActivity, annotate);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    protected void doInBackground(MainActivity activity, Vision.Images.Annotate mRequest)
    {
        try{
            Log.d(MainActivity.TAG, "Sending request");
            BatchAnnotateImagesResponse response = mRequest.execute();
            Log.d(MainActivity.TAG, "Response arrived");
            String responseString = convertResponseToString(response);
            Log.d(MainActivity.TAG, responseString);

        } catch(GoogleJsonResponseException e) {
            Log.d(MainActivity.TAG, "failed to make API request because " + e.getContent());
        } catch (IOException e) {
            Log.d(MainActivity.TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }

    }

    private static class LogoDetectionTask extends AsyncTask<Object,Void,String> {
        private final WeakReference<MainActivity> mainActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LogoDetectionTask(MainActivity activity, Vision.Images.Annotate annotate) {
            mainActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }
        @Override
        protected String doInBackground(Object... params)
        {
            try{
                Log.d(MainActivity.TAG, "Sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                String responseString = convertResponseToString(response);
                Log.d(MainActivity.TAG, responseString);
                return responseString;

            } catch(GoogleJsonResponseException e) {
                Log.d(MainActivity.TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(MainActivity.TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("I found these things:\n\n");

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing");
        }

        return message.toString();
    }


    private static String buildRequestJson(byte[] imageBytes) {
        String requestJson = "Paska";
        return requestJson;
    }

    private static byte[] convertFileToByteArray(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        fis.close();
        return bos.toByteArray();
    }
    private String sendPostRequest(String requestJson) throws Exception {
        URL url = new URL(CLOUD_VISION_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
        outStream.writeBytes(requestJson);
        outStream.flush();
        outStream.close();

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return response.toString();
        } else {
            return null;
        }
    }
}
