package com.teamforce.thanksapp.presentation.customViews.photoSelectionView

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.teamforce.thanksapp.data.api.ImageFileData
import com.teamforce.thanksapp.databinding.ListImagesLayoutBinding
import com.teamforce.thanksapp.domain.models.branding.ColorsModel
import com.teamforce.thanksapp.presentation.adapter.transactions.AdapterItem
import com.teamforce.thanksapp.presentation.adapter.transactions.ImageAdapter
import com.teamforce.thanksapp.presentation.fragment.newTransactionScreen.ItemMoveCallback
import com.teamforce.thanksapp.presentation.theme.Themable
import com.teamforce.thanksapp.utils.Consts
import com.teamforce.thanksapp.utils.getFileFromContentUri
import okhttp3.MultipartBody

class PhotoSelectionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Themable {

    private var _binding: ListImagesLayoutBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }

    var photoSelectionListener: PhotoSelectionListener? = null
    private val imageAdapter: ImageAdapter =
        ImageAdapter(onCrossClicked = ::onCrossClicked, photos = mutableListOf())


    init {
        _binding = ListImagesLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        setupListeners()
        initRecycler()
    }

    private fun setupListeners(){
        binding.attachImageBtn.setOnClickListener {
            photoSelectionListener?.onAttachImageClicked(MAX_SELECTION, imageAdapter.getItems().size)
        }
    }

    private fun initRecycler() {
        val callback: ItemTouchHelper.Callback = ItemMoveCallback(imageAdapter)
        val touchHelper = ItemTouchHelper(callback)

        binding.list.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            this.adapter = imageAdapter
        }
        touchHelper.attachToRecyclerView(binding.list)
    }

    /**
     * Обновление списка выбранных картинок
     * @param listOfPhoto Список Uri картинок.
     */
    fun updateRecyclerView(listOfPhoto: List<Uri>) {
        imageAdapter.updateAdapter(listOfPhoto.toItems(context).toMutableList())
    }

    /**
     * Обновление списка выбранных картинок с URL-адресами.
     * @param listOfPhoto Список URL-адресов картинок.
     */
    fun updateRecyclerViewStringList(listOfPhoto: List<String>) {
        imageAdapter.updateAdapter(listOfPhoto.toItems().toMutableList())
    }

    fun getItems(): List<AdapterItem>{
        return imageAdapter.getItems()
    }

    fun getPhotos(): List<MultipartBody.Part>{
        return imageAdapter.getPhotos(context)
    }

    fun getFileList(): List<ImageFileData>{
        return imageAdapter.getFileList(context)
    }


    private fun onCrossClicked(position: Int) {
        // listOfImagesUri.removeAt(position)
        (binding.list.adapter as ImageAdapter).remove(position)
        // (binding.list.adapter as ImageAdapter).updateAdapter(listOfImagesUri, position)
    }

    override fun setThemeColor(theme: ColorsModel) {

    }

    companion object {
        private const val MAX_SELECTION = 5
    }


}

interface PhotoSelectionListener {
    fun onAttachImageClicked(maxSelection: Int, alreadySelected: Int)

}

fun List<Uri>.toFileNameList(context: Context): List<String> = mapNotNull { it.toFileName(context) }
fun Uri.toFileName(context: Context): String? {
    val uri = this

    return if (uri.toString().contains("file://")) {
        uri.path
    } else {
        getFileFromContentUri(context, uri, true).path
    }
}

fun List<Uri>.toItems(context: Context): List<AdapterItem.File> = mapNotNull {
    val image = it.toFileName(context) ?: return@mapNotNull null
    val fileName = it.lastPathSegment ?: return@mapNotNull null
    AdapterItem.File(it, image, fileName)
}

fun List<String>.toItems(): List<AdapterItem.Image> = mapNotNull {
    val ind = it.toUri().lastPathSegment?.toIntOrNull() ?: return@mapNotNull null
    AdapterItem.Image(Consts.BASE_URL.plus(it), ind)
}