package com.teamforce.thanksapp.domain.models.recommendations


data class RecommendModel(
    val id: Long,
    val name: String,
    val header: String,
    val type: RecommendObjectType,
    val photo: String?,
    val isNew: Boolean,
    val marketplaceId: Int?
){
    enum class RecommendObjectType{
        QUESTIONNAIRE, CHAIN, CHALLENGE, OFFER;

        companion object {
            fun safetyValueOf(value: String): RecommendObjectType? {
                return try {
                    RecommendObjectType.valueOf(value)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}
