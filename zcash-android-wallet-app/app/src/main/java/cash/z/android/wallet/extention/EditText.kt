package cash.z.android.wallet.extention

import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.getSystemService

inline fun EditText.afterTextChanged(crossinline block: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            block.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

inline fun EditText.doOnDone(crossinline block: (String) -> Unit) {
    setOnEditorActionListener { v, actionId, _ ->
        return@setOnEditorActionListener if ((actionId == EditorInfo.IME_ACTION_DONE)) {
            v.clearFocus()
//            v.clearComposingText()
            v.context.getSystemService<InputMethodManager>()
                ?.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            block(this.text.toString())
            true
        } else {
            false
        }
    }
}

inline fun EditText.doOnFocusLost(crossinline block: (String) -> Unit) {
    setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) block(this.text.toString())
    }
}

inline fun EditText.doOnDoneOrFocusLost(crossinline block: (String) -> Unit) {
    doOnDone(block)
    doOnFocusLost(block)
}