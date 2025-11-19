package com.example.zalgneyhmusic.ui.utils

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.example.zalgneyhmusic.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

fun setupFullHeightBottomSheet(
    dialog: BottomSheetDialog?,
    resources: Resources
) {
    // check null safety
    val bottomSheetDialog = dialog ?: return
    val bottomSheet =
        bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return

    // set view height MATCH_PARENT
    bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

    // Behavior
    val behavior = BottomSheetBehavior.from(bottomSheet)
    behavior.isFitToContents = false
    // set peek height = 60% screen height
    behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()
    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    setupBottomSheetCornerTransition(bottomSheet, behavior, resources)
}

/**
 * Utility function to setup corner radius transition for BottomSheet
 */
fun setupBottomSheetCornerTransition(
    bottomSheet: View,
    behavior: BottomSheetBehavior<View>,
    resources: Resources
) {
    val radius = resources.getDimension(R.dimen.bottom_sheet_corner_radius)

    val shapeAppearanceModel = ShapeAppearanceModel.builder()
        .setTopLeftCorner(CornerFamily.ROUNDED, radius)
        .setTopRightCorner(CornerFamily.ROUNDED, radius)
        .build()

    val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
        fillColor = ColorStateList.valueOf(Color.WHITE)
    }

    bottomSheet.background = materialShapeDrawable

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