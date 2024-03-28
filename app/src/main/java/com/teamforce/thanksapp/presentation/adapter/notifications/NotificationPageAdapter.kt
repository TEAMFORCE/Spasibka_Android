package com.teamforce.thanksapp.presentation.adapter.notifications

import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemNotificationBinding
import com.teamforce.thanksapp.databinding.SeparatorDateTimeBinding
import com.teamforce.thanksapp.databinding.UnknownItemBinding
import com.teamforce.thanksapp.domain.models.challenge.SectionsOfChallengeType
import com.teamforce.thanksapp.domain.models.notifications.NotificationAdditionalData
import com.teamforce.thanksapp.domain.models.notifications.NotificationItem
import com.teamforce.thanksapp.utils.SpannableUtils.Companion.createClickableSpannable
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import com.teamforce.thanksapp.utils.branding.Cases
import com.teamforce.thanksapp.utils.displayDateToEnd
import com.teamforce.thanksapp.utils.displayFriendlyDateTime
import com.teamforce.thanksapp.utils.doubleQuoted
import com.teamforce.thanksapp.utils.invisible
import com.teamforce.thanksapp.utils.parseDateTimeOutputOnlyDate
import com.teamforce.thanksapp.utils.username
import com.teamforce.thanksapp.utils.visible

