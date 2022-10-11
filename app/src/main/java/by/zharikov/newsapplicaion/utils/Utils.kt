package by.zharikov.newsapplicaion.utils

import androidx.fragment.app.Fragment
import by.zharikov.newsapplicaion.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


fun Fragment.showAlert(
    title: Int,
    message: String,
    positiveButtonResId: Int = R.string.positive_button,
    negativeButtonResId: Int = R.string.negative_button,
    positiveButtonFun: (() -> Unit)? = null,
    negativeButtonFun: (() -> Unit)? = null
) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(
            positiveButtonResId
        ) { dialog, _ ->
            positiveButtonFun?.let { it() }
            dialog?.dismiss()
        }
        .setNegativeButton(
            negativeButtonResId
        ) { dialog, _ ->
            negativeButtonFun?.let { it() }
            dialog?.dismiss()
        }
        .show()
}
