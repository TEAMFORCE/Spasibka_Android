package com.teamforce.thanksapp.presentation.adapter.history


import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Icon
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.databinding.ItemTransferBinding
import com.teamforce.thanksapp.databinding.SeparatorDateTimeBinding
import com.teamforce.thanksapp.domain.models.history.HistoryItemModel
import com.teamforce.thanksapp.presentation.customViews.AvatarView.AvatarView
import com.teamforce.thanksapp.utils.*
import com.teamforce.thanksapp.utils.SpannableUtils.Companion.createClickableSpannable
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.branding.Branding.Companion.appTheme
import com.teamforce.thanksapp.utils.branding.Cases


// TODO tgNameUser and statusCard don't use anymore
// TODO Need to clean this code, especially check bundle vars

class HistoryPageAdapter(
    private val myId: Int?,
    private val username: String?,
    private val onCancelClicked: (id: Int) -> Unit,
    private val onSomeonesClicked: (userId: Int) -> Unit,
    private val onChallengeClicked: (challengeId: Int) -> Unit
) : PagingDataAdapter<HistoryItemModel, RecyclerView.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_transfer -> {
                val binding = ItemTransferBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HistoryItemViewHolder(binding)
            }

            else -> {
                val binding = SeparatorDateTimeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SeparatorViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            when (item) {
                is HistoryItemModel.DateTimeSeparator -> {
                    val viewHolder = holder as SeparatorViewHolder
                    viewHolder.bind(item)
                }

                is HistoryItemModel.UserTransactionsModel -> {
                    val viewHolder = holder as HistoryItemViewHolder
                    viewHolder.bind(
                        item, myId, onChallengeClicked, onSomeonesClicked
                    )
                }

            }
        }
    }

    class SeparatorViewHolder(
        private val binding: SeparatorDateTimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: HistoryItemModel.DateTimeSeparator) {
            binding.apply {
                separatorText.text = data.date
            }
        }
    }

    inner class HistoryItemViewHolder(
        private val binding: ItemTransferBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        var photoFromSender: String? = null
        var userId: Int? = null

        var dateGetInfo: String = "null"
        var labelStatusTransaction: String = "null"
        var descr_transaction_1: String = "null"
        var comingStatusTransaction: String = "null"
        var weRefusedYourOperation: String? = null
        var avatar: String? = null
        var date: String = ""
        var mySpannable: SpannableStringBuilder? = null

        fun bind(
            data: HistoryItemModel.UserTransactionsModel, myId: Int?,
            onChallengeClicked: (challengeId: Int) -> Unit,
            onSomeonesClicked: (userId: Int) -> Unit
        ) {

            // Без этого клики по spannable не работают
            binding.message.movementMethod = LinkMovementMethod.getInstance()
            binding.dateTime.text =
                parseDateTimeWithBindToTimeZone(data.updatedAt, binding.root.context)
            binding.apply {
                canICancelTheTransaction(data, binding)
                setStatusTransaction(data)
                setHeader(data, binding)
                when (data.transactionClass) {
                    HistoryItemModel.UserTransactionsModel.TransactionClass.THANKS,
                    HistoryItemModel.UserTransactionsModel.TransactionClass.THANKS_FROM_SYSTEM -> {
                        if (data.sender?.sender_tg_name != ANONYMOUS &&
                            data.sender?.sender_id == myId
                        )
                        // Я отправитель
                        {
                            val spannable = SpannableStringBuilder(
                            ).append(
                                createClickableSpannable(
                                    root.context.getString(R.string.youSended) + " ",
                                    appTheme.generalContrastColor
                                ) {
                                    transactionToChallengeOrDetailOfTransaction(data)
                                }
                            ).append(
                                createClickableSpannable(
                                    "${data.amount} ${
                                        Branding.declineCurrency(
                                            data.amount.toInt(),
                                            Cases.ACCUSATIVE
                                        )
                                    }" + " ",
                                    appTheme.minorSuccessColor
                                ) {
                                    transactionToChallengeOrDetailOfTransaction(data)
                                }
                            ).append(
                                if (data.recipient?.recipient_first_name.isNullOrEmpty() &&
                                    data.recipient?.recipient_surname.isNullOrEmpty()
                                ) {
                                    createClickableSpannable(
                                        data.recipient?.recipient_tg_name + " ",
                                        appTheme.mainBrandColor
                                    )
                                    {
                                        data.recipient_id?.let { onSomeonesClicked(it) }
                                    }
                                } else {
                                    createClickableSpannable(
                                        data.recipient?.recipient_first_name + " " +
                                                data.recipient?.recipient_surname + " ",
                                        appTheme.mainBrandColor
                                    ) {
                                        data.recipient_id?.let {
                                            onSomeonesClicked(it)
                                        }
                                    }
                                }

                            )
                            descr_transaction_1 = binding.root.context.getString(R.string.youSended)
                            labelStatusTransaction =
                                binding.root.context.getString(R.string.typeTransfer)
                            comingStatusTransaction =
                                binding.root.context.getString(R.string.incomingTransfer)
                            message.text = spannable
                            mySpannable = spannable
                            setPhotoToItem(
                                photo = data.recipient?.recipient_photo,
                                name = data.recipient?.recipient_first_name,
                                surname = data.recipient?.recipient_surname
                            )
                        } else if (data.sender?.sender_tg_name == ANONYMOUS)
                        // Я получатель от анонима
                        {
                            descr_transaction_1 = binding.root.context.getString(R.string.youGot)
                            labelStatusTransaction =
                                binding.root.context.getString(R.string.typeTransfer)
                            valueTransfer.text = "+ " + data.amount
                            comingStatusTransaction =
                                binding.root.context.getString(R.string.comingTransfer)
                            val spannable = SpannableStringBuilder(
                            ).append(
                                createClickableSpannable(
                                    " " + root.context.getString(R.string.youGot) + " ",
                                    appTheme.generalContrastColor
                                ) {
                                    transactionToChallengeOrDetailOfTransaction(data)
                                }
                            ).append(
                                createClickableSpannable(
                                    "${data.amount} ${
                                        Branding.declineCurrency(
                                            data.amount.toInt(),
                                            Cases.ACCUSATIVE
                                        )
                                    }",
                                    appTheme.minorSuccessColor
                                ) {
                                    transactionToChallengeOrDetailOfTransaction(data)
                                }
                            )

                            message.text = spannable
                            mySpannable = spannable
                            setPhotoToItem(
                                photo = data.recipient?.recipient_photo,
                                name = data.recipient?.recipient_first_name,
                                surname = data.recipient?.recipient_surname
                            )
                        } else
                        // Я получатель
                        {
                            if (!data.sender?.sender_photo.isNullOrEmpty() &&
                                data.sender?.sender_photo != "null"
                            ) {
                                Glide.with(binding.root.context)
                                    .load("${Consts.BASE_URL}${data.sender?.sender_photo}".toUri())
                                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(avatarAv)
                                avatar =
                                    "${Consts.BASE_URL}${data.sender?.sender_photo}"
                            } else {
                                binding.avatarAv.avatarInitials =
                                    "${data.sender?.sender_first_name}${data.sender?.sender_surname}"
                            }
                            descr_transaction_1 = binding.root.context.getString(R.string.youGot)
                            val spannable = SpannableStringBuilder(
                            ).append(
                                createClickableSpannable(
                                    "${data.amount} ${
                                        Branding.declineCurrency(
                                            data.amount.toInt(),
                                            Cases.ACCUSATIVE
                                        )
                                    }",
                                    appTheme.minorSuccessColor
                                ) {
                                    transactionToChallengeOrDetailOfTransaction(
                                        data
                                    )
                                }
                            ).append(
                                createClickableSpannable(
                                    " " + root.context.getString(R.string.from) + " ",
                                    appTheme.generalContrastColor
                                ) {
                                    transactionToChallengeOrDetailOfTransaction(
                                        data
                                    )
                                }
                            ).append(
                                // Если у отправителя нет имени и фамилии вставляем телеграм
                                if (data.sender?.sender_first_name.isNullOrEmpty() &&
                                    data.sender?.sender_surname.isNullOrEmpty()
                                ) {
                                    createClickableSpannable(
                                        data.sender?.sender_tg_name + " ",
                                        appTheme.mainBrandColor
                                    )
                                    {
                                        data.sender_id?.let { onSomeonesClicked(it) }
                                    }
                                } else {
                                    createClickableSpannable(
                                        data.sender?.sender_first_name + " " +
                                                data.sender?.sender_surname,
                                        appTheme.mainBrandColor
                                    )
                                    {
                                        data.sender_id?.let { onSomeonesClicked(it) }
                                    }
                                }

                            )
                            message.text = spannable
                            mySpannable = spannable

                            binding.avatarAv.setAvatarImageOrInitials(data.sender?.sender_photo, "${data.sender?.sender_first_name}${data.sender?.sender_surname}")
                            labelStatusTransaction =
                                binding.root.context.getString(R.string.typeTransfer)
                            comingStatusTransaction =
                                binding.root.context.getString(R.string.comingTransfer)
                        }

                    }

                    HistoryItemModel.UserTransactionsModel.TransactionClass.CONTRIBUTION_TO_CHALLENGE -> {
                        // Я отправитель
                        // В случае создания челленджа
                        val spannable = SpannableStringBuilder(
                        ).append(
                            createClickableSpannable(
                                root.context.getString(R.string.youSended) + " ",
                                appTheme.generalContrastColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(data)
                            }
                        ).append(
                            createClickableSpannable(
                                "${data.amount} ${
                                    Branding.declineCurrency(
                                        data.amount.toInt(),
                                        Cases.ACCUSATIVE
                                    )
                                }" + " ",
                                appTheme.minorSuccessColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(data)
                            }
                        ).append(
                            createClickableSpannable(
                                " " + root.context.getString(R.string.inFundOfChallenge) + " ",
                                appTheme.generalContrastColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(data)
                            }
                        ).append(
                            createClickableSpannable(
                                data.sender?.challenge_name?.doubleQuoted() ?: "",
                                appTheme.mainBrandColor
                            ) {
                                data.sender?.challenge_id?.let { onChallengeClicked(it) }
                            }
                        )
                        message.text = spannable
                        mySpannable = spannable

                        setPhotoToItem(
                            photo = data.sender?.sender_photo,
                            name = data.sender?.sender_first_name,
                            surname = data.sender?.sender_surname
                        )
                    }

                    HistoryItemModel.UserTransactionsModel.TransactionClass.REWARD_FOR_CHALLENGE -> {
                        // В случае победы в челлендже
                        // Я получатель
                        val spannable = SpannableStringBuilder(
                        ).append(
                            createClickableSpannable(
                                "${data.amount} ${
                                    Branding.declineCurrency(
                                        data.amount.toInt(),
                                        Cases.ACCUSATIVE
                                    )
                                }",
                                appTheme.minorSuccessColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(
                                    data
                                )
                            }
                        ).append(
                            createClickableSpannable(
                                " " + root.context.getString(R.string.forWinningInChallenge) + " ",
                                appTheme.generalContrastColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(
                                    data
                                )
                            }
                        ).append(
                            createClickableSpannable(
                                // Занести сюда название челленджа
                                data.sender?.challenge_name?.doubleQuoted() ?: "",
                                appTheme.mainBrandColor
                            ) {
                                data.sender?.challenge_id?.let { onChallengeClicked(it) }
                            }
                        )
                        message.text = spannable
                        mySpannable = spannable
                        setPhotoToItem(
                            photo = data.recipient?.recipient_photo,
                            name = data.recipient?.recipient_first_name,
                            surname = data.recipient?.recipient_surname
                        )
                    }

                    HistoryItemModel.UserTransactionsModel.TransactionClass.REFUND_FROM_CHALLENGE -> {
                        // Я создатель челленджа, возврат средств из чалика
                        val spannable = SpannableStringBuilder(
                        ).append(
                            createClickableSpannable(
                                root.context.getString(R.string.youGetReturnedThanks) + " ",
                                appTheme.generalContrastColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(data)
                            }
                        ).append(
                            createClickableSpannable(
                                "${data.amount} ${
                                    Branding.declineCurrency(
                                        data.amount.toInt(),
                                        Cases.GENITIVE
                                    )
                                }" + " ",
                                appTheme.minorSuccessColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(data)
                            }
                        ).append(
                            createClickableSpannable(
                                " " + root.context.getString(R.string.fromFundOfChallenge) + " ",
                                appTheme.generalContrastColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(data)
                            }
                        ).append(
                            createClickableSpannable(
                                data.sender?.challenge_name?.doubleQuoted() ?: "",
                                appTheme.mainBrandColor
                            ) {
                                data.sender?.challenge_id?.let { onChallengeClicked(it) }
                            }
                        )
                        message.text = spannable
                        mySpannable = spannable
                        setPhotoToItem(
                            photo = data.recipient?.recipient_photo,
                            name = data.recipient?.recipient_first_name,
                            surname = data.recipient?.recipient_surname
                        )
                    }

                    HistoryItemModel.UserTransactionsModel.TransactionClass.FIRED_THANKS -> {
                        val spannable = SpannableStringBuilder(
                        ).append(
                            createClickableSpannable(
                                root.context.getString(R.string.youGetReturnedThanks) + " ",
                                appTheme.generalContrastColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(data)
                            }
                        ).append(
                            createClickableSpannable(
                                "${data.amount} ${
                                    Branding.declineCurrency(
                                        data.amount.toInt(),
                                        Cases.ACCUSATIVE
                                    )
                                }" + " ",
                                appTheme.minorSuccessColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(data)
                            }
                        ).append(
                            createClickableSpannable(
                                " " + root.context.getString(R.string.toSystem) + " ",
                                appTheme.generalContrastColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(
                                    data
                                )
                            }
                        )
                        message.text = spannable
                        mySpannable = spannable
                        setPhotoToItem(
                            photo = data.recipient?.recipient_photo,
                            name = data.recipient?.recipient_first_name,
                            surname = data.recipient?.recipient_surname
                        )
                    }

                    HistoryItemModel.UserTransactionsModel.TransactionClass.REFUND_FROM_BENEFIT -> {
                        val spannable = SpannableStringBuilder(
                        ).append(
                            createClickableSpannable(
                                root.context.getString(R.string.history_refund) + " ",
                                appTheme.generalContrastColor
                            ) {}
                        ).append(
                            createClickableSpannable(
                                "${data.amount} ${
                                    Branding.declineCurrency(
                                        data.amount.toInt(),
                                        Cases.GENITIVE
                                    )
                                }" + " ",
                                appTheme.minorSuccessColor
                            ) {
                                transactionToChallengeOrDetailOfTransaction(data)
                            }
                        ).append(
                            createClickableSpannable(
                                root.context.getString(R.string.history_from_benefit),
                                appTheme.generalContrastColor
                            ) {}
                        )
                        message.text = spannable
                        mySpannable = spannable
                        setPhotoToItem(
                            photo = data.recipient?.recipient_photo,
                            name = data.recipient?.recipient_first_name,
                            surname = data.recipient?.recipient_surname
                        )
                    }
                }

                dateGetInfo = parseDateTimeWithBindToTimeZone(data.updatedAt, root.context)
                transactionToAnotherProfile(username, data)

                photoFromSender = data.photo
                transactionToChallengeOrDetailOfTransactionByMainCardTap(data)

                // Установка спец фото спец после всех действий, тк выше фото устанавилвается еще раз, нужно зарефакторить

                setSpecialIcon(data, binding)

            }
        }

        private fun setSpecialIcon(
            data: HistoryItemModel.UserTransactionsModel,
            binding: ItemTransferBinding
        ) {
            val c = binding.root.context
            val icon = when (data.transactionClass) {
                HistoryItemModel.UserTransactionsModel.TransactionClass.REWARD_FOR_CHALLENGE,
                HistoryItemModel.UserTransactionsModel.TransactionClass.REFUND_FROM_CHALLENGE,
                HistoryItemModel.UserTransactionsModel.TransactionClass.CONTRIBUTION_TO_CHALLENGE -> {
                   R.drawable.ic_challenge_badge
                }

                HistoryItemModel.UserTransactionsModel.TransactionClass.THANKS_FROM_SYSTEM,
                HistoryItemModel.UserTransactionsModel.TransactionClass.FIRED_THANKS -> {
                    R.drawable.ic_sys_badge

                }

                HistoryItemModel.UserTransactionsModel.TransactionClass.REFUND_FROM_BENEFIT -> {
                    R.drawable.ic_benefit_badge
                }
                else -> {
                    binding.iconSiv.invisible()
                    binding.avatarAv.visible()
                    return
                }
            }
            binding.avatarAv.invisible()
            binding.iconSiv.visible()
            binding.iconSiv.setImageResource(icon)
            binding.iconSiv.setThemeColor(appTheme)
        }

        private fun setHeader(
            data: HistoryItemModel.UserTransactionsModel,
            binding: ItemTransferBinding
        ) {
            val c = binding.root.context
            binding.header.text = when (data.transactionClass) {
                HistoryItemModel.UserTransactionsModel.TransactionClass.THANKS -> c.getString(R.string.history_list_header_gratitude)
                HistoryItemModel.UserTransactionsModel.TransactionClass.CONTRIBUTION_TO_CHALLENGE -> c.getString(
                    R.string.history_list_header_creating_challenge
                )

                HistoryItemModel.UserTransactionsModel.TransactionClass.REWARD_FOR_CHALLENGE -> c.getString(
                    R.string.history_list_header_win_challenge
                )

                HistoryItemModel.UserTransactionsModel.TransactionClass.REFUND_FROM_CHALLENGE -> c.getString(
                    R.string.history_list_header_refund_prize_fund
                )

                HistoryItemModel.UserTransactionsModel.TransactionClass.THANKS_FROM_SYSTEM -> c.getString(
                    R.string.history_list_header_system_refill
                )

                HistoryItemModel.UserTransactionsModel.TransactionClass.FIRED_THANKS -> c.getString(
                    R.string.history_list_header_system_debit
                )

                HistoryItemModel.UserTransactionsModel.TransactionClass.REFUND_FROM_BENEFIT -> c.getString(
                    R.string.history_list_header_refund_benefit
                )
            }
        }

        private fun canICancelTheTransaction(
            data: HistoryItemModel.UserTransactionsModel,
            binding: ItemTransferBinding
        ) {
            if (data.canUserCancel == true) {
                binding.refuseTransactionBtn.visible()
                binding.refuseTransactionBtn.setOnClickListener {
                    onCancelClicked(data.id)
                }
            } else {
                binding.refuseTransactionBtn.invisible()
            }
        }

        private fun setPhotoToItem(photo: String?, name: String?, surname: String?) {
            if (!photo.isNullOrEmptyMy()) {
                Glide.with(binding.root.context)
                    .load(photo?.addBaseUrl())
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.avatarAv)
                avatar =
                    "${Consts.BASE_URL}${photo}"
            } else {
                binding.avatarAv.avatarInitials =
                    "${name} ${surname}"
            }
        }

        // Существуют 2 почти одниковые функции, отлчие в контексте клика, одна из них для клика по тексту, другя для клика по карточке
        private fun transactionToChallengeOrDetailOfTransaction(data: HistoryItemModel.UserTransactionsModel) {
            if (ifTransactionThanks(data)) {
                navigateSafely(binding.root, getBundle(data))
            } else {
                tryChallengeClicked(data)
            }
        }

        private fun transactionToChallengeOrDetailOfTransactionByMainCardTap(data: HistoryItemModel.UserTransactionsModel) {
            if (ifTransactionThanks(data)) {
                binding.mainCard.setOnClickListener { navigateSafely(it, getBundle(data)) }
            } else {
                binding.mainCard.setOnClickListener { tryChallengeClicked(data) }
            }
        }

        private fun navigateSafely(
            view: View,
            bundle: Bundle
        ) {
            view.findNavController().navigateSafely(
                R.id.action_historyFragment_to_additionalInfoTransactionBottomSheetFragment2,
                bundle
            )
        }

        private fun tryChallengeClicked(data: HistoryItemModel.UserTransactionsModel) {
            data.sender?.challenge_id?.let { it -> onChallengeClicked(it) }
        }

        private fun getBundle(data: HistoryItemModel.UserTransactionsModel): Bundle {
            val bundle = Bundle()
            bundle.apply {
                putString(Consts.DATE_TRANSACTION, dateGetInfo)
                putString(Consts.STATUS_TRANSACTION, comingStatusTransaction)
                putString(Consts.WE_REFUSED_YOUR_OPERATION, weRefusedYourOperation)
                putParcelable(Consts.ALL_DATA, data)
                putCharSequence(Consts.MESSSAGE, mySpannable)
                putString(Consts.USERNAME, username)
            }
            return bundle
        }

        private fun ifTransactionThanks(data: HistoryItemModel.UserTransactionsModel) =
            data.transactionClass == HistoryItemModel.UserTransactionsModel.TransactionClass.THANKS ||
                    data.transactionClass == HistoryItemModel.UserTransactionsModel.TransactionClass.THANKS_FROM_SYSTEM ||
                    data.transactionClass == HistoryItemModel.UserTransactionsModel.TransactionClass.FIRED_THANKS

        private fun setStatusTransaction(data: HistoryItemModel.UserTransactionsModel) {
            binding.apply {
                when (data.transaction_status) {
                    HistoryItemModel.UserTransactionsModel.TransactionStatus.WAITING -> {
                    }

                    HistoryItemModel.UserTransactionsModel.TransactionStatus.APPROVED,
                    HistoryItemModel.UserTransactionsModel.TransactionStatus.READY -> {
                        statusTransferTextView.text =
                            binding.root.context.getString(R.string.occured)
                        statusTransferTextView.setBackgroundColor(binding.root.context.getColor(R.color.minor_success))
                        statusCard.setCardBackgroundColor(binding.root.context.getColor(R.color.minor_success))
                        comingStatusTransaction = binding.root.context.getString(R.string.occured)
                    }

                    HistoryItemModel.UserTransactionsModel.TransactionStatus.DECLINED -> {
                        comingStatusTransaction =
                            binding.root.context.getString(R.string.operationWasRefused)
                        statusTransferTextView.text =
                            binding.root.context.getString(R.string.refused)
                        weRefusedYourOperation =
                            binding.root.context.getString(R.string.weRefusedYourOperation)
                        statusTransferTextView.setBackgroundColor(binding.root.context.getColor(R.color.minor_error))
                        statusCard.setCardBackgroundColor(binding.root.context.getColor(R.color.minor_error))
                    }

                    HistoryItemModel.UserTransactionsModel.TransactionStatus.INGRACE -> {
                        statusTransferTextView.text = binding.root.context.getString(R.string.G)
                        statusTransferTextView.setBackgroundColor(binding.root.context.getColor(R.color.minor_warning))
                        statusCard.setCardBackgroundColor(binding.root.context.getColor(R.color.minor_warning))
                        comingStatusTransaction = binding.root.context.getString(R.string.G)
                    }

                    HistoryItemModel.UserTransactionsModel.TransactionStatus.CANCELLED -> {
                        statusTransferTextView.text =
                            binding.root.context.getString(R.string.refused)
                        statusTransferTextView.setBackgroundColor(binding.root.context.getColor(R.color.minor_error))
                        statusCard.setCardBackgroundColor(binding.root.context.getColor(R.color.minor_error))
                        descr_transaction_1 =
                            binding.root.context.getString(R.string.youWantedToSend)
                        weRefusedYourOperation =
                            binding.root.context.getString(R.string.iRefusedMyOperation)
                        comingStatusTransaction =
                            binding.root.context.getString(R.string.operationWasRefused)
                    }

                    HistoryItemModel.UserTransactionsModel.TransactionStatus.UNKNOW -> {}
                }
            }
        }


        private fun transactionToAnotherProfile(
            username: String?,
            data: HistoryItemModel.UserTransactionsModel
        ) {
            if (data.sender?.sender_tg_name == username) {
                userId = data.recipient_id
            } else if ((data.sender?.sender_tg_name != ANONYMOUS && data.recipient?.recipient_tg_name == username)) {
                userId = data.sender_id
            }

            binding.tgNameUser.setOnClickListener { view ->
                val bundle = Bundle()
                if (userId != 0) {
                    userId?.let {
                        bundle.putInt("userId", it)
                        view.findNavController().navigateSafely(
                            R.id.action_global_someonesProfileFragment,
                            bundle, OptionsTransaction().optionForProfileFragment
                        )
                    }
                }

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HistoryItemModel.UserTransactionsModel -> R.layout.item_transfer
            is HistoryItemModel.DateTimeSeparator -> R.layout.separator_date_time
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }

    companion object {
        const val TAG = "HistoryPageAdapter"
        const val ANONYMOUS = "anonymous"

        private val DiffCallback = object : DiffUtil.ItemCallback<HistoryItemModel>() {
            override fun areItemsTheSame(
                oldItem: HistoryItemModel,
                newItem: HistoryItemModel
            ): Boolean {
                return (oldItem is HistoryItemModel.UserTransactionsModel &&
                        newItem is HistoryItemModel.UserTransactionsModel &&
                        oldItem.id == newItem.id) ||
                        (oldItem is HistoryItemModel.DateTimeSeparator &&
                                newItem is HistoryItemModel.DateTimeSeparator &&
                                oldItem.uuid == newItem.uuid)
            }

            override fun areContentsTheSame(
                oldItem: HistoryItemModel,
                newItem: HistoryItemModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}