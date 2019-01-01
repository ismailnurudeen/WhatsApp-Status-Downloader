package xyz.ismailnurudeen.whatsappstatusdownloader.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_preview.view.*
import xyz.ismailnurudeen.whatsappstatusdownloader.PreviewActivity
import xyz.ismailnurudeen.whatsappstatusdownloader.R
import java.io.File

class PreviewFragment : Fragment(), PreviewActivity.PreviewReadyListener {
    var position: Int = -1
    private var statuses: MutableCollection<File>? = null
    private lateinit var preview: View
    var _isFirstLoad = false
    var mListener: PreviewActivity.PreviewReadyListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        position = args?.getInt("POSITION") ?: position
        statuses = statusList
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_preview, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        preview = v
        if (isVisible && userVisibleHint) {
            selectPreviewType(preview, statuses!!.elementAt(position))
            Toast.makeText(context, "position ${position} is Visible", Toast.LENGTH_SHORT).show()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisible && isVisibleToUser) {
            if (!_isFirstLoad) {
                selectPreviewType(preview, statuses!!.elementAt(position))
                _isFirstLoad = true
            }
        }
    }

    private fun selectPreviewType(preview: View, file: File) {
        if (preview.video_preview.isPlaying || preview.video_preview.isActivated) preview.video_preview.stopPlayback()
        if (file.extension.contains("jpg", true)) {
            preview.image_preview.visibility = View.VISIBLE
            preview.video_preview.visibility = View.GONE
            Glide.with(context!!)
                    .load(file)
                    .into(preview.image_preview)
        } else {
            preview.video_preview.visibility = View.VISIBLE
            preview.image_preview.visibility = View.GONE
            preview.video_preview.setVideoPath(file.absolutePath)
            preview.video_preview.start()
        }
    }

    override fun onPreviewReady(pos: Int) {
        selectPreviewType(preview, statuses!!.elementAt(pos))
        Toast.makeText(context, "position ${pos} has been scrolled to", Toast.LENGTH_SHORT).show()
    }

    companion object {
        var statusList: MutableCollection<File>? = null
        fun newInstance(position: Int): PreviewFragment {
            val pf = PreviewFragment()
            val args = Bundle()
            args.putInt("POSITION", position)
            pf.arguments = args
            return pf
        }
    }
}