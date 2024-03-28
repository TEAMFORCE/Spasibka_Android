package com.teamforce.thanksapp.domain.interactors

import androidx.annotation.StringRes
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.teamforce.thanksapp.NotificationsRepository
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.domain.mappers.notifications.NotificationMapper
import com.teamforce.thanksapp.domain.models.notifications.NotificationItem
import com.teamforce.thanksapp.domain.models.notifications.ResourceWrapper
import com.teamforce.thanksapp.utils.addLeadZero
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject


class NotificationsInteractor @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val notificationMapper: NotificationMapper,
) {
    fun getNotifications(): Flow<PagingData<NotificationItem>> =
        notificationsRepository.getNotifications()
            .map { pagingData -> pagingData.map(notificationMapper::map) }
            .map(this::insertSeparators)

    private fun insertSeparators(pagingData: PagingData<NotificationItem.NotificationModel>): PagingData<NotificationItem> {
        return pagingData.insertSeparators { before, after ->
            when {
                after == null -> null
                before == null ->
                    getDateTimeLabel(after.toLocalDate())
                else -> {
                    val afterTime: LocalDateTime = after.toLocalDate()
                    if (before.toLocalDate().dayOfMonth != afterTime.dayOfMonth) {
                        getDateTimeLabel(afterTime)
                    } else {
                        null
                    }
                }
            }
        }
    }

    @StringRes
    private fun getMonth(dateTime: LocalDateTime): Int =
        when (dateTime.month.value) {
            1 -> (R.string.januaryDate)
            2 -> (R.string.februaryDate)
            3 -> (R.string.marchDate)
            4 -> (R.string.aprilDate)
            5 -> (R.string.mayDate)
            6 -> (R.string.juneDate)
            7 -> (R.string.julyDate)
            8 -> (R.string.augustDate)
            9 -> (R.string.septemberDate)
            10 -> (R.string.octoberDate)
            11 -> (R.string.novemberDate)
            12 -> (R.string.decemberDate)
            else -> R.string.empty
        }


    private fun getDateTimeLabel(
        dateTime: LocalDateTime,
    ): NotificationItem.DateTimeSeparator {
        val getMonthRes = getMonth(dateTime)
        val result = dateTime.toLocalDate()
        val today: LocalDate = LocalDate.now()
        val yesterday = today.minusDays(1)

        val dateTitle = NotificationItem.DateTimeSeparator.DateTitle(
            dayText = ResourceWrapper.Text.Strings("${dateTime.dayOfMonth.addLeadZero()}.${dateTime.monthValue.addLeadZero()}.${dateTime.year}"),
            monthText = ResourceWrapper.Text.Resource(getMonthRes)
        )

        return when {
            result.isEqual(today) -> {
                NotificationItem.DateTimeSeparator(date = ResourceWrapper.Text.Resource(R.string.today))
            }

            result.isEqual(yesterday) -> {
                NotificationItem.DateTimeSeparator(date = ResourceWrapper.Text.Resource(R.string.yesterday))
            }

            else -> NotificationItem.DateTimeSeparator(dateTitle = dateTitle)

        }

    }
}

private fun NotificationItem.NotificationModel.toLocalDate() =
    LocalDateTime.parse(this.createdAt.removeZoneSign())

private fun String.removeZoneSign(): String = replace(
    "Z",
    ""
)

