/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.guide

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Window
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager

class Tip : AppCompatDialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val dialog = AppCompatDialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_tips)

        val window = dialog.window
        window?.setBackgroundDrawableResource(R.drawable.bg_semitransparent_rounded)

        val button = dialog.findViewById<ImageButton>(R.id.tip_dismiss)!!
        button.setOnClickListener { dialog.cancel() }
        return dialog
    }

    companion object {

        private fun newInstance(): Tip {
            return Tip()
        }

        /**
         * Show tips
         */
        @JvmStatic
        fun show(fragmentManager: FragmentManager) {
            val newFragment: AppCompatDialogFragment = newInstance()
            newFragment.show(fragmentManager, "dialog")
        }
    }
}
