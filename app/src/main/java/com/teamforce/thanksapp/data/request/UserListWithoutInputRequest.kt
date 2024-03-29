package com.teamforce.thanksapp.data.request

data class UserListWithoutInputRequest(
    val get_users: String = "true",
    val limit: Int = 20,
    val offset: Int = 1
)