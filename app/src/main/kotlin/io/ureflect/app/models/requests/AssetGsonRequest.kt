package io.ureflect.app.models.requests

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request.Method.GET
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.lang.reflect.Type

/**
 * Mock Request that returns the content of a json file stored in assets
 */
class AssetGsonRequest<T>(method: Int,
                          url: String,
                          private val asset: Any,
                          type: Type,
                          headers: MutableMap<String, String>?,
                          listener: Response.Listener<T>,
                          errorListener: Response.ErrorListener) : GsonRequest<T>(GET, url, Unit, type, headers, listener, errorListener) {
    private val gson = Gson()

    override fun parseNetworkResponse(response: NetworkResponse?): Response<T> {
        return try {
            val json = (asset as InputStream).bufferedReader().use {
                it.readText()
            }
            Response.success(gson.fromJson(json, type), HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        } catch (e: JsonSyntaxException) {
            Response.error(ParseError(e))
        }
    }
}