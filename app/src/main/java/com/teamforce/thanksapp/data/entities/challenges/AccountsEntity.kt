package com.teamforce.thanksapp.data.entities.challenges

data class AccountsEntity(
    val organization_accounts: List<DebitAccountEntity>,
    val personal_accounts: List<DebitAccountEntity>
)