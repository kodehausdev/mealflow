package com.fatokifury.meal_flow.ui.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONTokener
import kotlin.coroutines.resume

/**
 * Downloads an image with a real browser engine by opening it as a top-level page, then
 * redrawing it onto a canvas and exporting JPEG bytes. Because the image is the page itself,
 * the canvas is same-origin and not tainted, so toDataURL works.
 */
class WebViewImageFetcher(private val context: Context) {

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun fetchAsJpeg(
        imageUrl: String,
        referer: String,
        maxWidth: Int = 1200,
        timeoutMs: Long = 20_000L
    ): ByteArray? = withContext(Dispatchers.Main) {
        val webView = WebView(context.applicationContext)
        try {
            webView.settings.javaScriptEnabled = true
            val finished = CompletableDeferred<Unit>()
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    finished.complete(Unit)
                }
            }

            webView.loadUrl(imageUrl, mapOf("Referer" to referer))
            withTimeout(timeoutMs) { finished.await() }

            val script = """
                (function() {
                  var i = document.images[0];
                  if (!i || !i.naturalWidth) return '';
                  var s = Math.min(1, $maxWidth / i.naturalWidth);
                  var c = document.createElement('canvas');
                  c.width = Math.round(i.naturalWidth * s);
                  c.height = Math.round(i.naturalHeight * s);
                  var x = c.getContext('2d');
                  x.fillStyle = '#ffffff';
                  x.fillRect(0, 0, c.width, c.height);
                  x.drawImage(i, 0, 0, c.width, c.height);
                  return c.toDataURL('image/jpeg', 0.85).split(',')[1];
                })()
            """.trimIndent()

            // A blocked page (403) has no <img>, so this returns '' and we give up cleanly.
            var base64 = ""
            repeat(5) {
                if (base64.isEmpty()) {
                    base64 = webView.evalString(script)
                    if (base64.isEmpty()) delay(300)
                }
            }

            if (base64.isEmpty()) null else Base64.decode(base64, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.w("WebViewImageFetcher", "Image download failed: ${e.message}")
            null
        } finally {
            webView.stopLoading()
            webView.destroy()
        }
    }

    private suspend fun WebView.evalString(js: String): String =
        suspendCancellableCoroutine { cont ->
            evaluateJavascript(js) { raw ->
                val value = try {
                    (JSONTokener(raw ?: "null").nextValue() as? String).orEmpty()
                } catch (e: Exception) {
                    ""
                }
                if (cont.isActive) cont.resume(value)
            }
        }
}