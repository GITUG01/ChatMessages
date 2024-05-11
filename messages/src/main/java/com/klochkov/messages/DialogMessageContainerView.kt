package com.klochkov.messages

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView

abstract class DialogMessageContainerView(context: Context, attrs: AttributeSet?) :
    DialogMessageView(context, attrs),
    MessageInterface {

    /**
     * attrs
     */
    private var gravity: Int = Gravity.START

    private var containerColor = 0

    private var replyContainerColor = 0

    private var replyDividingLineColor = 0

    private var replyTitleTextColor = 0

    private var replyDescriptionTextColor = 0

    var timeTextColor = 0

    var isChecksEnabled = false

    private var tailResource = 0

    private var cornerRadius = 0F

    private var cardStartMargin = 0F

    private var cardTopMargin = 0F

    private var cardRightMargin = 0F

    private var cardBottomMargin = 0F

    private var tailView: View? = null
    private var cardViewContainer: LinearLayout? = null

    private var replyMessageLinearLayout: View? = null
    private var verticalDividingLine: CardView? = null
    private var titleTextView: TextView? = null
    private var contentTextView: TextView? = null


    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DialogMessageContainerView,
            0, 0
        ).apply {

            try {
                gravity = getInteger(
                    R.styleable.DialogMessageContainerView_android_gravity,
                    Gravity.START
                )
                containerColor = getInteger(
                    R.styleable.DialogMessageContainerView_containerColor,
                    0
                )
                replyContainerColor = getInteger(
                    R.styleable.DialogMessageContainerView_replyContainerColor,
                    R.color.blue
                )
                replyDividingLineColor = getInteger(
                    R.styleable.DialogMessageContainerView_replyDividingLineColor,
                    Color.WHITE
                )
                replyTitleTextColor = getInteger(
                    R.styleable.DialogMessageContainerView_replyTitleTextColor,
                    Color.WHITE
                )
                replyDescriptionTextColor = getInteger(
                    R.styleable.DialogMessageContainerView_replyDescriptionTextColor,
                    Color.WHITE
                )
                timeTextColor = getInteger(
                    R.styleable.DialogMessageContainerView_timeTextColor,
                    0
                )
                isChecksEnabled =
                    getBoolean(R.styleable.DialogMessageContainerView_isCheckEnabled, false)
                tailResource = getResourceId(R.styleable.DialogMessageContainerView_tailView, 0)
                cornerRadius = getDimension(R.styleable.DialogMessageContainerView_cornerRadius, 0F)
                cardStartMargin =
                    getDimension(R.styleable.DialogMessageContainerView_cardStartMargin, 0F)
                cardTopMargin =
                    getDimension(R.styleable.DialogMessageContainerView_cardTopMargin, 0F)
                cardRightMargin =
                    getDimension(R.styleable.DialogMessageContainerView_cardEndMargin, 0F)
                cardBottomMargin =
                    getDimension(R.styleable.DialogMessageContainerView_cardBottomMargin, 0F)
            } finally {
                recycle()
            }
        }

        inflateContentContainer()
    }

    private fun inflateContentContainer() {
        // Create views with weight
        val mainLinearLayoutContainer = LinearLayout(context)
        mainLinearLayoutContainer.orientation = LinearLayout.HORIZONTAL
        mainLinearLayoutContainer.elevation = 1F

        val contentContainer = FrameLayout(context)
        val contentLayoutParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        contentLayoutParams.setMargins(
            cardStartMargin.toInt(),
            cardTopMargin.toInt(),
            cardRightMargin.toInt(),
            cardBottomMargin.toInt()
        )
        contentLayoutParams.weight = 85F
        contentContainer.layoutParams = contentLayoutParams

        val emptyContainer = View(context)
        val emptyLayoutParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        emptyLayoutParams.weight = 15F
        emptyContainer.layoutParams = emptyLayoutParams

        // Create content views
        val cardView = CardView(context)
        val cardViewLayoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        cardViewLayoutParams.gravity = gravity

        cardView.layoutParams = cardViewLayoutParams

        cardView.radius = cornerRadius
        cardView.setCardBackgroundColor(containerColor)
        cardView.cardElevation = 0F
        cardView.elevation = 0F

        // Create cardView container linear layout
        cardViewContainer = LinearLayout(context)
        cardViewContainer?.orientation = LinearLayout.VERTICAL
        val cardViewContainerLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardViewContainer?.layoutParams = cardViewContainerLayoutParams

        cardView.addView(cardViewContainer)

        contentContainer.addView(cardView)

        // Place content according to gravity
        if (gravity == Gravity.START) {
            mainLinearLayoutContainer.addView(contentContainer)
            mainLinearLayoutContainer.addView(emptyContainer)
        } else if (gravity == Gravity.END) {
            mainLinearLayoutContainer.addView(emptyContainer)
            mainLinearLayoutContainer.addView(contentContainer)
        }

        container.addView(mainLinearLayoutContainer)
    }

    private fun inflateTail() {
        if (tailView == null) {
            tailView = View(context)
            tailView!!.setBackgroundResource(tailResource)

            val layoutParams = LayoutParams(
                11.dpToPx(),
                9.dpToPx()
            )
            layoutParams.gravity = gravity or Gravity.BOTTOM
            tailView!!.elevation = 0F

            if (gravity == Gravity.END) {
                layoutParams.marginEnd = cardRightMargin.toInt() - 1.dpToPx()
            } else {
                layoutParams.marginStart = cardStartMargin.toInt() - 1.dpToPx()
            }

            tailView!!.layoutParams = layoutParams

            container.addView(tailView)

            tailView?.visibility = View.INVISIBLE
        }
    }

    private fun showReplyContainer(title: String, description: String) {
        if (replyMessageLinearLayout == null) {
            replyMessageLinearLayout = View.inflate(context, R.layout.reply_message_layout, null)

            verticalDividingLine =
                replyMessageLinearLayout!!.findViewById(R.id.message_reply_vertical_line_card_view)
            titleTextView =
                replyMessageLinearLayout!!.findViewById(R.id.message_reply_title_text_view)
            contentTextView =
                replyMessageLinearLayout!!.findViewById(R.id.message_reply_content_text_view)

            replyMessageLinearLayout!!.setBackgroundColor(replyContainerColor)
            verticalDividingLine!!.setBackgroundColor(replyDividingLineColor)
            titleTextView!!.setTextColor(replyTitleTextColor)
            contentTextView!!.setTextColor(replyDescriptionTextColor)

            cardViewContainer?.addView(replyMessageLinearLayout, 0)
        }

        verticalDividingLine?.setCardBackgroundColor(Color.WHITE)
        titleTextView?.text = title
        contentTextView?.text = description

        replyMessageLinearLayout?.visibility = View.VISIBLE
    }

    fun addMessageContent(view: View) {
        cardViewContainer?.addView(view)
    }

    override fun showTail() {
        inflateTail()

        tailView?.visibility = View.VISIBLE
    }

    override fun hideTail() {
        tailView?.visibility = View.INVISIBLE
    }

    override fun showReply(title: String, description: String) {
        showReplyContainer(title, description)
    }

    override fun hideReply() {
        replyMessageLinearLayout?.visibility = View.INVISIBLE
    }

    override fun setBottomSpacing(pixels: Int) {

    }

    private fun Int.dpToPx(): Int {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics)
            .toInt()
    }

}