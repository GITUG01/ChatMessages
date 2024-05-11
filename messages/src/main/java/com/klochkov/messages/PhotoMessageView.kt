package com.klochkov.messages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class PhotoMessageView(context: Context, attrs: AttributeSet?) :
    DialogMessageContainerView(context, attrs) {

    /**
     * fields
     */
    var timeText = "00:00"
        set(value) {
            field = value

            timeTextView?.text = value
            requestLayout()
        }


    /**
     * views
     */
    private var imageView: ImageView? = null
    private var timeTextView: TextView? = null

    init {
        inflateContent()
    }

    private fun inflateContent() {
        val photoMessageLayout = View.inflate(context, R.layout.photo_message_layout, null)

        imageView = photoMessageLayout.findViewById(R.id.photo_message_image_view)
        timeTextView = photoMessageLayout.findViewById(R.id.photo_message_time_text_view)


        addMessageContent(photoMessageLayout)
    }

    fun setImage(imageUrl: String) {
        Glide
            .with(imageView ?: return)
            .load(imageUrl)
            .into(imageView ?: return)
    }

}