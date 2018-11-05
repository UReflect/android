package io.ureflect.app.services

import android.app.Application
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ureflect.app.models.GsonRequest
import io.ureflect.app.models.MirrorModel
import io.ureflect.app.models.Responses.ApiErrorResponse
import io.ureflect.app.models.Responses.ApiResponse
import io.ureflect.app.models.Responses.SigninResponse
import io.ureflect.app.models.Responses.SimpleApiResponse
import io.ureflect.app.utils.TOKEN
import io.ureflect.app.utils.fromStorage

fun VolleyError.errMsg(fallback: String): String {
    networkResponse?.let {
        val errorResponse = Gson().fromJson(String(networkResponse.data), ApiErrorResponse::class.java)
        errorResponse.error?.let { error ->
            return error
        }
    }
    message?.let { error ->
        return error
    }
    return fallback
}

object Api {
    private const val host = "http://api.dev.ureflect.io"
//        private const val host = "http://localhost:8000/api"

    init {
        println("Api service initialized")
    }

    fun log(message: String = ""): Boolean {
        println("Message from API Log $message")
        return message.isNotEmpty()
    }

    private inline fun <reified T> genericType() = object : TypeToken<T>() {}.type

    object Misc {
        private const val ping = "/ping"

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
    }

    object Auth {
        private const val signin = "/v1/signin"
        private const val signup = "/v1/signup"

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
    }

    object Mirror {
        private const val mirrors = "/v1/mirror"
        private const val join = "/v1/mirror/join"
        private const val update = "/v1/mirror"

        /**
         * Needs auth token
         */
        fun all(app: Application, callback: Response.Listener<ApiResponse<ArrayList<MirrorModel>>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ArrayList<MirrorModel>>> =
                GsonRequest(
                        Request.Method.GET,
                        host + mirrors,
                        Object(),
                        genericType<ApiResponse<ArrayList<MirrorModel>>>(),
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
        fun join(app: Application, data: Any, callback: Response.Listener<ApiResponse<MirrorModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<MirrorModel>> =
                GsonRequest(
                        Request.Method.POST,
                        host + join,
                        data,
                        genericType<ApiResponse<MirrorModel>>(),
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
        fun update(app: Application, mirrorId: String, data: Any, callback: Response.Listener<ApiResponse<MirrorModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<MirrorModel>> =
                GsonRequest(
                        Request.Method.POST,
                        "$host$update/$mirrorId",
                        data,
                        genericType<ApiResponse<MirrorModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )
    }
}
