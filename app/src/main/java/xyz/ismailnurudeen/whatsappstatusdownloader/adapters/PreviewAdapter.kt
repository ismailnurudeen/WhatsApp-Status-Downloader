package xyz.ismailnurudeen.whatsappstatusdownloader.adapters

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.v4.view.PagerAdapter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.MediaController
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_preview.view.*
import xyz.ismailnurudeen.whatsappstatusdownloader.MainActivity
import xyz.ismailnurudeen.whatsappstatusdownloader.R
import xyz.ismailnurudeen.whatsappstatusdownloader.utils.AppUtil
import java.io.File
import java.util.*


class PreviewAdapter(val context: Context, val allStatuses: MutableCollection<File>, val previewTitles: ArrayList<String>, val slideCompleteListener: OnSlideCompleteListener) : PagerAdapter() {
    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    private var mSlideShow = sharedPrefs.getBoolean(context.getString(R.string.do_slide_show_key), true)
    private var mSlideShowTime = sharedPrefs.getString(context.getString(R.string.slide_show_time_key), "30").toInt()
    private var mShowVideoControls = sharedPrefs.getBoolean(context.getString(R.string.show_video_controls_key), false)

    override
    fun isViewFromObject(v: View, obj: Any): Boolean {
        return v == obj as ConstraintLayout
    }

    override fun getCount(): Int {
        return allStatuses.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_preview, container, false)

        selectPreviewType(itemView, allStatuses.elementAt(position))
        container.addView(itemView)

        itemView.video_preview.setOnTouchListener { v, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                itemView.preview_toolbar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                controller?.show()
            } else if (event.action == KeyEvent.ACTION_UP) {
                itemView.preview_toolbar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
            }
            true
        }
        itemView.image_preview.setOnTouchListener { v, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                itemView.preview_toolbar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
            } else if (event.action == KeyEvent.ACTION_UP) {
                itemView.preview_toolbar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
            }
            true
        }

        itemView.toolbar_time_left.text = previewTitles[position]
        itemView.preview_download.setOnClickListener {
            AppUtil(context).renameFileAndDownload(position)
        }
        itemView.toolbar_action_back.setOnClickListener {
            val backIntent = Intent(context, MainActivity::class.java)
            backIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(backIntent)
        }
        itemView.preview_action_menu.setOnClickListener {
            val menu = PopupMenu(context, it)
            val inflater = menu.menuInflater
            inflater.inflate(R.menu.preview_menu, menu.menu)
            menu.show()

        }
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as ConstraintLayout)
    }

    private var controller: MediaController? = null

    fun selectPreviewType(preview: View, file: File) {
        if (preview.video_preview.isPlaying || preview.video_preview.isActivated) preview.video_preview.stopPlayback()
        if (file.extension.contains("jpg", true)) {
            preview.image_preview.visibility = View.VISIBLE
            preview.video_preview.visibility = View.GONE
            Glide.with(context)
                    .load(file)
                    .into(preview.image_preview)
        } else {
            preview.video_preview.visibility = View.VISIBLE
            preview.image_preview.visibility = View.GONE
            preview.video_preview.setVideoPath(file.absolutePath)

            if (mShowVideoControls) {
                controller = MediaController(context)
                preview.video_preview.setMediaController(controller)
            }
            preview.video_preview.start()
          //  mSlideShowTime = preview.video_preview.duration
        }
        preview.toolbar_progressBar.visibility = View.INVISIBLE
        if (mSlideShow) {
            preview.toolbar_progressBar.visibility = View.VISIBLE
            animate_slider(preview, allStatuses.indexOf(file))
        }
    }

    fun animate_slider(preview: View, pos: Int) {
        val progressAnimator = ObjectAnimator.ofInt(preview.toolbar_progressBar, "progress", 0, 100)
        progressAnimator.duration = mSlideShowTime.toLong() * 1000
        progressAnimator.interpolator = DecelerateInterpolator()
        progressAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                slideCompleteListener.onSlideComplete(pos)
                if(pos !=allStatuses.size) preview.toolbar_progressBar.setProgress(0)
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
        progressAnimator.start()

    }

    interface OnSlideCompleteListener {
        fun onSlideComplete(pos: Int)
    }
}