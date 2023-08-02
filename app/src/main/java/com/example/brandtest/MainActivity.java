package com.example.brandtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements LifecycleOwner {

    Executor cameraExecutor = null;

    public static final String TAG = MainActivity.class.getSimpleName();
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private CloudVisionApiClient client = null;
    private BarcodeScanningActivity barcodeScanner = null;
    private TextRecognitionActivity textRecognitionActivity = null;

    ImageView imageView = null;

    private File outputDirectory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();

        client = new CloudVisionApiClient();
        barcodeScanner = new BarcodeScanningActivity();
        textRecognitionActivity = new TextRecognitionActivity();

        final Button button = (Button) findViewById((R.id.camera_capture_button));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecognize();
            }
        });

        imageView = findViewById(R.id.imageView);

        Executor cameraExecutor = Executors.newSingleThreadExecutor();
        outputDirectory = this.getCacheDir();
    }

    protected void onDestroy()
    {
        super.onDestroy();
    }

    void requestPermission()
    {
        startCamera();
    }


    void startCamera()
    {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable()
        {
            @Override
            public void run()
            {
                try{
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    PreviewView viewFinder = findViewById(R.id.viewFinder);
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                    imageCapture = new ImageCapture.Builder()
                            .setTargetRotation(viewFinder.getDisplay().getRotation())
                            .build();

                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                    cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, preview, imageCapture);

                }
                catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        },ContextCompat.getMainExecutor(this));
    }

    public void previewAnalyzedBitmap(Bitmap bitmap)
    {
        imageView.setImageBitmap(bitmap);
    }
    void onRecognize() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        try {
            File photoFile = File.createTempFile("prefix", ".extension", outputDirectory);

            ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
            imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onError(@NonNull ImageCaptureException exc) {
                            Log.e("PHOTO", "Photo capture failed: " + exc.getMessage(), exc);
                        }

                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath());

                            textRecognitionActivity.Recognize(bitmap, imageCapture.getTargetRotation());

                            //barcodeScanner.Scan(bitmap,imageCapture.getTargetRotation());
                            /*
                            try {
                                client.detectLogos(bitmap,MainActivity.this);

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            */
                            String msg = "Detecting...";
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            Log.d("PHOTO", msg);
                        }
                    });
        } catch (IOException e){
            e.printStackTrace();
        }

    }


}