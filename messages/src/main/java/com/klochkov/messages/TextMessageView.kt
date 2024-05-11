package com.klochkov.messages

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView

class TextMessageView(context: Context, attrs: AttributeSet?) :
    DialogMessageContainerView(context, attrs) {

    /**
     * attrs
     */
    private var textColor = 0

    private var textSize = 0F

    /**
     * fields
     */
    var checkStatus: CheckStatus? = null
        set(value) {
            field = value

            checkedView?.setBackgroundResource(value?.iconRes ?: return)
        }

    var contentText = "Hello text"
        set(value) {
            field = value

            textView?.text = value
        }

    var timeText = "00:00"
        set(value) {
            field = value

            timeTextView?.text = value
        }

    /**
     * views
     */
    private var textView: TextView? = null
    private var timeTextView: TextView? = null
    private var checkedView: View? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TextMessageView,
            0, 0
        ).apply {

            try {
                textColor = getInteger(
                    R.styleable.TextMessageView_android_textColor,
                    0
                )
                textSize = getDimension(R.styleable.TextMessageView_android_textSize, 0F)
            } finally {
                recycle()
            }
        }

        inflateContent()
    }


    private fun inflateContent() {
        val messageLinearLayout = View.inflate(context, R.layout.text_message_content, null)

        textView = messageLinearLayout.findViewById(R.id.text_message_text_view)
        timeTextView = messageLinearLayout.findViewById(R.id.text_message_time_text_view)
        checkedView = messageLinearLayout.findViewById(R.id.text_message_checked_view)

        if (isChecksEnabled) {
            checkedView?.visibility = View.VISIBLE
        } else {
            checkedView?.visibility = View.GONE
        }
        textView?.setTextColor(textColor)
        textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        timeTextView?.setTextColor(timeTextColor)

        addMessageContent(messageLinearLayout)
    }
}