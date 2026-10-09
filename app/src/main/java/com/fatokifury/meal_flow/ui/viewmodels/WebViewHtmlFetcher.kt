package com.fatokifury.meal_flow.ui.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONTokener
import kotlin.coroutines.resume

/**
 * Loads a URL in a real (hidden) WebView so bot checks that reject plain HTTP clients
 * can run their JavaScript. Polls the DOM until recipe JSON-LD appears or time runs out.
 */
class WebViewHtmlFetcher(private val context: Context) {

    class FetchFailedException(message: String, val httpStatus: Int? = null) : Exception(message)

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun fetch(url: String, timeoutMs: Long = 25_000L): String =
        withContext(Dispatchers.Main) {          // WebView must be used on the main thread
            val webView = WebView(context.applicationContext)
            var mainFrameStatus: Int? = null
            var mainFrameError: String? = null

            try {
                webView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    blockNetworkImage = true       // faster, we only need the HTML
                    mediaPlaybackRequiresUserGesture = true
                }
                CookieManager.getInstance().setAcceptCookie(true)

                webView.webViewClient = object : WebViewClient() {
                    override fun onReceivedHttpError(
                        view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse
                    ) {
                        // A challenge page may return 403 and then redirect, so only record it.
                        if (request.isForMainFrame) mainFrameStatus = errorResponse.statusCode
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onReceivedError(
                        view: WebView, errorCode: Int, description: String?, failingUrl: String?
                    ) {
                        if (failingUrl == url) mainFrameError = description
                    }
                }

                webView.loadUrl(url)

                var lastHtml = ""
                val found = withTimeoutOrNull(timeoutMs) {
                    while (true) {
                        delay(1_000)
                        val html = webView.currentHtml()
                        if (html.isNotEmpty()) lastHtml = html
                        if (html.contains("ld+json", ignoreCase = true) &&
                            html.contains("Recipe")
                        ) return@withTimeoutOrNull html
                    }
                    @Suppress("UNREACHABLE_CODE") null
                }

                when {
                    found != null -> found
                    // Page loaded but has no recipe data: let the parser report that properly.
                    lastHtml.contains("ld+json", ignoreCase = true) -> lastHtml
                    else -> throw FetchFailedException(
                        mainFrameError ?: "Timed out waiting for the page to load",
                        mainFrameStatus
                    )
                }
            } finally {
                webView.stopLoading()
                webView.destroy()
            }
        }

    /** Returns the current DOM as a String, or "" if unavailable. */
    private suspend fun WebView.currentHtml(): String =
        suspendCancellableCoroutine { cont ->
            evaluateJavascript("(function(){return document.documentElement.outerHTML;})()") { raw ->
                val html = try {
                    // evaluateJavascript returns a JSON-encoded string: decode it
                    (JSONTokener(raw ?: "null").nextValue() as? String).orEmpty()
                } catch (e: Exception) {
                    Log.w("WebViewHtmlFetcher", "Could not decode page HTML: ${e.message}")
                    ""
                }
                if (cont.isActive) cont.resume(html)
            }
        }
}