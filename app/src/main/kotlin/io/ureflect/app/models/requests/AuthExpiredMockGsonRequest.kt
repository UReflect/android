package io.ureflect.app.models.requests

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.google.gson.Gson
import io.ureflect.app.models.responses.ApiErrorResponse
import java.lang.reflect.Type

/**
 * Make a request with a file and return a parsed object from JSON.
 */
class AuthExpiredMockGsonRequest<T>(method: Int,
                                    url: String,
                                    data: Any,
                                    type: Type,
                                    headers: MutableMap<String, String>?,
                                    listener: Response.Listener<T>,
                                    private val error: Response.ErrorListener) : GsonRequest<T>(method, url, Unit, type, headers, listener, error) {


    override fun deliverResponse(response: T) = error.onErrorResponse(VolleyError(NetworkResponse(401, Gson().toJson(ApiErrorResponse().apply { error = "Token expired" }).toByteArray(), false, 0, null)))
}