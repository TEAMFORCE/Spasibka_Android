package com.teamforce.thanksapp.domain.repositories

import androidx.paging.PagingData
import com.teamforce.thanksapp.data.response.UserListItem
import com.teamforce.thanksapp.data.response.UserListItem.UserBean
import com.teamforce.thanksapp.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface UsersRepository {

    suspend fun getUsersWithoutPaging(
    ): ResultWrapper<List<UserListItem>>

    fun getUsersWithoutInput(
    ): Flow<PagingData<UserBean>>

    fun getUsersWithInput(
        input: String
    ): Flow<PagingData<UserBean>>

    suspend fun getUserBean(userId: Int): ResultWrapper<UserBean>
}