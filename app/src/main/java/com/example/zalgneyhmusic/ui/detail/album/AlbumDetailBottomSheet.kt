package com.example.zalgneyhmusic.ui.detail.album

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.example.zalgneyhmusic.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumDetailBottomSheet : BottomSheetDialogFragment() {

    private var albumId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Áp dụng theme mới đã cấu hình
        setStyle(STYLE_NORMAL, R.style.CustomDetailBottomSheetDialogTheme)
        albumId = arguments?.getString(ARG_ALBUM_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO: Bạn cần tạo file layout fragment_album_detail.xml
        return LayoutInflater.from(context).inflate(R.layout.fragment_album_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        // Lấy Dialog và View cha (design_bottom_sheet)
        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return

        // 1. Cài đặt chiều cao
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        // 2. Lấy behavior từ bottomSheet (View cha) - ĐÂY LÀ CÁCH ĐÚNG
        val behavior = BottomSheetBehavior.from(bottomSheet)

        behavior.isFitToContents = false
        behavior.peekHeight = (screenHeight * 0.6).toInt()
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // 3. Thiết lập hiệu ứng
        setupCornerTransition(bottomSheet, behavior)
    }

    private fun setupCornerTransition(bottomSheet: View, behavior: BottomSheetBehavior<View>) {
        val radius = resources.getDimension(R.dimen.bottom_sheet_corner_radius)

        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, radius)
            .setTopRightCorner(CornerFamily.ROUNDED, radius)
            .build()

        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
            fillColor = ColorStateList.valueOf(Color.WHITE)
        }

        ViewCompat.setBackground(bottomSheet, materialShapeDrawable)

        // Dùng behavior đã được truyền vào
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val currentOffset = if (slideOffset < 0) 0f else slideOffset
                val newRadius = radius * (1f - currentOffset)

                materialShapeDrawable.shapeAppearanceModel = materialShapeDrawable.shapeAppearanceModel
                    .toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, newRadius)
                    .setTopRightCorner(CornerFamily.ROUNDED, newRadius)
                    .build()
            }
        })
    }

    companion object {
        private const val ARG_ALBUM_ID = "album_id"
        fun newInstance(albumId: String) = AlbumDetailBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_ALBUM_ID, albumId)
            }
        }
    }
}