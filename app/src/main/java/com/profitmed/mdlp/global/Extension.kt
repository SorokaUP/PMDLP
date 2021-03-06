package com.profitmed.mdlp.ui

import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showToast(msg: String) {
    Toast.makeText(this.context, msg, Toast.LENGTH_SHORT).show()
}

fun View.showSnackBar(
    text: String,
    actionText: String,
    action: (View) -> Unit,
    length: Int = Snackbar.LENGTH_INDEFINITE
) {
    Snackbar.make(this, text, length).setAction(actionText, action).show()
}

private const val PREFIX_SSCC = "00"
private const val PREFIX_SGTIN = "01"
private const val POSTIX_SGTIN = '='

fun String.checkKIZ(isSGTIN: Boolean): Boolean {
    if (this.isEmpty())
        return false

    if (isSGTIN) {
        if (this.length < 27)
            return false
        if (this.substring(0,2) != PREFIX_SGTIN && this.substring(1,3) != PREFIX_SGTIN)
            return false
        if (this[this.length-1] != POSTIX_SGTIN)
            return false

        return true
    }
    else {
        if (this.length < 18)
            return false
        if (this.substring(0,2) != PREFIX_SSCC)
            return false

        return true
    }
}

private fun ConstraintLayout.pmStartAnimation() {
    val animOn = AlphaAnimation(0.0f, 1.0f).apply {
        this.duration = 500
        this.startOffset = 200
        this.fillAfter = true
    }
    this.startAnimation(animOn)

    val animOff = AlphaAnimation(1.0f, 0.0f).apply {
        this.duration = 500
        this.startOffset = 500
        this.fillAfter = true
    }
    this.startAnimation(animOff)
}