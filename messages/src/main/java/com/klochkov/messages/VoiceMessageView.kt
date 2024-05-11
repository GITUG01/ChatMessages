package com.klochkov.messages

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

class VoiceMessageView(context: Context, attrs: AttributeSet?) :
    DialogMessageContainerView(context, attrs) {

    /**
     * attrs
     */
    private var waveBackgroundColor = 0

    private var waveProgressColor = 0

    private var durationColor = 0

    private var playBtnResource = 0

    private var pauseBtnResource = 0

    /**
     * fields
     */
    var checkStatus: CheckStatus? = null
        set(value) {
            field = value

            checkedView?.setBackgroundResource(value?.iconRes ?: return)
        }

    var voiceDuration = "00:00"
        set(value) {
            field = value

            durationTime?.text = value
        }

    var voiceDurationInMills = 0L

    var timeText = "00:00"
        set(value) {
            field = value

            timeTextView?.text = value
        }

    var samples: IntArray? = null
        set(value) {
            field = value

            if (value != null) {
                messageSoundWaveSeekBar?.setSampleFrom(value)
            }
        }

    /**
     * views
     */
    private var messageSoundWaveSeekBar: WaveForm? = null
    private var playBtn: View? = null
    private var durationTime: TextView? = null
    private var timeTextView: TextView? = null
    private var checkedView: View? = null

    private var onPlayBtnClickListener: ((isPlaying: Boolean) -> Unit)? = null

    var seekProgress = 0

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.VoiceMessageView,
            0, 0
        ).apply {

            try {
                waveBackgroundColor = getInteger(
                    R.styleable.VoiceMessageView_waveBackgroundColor,
                    Color.LTGRAY
                )
                waveProgressColor = getInteger(
                    R.styleable.VoiceMessageView_waveProgressColor,
                    Color.WHITE
                )
                durationColor = getInteger(
                    R.styleable.VoiceMessageView_durationColor,
                    Color.WHITE
                )
                playBtnResource = getResourceId(
                    R.styleable.VoiceMessageView_playView,
                    R.drawable.my_message_voice_play
                )
                pauseBtnResource = getResourceId(
                    R.styleable.VoiceMessageView_pauseView,
                    R.drawable.my_message_voice_pause
                )
            } finally {
                recycle()
            }
        }

        inflateContent()
        setClickListeners()
        setOnProgressChangeListener()
    }

    private fun inflateContent() {
        val voiceMessageLayout = View.inflate(context, R.layout.voice_message_layout, null)

        messageSoundWaveSeekBar =
            voiceMessageLayout.findViewById(R.id.voice_message_sound_wave_seek_bar)
        playBtn = voiceMessageLayout.findViewById(R.id.voice_message_voice_play_btn)
        durationTime = voiceMessageLayout.findViewById(R.id.voice_message_voice_duration_text_view)
        timeTextView = voiceMessageLayout.findViewById(R.id.voice_message_time)
        checkedView = voiceMessageLayout.findViewById(R.id.voice_message_is_checked)

        timeTextView?.setTextColor(timeTextColor)
        durationTime?.setTextColor(durationColor)

        messageSoundWaveSeekBar?.waveBackgroundColor = waveBackgroundColor
        messageSoundWaveSeekBar?.waveProgressColor = waveProgressColor

        playBtn?.tag = 1

        if (isChecksEnabled) {
            checkedView?.visibility = View.VISIBLE
        } else {
            checkedView?.visibility = View.GONE
        }

        addMessageContent(voiceMessageLayout)
    }

    private fun setClickListeners() {
        playBtn?.setOnClickListener {
            val isPlaying = playBtn?.tag == 0
            onPlayBtnClickListener?.invoke(isPlaying)

            if (playBtn?.tag == 0) {
                playBtn?.tag = 1
                playBtn?.setBackgroundResource(playBtnResource)
                messageSoundWaveSeekBar?.pauseProgress()
            } else {
                playBtn?.tag = 0
                playBtn?.setBackgroundResource(pauseBtnResource)
                messageSoundWaveSeekBar?.startProgress(voiceDurationInMills) {
                    playBtn?.tag = 1
                    playBtn?.setBackgroundResource(playBtnResource)
                    seekProgress = 0
                }
            }
        }
    }

    private fun setOnProgressChangeListener() {
        messageSoundWaveSeekBar?.onProgressChanged =
            object : WaveFormOnProgressChangeListener {
                override fun onProgressChanged(progress: Float, fromUser: Boolean) {
                    seekProgress = progress.toInt()
                }
            }
    }

    fun resetProgress() {
        playBtn?.setBackgroundResource(playBtnResource)
        messageSoundWaveSeekBar?.pauseProgress()
        messageSoundWaveSeekBar?.progress = 100F
        playBtn?.tag = 1
        seekProgress = 0
    }

    fun setOnPlayButtonClickListener(listener: (isPlaying: Boolean) -> Unit) {
        onPlayBtnClickListener = listener
    }

}