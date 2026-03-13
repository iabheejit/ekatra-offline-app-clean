package org.ekatra.alfred

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.coroutineContext
import org.ekatra.alfred.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - WebView-based UI with native LLM backend
 * 
 * The WebView loads the chat UI from assets, and JavaScript
 * calls native methods through the AlfredBridge interface.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }

    private val app get() = application as EkatraApp

    private val modelUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"
    
    private lateinit var binding: ActivityMainBinding
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle edge-to-edge insets for the root layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        setupWebView()
        loadModel()
    }
    
    private fun setupWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            
            // Add JavaScript bridge
            addJavascriptInterface(AlfredBridge(), "Maya")
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d(TAG, "Page loaded: $url")
                }
                
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    Log.e(TAG, "WebView error: ${error?.description}")
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                    Log.d("WebView", "${message?.message()} [${message?.lineNumber()}]")
                    return true
                }
            }
            
            // Load the chat UI
            loadUrl("file:///android_asset/index.html")
        }
    }
    
    private fun loadModel() {
        scope.launch {
            updateLoadingText("Checking for model...")
            
            // Check for model in app's files directory
            val modelDir = File(filesDir, "models")
            modelDir.mkdirs()
            val modelFile = File(modelDir, "qwen2.5-0.5b-instruct-q4_k_m.gguf")
            
            if (!modelFile.exists()) {
                // Offer to download model
                withContext(Dispatchers.Main) {
                    updateLoadingText("Model not found. Ready to download (~470 MB)?")
                    binding.downloadButton.visibility = View.VISIBLE
                    binding.downloadButton.setOnClickListener {
                        it.visibility = View.GONE
                        scope.launch { downloadModel(modelFile) }
                    }
                }
                return@launch
            }
            
            // Verify model file isn't truncated using server size when available
            val remoteSize = withContext(Dispatchers.IO) { fetchRemoteSize(modelUrl) }
            val minValidSize = remoteSize?.let { (it * 0.98).toLong() }
            if (minValidSize != null && modelFile.length() < minValidSize) {
                Log.w(TAG, "Model file appears truncated: ${modelFile.length()} bytes, expected >= $minValidSize")
                modelFile.delete()
                withContext(Dispatchers.Main) {
                    updateLoadingText("Model file was incomplete. Ready to re-download?")
                    binding.downloadButton.visibility = View.VISIBLE
                    binding.downloadButton.setOnClickListener {
                        it.visibility = View.GONE
                        scope.launch { downloadModel(modelFile) }
                    }
                }
                return@launch
            }
            
            // Model exists, load it
            loadExistingModel(modelFile)
        }
    }
    
    private suspend fun downloadModel(modelFile: File) {
        updateLoadingText("Downloading model...")
        withContext(Dispatchers.Main) {
            binding.downloadProgress.visibility = View.VISIBLE
        }
        
        val tempFile = File(modelFile.absolutePath + ".tmp")
        
        try {
            val remoteSize = withContext(Dispatchers.IO) { fetchRemoteSize(modelUrl) }
            
            withContext(Dispatchers.IO) {
                val url = URL(modelUrl)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 60000
                connection.readTimeout = 120000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
                connection.connect()
                
                val fileSize = connection.contentLengthLong.takeIf { it > 0 } ?: remoteSize ?: 0L
                var downloadedSize = 0L
                
                // Clean up any previous temp file
                if (tempFile.exists()) tempFile.delete()
                
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(32768)
                        var bytesRead: Int
                        
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedSize += bytesRead
                            
                            val progress = if (fileSize > 0) {
                                (downloadedSize * 100 / fileSize).toInt().coerceAtMost(99)
                            } else {
                                (downloadedSize / 1_000_000L).toInt().coerceAtMost(99)
                            }
                            withContext(Dispatchers.Main) {
                                binding.downloadProgress.progress = progress
                                updateLoadingText("Downloading model... ${progress}% (${downloadedSize / 1_000_000}/${fileSize / 1_000_000} MB)")
                            }
                        }
                    }
                }
                
                // Verify download completeness using the best size we know
                val expectedSize = when {
                    fileSize > 0 -> fileSize
                    remoteSize != null -> remoteSize
                    else -> tempFile.length()
                }

                if (expectedSize > 0 && tempFile.length() < expectedSize * 0.98) {
                    Log.e(TAG, "Download incomplete: got ${tempFile.length()} expected $expectedSize")
                    tempFile.delete()
                    throw Exception("Download incomplete (${tempFile.length() / 1_000_000}/${ expectedSize / 1_000_000} MB)")
                }
                
                // Move temp to final
                if (modelFile.exists()) modelFile.delete()
                tempFile.renameTo(modelFile)
            }
            
            // Download complete, hide progress and load model
            withContext(Dispatchers.Main) {
                binding.downloadProgress.visibility = View.GONE
            }
            
            loadExistingModel(modelFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Model download failed", e)
            tempFile.delete() // Clean up partial download
            modelFile.delete()
            
            withContext(Dispatchers.Main) {
                binding.downloadProgress.visibility = View.GONE
                updateLoadingText("Download failed: ${e.message}\n\nTap to retry")
                binding.downloadButton.text = "Retry Download"
                binding.downloadButton.visibility = View.VISIBLE
                binding.downloadButton.setOnClickListener {
                    it.visibility = View.GONE
                    scope.launch { downloadModel(modelFile) }
                }
            }
        }
    }
    
    private suspend fun loadExistingModel(modelFile: File) {
        Log.d(TAG, "=== loadExistingModel START ===")
        Log.d(TAG, "Model file: ${modelFile.absolutePath}")
        Log.d(TAG, "File exists: ${modelFile.exists()}, Size: ${modelFile.length()}")
        updateLoadingText("Loading model...")
        
        Log.d(TAG, "Calling llamaEngine.loadModel()")
        val success = app.llamaEngine.loadModel(modelFile)
        Log.d(TAG, "llamaEngine.loadModel() returned: $success")
        
        if (success) {
            // Hide loading overlay
            withContext(Dispatchers.Main) {
                binding.loadingOverlay.visibility = View.GONE
                notifyWebView("modelLoaded", "true")
            }
        } else {
            // Model loading failed - could be corrupted
            withContext(Dispatchers.Main) {
                updateLoadingText("Model loading failed: ${app.llamaEngine.lastError}\n\nDelete and re-download?")
                binding.downloadButton.text = "Delete & Re-download"
                binding.downloadButton.visibility = View.VISIBLE
                binding.downloadButton.setOnClickListener {
                    it.visibility = View.GONE
                    modelFile.delete() // Delete corrupted model
                    scope.launch { downloadModel(modelFile) }
                }
            }
        }
    }
    
    private suspend fun updateLoadingText(text: String) {
        withContext(Dispatchers.Main) {
            binding.loadingText.text = text
        }
    }
    
    private fun notifyWebView(event: String, data: String) {
        binding.webView.evaluateJavascript(
            "window.onNativeEvent && window.onNativeEvent('$event', '$data')",
            null
        )
    }

    // Lightweight HEAD probe to determine remote model size; returns null if unavailable
    private fun fetchRemoteSize(urlStr: String): Long? {
        return try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = true
            connection.connect()
            val size = connection.contentLengthLong.takeIf { it > 0 }
            connection.disconnect()
            size
        } catch (e: Exception) {
            Log.w(TAG, "HEAD size probe failed: ${e.message}")
            null
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
    
    // ==================== JavaScript Bridge ====================
    
    /**
     * AlfredBridge - Exposes native methods to JavaScript
     * 
     * JavaScript can call: Maya.sendMessage(text), Maya.startServer(), etc.
     */
    inner class AlfredBridge {
        
        @JavascriptInterface
        fun sendMessage(message: String): String {
            Log.d(TAG, "JS -> sendMessage: ${message.take(50)}")
            
            if (!app.llamaEngine.isReady()) {
                return "Model not loaded yet. Please wait..."
            }
            
            // Generate response synchronously (blocks JS)
            // For streaming, use sendMessageAsync instead
            return runBlocking {
                val response = StringBuilder()
                app.llamaEngine.generate(message).collect { token ->
                    response.append(token)
                }
                response.toString()
            }
        }
        
        @JavascriptInterface
        fun sendMessageAsync(message: String, callbackId: String) {
            Log.d(TAG, "JS -> sendMessageAsync: ${message.take(50)}")
            
            if (!app.llamaEngine.isReady()) {
                callJsCallback(callbackId, "error", "Model not loaded yet")
                return
            }
            
            scope.launch {
                try {
                    val response = StringBuilder()
                    val tokenBuffer = StringBuilder()
                    var tokenCount = 0
                    
                    app.llamaEngine.generate(message).collect { token ->
                        coroutineContext.ensureActive() // Allow cancellation
                        response.append(token)
                        tokenBuffer.append(token)
                        tokenCount++
                        
                        // Batch tokens - send every 8 tokens for fewer WebView calls
                        if (tokenCount % 8 == 0) {
                            val bufferedTokens = tokenBuffer.toString()
                            tokenBuffer.clear()
                            withContext(Dispatchers.Main) {
                                callJsCallback(callbackId, "token", bufferedTokens)
                            }
                        }
                    }
                    
                    // Send any remaining tokens
                    if (tokenBuffer.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            callJsCallback(callbackId, "token", tokenBuffer.toString())
                        }
                    }
                    
                    // Send completion
                    withContext(Dispatchers.Main) {
                        callJsCallback(callbackId, "done", response.toString())
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        callJsCallback(callbackId, "error", e.message ?: "Unknown error")
                    }
                }
            }
        }
        
        @JavascriptInterface
        fun isModelReady(): Boolean {
            return app.llamaEngine.isReady()
        }
        
        @JavascriptInterface
        fun getModelStatus(): String {
            return app.llamaEngine.state.name
        }
        
        @JavascriptInterface
        fun startServer(): String {
            Log.d(TAG, "JS -> startServer")
            
            return try {
                if (app.alfredServer == null) {
                    // Start foreground service
                    val intent = Intent(this@MainActivity, AlfredServerService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                    
                    // Wait briefly for server to start
                    Thread.sleep(500)
                }
                
                app.alfredServer?.getServerUrl() ?: "Server failed to start"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start server", e)
                "Error: ${e.message}"
            }
        }
        
        @JavascriptInterface
        fun stopServer() {
            Log.d(TAG, "JS -> stopServer")
            stopService(Intent(this@MainActivity, AlfredServerService::class.java))
        }
        
        @JavascriptInterface
        fun isServerRunning(): Boolean {
            return app.alfredServer?.isAlive == true
        }
        
        @JavascriptInterface
        fun getServerUrl(): String {
            return app.alfredServer?.getServerUrl() ?: ""
        }
        
        private fun callJsCallback(callbackId: String, event: String, data: String) {
            val escapedData = data.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "")
            
            binding.webView.evaluateJavascript(
                "window.nativeCallback && window.nativeCallback('$callbackId', '$event', '$escapedData')",
                null
            )
        }
    }
}
