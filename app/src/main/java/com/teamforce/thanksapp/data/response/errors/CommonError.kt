package com.teamforce.thanksapp.data.response.errors

data class CommonError(
    val detail: String?, // "detail": "Учетные данные не были предоставлены."
    val status: String?, // "status": "data should be an array", "status": -1,
    val errors: String?, // "errors": "problem description"
)
