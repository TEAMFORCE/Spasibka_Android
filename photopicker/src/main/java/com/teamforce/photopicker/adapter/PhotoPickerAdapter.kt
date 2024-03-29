package com.teamforce.photopicker.adapter

import android.R
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teamforce.photopicker.databinding.ViewPickableImageBinding
import com.teamforce.photopicker.databinding.ViewCameraElementBinding
import com.teamforce.photopicker.loader.ImageLoader

internal class ImagePickerAdapter(
    private val onImageClick: (SelectableImage) -> Unit,
    private val onCameraClick: () -> Unit,
    private val multiple: Boolean,
    private val imageLoader: ImageLoader,
    private val mainColor: String
) : ListAdapter<SelectableImage, RecyclerView.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PhotoViewType -> {
                val binding = ViewPickableImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ImagePickerViewHolder(binding)
            }
            else -> {
                val binding = ViewCameraElementBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ImageCameraViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ImagePickerViewHolder -> {
                holder.binding.apply {
                    theming(this)
                    imageLoader.loadImage(root.context, photoItem, item.uri)
                    root.setOnClickListener { onImageClick(getItem(position)) }
                    checkbox.backgroundTintList = checkBoxColor
                    checkbox.isChecked = item.selected
                    number.text = if (item.selected && item.number != null) item.number.toString() else ""
                }
            }
            is ImageCameraViewHolder -> {
                holder.binding.root.setOnClickListener { onCameraClick() }
                val rippleDrawable = RippleDrawable(ColorStateList.valueOf(Color.parseColor(mainColor)), null, null)
                holder.binding.photoItem.foreground = rippleDrawable
            }
        }
    }

    private fun theming(viewPickableImageBinding: ViewPickableImageBinding) {
        viewPickableImageBinding.apply {
            checkbox.backgroundTintList = checkBoxColor
            val rippleDrawable = RippleDrawable(ColorStateList.valueOf(Color.parseColor(mainColor)), null, null)
            photoItem.foreground = rippleDrawable
        }
    }

    val checkBoxColor = ColorStateList(
        arrayOf(
            intArrayOf(R.attr.state_checked),
            intArrayOf(-R.attr.state_checked),
        ), intArrayOf(
            Color.parseColor(mainColor),
            Color.TRANSPARENT
        )
    )

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> CameraViewType
            else -> PhotoViewType
        }
    }

    class ImagePickerViewHolder(val binding: ViewPickableImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ImageCameraViewHolder(val binding: ViewCameraElementBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val SELECTED_PAYLOAD = "selected_payload"
        private const val SELECTED_NUMBER_CHANGED_PAYLOAD = "selected_number_changed_payload"

        private val DiffCallback = object : DiffUtil.ItemCallback<SelectableImage>() {
            override fun areItemsTheSame(
                oldItem: SelectableImage,
                newItem: SelectableImage
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: SelectableImage,
                newItem: SelectableImage
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: SelectableImage,
                newItem: SelectableImage
            ): Any? = when {
                oldItem.selected != newItem.selected -> SELECTED_PAYLOAD
                oldItem.number != newItem.number -> SELECTED_NUMBER_CHANGED_PAYLOAD
                else -> null
            }
        }

        private const val PhotoViewType = 1
        private const val CameraViewType = 2
    }
}
