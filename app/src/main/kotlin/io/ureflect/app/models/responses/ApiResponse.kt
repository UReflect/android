package io.ureflect.app.models.responses

class ApiResponse<T> {
    var message: String? = null
    var data: T? = null
}
