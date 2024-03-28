package com.teamforce.thanksapp.domain.mappers.feed

import android.content.Context
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.entities.events.EventDataEntity
import com.teamforce.thanksapp.data.entities.events.EventFilterEntity
import com.teamforce.thanksapp.data.entities.events.EventTypeEntity
import com.teamforce.thanksapp.data.entities.events.TagEventEntity
import com.teamforce.thanksapp.data.entities.feed.*
import com.teamforce.thanksapp.domain.models.events.EventDataModel
import com.teamforce.thanksapp.domain.models.events.EventFilterModel
import com.teamforce.thanksapp.domain.models.events.EventTypeModel
import com.teamforce.thanksapp.domain.models.events.TagEventModel
import com.teamforce.thanksapp.domain.models.feed.FeedItemByIdModel
import com.teamforce.thanksapp.domain.models.feed.FeedModel
import com.teamforce.thanksapp.domain.models.general.ObjectsToLike
import com.teamforce.thanksapp.utils.UserDataRepository
import com.teamforce.thanksapp.utils.parseDateTimeWithBindToTimeZone
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FeedMapper @Inject constructor(
    private val userDataRepository: UserDataRepository,
    @ApplicationContext private val context: Context,
) {

    fun mapListNewFeed(from: List<EventDataEntity>): List<EventDataModel>{
        return from.map(::mapItemNewFeed)
    }
    private fun mapItemNewFeed(from: EventDataEntity): EventDataModel{
        return EventDataModel(
            id = from.id,
            time = from.time,
            header = from.header,
            icon = from.icon,
            text = from.text ?: "",
            typeOfObject = mapSelector(from.selector),
            userLiked = from.userLiked,
            photos = from.photos,
            likesAmount = from.likesAmount,
            textIcon = from.textIcon,
            commentsAmount = from.commentsAmount,
            recipient = from.recipient,
            mainlink = extractLinkFromString(from.mainlink),
            endAt = from.endAt,
            tags = mapTagList(from.tags),
            canBeReacted = toggleCanBeReacted(from.selector)
        )
    }
    private fun toggleCanBeReacted(selector: String): Boolean{
        return when (selector) {
            ObjectsToLike.CHALLENGE.eventSelector, ObjectsToLike.REPORT_OF_CHALLENGE.eventSelector, ObjectsToLike.TRANSACTION.eventSelector ,
            ObjectsToLike.OFFER.eventSelector -> {
                true
            }
            else -> {
                false
            }
        }
    }

    private fun mapTagList(from: List<TagEventEntity>?): List<TagEventModel>?{
        return from?.map(::mapTag)
    }
    private fun mapTag(from: TagEventEntity): TagEventModel{
        return TagEventModel(
            id = from.id,
            name = from.name
        )
    }

    fun extractLinkFromString(input: String?): String? {
        val startTag = "<a href="
        val endTag = ">"
        val startIndex = input?.indexOf(startTag) ?: -1
        val endIndex = input?.indexOf(endTag, startIndex + startTag.length) ?: -1

        return if (startIndex != -1 && endIndex != -1) {
            val link = input?.substring(startIndex + startTag.length, endIndex)
            link?.trim('\'', '\"')
        } else {
            null
        }
    }

    fun mapFilterList(from: EventFilterEntity): EventFilterModel{
        return EventFilterModel(
            eventtypes = from.eventtypes.map(::mapFilterItem).toMutableList(),
        )
    }

    private fun mapFilterItem(from: EventTypeEntity): EventTypeModel{
        return EventTypeModel(
            ids = listOf(from.id),
            name = from.name,
            on = from.on
        )
    }

    private fun mapSelector(selector: String): ObjectsToLike?{
       return  when(selector){
            ObjectsToLike.TRANSACTION.eventSelector -> ObjectsToLike.TRANSACTION
            ObjectsToLike.OFFER.eventSelector -> ObjectsToLike.OFFER
            ObjectsToLike.REPORT_OF_CHALLENGE.eventSelector -> ObjectsToLike.REPORT_OF_CHALLENGE
            ObjectsToLike.CHALLENGE.eventSelector -> ObjectsToLike.CHALLENGE
            ObjectsToLike.PURCHASE.eventSelector -> ObjectsToLike.PURCHASE
            ObjectsToLike.BIRTHDAY.eventSelector -> ObjectsToLike.BIRTHDAY
            else -> null
        }
    }
    fun map(from: FeedItemEntity): FeedModel? =
        from.challenge?.toModel(from, context) ?: from.transaction?.toModel(from, context, userDataRepository)
        ?: from.winner?.toModel(from, context) ?: from.order?.toModel(from, context, userDataRepository)

    fun mapList(from: List<FeedItemEntity>): List<FeedModel> {
        return from.mapNotNull(::map)
    }

    fun mapEntityByIdToModel(from: FeedItemByIdEntity): FeedItemByIdModel {
        return FeedItemByIdModel(
            id = from.id,
            tags = from.tags,
            like_amount = from.like_amount,
            updated_at = parseDateTimeWithBindToTimeZone(from.updated_at, context),
            reason = from.reason,
            sender_id = from.sender_id,
            sender_first_name = from.sender_first_name,
            sender_surname = from.sender_surname,
            sender_photo = from.sender_photo,
            sender_tg_name = from.sender_tg_name?: "anonymous",
            recipient_id = from.recipient_id,
            recipientName = from.recipient_first_name + " " + from.recipient_surname,
            recipient_photo = from.recipient_photo,
            recipient_tg_name = from.recipient_tg_name?: "anonymous",
            is_anonymous = from.is_anonymous,
            user_liked = from.user_liked,
            amount = from.amount,
            photos = mapPhotos(from.photo, from.photos, from.sticker)
        )
    }
    private fun mapPhotos(photo: String?, photos: List<String?>?, sticker: String?): List<String> {
        val result = mutableListOf<String>()
        sticker?.takeIf { it.isNotEmpty() }?.let { result.add(it) }
        photo?.takeIf { it.isNotEmpty() }?.let { result.add(it) }
        photos?.let {
            result.addAll(it.filterNotNull().filter { photo -> photo.isNotEmpty() })
        }
        return result
    }

}

