package io.ureflect.app.models.requests

import com.android.volley.Response
import com.android.volley.VolleyLog
import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.reflect.Type

/**
 * Make a request with a file and return a parsed object from JSON.
 *
 * @param method Method of the http request
 * @param url URL of the request to make
 * @param filePart File to send
 * @param stringPart: Data to send
 * @param type Relevant class object, for Gson's reflection
 * @param headers Map of request headers
 * @param listener Callback for success
 * @param errorListener Callback for error
 */
class MultipartGsonRequest<T>(method: Int,
                              url: String,
                              filePart: File,
                              stringPart: String,
                              type: Type,
                              headers: MutableMap<String, String>?,
                              listener: Response.Listener<T>,
                              errorListener: Response.ErrorListener) : GsonRequest<T>(method, url, Unit, type, headers, listener, errorListener) {
    private lateinit var entity: HttpEntity

    init {
        try {
            val builder = MultipartEntityBuilder.create()
            builder.addPart("file", FileBody(filePart))
            builder.addPart("text", StringBody(stringPart, ContentType.APPLICATION_JSON))
            entity = builder.build()
        } catch (e: UnsupportedEncodingException) {
            VolleyLog.e("UnsupportedEncodingException")
        }
    }

    override fun getBodyContentType(): String {
        return entity.contentType.value
    }

    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        try {
            entity.writeTo(bos)
        } catch (e: IOException) {
        }
        return bos.toByteArray()
    }
}