class NotificationPageAdapter(
    private val onUserClicked: (userId: Int) -> Unit,
    private val onTransactionClicked: (transactionId: Int, message: CharSequence, typeOfReactions: TypeOfReaction?) -> Unit,
    private val onChallengeClicked: (challengeId: Int, challengeSection: SectionsOfChallengeType) -> Unit,
    private val onOfferClicked: (offerId: Int?, marketId: Int) -> Unit,

    ) : PagingDataAdapter<NotificationItem, RecyclerView.ViewHolder>(DIffCallback) {

    private var unreadCount: Int = 0

    fun setUnreadCountNotify(amount: Int) {
        unreadCount = amount
        Log.e("NotifyAdapter", "Undread Count ${amount}")
    }


    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is NotificationItem.NotificationModel -> {
                return when (item.data) {
                    is NotificationAdditionalData.NotificationChallengeDataModel -> CHALLENGE_VIEW_TYPE
                    is NotificationAdditionalData.NotificationTransactionDataModel -> TRANSACTION_VIEW_TYPE
                    is NotificationAdditionalData.NotificationCommentDataModel -> COMMENT_VIEW_TYPE
                    is NotificationAdditionalData.NotificationReactionDataModel -> LIKE_VIEW_TYPE
                    is NotificationAdditionalData.NotificationChallengeWinnerDataModel -> WINNER_VIEW_TYPE
                    is NotificationAdditionalData.NotificationChallengeReportDataModel -> CHALLENGE_REPORT_VIEW_TYPE
                    is NotificationAdditionalData.NotificationNewOrderDataModel -> NEW_ORDER_TYPE
                    is NotificationAdditionalData.NotificationChallengeEndingDataModel -> CHALLENGE_ENDING_TYPE
                    is NotificationAdditionalData.NotificationQuestionnaireDataModel -> QUESTIONNAIRE_TYPE
                    else -> UNKNOWN_VIEW_TYPE
                }
            }

            is NotificationItem.DateTimeSeparator -> {
                DATE_TIME_SEPARATOR_VIEW_TYPE
            }

            null -> {
                throw UnsupportedOperationException("Unknown view")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TRANSACTION_VIEW_TYPE, CHALLENGE_VIEW_TYPE, COMMENT_VIEW_TYPE, LIKE_VIEW_TYPE,
            WINNER_VIEW_TYPE, CHALLENGE_REPORT_VIEW_TYPE, NEW_ORDER_TYPE, CHALLENGE_ENDING_TYPE, QUESTIONNAIRE_TYPE -> {
                val binding = ItemNotificationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                NotificationViewHolder(binding)
            }

            DATE_TIME_SEPARATOR_VIEW_TYPE -> {
                val binding = SeparatorDateTimeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SeparatorViewHolder(binding)
            }

            else -> {
                val binding = UnknownItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                UnknownViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            when (item) {
                is NotificationItem.NotificationModel -> {
                    val viewHolder = holder as? NotificationViewHolder
                    viewHolder?.bind(item, position)
                }

                is NotificationItem.DateTimeSeparator -> {
                    val viewHolder = holder as? SeparatorViewHolder
                    viewHolder?.bind(item)
                }
            }
        }
    }

    class UnknownViewHolder(
        private val binding: UnknownItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
        }
    }

    class SeparatorViewHolder(
        private val binding: SeparatorDateTimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NotificationItem.DateTimeSeparator) {
            binding.apply {

                when {
                    data.date != null -> {
                        separatorText.text = data.date.toText(binding.root.context)

                    }

                    data.dateTitle != null -> {
                        val dateTitle = data.dateTitle
                        separatorText.text =
                            "${dateTitle.dayText.toText(binding.root.context)}"
                    }
                }


            }
        }
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: NotificationItem.NotificationModel,
            position: Int
        ) {
            val unread = position <= unreadCount
            binding.unreadNotifyIv.visibility = if (unread) View.VISIBLE else View.INVISIBLE
            binding.dateTime.text = displayFriendlyDateTime(data.createdAt, binding.root.context)
            binding.description.movementMethod = LinkMovementMethod.getInstance()

            when (data.data) {
                is NotificationAdditionalData.NotificationTransactionDataModel -> bindTransaction(
                    data
                )

                is NotificationAdditionalData.NotificationCommentDataModel -> bindComment(data)
                is NotificationAdditionalData.NotificationReactionDataModel -> bindReaction(data)
                is NotificationAdditionalData.NotificationChallengeDataModel -> bindChallengeCreating(data)
                is NotificationAdditionalData.NotificationChallengeReportDataModel -> bindNewReport(
                    data
                )

                is NotificationAdditionalData.NotificationChallengeWinnerDataModel -> bindWinner(
                    data
                )

                is NotificationAdditionalData.NotificationNewOrderDataModel -> bindNewOrder(data)
                is NotificationAdditionalData.NotificationChallengeEndingDataModel -> bindChallengeEnding(
                    data
                )

                is NotificationAdditionalData.NotificationQuestionnaireDataModel -> {
                    setIconItem(R.drawable.clipboard)
                    when (data.data.mode) {
                        NotificationAdditionalData.NotificationQuestionnaireDataModel.QuestionnaireMode.CLOSED_NOW -> {
                            binding.title.text = binding.root.context.getString(R.string.notifications_questionnaire_expired_soon_title)
                            setQuestionnaireText(
                                data.data, R.string.notifications_questionnaire_closed_now,
                                data.data.title
                            )
                        }

                        NotificationAdditionalData.NotificationQuestionnaireDataModel.QuestionnaireMode.CLOSING_TO_ADMIN -> {
                            binding.title.text = binding.root.context.getString(R.string.notifications_questionnaire_expired_soon_title)
                            setQuestionnaireText(
                                data.data,
                                R.string.notifications_questionnaire_closing_to_admin,
                                data.data.finishedAt ?: "",
                                data.data.title
                            )
                        }

                        NotificationAdditionalData.NotificationQuestionnaireDataModel.QuestionnaireMode.CLOSING_TO_PARTICIPANT -> {
                            binding.title.text = binding.root.context.getString(R.string.notifications_questionnaire_expired_soon_title)
                            setQuestionnaireText(
                                data.data,
                                R.string.notifications_questionnaire_closing_to_participant,
                                data.data.finishedAt ?: "",
                                data.data.title
                            )
                        }

                        NotificationAdditionalData.NotificationQuestionnaireDataModel.QuestionnaireMode.CREATED -> {
                            binding.title.text = binding.root.context.getString(R.string.notifications_questionnaire_create_title)
                            setQuestionnaireText(
                                data.data,
                                R.string.notifications_questionnaire_created,
                                data.data.title
                            )
                        }

                        null -> {
                            // do nothing
                        }
                    }
                }

                else -> {
                    // do nothing
                }
            }
            theming()
        }

        private fun theming() {
            binding.unreadNotifyIv.setColorFilter(Color.parseColor(Branding.brand.colorsJson.minorErrorColor))
            binding.iconSiv.setThemeColor(appTheme)
        }

        private fun setQuestionnaireText(
            data: NotificationAdditionalData.NotificationQuestionnaireDataModel,
            @StringRes stringRes: Int,
            vararg formatArgs: Any
        ) {
            val originalString = binding.root.context.getString(stringRes, *formatArgs)
            val color = Color.parseColor(Branding.brand.colorsJson.mainBrandColor)

            val spannableString = SpannableString(originalString)
            val startIndex = originalString.indexOf(data.title) - 1

            spannableString.setSpan(
                ForegroundColorSpan(color),
                startIndex,
                startIndex + data.title.length + 2,
                0
            )
            binding.description.text = spannableString
        }


        private fun bindTransaction(data: NotificationItem.NotificationModel) {
            val data = data.data as NotificationAdditionalData.NotificationTransactionDataModel
            setUserAvatar(data.senderPhoto, data.senderNameOrTg)
            binding.title.text =
                binding.root.context.getString(R.string.notifications_gratitude_title)
            binding.apply {
                val spannable = SpannableStringBuilder()
                    .append(
                        createClickableSpannable(
                            root.context.getString(R.string.received_part) + " ",
                            appTheme.generalContrastColor,
                            null
                        )
                    )
                    .append(
                        createClickableSpannable(
                            "${data.amount} ${
                                Branding.declineCurrency(
                                    data.amount,
                                    Cases.ACCUSATIVE
                                )
                            }" + " ",
                            appTheme.mainBrandColor,
                            null
                        )
                    )
                // Проверка на анонимную транзакцию
                if (data.senderId != null) {
                    spannable.append(
                        root.context.getString(R.string.from) + " "
                    ).append(
                        createClickableSpannable(
                            data.senderNameOrTg,
                            appTheme.mainBrandColor,
                        ) {
                            try {
                                onUserClicked(data.senderId.toInt())
                            } catch (e: NumberFormatException) {

                            }
                        }
                    )
                }
                binding.description.text = spannable
            }

            binding.root.setOnClickListener {
                onTransactionClicked(
                    data.transactionId,
                    binding.description.text,
                    null
                )
            }
        }

        private fun bindComment(data: NotificationItem.NotificationModel) {
            val targetType: Targets
            val data = data.data as NotificationAdditionalData.NotificationCommentDataModel
            setUserAvatar(
                data.commentFromPhoto,
                data.commentFromNameOrTg
            )
            binding.title.text =
                binding.root.context.getString(R.string.notifications_new_comment_title)
            val target =
                if (data.transactionId != null) {
                    targetType = Targets.Transaction
                    binding.root.context.getString(R.string.to_transaction)
                } else if (data.challengeId != null) {
                    targetType = Targets.Challenge
                    binding.root.context.getString(R.string.to_challenge)
                } else {
                    targetType = Targets.Report
                    binding.root.context.getString(R.string.to_your_report)
                }

            binding.apply {

                val spannable = SpannableStringBuilder()
                    .append(
                        root.context.getString(R.string.new_comment_to) + " "
                    )
                    .append(createClickableSpannable("$target ", appTheme.mainBrandColor, null))
                    .append(binding.root.context.getString(R.string.notifications_challenge_from) + " ")
                    .append(
                        createClickableSpannable(
                            data.commentFromNameOrTg,
                            appTheme.mainBrandColor,
                            null)
                    )
                description.text = spannable

                root.setOnClickListener {
                    when (targetType) {
                        Targets.Challenge -> {
                            if (data.challengeId != null) {
                                onChallengeClicked(data.challengeId, SectionsOfChallengeType.COMMENTS)
                            }
                        }

                        Targets.Transaction -> {
                            if (data.transactionId != null) {
                                onTransactionClicked(
                                    data.transactionId,
                                    description.text,
                                    TypeOfReaction.COMMENTS
                                )
                            }
                        }

                        else -> {
                            //nothing to do
                        }
                    }
                }
            }
        }

        private fun bindReaction(data: NotificationItem.NotificationModel) {
            val data = data.data as NotificationAdditionalData.NotificationReactionDataModel
            setUserAvatar(
                data.reactionFromPhoto,
                data.reactionFromNameOrTg
            )
            binding.title.text =
                binding.root.context.getString(R.string.notifications_new_reaction_title)

            val targetType: Targets

            val target =
                if (data.challengeId != null) {
                    targetType = Targets.Challenge
                    binding.root.context.getString(R.string.to_challenge)
                } else if (data.transactionId != null) {
                    targetType = Targets.Transaction
                    binding.root.context.getString(R.string.to_transaction)
                } else {
                    targetType = Targets.Report
                    binding.root.context.getString(R.string.to_your_report)
                }

            binding.apply {

                val spannable = SpannableStringBuilder()
                    .append(
                        root.context.getString(R.string.new_reaction_to) + " "
                    )
                    .append(
                        createClickableSpannable(
                            "$target ",
                            appTheme.mainBrandColor,
                            null
                        )
                    )
                    .append(
                        binding.root.context.getString(R.string.notifications_challenge_from) + " "
                    ).append(
                        createClickableSpannable(
                            data.reactionFromNameOrTg,
                            appTheme.mainBrandColor,
                            null
                        )
                    )
                description.text = spannable
                root.setOnClickListener {
                    when (targetType) {
                        Targets.Transaction -> {
                            if (data.transactionId != null) {
                                onTransactionClicked(
                                    data.transactionId,
                                    description.text,
                                    TypeOfReaction.REACTIONS
                                )
                            }
                        }

                        Targets.Challenge -> {
                            if (data.challengeId != null) {
                                onChallengeClicked(data.challengeId, SectionsOfChallengeType.REACTIONS)
                            }
                        }

                        else -> {
                            //nothing to do
                        }
                    }
                }
            }
        }

        private fun bindChallengeCreating(data: NotificationItem.NotificationModel) {
            val data = data.data as NotificationAdditionalData.NotificationChallengeDataModel
            binding.title.text =
                binding.root.context.getString(R.string.notifications_challenge_title)

            binding.apply {
                setIconItem(R.drawable.ic_challenge_badge)
                val spannable = SpannableStringBuilder()
                    .append(root.context.getString(R.string.new_challenge) + " ")
                    .append(
                        createClickableSpannable(
                            data.challengeName.doubleQuoted(),
                            appTheme.mainBrandColor,
                            null
                        )
                    )
                description.text = spannable
                root.setOnClickListener {
                    onChallengeClicked(data.challengeId, SectionsOfChallengeType.DETAIL)

                }
            }

        }

        private fun bindChallengeEnding(data: NotificationItem.NotificationModel) {
            val data = data.data as NotificationAdditionalData.NotificationChallengeEndingDataModel
            binding.title.text =
                binding.root.context.getString(R.string.notifications_challenge_ending_title)

            binding.apply {
                setIconItem(R.drawable.ic_challenge_badge)
                val spannable = SpannableStringBuilder()
                    .append(root.context.getString(R.string.notification_Challenge) + " ")
                    .append(
                        createClickableSpannable(
                            data.challengeName.doubleQuoted() + " ",
                            appTheme.mainBrandColor,
                            null
                        )
                    )
                    .append(
                        root.context.getString(R.string.notification_challenge_ends)
                    )

                description.text = spannable
                root.setOnClickListener {
                    onChallengeClicked(data.challengeId, SectionsOfChallengeType.DETAIL)
                }
            }
        }

        private fun bindNewReport(data: NotificationItem.NotificationModel) {
            val data = data.data as NotificationAdditionalData.NotificationChallengeReportDataModel
            binding.title.text = binding.root.context.getString(R.string.notifications_report_title)
            binding.apply {
                setIconItem(R.drawable.ic_challenge_badge)
                val spannable = SpannableStringBuilder()
                    .append(root.context.getString(R.string.new_report_to_challenge) + " ")
                    .append(
                        createClickableSpannable(
                            data.challengeName.doubleQuoted() + " ",
                            appTheme.mainBrandColor,
                            null
                        )
                    )
                    .append(binding.root.context.getString(R.string.notifications_challenge_from) + " ")
                    .append(
                        createClickableSpannable(
                            data.reportSenderNameOrTg,
                            appTheme.mainBrandColor,
                            null
                        )
                    )
                description.text = spannable
                root.setOnClickListener {
                    // TODO Переход к списку отчета чаликов
                    onChallengeClicked(data.challengeId, SectionsOfChallengeType.CONTENDERS)
                }
            }
        }

        private fun bindWinner(data: NotificationItem.NotificationModel) {
            val data = data.data as NotificationAdditionalData.NotificationChallengeWinnerDataModel
            binding.title.text = binding.root.context.getString(R.string.notifications_winner_title)
            binding.apply {
                setIconItem(R.drawable.ic_challenge_badge)
                val spannable = SpannableStringBuilder()
                    .append(root.context.getString(R.string.you_win_challenge) + " ")
                    .append(
                        createClickableSpannable(
                            data.challengeName.doubleQuoted(),
                            appTheme.mainBrandColor,
                            null
                        )
                    )
                description.text = spannable
                root.setOnClickListener {
                    onChallengeClicked(data.challengeId, SectionsOfChallengeType.MY_RESULT)
                }
            }
        }

        private fun bindNewOrder(dataOuter: NotificationItem.NotificationModel) {
            val data = dataOuter.data as NotificationAdditionalData.NotificationNewOrderDataModel
            binding.title.text =
                binding.root.context.getString(R.string.notifications_new_order_title)
            setIconItem(R.drawable.ic_benefit_badge)
            binding.apply {
                val spannable = SpannableStringBuilder()
                    .append(root.context.getString(R.string.new_order) + " ")
                    .append(
                        createClickableSpannable(
                            data.offerName.doubleQuoted() + " ",
                            appTheme.mainBrandColor
                        ) {
                            onOfferClicked(data.offerId, data.marketplaceId)
                        }
                    )
                    .append(
                        root.context.getString(R.string.notification_from) + " "
                    )
                    .append(
                        createClickableSpannable(
                            data.customerName,
                            appTheme.mainBrandColor,
                        ) {
                            onUserClicked(data.customerId)
                        }
                    )
                description.text = spannable
            }
        }

        private fun setUserAvatar(avatar: String?, initials: String?) {
            binding.avatarAv.visible()
            binding.iconSiv.invisible()
            binding.avatarAv.setAvatarImageOrInitials(avatar, initials)
        }

        private fun setIconItem(@DrawableRes resId: Int) {
            binding.avatarAv.invisible()
            binding.iconSiv.visible()
            binding.iconSiv.setImageResource(resId)
        }

    }

    companion object {
        const val TRANSACTION_VIEW_TYPE = 1
        const val CHALLENGE_VIEW_TYPE = 2
        const val UNKNOWN_VIEW_TYPE = 3
        const val DATE_TIME_SEPARATOR_VIEW_TYPE = 4
        const val CHALLENGE_REPORT_VIEW_TYPE = 5
        const val COMMENT_VIEW_TYPE = 6
        const val LIKE_VIEW_TYPE = 7
        const val WINNER_VIEW_TYPE = 8
        const val NEW_ORDER_TYPE = 9
        const val CHALLENGE_ENDING_TYPE = 10
        const val QUESTIONNAIRE_TYPE = 11


        private val DIffCallback = object : DiffUtil.ItemCallback<NotificationItem>() {
            override fun areItemsTheSame(
                oldItem: NotificationItem,
                newItem: NotificationItem
            ): Boolean {
                return (oldItem is NotificationItem.NotificationModel &&
                        newItem is NotificationItem.NotificationModel &&
                        oldItem.id == newItem.id) ||
                        (oldItem is NotificationItem.DateTimeSeparator &&
                                newItem is NotificationItem.DateTimeSeparator &&
                                oldItem.date == newItem.date)
            }

            override fun areContentsTheSame(
                oldItem: NotificationItem,
                newItem: NotificationItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}

enum class TypeOfReaction {
    COMMENTS,
    REACTIONS
}

enum class Targets {
    Challenge,
    Transaction,
    Report
}

