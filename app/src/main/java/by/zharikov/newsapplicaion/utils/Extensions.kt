package by.zharikov.newsapplicaion.utils

import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.zharikov.newsapplicaion.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


fun ComponentActivity.showAlert(
    title: Int,
    view: View,
    positiveButtonResId: Int = R.string.positive_button,
    negativeButtonResId: Int = R.string.negative_button,
    positiveButtonFun: () -> Unit,
    negativeButtonFun: () -> Unit
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setView(view)
        .setPositiveButton(
            positiveButtonResId
        ) { dialog, _ ->
            positiveButtonFun()
            dialog?.dismiss()
        }
        .setNegativeButton(
            negativeButtonResId
        ) { dialog, _ ->
            negativeButtonFun()
            dialog?.dismiss()
        }
        .show()
}

fun <T> ComponentActivity.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {

    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun <T> Fragment.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        Log.d("UI_ARTICLE_SIZE", "LIFECYCLE_SCOPE")
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun ComponentActivity.showSnackBar(msg: String, view: View) {
    Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        .show()
}

fun ComponentActivity.showSnackBarWithAction(
    msg: String,
    view: View,
    resource: Int,
    onAction: () -> Unit
) {
    Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        .setAction(resource) {
            onAction()
        }
        .show()
}


fun Fragment.showSnackBar(msg: String, view: View) {
    Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        .show()
}
