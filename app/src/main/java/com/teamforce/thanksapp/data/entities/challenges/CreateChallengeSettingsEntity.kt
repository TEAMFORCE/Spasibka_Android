package com.teamforce.thanksapp.data.entities.challenges

data class CreateChallengeSettingsEntity(
    val accounts: AccountsEntity,
    val multiple_reports: String,
    val show_contenders: String,
    val types: TypesChallengeEntity
)