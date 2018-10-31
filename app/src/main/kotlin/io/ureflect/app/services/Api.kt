package io.ureflect.app.services

import android.app.Application
import com.android.volley.Request
import com.android.volley.Response
import com.google.gson.reflect.TypeToken
import io.ureflect.app.models.*
import io.ureflect.app.models.Responses.ApiResponse
import io.ureflect.app.models.Responses.SigninResponse
import io.ureflect.app.models.Responses.SimpleApiResponse
import io.ureflect.app.utils.TOKEN
import io.ureflect.app.utils.fromStorage
import kotlin.collections.ArrayList

object Api {
    private const val host = "http://api.dev.ureflect.io"
    //    private const val host = "http://localhost:8000/api"
    private const val ping = "/ping"
    private const val signin = "/v1/signin"
    private const val signup = "/v1/signup"

    private const val mirrors = "/v1/mirror"
    private const val join = "/v1/mirror/join"

    init {
        println("Api service initialized")
    }

    fun log(message: String = ""): Boolean {
        println("Message from API Log $message")
        return message.isNotEmpty()
    }

    private inline fun <reified T> genericType() = object : TypeToken<T>() {}.type

    /**
     *
     */
    fun ping(callback: Response.Listener<SimpleApiResponse>, error: Response.ErrorListener):
            GsonRequest<SimpleApiResponse> =
            GsonRequest(
                    Request.Method.POST,
                    host + ping,
                    Object(),
                    SimpleApiResponse::class.java,
                    null,
                    callback,
                    error
            )

    /**
     * data:
     * email: String
     * password: String
     */
    fun signin(data: Any, callback: Response.Listener<ApiResponse<SigninResponse>>, error: Response.ErrorListener):
            GsonRequest<ApiResponse<SigninResponse>> =
            GsonRequest(
                    Request.Method.POST,
                    host + signin,
                    data,
                    genericType<ApiResponse<SigninResponse>>(),
                    null,
                    callback,
                    error
            )

    /**
     * data:
     * email: String
     * password: String
     * name: String
     */
    fun signup(data: Any, callback: Response.Listener<ApiResponse<SigninResponse>>, error: Response.ErrorListener):
            GsonRequest<ApiResponse<SigninResponse>> =
            GsonRequest(Request.Method.POST,
                    host + signup,
                    data,
                    genericType<ApiResponse<SigninResponse>>(),
                    null,
                    callback,
                    error
            )

    /**
     * Needs auth token
     */
    fun mirrors(app: Application, callback: Response.Listener<ApiResponse<ArrayList<Mirror>>>, error: Response.ErrorListener):
            GsonRequest<ApiResponse<ArrayList<Mirror>>> =
            GsonRequest(
                    Request.Method.GET,
                    host + mirrors,
                    Object(),
                    genericType<ApiResponse<ArrayList<Mirror>>>(),
                    mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                    callback,
                    error
            )

    /**
     * data:
     * code: String
     *
     * Needs auth token
     */
    fun join(app: Application, data: Any, callback: Response.Listener<ApiResponse<Mirror>>, error: Response.ErrorListener):
            GsonRequest<ApiResponse<Mirror>> =
            GsonRequest(
                    Request.Method.POST,
                    host + join,
                    data,
                    genericType<ApiResponse<Mirror>>(),
                    mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                    callback,
                    error
            )
}
