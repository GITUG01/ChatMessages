package com.klochkov.messages

interface MessageInterface {

    fun showTail()
    fun hideTail()
    fun showReply(title: String, description: String)
    fun hideReply()
    fun setBottomSpacing(pixels: Int)

}