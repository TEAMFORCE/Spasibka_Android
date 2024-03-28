package com.teamforce.thanksapp.presentation.adapter.transactions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorFilter
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teamforce.thanksapp.data.api.ImageFileData
import com.teamforce.thanksapp.data.api.toIndexedList
import com.teamforce.thanksapp.databinding.ImageWithCrossBinding
import com.teamforce.thanksapp.presentation.fragment.newTransactionScreen.ItemMoveCallback
import com.teamforce.thanksapp.utils.BitmapUtils
import com.teamforce.thanksapp.utils.Extensions.toMultipart
import com.teamforce.thanksapp.utils.animateElevation
import com.teamforce.thanksapp.utils.animateScale
import com.teamforce.thanksapp.utils.branding.Branding
import com.teamforce.thanksapp.utils.getFilePathFromUri
import com.teamforce.thanksapp.utils.glide.setImageFromStorage
import okhttp3.MultipartBody
import java.io.File
import java.util.Collections

class ImageAdapter(
    private var photos: MutableList<AdapterItem>,
    private val onCrossClicked: (position: Int) -> Unit,
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>(), ItemMoveCallback.ItemTouchHelperContract {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageWithCrossBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ImageViewHolder(binding)
    }

    fun updateAdapter(listOfPhotos: MutableList<AdapterItem>) {
        photos.addAll(listOfPhotos)
        if (listOfPhotos.size == 0) notifyDataSetChanged()
        else {
            notifyItemRangeInserted(photos.size, itemCount)
        }
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        with(holder.binding) {
            val url = photos[position].picture
            image.setImageFromStorage(url) {
                setColorToCrossBtn(holder, it)
            }
            detachImage.tag = url
            detachImage.setOnClickListener {
                val ind = photos.indexOfFirst { it.picture == detachImage.tag }
                if (ind >= 0) {
                    onCrossClicked(ind)
                }
            }
        }

    }

    private fun setColorToCrossBtn(holder: ImageViewHolder, bitmap: Bitmap) {
        val isColorLight = BitmapUtils.isColorLight(bitmap)
        if (isColorLight) {
            holder.binding.detachImage.setColorFilter(Color.BLACK)
        } else {
            holder.binding.detachImage.setColorFilter(Color.WHITE)
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    class ImageViewHolder(val binding: ImageWithCrossBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(photos, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(photos, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: RecyclerView.ViewHolder) {
        myViewHolder.itemView.animateScale(1.1f)
        myViewHolder.itemView.animateElevation(20f)
    }

    override fun onRowClear(myViewHolder: RecyclerView.ViewHolder) {
        myViewHolder.itemView.animateScale(1f)
        myViewHolder.itemView.animateElevation(0f)
    }

    fun remove(position: Int) {
        photos = photos.toMutableList().apply {
            removeAt(position)
        }
        notifyItemRemoved(position)
    }

    fun getItems(): List<AdapterItem> = photos

    private var images: List<MultipartBody.Part>? = null
    private var fileList: List<ImageFileData>? = null
    private fun generatePhotosAndFileList(context: Context) {
        val fileNameMap = mutableMapOf<String, String>()
        images = this.getItems().filterIsInstance<AdapterItem.File>().map {
            val path = getFilePathFromUri(context, it.uri, false)
            val file = File(path)
            fileNameMap[it.fileName] = file.name
            it.uri.toMultipart(context, file.name)
        }
        fileList = this.getItems().toIndexedList().mapIndexed { index, imageFileData ->
            (imageFileData as? ImageFileData.FileData)?.copy(filename = fileNameMap[imageFileData.filename]!!)
                ?: imageFileData
        }

    }
    fun getPhotos(context: Context): List<MultipartBody.Part> {
        if (images == null) {
            generatePhotosAndFileList(context)
        }
        return images ?: emptyList()
    }

    fun getFileList(context: Context): List<ImageFileData> {
        if (fileList == null) {
            generatePhotosAndFileList(context)
        }
        return fileList ?: emptyList()
    }

    /*
    var fileList = imageAdapter.getItems().toIndexedList()
        val fileNameMap = mutableMapOf<String, String>()
        val photos = imageAdapter.getItems().filterIsInstance<AdapterItem.File>().map {
            val path = getFilePathFromUri(requireContext(), it.uri, true)
            val file = File(path)
            fileNameMap[it.fileName] = file.name
            it.uri.toMultipart(requireContext(), file.name)
        }

        fileList =
            fileList.mapIndexed { index, imageFileData ->
                (imageFileData as? ImageFileData.FileData)?.copy(filename = fileNameMap[imageFileData.filename]!!)
                    ?: imageFileData
            }
     */
}

sealed class AdapterItem(open val picture: String) {
    data class File(val uri: Uri, override val picture: String, val fileName: String) :
        AdapterItem(picture)

    data class Image(override val picture: String, val index: Int) : AdapterItem(picture)
}