private fun Winner.toModel(from: FeedItemEntity, context: Context) =
    FeedModel.WinnerFeedEvent(
        id = from.id,
        commentAmount = from.commentsAmount ?: 0,
        eventObjectId = from.eventObjectId,
        eventRecordId = from.eventRecordId,
        likesAmount = from.likesAmount ?: 0,
        time = parseDateTimeWithBindToTimeZone(from.time, context),
        userId = from.userId,
        challengeName = from.winner!!.challengeName ?: "Unknown",
        reportId = id,
        updatedAt = updatedAt,
        userLiked = userLiked,
        winnerName = "${from.winner.winnerFirstName} ${winnerSurname}",
        winnerId = winnerId,
        winnerPhoto = winnerPhoto,
        winnerTgName = winnerTgName ?: "tg_name_not_set",
        challengeId = challengeId
    )

private fun Transaction.toModel(from: FeedItemEntity, context: Context, userDataRepository: UserDataRepository) =
    FeedModel.TransactionFeedEvent(
        id = from.id,
        commentAmount = from.commentsAmount ?: 0,
        eventObjectId = from.eventObjectId,
        eventRecordId = from.eventRecordId,
        likesAmount = from.likesAmount ?: 0,
        time = parseDateTimeWithBindToTimeZone(from.time, context),
        userId = from.userId,
        transactionAmount = amount,
        transactionId = id,
        transactionIsAnonymous = isAnonymous,
        transactionRecipientId = recipientId,
        transactionRecipientPhoto = recipientPhoto,
        transactionRecipientTgName = recipientTgName,
        transactionSenderId = senderId,
        transactionSenderTgName = senderTgName ?: "anonymous",
        userLiked = userLiked,
        transactionUpdatedAt = updatedAt,
        transactionTags = tags.map {
            it.name
        },
        isWithMe = (userDataRepository.getProfileId() == recipientId.toString() ||
                userDataRepository.getProfileId() == senderId.toString()),
        isFromMe = userDataRepository.getProfileId() == senderId.toString(),
        isAnon = isAnonymous,
        senderName = "${senderFirstName} ${senderSurname}",
        recipientName = "${recipientFirstName} ${recipientSurname}",
    )

private fun Challenge.toModel(from: FeedItemEntity, context: Context) =
    FeedModel.ChallengeFeedEvent(
        id = from.id,
        commentAmount = from.commentsAmount ?: 0,
        eventObjectId = from.eventObjectId,
        eventRecordId = from.eventRecordId,
        likesAmount = from.likesAmount ?: 0,
        time = parseDateTimeWithBindToTimeZone(from.time, context),
        userId = from.userId,
        challengeCreatedAt = parseDateTimeWithBindToTimeZone(
            createdAt,
            context
        ), 
        challengeCreatorName = "${creatorFirstName} ${creatorSurname}",
        challengeId = id,
        challengeCreatorTgName = creatorTgName ?: "anonymous",
        challengeName = name ?: "Unknown",
        challengePhoto = photo,
        userLiked = userLiked,
        challengeCreatorId = creatorId,
        challengeEndAt = parseDateTimeWithBindToTimeZone(
            endAt,
            context
        )
    )

private fun OrderEntity.toModel(from: FeedItemEntity, context: Context,  userDataRepository: UserDataRepository) =
    FeedModel.PurchaseFeedEvent(
        id = from.id,
        commentAmount = from.commentsAmount ?: 0,
        eventObjectId = from.eventObjectId,
        eventRecordId = from.eventRecordId,
        likesAmount = from.likesAmount ?: 0,
        time = parseDateTimeWithBindToTimeZone(from.time, context),
        userId = from.userId,
        userLiked = userLiked,
        scopeId = from.scopeId,
        createdAt = createdAt,
        customerName =  "${customerFirstName} ${customerSurname}",
        customerId = customerId,
        customerPhoto = customerPhoto,
        customerTgName = customerTgName,
        isWithMe = userDataRepository.getProfileId() == customerId.toString(),
        productId = from.dataOfOffer?.offerId,
        productName = from.dataOfOffer?.offerName,
        marketId = from.dataOfOffer?.marketplaceId,
        orderId = orderId
    )