package com.teamforce.thanksapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.teamforce.thanksapp.domain.interactors.NotificationsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val interactor: NotificationsInteractor,
) : ViewModel() {
    private val coroutineScope =
        CoroutineScope(CoroutineExceptionHandler { _, exception ->
            Timber.e(exception)
        } + Dispatchers.IO)


    fun getNotifications() = interactor.getNotifications()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        ).cachedIn(coroutineScope)
}