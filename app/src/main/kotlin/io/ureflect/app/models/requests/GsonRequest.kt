package io.ureflect.app.models.requests

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.UnsupportedEncodingException
import java.lang.reflect.Type
import java.nio.charset.Charset

/**
 * Make a request and return a parsed object from JSON.
 *
 * @param method Method of the http request
 * @param url URL of the request to make
 * @param data The parameters to send in the body
 * @param type Relevant class object, for Gson's reflection
 * @param headers Map of request headers
 * @param listener Callback for success
 * @param errorListener Callback for error
 */
open class GsonRequest<T>(method: Int,
                          url: String,
                          private val data: Any,
                          private val type: Type,
                          private val headers: MutableMap<String, String>?,
                          private val listener: Response.Listener<T>,
                          errorListener: Response.ErrorListener) : Request<T>(method, url, errorListener) {
    private val gson = Gson()

    override fun getHeaders(): MutableMap<String, String> = headers ?: super.getHeaders()

    override fun deliverResponse(response: T) = listener.onResponse(response)

    override fun getBody(): ByteArray = gson.toJson(data).toByteArray()

    override fun parseNetworkResponse(response: NetworkResponse?): Response<T> {
        return try {
            val json = String(response?.data
                    ?: ByteArray(0), Charset.forName(HttpHeaderParser.parseCharset(response?.headers)))
            Response.success(gson.fromJson(json, type), HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        } catch (e: JsonSyntaxException) {
            Response.error(ParseError(e))
        }
    }
}