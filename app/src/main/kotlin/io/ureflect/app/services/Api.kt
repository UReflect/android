package io.ureflect.app.services

import android.app.Application
import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ureflect.app.R
import io.ureflect.app.models.*
import io.ureflect.app.models.requests.AssetGsonRequest
import io.ureflect.app.models.requests.GsonRequest
import io.ureflect.app.models.requests.MultipartGsonRequest
import io.ureflect.app.models.responses.ApiErrorResponse
import io.ureflect.app.models.responses.ApiResponse
import io.ureflect.app.models.responses.SigninResponse
import io.ureflect.app.models.responses.SimpleApiResponse
import io.ureflect.app.utils.TOKEN
import io.ureflect.app.utils.fromStorage
import java.io.InputStream
import java.lang.reflect.Type
import java.net.UnknownHostException

fun VolleyError.expired(): Boolean = networkResponse?.statusCode == 401 && errMsg().contains("Token expired")

fun VolleyError.errMsg(context: Context? = null, fallback: String = ""): String {
    try {
//        this.cause
        if (context != null) {
            if (this.cause is UnknownHostException) {
                return context.getString(R.string.internet_error)
            }
        }
        networkResponse?.let {
            val errorResponse = Gson().fromJson(String(networkResponse.data), ApiErrorResponse::class.java)
            errorResponse.error?.let { error ->
                return error
            }
        }
        message?.let { error ->
            return error
        }
    } catch (e: Exception) {
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

    inline fun <reified T> genericType(): Type = object : TypeToken<T>() {}.type

    object Misc {
        private const val ping = "/ping"
        private const val payments = "payments"

        /**
         * Useless
         */
        fun ping(callback: Response.Listener<SimpleApiResponse>, error: Response.ErrorListener):
                GsonRequest<SimpleApiResponse> =
                GsonRequest(
                        Request.Method.POST,
                        host + ping,
                        Unit,
                        SimpleApiResponse::class.java,
                        null,
                        callback,
                        error
                )

        /**
         * Useless
         */
        fun connectedDevices(app: Application, data: InputStream, callback: Response.Listener<ApiResponse<ArrayList<ConnectedDeviceModel>>>, error: Response.ErrorListener):
                AssetGsonRequest<ApiResponse<ArrayList<ConnectedDeviceModel>>> =
                AssetGsonRequest(
                        Request.Method.GET,
                        host + ping,
                        data,
                        genericType<ApiResponse<ArrayList<ConnectedDeviceModel>>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * Needs auth token
         */
        fun payments(app: Application, callback: Response.Listener<ApiResponse<ArrayList<CreditCardModel>>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ArrayList<CreditCardModel>>> =
                GsonRequest(
                        Request.Method.GET,
                        "$host/v1/$payments",
                        Unit,
                        genericType<ApiResponse<ArrayList<CreditCardModel>>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * Needs auth token
         */
        fun createPayment(app: Application, data: Any, callback: Response.Listener<ApiResponse<CreditCardModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<CreditCardModel>> =
                GsonRequest(
                        Request.Method.POST,
                        "$host/v1/$payments",
                        data,
                        genericType<ApiResponse<CreditCardModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
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

    object User {
        private const val url = "/v1/user"

        /**
         * data:
         * email: String
         * password: String
         * ...
         *
         * Needs auth token
         */
        fun update(app: Application, userId: Long, data: Any, callback: Response.Listener<ApiResponse<UserModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<UserModel>> =
                GsonRequest(
                        Request.Method.PUT,
                        "$host$url/$userId",
                        data,
                        genericType<ApiResponse<UserModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )
    }

    object Mirror {
        private const val url = "/v1/mirror"
        private const val join = "join"
        private const val unjoin = "unjoin"
        private const val linkProfile = "profile"
        private const val allProfile = "profiles"

        /**
         * Needs auth token
         */
        fun all(app: Application, callback: Response.Listener<ApiResponse<ArrayList<MirrorModel>>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ArrayList<MirrorModel>>> =
                GsonRequest(
                        Request.Method.GET,
                        host + url,
                        Unit,
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
                        "$host$url/$join",
                        data,
                        genericType<ApiResponse<MirrorModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * Needs auth token
         */
        fun unjoin(app: Application, mirrorId: Long, callback: Response.Listener<ApiResponse<MirrorModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<MirrorModel>> =
                GsonRequest(
                        Request.Method.POST,
                        "$host$url/$mirrorId/$unjoin",
                        Unit,
                        genericType<ApiResponse<MirrorModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * data:
         * name: String
         * location: String
         * timezone: String
         * ...
         *
         * Needs auth token
         */
        fun update(app: Application, mirrorId: Long, data: Any, callback: Response.Listener<ApiResponse<MirrorModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<MirrorModel>> =
                GsonRequest(
                        Request.Method.PUT,
                        "$host$url/$mirrorId",
                        data,
                        genericType<ApiResponse<MirrorModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * data:
         * profile_id: ID of the profile to link
         *
         * Needs auth token
         */
        fun linkProfile(app: Application, mirrorId: Long, data: Any, callback: Response.Listener<ApiResponse<ProfileModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ProfileModel>> =
                GsonRequest(
                        Request.Method.POST,
                        "$host$url" + "s" + "/$mirrorId/$linkProfile",
                        data,
                        genericType<ApiResponse<ProfileModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * Needs auth token
         */
        fun profiles(app: Application, mirrorId: Long, callback: Response.Listener<ApiResponse<ArrayList<ProfileModel>>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ArrayList<ProfileModel>>> =
                GsonRequest(
                        Request.Method.GET,
                        "$host$url/$mirrorId/$allProfile",
                        Unit,
                        genericType<ApiResponse<ArrayList<ProfileModel>>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )
    }

    object Profile {
        private const val url = "/v1/profile"
        private const val face = "face"
        private const val pin = "pin"
        private const val verify = "verify"

        /**
         * Needs auth token
         */
        fun mine(app: Application, callback: Response.Listener<ApiResponse<ArrayList<ProfileModel>>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ArrayList<ProfileModel>>> =
                GsonRequest(
                        Request.Method.GET,
                        host + url,
                        Unit,
                        genericType<ApiResponse<ArrayList<ProfileModel>>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * Needs auth token
         */
        fun one(app: Application, profileId: Long, callback: Response.Listener<ApiResponse<ProfileModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ProfileModel>> =
                GsonRequest(
                        Request.Method.GET,
                        "$host$url/$profileId",
                        Unit,
                        genericType<ApiResponse<ProfileModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * data:
         * title: String
         * content: String
         *
         * Needs auth token
         */
        fun create(app: Application, data: Any, callback: Response.Listener<ApiResponse<ProfileModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ProfileModel>> =
                GsonRequest(
                        Request.Method.POST,
                        host + url,
                        data,
                        genericType<ApiResponse<ProfileModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * data:
         * title: String
         * content: String
         *
         * Needs auth token
         */
        fun update(app: Application, profileId: Long, data: Any, callback: Response.Listener<ApiResponse<ProfileModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ProfileModel>> =
                GsonRequest(
                        Request.Method.PUT,
                        "$host$url/$profileId",
                        data,
                        genericType<ApiResponse<ProfileModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * Needs auth token
         */
        fun delete(app: Application, profileId: Long, callback: Response.Listener<SimpleApiResponse>, error: Response.ErrorListener):
                GsonRequest<SimpleApiResponse> =
                GsonRequest(
                        Request.Method.DELETE,
                        "$host$url/$profileId",
                        Unit,
                        genericType<SimpleApiResponse>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * Needs auth token
         */
        fun setupFaces(app: Application, profileId: Long, fileParts: List<String>, callback: Response.Listener<ApiResponse<ProfileModel>>, error: Response.ErrorListener):
                MultipartGsonRequest<ApiResponse<ProfileModel>> =
                MultipartGsonRequest(
                        Request.Method.POST,
                        "$host$url/$profileId/$face",
                        fileParts,
                        genericType<ApiResponse<ProfileModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * data:
         * pin: String
         *
         * Needs auth token
         */
        fun setupPin(app: Application, profileId: Long, data: Any, callback: Response.Listener<ApiResponse<ProfileModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ProfileModel>> =
                GsonRequest(
                        Request.Method.POST,
                        "$host$url/$profileId/$pin",
                        data,
                        genericType<ApiResponse<ProfileModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )

        /**
         * data:
         * pin: String
         *
         * Needs auth token
         */
        fun verifyPin(app: Application, profileId: Long, data: Any, callback: Response.Listener<ApiResponse<ProfileModel>>, error: Response.ErrorListener):
                GsonRequest<ApiResponse<ProfileModel>> =
                GsonRequest(
                        Request.Method.POST,
                        "$host$url/$profileId/$pin/$verify",
                        data,
                        genericType<ApiResponse<ProfileModel>>(),
                        mutableMapOf("x-access-token" to String.fromStorage(app, TOKEN)),
                        callback,
                        error
                )
    }
}
