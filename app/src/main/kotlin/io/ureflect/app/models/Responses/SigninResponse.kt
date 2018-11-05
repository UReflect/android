package io.ureflect.app.models.Responses

import io.ureflect.app.models.User

class SigninResponse {
    lateinit var token: String
    lateinit var user: User
}
