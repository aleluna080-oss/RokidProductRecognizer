package com.alexluna.rokidproduct;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Size;
import android.view.WindowManager;
import android.view.View;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends ComponentActivity {

    // V1.1: elevamos el filtro para reducir detecciones débiles.
    // No representa "90% de exactitud real"; eso se validará con el modelo V2.
    private static final float MIN_CONFIDENCE = 0.75f;
    private static final long ANALYSIS_INTERVAL_MS = 450L;

    private TextView productText;
    private TextView confidenceText;
    private TextView statusText;

    private ExecutorService cameraExecutor;
    private ImageLabeler labeler;
    private final AtomicBoolean processing = new AtomicBoolean(false);

    private long lastAnalysisAt = 0L;
    private final RecognitionStabilizer stabilizer = new RecognitionStabilizer();
    private final Handler hudHandler = new Handler(Looper.getMainLooper());
    private final Object labelerLock = new Object();
    private volatile boolean destroyed;
    private boolean resumed;
    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis imageAnalysis;

    private final Runnable expiryCheck = new Runnable() {
        @Override public void run() {
            if (stabilizer.expire(SystemClock.elapsedRealtime())) {
                productText.setText("Apunta a un objeto");
                confidenceText.setText("");
            }
            hudHandler.postDelayed(this, 500L);
        }
    };

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    productText.setText("Permiso requerido");
                    confidenceText.setText("");
                    statusText.setText("Activa el permiso de cámara para reconocer objetos.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_main);

        productText = findViewById(R.id.productText);
        confidenceText = findViewById(R.id.confidenceText);
        statusText = findViewById(R.id.statusText);

        statusText.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName())));
            } else if (cameraProvider == null) {
                startCamera();
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
        labeler = ImageLabeling.getClient(
                new ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(MIN_CONFIDENCE)
                        .build()
        );

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        statusText.setText("Iniciando cámara…");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                if (destroyed) return;
                cameraProvider = cameraProviderFuture.get();

                imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                cameraProvider.unbindAll();
                CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
                if (!cameraProvider.hasCamera(selector)) {
                    if (cameraProvider.getAvailableCameraInfos().isEmpty()) {
                        throw new IllegalStateException("No hay una cámara accesible");
                    }
                    selector = new CameraSelector.Builder().addCameraFilter(infos ->
                            java.util.Collections.singletonList(infos.get(0))).build();
                }

                cameraProvider.bindToLifecycle(this, selector, imageAnalysis);
                statusText.setText("Cámara activa · apunta a un objeto");
            } catch (Exception e) {
                productText.setText("Error de cámara");
                statusText.setText("No se pudo iniciar la cámara");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @androidx.annotation.OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeFrame(@NonNull ImageProxy imageProxy) {
        long now = SystemClock.elapsedRealtime();

        if (now - lastAnalysisAt < ANALYSIS_INTERVAL_MS
                || !processing.compareAndSet(false, true)) {
            imageProxy.close();
            return;
        }

        lastAnalysisAt = now;

        synchronized (labelerLock) {
            if (destroyed) {
                finishFrame(imageProxy);
                return;
            }

            try {
                Image mediaImage = imageProxy.getImage();
                if (mediaImage == null) {
                    finishFrame(imageProxy);
                    return;
                }

                InputImage image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.getImageInfo().getRotationDegrees()
                );

                labeler.process(image)
                        .addOnSuccessListener(this::handleLabels)
                        .addOnFailureListener(error -> {
                            if (!destroyed && resumed) {
                                stabilizer.accept(null, SystemClock.elapsedRealtime());
                                runOnUiThread(() ->
                                        statusText.setText("No fue posible reconocer el objeto"));
                            }
                        })
                        .addOnCompleteListener(Runnable::run,
                                task -> finishFrame(imageProxy));

            } catch (RuntimeException error) {
                finishFrame(imageProxy);
                hudHandler.post(() -> {
                    if (!destroyed && resumed) {
                        stabilizer.accept(null, SystemClock.elapsedRealtime());
                        statusText.setText("No fue posible analizar la imagen");
                    }
                });
            }
        }
    }

    private void finishFrame(ImageProxy imageProxy) {
        synchronized (labelerLock) {
            imageProxy.close();
            processing.set(false);
            if (destroyed) labeler.close();
        }
    }

    private void handleLabels(List<ImageLabel> labels) {
        if (destroyed || !resumed) return;

        ImageLabel best = null;
        String bestDisplayName = null;

        // V1.1: ignoramos por completo cualquier etiqueta sin traducción aprobada.
        // Así nunca mostramos texto inglés ni confirmamos una clase desconocida.
        for (ImageLabel label : labels) {
            if (label.getConfidence() < MIN_CONFIDENCE) continue;

            String translated = ProductNameMapper.toDisplayName(label.getText());
            if (translated == null) continue;

            if (best == null || label.getConfidence() > best.getConfidence()) {
                best = label;
                bestDisplayName = translated;
            }
        }

        if (best == null || bestDisplayName == null) {
            stabilizer.accept(null, SystemClock.elapsedRealtime());
            runOnUiThread(() -> {
                productText.setText("Objeto no identificado");
                confidenceText.setText("");
                statusText.setText("Buscando una categoría conocida…");
            });
            return;
        }

        final String displayName = bestDisplayName;
        final float confidence = best.getConfidence();

        if (!stabilizer.accept(displayName, SystemClock.elapsedRealtime())) {
            runOnUiThread(() ->
                    statusText.setText("Confirmando detección…"));
            return;
        }

        final String confidenceString = String.format(
                Locale.forLanguageTag("es-MX"),
                "Confianza del modelo: %.0f%%",
                confidence * 100f
        );

        runOnUiThread(() -> {
            productText.setText(displayName);
            confidenceText.setText(confidenceString);
            statusText.setText("Objeto detectado");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        hudHandler.post(expiryCheck);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (cameraProvider == null
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        resumed = false;
        hudHandler.removeCallbacks(expiryCheck);
        stabilizer.reset();
        productText.setText("Apunta a un objeto");
        confidenceText.setText("");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        synchronized (labelerLock) {
            destroyed = true;
            if (labeler != null && !processing.get()) {
                labeler.close();
            }
        }

        hudHandler.removeCallbacksAndMessages(null);

        if (imageAnalysis != null) {
            imageAnalysis.clearAnalyzer();
        }

        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }

        super.onDestroy();
    }
}
