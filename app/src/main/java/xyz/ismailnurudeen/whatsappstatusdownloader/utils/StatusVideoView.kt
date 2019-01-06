package xyz.ismailnurudeen.whatsappstatusdownloader.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView


class StatusVideoView : VideoView {

    private var mListener: PlayPauseListener? = null
    var isPaused: Boolean = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setPlayPauseListener(listener: PlayPauseListener) {
        mListener = listener
    }

    override fun pause() {
        super.pause()
        if (mListener != null) {
            mListener!!.onPause()
            isPaused = true
        }
    }

    override fun start() {
        super.start()
        if (mListener != null) {
            mListener!!.onPlay()
            isPaused = false
        }
    }

    override fun resume() {
        super.resume()
        isPaused = false
    }

    interface PlayPauseListener {
        fun onPlay()
        fun onPause()
    }

}
