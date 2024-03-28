package com.teamforce.thanksapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.teamforce.thanksapp.data.api.ThanksApi
import com.teamforce.thanksapp.data.request.UserListWithoutInputRequest
import com.teamforce.thanksapp.data.response.UserListItem.UserBean
import com.teamforce.thanksapp.data.response.UserListItem
import com.teamforce.thanksapp.data.sources.newTransaction.UsersPagingSource
import com.teamforce.thanksapp.domain.mappers.proflle.ProfileMapper
import com.teamforce.thanksapp.domain.repositories.UsersRepository
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.ResultWrapper
import com.teamforce.thanksapp.utils.mapWrapperData
import com.teamforce.thanksapp.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val thanksApi: ThanksApi,
    private val profileMapper: ProfileMapper
): UsersRepository {
    override suspend fun getUsersWithoutPaging(): ResultWrapper<List<UserListItem>>{
        return safeApiCall(Dispatchers.IO){
            thanksApi.getUsersWithoutInput(
                UserListWithoutInputRequest()
            )
        }.mapWrapperData {
            val result = mutableListOf<UserListItem>()
            result.add(0, UserListItem.NewTransactionBtn)
            result.addAll(it)
            result
        }
    }

    override fun getUsersWithoutInput(): Flow<PagingData<UserBean>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                UsersPagingSource(
                    api = thanksApi,
                )
            }
        ).flow
    }

    override fun getUsersWithInput(input: String): Flow<PagingData<UserBean>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = Consts.PAGE_SIZE,
                prefetchDistance = 1,
                pageSize = Consts.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                UsersPagingSource(
                    api = thanksApi,
                    input = input
                )
            }
        ).flow
    }

    override suspend fun getUserBean(userId: Int): ResultWrapper<UserBean> {
        return safeApiCall(Dispatchers.IO){
            thanksApi.getAnotherProfile(userId)
        }.mapWrapperData { profileEntity ->  profileMapper.mapUserProfileEntityToProfileBean(profileEntity)}

    }

}