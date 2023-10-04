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
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request


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
  private lateinit var webView: WebView
  private lateinit var  cookieManager: CookieManager
  private var isFirst = true
  private var cookies: String? = null

  override fun getView(): View {
    return webView
  }

  override fun dispose() {
  }

  init {
    val header: Map<String, String>? = creationParams?.get("header") as Map<String, String>?
    val body: Map<String, String>? = creationParams?.get("body") as Map<String, String>?
    val url: String = creationParams?.get("url") as String
    val method: String = creationParams?.get("method") as String

    webView = WebView(context)
    this.setWebViewClient(header, body, method)
//    webView.addJavascriptInterface(WebAppInterface(context), "OllyWebView")
    this.setCookieManager()
    webView.loadUrl(url)
  }

  /**
   * webview setting
   */
  @SuppressLint("SetJavaScriptEnabled")
  private fun setWebViewClient(
    header: Map<String, String>?,
    body: Map<String, String>?,
    method: String
  ) {
    webView.settings.javaScriptEnabled = true
    webView.settings.javaScriptCanOpenWindowsAutomatically = true
    webView.settings.loadsImagesAutomatically = true
    webView.settings.useWideViewPort = true
    webView.settings.setSupportZoom(true)
    webView.settings.domStorageEnabled = true
    webView.settings.allowFileAccess = true
    webView.webViewClient = MyWebViewClient(header, body, method)
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
  inner class MyWebViewClient(header: Map<String, String>?, body: Map<String, String>?, method: String) : WebViewClient() {

    private var header: Map<String, String>? = null
    private var body: Map<String, String>? = null
    private var method: String
    init {
      this.header = header
      this.body = body
      this.method = method
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(
      view: WebView?,
      request: WebResourceRequest
    ): WebResourceResponse? {
      return if (isFirst) {
        isFirst = false
        val url = request.url.toString()
        getNewResponse(url)
      } else {
        super.shouldInterceptRequest(view, request)
      }
    }
    private fun getNewResponse(url: String): WebResourceResponse? {
      return try {
        val client = OkHttpClient()
        var request: Request

        //
        // GET
        //
        if(method == "get"){
          val parseUrl = url.split("://")
          val call = HttpUrl.Builder()
            .scheme(parseUrl[0])
            .host(parseUrl[1])
          body?.forEach { (key, value) ->
            call.addQueryParameter(key, value)
            Log.d(key,value)
          }
          request = Request.Builder()
            .url(url)
            .build()
        }
        //
        // POST
        //
        else {
          val bodyData = FormBody.Builder().apply {
            body?.forEach { (key, value) ->
              add(key, value)
              Log.d(key,value)
            }
          }.build()

          // -- request header setting
          request = Request.Builder().apply {
            url(url.trim { it <= ' ' })
            header?.forEach { (key, value) ->
              addHeader(key, value)
              Log.d(key,value)
            }
            post(bodyData)
          }.build()
        }

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
