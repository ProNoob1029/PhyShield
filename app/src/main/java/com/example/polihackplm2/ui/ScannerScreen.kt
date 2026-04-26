package com.example.polihackplm2.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScannerScreen(
    modifier: Modifier = Modifier,
    onScanTriggered: (String) -> Unit = {}
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scannedUrl by remember { mutableStateOf("") }
    var isTriggered by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { hasCameraPermission = it })
    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.CAMERA) }

    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize()
            .imePadding()
    ) {
        PhishShieldHeader("Scanner")
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(text = "QR scanner", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp)).background(Color.Black), contentAlignment = Alignment.Center) {
                    if (hasCameraPermission) {
                        CameraPreview(onBarcodeDetected = { url ->
                            if (url.isNotEmpty() && url != scannedUrl && !isTriggered) {
                                scannedUrl = url
                                isTriggered = true
                                onScanTriggered(url)
                            }
                        })
                    } else { Text("Camera permission required", color = Color.White) }
                    Box(modifier = Modifier.size(200.dp).border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)))
                    Text(text = if (scannedUrl.isEmpty()) "Point camera at QR code" else "Scanned: $scannedUrl", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp), textAlign = TextAlign.Center)
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(text = "Or paste a URL / SMS text", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                TextField(value = textInput, onValueChange = { textInput = it }, placeholder = { Text("https://... or paste message") }, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)), colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant, unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant, unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Button(onClick = {
                    if (textInput.isNotEmpty() && !isTriggered) {
                        isTriggered = true
                        onScanTriggered(textInput)
                    }
                }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) {
                    Text("Analyse now", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CameraPreview(onBarcodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(factory = { ctx ->
        val previewView = PreviewView(ctx).apply { layoutParams = android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT) }
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val barcodeScanner = BarcodeScanning.getClient()
            val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    barcodeScanner.process(image).addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) { barcode.rawValue?.let { onBarcodeDetected(it) } }
                    }.addOnCompleteListener { imageProxy.close() }
                } else { imageProxy.close() }
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
            } catch (e: Exception) { android.util.Log.e("CameraPreview", "Use case binding failed", e) }
        }, ContextCompat.getMainExecutor(ctx))
        previewView
    }, modifier = Modifier.fillMaxSize())
}
