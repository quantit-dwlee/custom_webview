package com.polymorph.custom_webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


private lateinit var webView: WebView
private lateinit var  cookieManager: CookieManager
private var isFirst = true
private var cookies: String? = null

/** CustomWebviewPlugin */
class CustomWebviewPlugin: FlutterPlugin{
  private val viewType = "FlutterCustomWebView"
  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    flutterPluginBinding
      .platformViewRegistry
      .registerViewFactory(viewType, NativeWebViewFactory())
  }
  override fun onDetachedFromEngine( binding: FlutterPlugin.FlutterPluginBinding) {
  }
}



@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private class NativeWebView(context: Context, creationParams: Map<String?, Any?>?): PlatformView {


  override fun getView(): View {
    return webView
  }

  override fun dispose() {
  }

  init {
    val header: Map<String, String>? = creationParams?.get("header") as Map<String, String>?
    val body: Map<String, String>? = creationParams?.get("body") as Map<String, String>?
    val url: String = creationParams?.get("url") as String

    webView = WebView(context)
    this.setWebViewClient(header, body)
//    webView.addJavascriptInterface(WebAppInterface(context), "OllyWebView")
    //webView.loadUrl("https://dbaasviewapi.kbsec.com/go.able?linkcd=p010101")
    this.setCookieManager()


    webView.loadUrl(url)
  }

  /**
   * webview setting
   */
  @SuppressLint("SetJavaScriptEnabled")
  private fun setWebViewClient(
    header: Map<String, String>?,
    body: Map<String, String>?
  ) {
    webView.settings.javaScriptEnabled = true
    webView.settings.javaScriptCanOpenWindowsAutomatically = true
    webView.settings.loadsImagesAutomatically = true
    webView.settings.useWideViewPort = true
    webView.settings.setSupportZoom(true)
    webView.settings.domStorageEnabled = true
    webView.settings.allowFileAccess = true
    webView.webViewClient = MyWebViewClient(header, body)
    webView.webChromeClient = WebChromeClient()
  }

  /**
   * Cookie manager setting
   */
  private fun setCookieManager() {
    cookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
      cookieManager.setAcceptThirdPartyCookies(webView, true)
      }
    cookieManager.removeAllCookies(null)
    cookieManager.flush()
  }

  /**
   * Webview client setting
   */
  inner class MyWebViewClient(header: Map<String, String>?, body: Map<String, String>?) : WebViewClient() {

    private var header: Map<String, String>? = null
    private var body: Map<String, String>? = null
    init {
      this.header = header
      this.body = body
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(
      view: WebView?,
      request: WebResourceRequest
    ): WebResourceResponse? {
      // Create OkHttpClient instance
      val client = OkHttpClient()

      // Create request with the intercepted url, passing through original headers
      val okhttpRequest: Request = Request.Builder()
        .url(request.url.toString())
        .headers(request.requestHeaders.toHeaders())
        .build()
      return try {
        // Execute request
        val response = client.newCall(okhttpRequest).execute()

        // Create a new WebResourceResponse using the okhttp response body and headers
        // This is a simplified handling that may not cover cases with charsets, chunked encoding etc.
        WebResourceResponse(
          null, null,
          response.body!!.byteStream()
        )
      } catch (e: IOException) {
        // You might want to handle exceptions better, this is just an example
        super.shouldInterceptRequest(view, request)
      }
    }
    private fun getNewResponse(url: String): WebResourceResponse? {
      return try {
        val client = OkHttpClient()
        val bodyData = FormBody.Builder().apply {
          body?.forEach { (key, value) ->
            add(key, value)
            Log.d(key,value)
          }
        }.build()

        // -- request header setting
        val request = Request.Builder().apply {
          url(url.trim { it <= ' ' })
          header?.forEach { (key, value) ->
            addHeader(key, value)
            Log.d(key,value)
          }
          get()
        }.build()

        // -- response header setting
        val response = client.newCall(request).execute()
        response.header("content-type", response.body!!.contentType()!!.type)

        if (cookies == null && response.headers["Set-Cookie"] != null) {
          cookies = response.headers["Set-Cookie"]
          cookieManager.setCookie(url, cookies)
        }

        return WebResourceResponse(
          null,
          null,
          response.body!!.byteStream()
        )
      } catch (e: Exception) {
        null
      }
    }

    override fun onReceivedError(
      view: WebView,
      errorCode: Int,
      description: String,
      failingUrl: String
    ) {
      Log.d("CustomWebView", description)
    }
  }
}


class NativeWebViewFactory: PlatformViewFactory(StandardMessageCodec.INSTANCE) {
  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
    val creationParams = args as Map<String?, Any?>?
    return NativeWebView(context, creationParams)
  }
}

class WebAppInterface(private val context: Context) {

  /** Show a toast from the web page  */
  @JavascriptInterface
  fun showToast(toast: String) {
//        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
  }

  /** Close the WebView from the web page  */
  @JavascriptInterface
  fun closeWebView() {
//        (context as Activity).finish()
  }
}
