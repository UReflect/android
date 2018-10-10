package io.ureflect.app.services

import com.android.volley.Request
import com.android.volley.Response
import com.google.gson.reflect.TypeToken
import io.ureflect.app.models.*

object Api {
    private const val host = "http://api.dev.ureflect.io"
    //    private const val host = "http://localhost:8000/api"
    private const val ping = "/ping"
    private const val signin = "/v1/signin"
    private const val signup = "/v1/signup"

    init {
        println("Api service initialized")
    }

    fun log(message: String = ""): Boolean {
        println("Message from API Log $message")
        return message.isNotEmpty()
    }

    inline fun <reified T> genericType() = object : TypeToken<T>() {}.type

    fun ping(callback: Response.Listener<SimpleApiResponse>, error: Response.ErrorListener):
            GsonRequest<SimpleApiResponse> =
            GsonRequest(Request.Method.POST, host + signin, Object(), SimpleApiResponse::class.java, null, callback, error)

    /**
     * data:
     * email: String
     * password: String
     */
    fun signin(data: Any, callback: Response.Listener<ApiResponse<SigninResponse>>, error: Response.ErrorListener):
            GsonRequest<ApiResponse<SigninResponse>> =
            GsonRequest(Request.Method.POST, host + signin, data, genericType<ApiResponse<SigninResponse>>(), null, callback, error)

    /**
     * data:
     * email: String
     * password: String
     * name: String
     */
    fun signup(data: Any, callback: Response.Listener<ApiResponse<SigninResponse>>, error: Response.ErrorListener):
            GsonRequest<ApiResponse<SigninResponse>> =
            GsonRequest(Request.Method.POST, host + signup, data, genericType<ApiResponse<SigninResponse>>(), null, callback, error)
}
