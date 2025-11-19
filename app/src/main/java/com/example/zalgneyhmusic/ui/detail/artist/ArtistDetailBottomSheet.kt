package com.example.zalgneyhmusic.ui.detail.artist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zalgneyhmusic.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

class ArtistDetailBottomSheet : BottomSheetDialogFragment() {
    private var artistId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        artistId = arguments?.getString(ARG_ARTIST_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ví dụ: return inflater.inflate(R.layout.fragment_artist_detail, container, false)
        return inflater.inflate(R.layout.fragment_artist_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCornerTransition(view)
    }

    override fun onStart() {
        super.onStart()

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            // Đảm bảo Bottom Sheet có thể mở rộng full height
            isFitToContents = false
            state = BottomSheetBehavior.STATE_EXPANDED
            // Nếu bạn muốn Bottom Sheet luôn ở trạng thái full height ngay từ đầu
            // bạn phải đặt chiều cao của nó là MATCH_PARENT trong onStart
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

     fun setupCornerTransition(rootView: View) {
        val behavior = (dialog as? BottomSheetDialog)?.behavior ?: return

        // Giá trị bán kính góc bo ban đầu (24dp)
        val initialCornerRadius = resources.getDimension(R.dimen.bottom_sheet_corner_radius)

        // Lấy background của sheet view để can thiệp vào shape
        val sheet = rootView.parent as View
        val shapeDrawable = sheet.background as? MaterialShapeDrawable

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Có thể bỏ qua logic này nếu chỉ dùng onSlide
            }

            // Đây là phần quan trọng nhất: Xử lý sự chuyển đổi mượt mà
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // slideOffset: 0.0 (Thu gọn) -> 1.0 (Mở rộng)

                // Chúng ta muốn bán kính góc bo giảm từ initialCornerRadius về 0 khi slideOffset tiến về 1.0
                if (shapeDrawable != null) {
                    // Tính toán bán kính mới: Bán kính = Bán kính_ban_đầu * (1 - slideOffset)
                    val newRadius = initialCornerRadius * (1f - slideOffset)

                    // Áp dụng bán kính mới chỉ cho góc trên
                    shapeDrawable.shapeAppearanceModel = shapeDrawable.shapeAppearanceModel.toBuilder()
                        .setTopLeftCorner(CornerFamily.ROUNDED, newRadius)
                        .setTopRightCorner(CornerFamily.ROUNDED, newRadius)
                        .build()
                }
            }
        })
    }

    companion object {
        private const val ARG_ARTIST_ID = "artist_id"
        fun newInstance(artistId: String) = ArtistDetailBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_ARTIST_ID, artistId)
            }
        }
    }
}