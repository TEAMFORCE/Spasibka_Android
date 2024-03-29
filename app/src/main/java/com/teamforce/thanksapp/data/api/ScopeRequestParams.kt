package com.teamforce.thanksapp.data.api

enum class ScopeRequestParams(val id: Int) {
    TEMPLATES(0),
    ORGANIZATION(1),
    COMMON(2);

    companion object {
        fun valueOf(value: Int) = ScopeRequestParams.values().first { it.id == value }
    }
}