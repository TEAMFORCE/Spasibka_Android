package com.teamforce.thanksapp.data.entities.challenges

data class DebitAccountEntity(
    val account_type: String,
    val amount: Int,
    val id: Int,
    val organization_name: String,
    val owner_id: Int
)