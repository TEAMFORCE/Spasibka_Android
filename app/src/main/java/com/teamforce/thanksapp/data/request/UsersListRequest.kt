package com.teamforce.thanksapp.data.request

data class UsersListRequest(
    val data: String,
    val limit: Int = 20,
    val offset: Int = 1
)

