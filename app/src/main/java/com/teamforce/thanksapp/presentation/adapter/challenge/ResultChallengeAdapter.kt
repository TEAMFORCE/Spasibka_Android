package com.teamforce.thanksapp.presentation.adapter.challenge

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.response.GetChallengeResultResponse
import com.teamforce.thanksapp.databinding.ItemResultChallengeBinding
import com.teamforce.thanksapp.utils.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ResultChallengeAdapter(
) : ListAdapter<GetChallengeResultResponse,
        ResultChallengeAdapter.ResultChallengeViewHolder>(DiffCallback) {

     var onImageClicked: ((view: View, photo: String) -> Unit)? = null


    companion object {
        object DiffCallback : DiffUtil.ItemCallback<GetChallengeResultResponse>() {
            override fun areItemsTheSame(
                oldItem: GetChallengeResultResponse,
                newItem: GetChallengeResultResponse
            ): Boolean {
                return oldItem.updated_at == newItem.updated_at
            }

            override fun areContentsTheSame(
                oldItem: GetChallengeResultResponse,
                newItem: GetChallengeResultResponse
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultChallengeViewHolder {
        val binding = ItemResultChallengeBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ResultChallengeViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }


    inner class ResultChallengeViewHolder(val binding: ItemResultChallengeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ResultChallengeViewHolder, position: Int) {
        if (currentList[position].photo.isNullOrEmpty()) {
            holder.binding.showAttachedImgCard.invisible()
        } else {
            holder.binding.showAttachedImgCard.visible()
            Glide.with(holder.binding.root.context)
                .load("${Consts.BASE_URL}${currentList[position].photo}".toUri())
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.binding.image)

//            holder.binding.image.setOnClickListener { view ->
//                currentList[position].photo?.let { photo ->
//                    (view as ImageView).viewSinglePhoto(photo, holder.binding.root.context)
//                }
//            }
            holder.binding.image.setOnClickListener{ view ->
                currentList[position].photo?.let { photo ->
                    onImageClicked?.invoke(view, photo)
                }
            }
        }
        with(holder.binding) {
            stateEt.text = currentList[position].status
            dateEt.text = parseDateTimeWithBindToTimeZone(currentList[position].updated_at, root.context)
            descriptionEt.text = currentList[position].text
            valueEt.text = currentList[position].received.toString()
            handleColorOfStatus(holder, position)
        }
    }

    private fun handleColorOfStatus(holder: ResultChallengeViewHolder, position: Int) {
        with(holder.binding) {
            if (currentList[position].status.contains("Получено вознаграждение") ||
                currentList[position].status.contains("Подтверждено")
            ) {
                stateEt.text = currentList[position].status
                cardStatus.setCardBackgroundColor(
                    root.context.getColor(R.color.minor_success_secondary)
                )
                stateEt.setTextColor(root.context.getColor(R.color.minor_success))
                valueEt.setTextColor(root.context.getColor(R.color.minor_success))
                imageCurrency.setColorFilter(
                    root.context.getColor(R.color.minor_success),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                cardStatus.setCardBackgroundColor(
                    root.context.getColor(R.color.minor_error_secondary)
                )
                stateEt.setTextColor(root.context.getColor(R.color.minor_error))
                valueEt.setTextColor(root.context.getColor(R.color.minor_error))
                stateEt.text = currentList[position].status
                imageCurrency.setColorFilter(
                    root.context.getColor(R.color.minor_error),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        }

    }
}