package io.ureflect.app.models.Responses

import io.ureflect.app.models.UserModel

class SigninResponse {
    lateinit var token: String
    lateinit var user: UserModel
}
