package xyz.ismailnurudeen.whatsappstatusdownloader.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import kotlinx.android.synthetic.main.layout_preview.view.*
import xyz.ismailnurudeen.whatsappstatusdownloader.*
import xyz.ismailnurudeen.whatsappstatusdownloader.utils.AppUtil
import xyz.ismailnurudeen.whatsappstatusdownloader.utils.StatusVideoView
import java.io.File

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PreviewFragment : Fragment(), PreviewActivity.PreviewReadyListener {
    private var position: Int = -1
    private var preview: View? = null
    private var _isFirstLoad = false
    private lateinit var sharedPrefs: SharedPreferences
    private var mSlideShow: Boolean = true
    private var mSlideShowTime: Float = 10.0F
    private var mShowVideoControls: Boolean = false
    private var progressAnimator: ObjectAnimator? = null
    private var isUserSlide = false
    private var fromAllStatus = true

    private var previewTitles = ArrayList<String>()
    private lateinit var statusList: MutableCollection<File>
    private var controller: MediaController? = null
    private var mSlideCompleteListener: OnSlideCompleteListener? = null

    private lateinit var appUtil: AppUtil
    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            appUtil = AppUtil(context!!)
        } catch (iae: IllegalArgumentException) {
            Toast.makeText(context, "WhatsApp folder could not be found!", Toast.LENGTH_LONG).show()
            activity!!.finish()
            return
        }

        val args = arguments
        position = args?.getInt("POSITION") ?: position
        fromAllStatus = args!!.getBoolean("USE_ALL_STATUS")
        if (fromAllStatus) {
            statusList = appUtil.allStatuses
            for (file in statusList) {
                previewTitles.add(appUtil.getStatusTimeLeft(file))
            }
        } else {
            statusList = appUtil.savedStatuses
            for (file in statusList) {
                previewTitles.add(file.name)
            }
        }

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        mSlideShow = sharedPrefs.getBoolean(context?.getString(R.string.do_slide_show_key), true)
        mShowVideoControls = sharedPrefs.getBoolean(context?.getString(R.string.show_video_controls_key), false)
        val slideShowTime = sharedPrefs.getString(context?.getString(R.string.slide_show_time_key), "10")
        mSlideShowTime = if (slideShowTime.isNotEmpty()) {
            if (slideShowTime.toInt() < 5) {
                5.0F
            } else if (slideShowTime.toInt() > 60) {
                60.0F
            } else {
                slideShowTime.toFloat()
            }
        } else {
            10.0F
        }
        // Create the InterstitialAd and set it up.
        mInterstitialAd = InterstitialAd(context!!).apply {
            adUnitId = context!!.getString(R.string.fullscreen_ad_unit)
            adListener = (object : AdListener() {
                override fun onAdClosed() {
                }
            })
        }
        loadFullScreenAd()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_preview, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        preview = v
        if (isVisible && userVisibleHint) {
            setupPreview(v)
            Log.i(Constant.TAG, "position $position is Visible")
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisible) {
            if (isVisibleToUser && !_isFirstLoad) {
                selectPreviewType(preview!!, statusList.elementAt(position))
                _isFirstLoad = true
            }
            if (!isVisibleToUser) {
                if (preview!!.video_preview.isPlaying) preview!!.video_preview.stopPlayback()
                if (progressAnimator != null && progressAnimator!!.isRunning) {
                    isUserSlide = true
                }
                progressAnimator?.cancel()
            }
        }
    }

    private fun selectPreviewType(preview: View, file: File) {
        //Stop any previously playing video
        if (preview.video_preview.isPlaying || preview.video_preview.isActivated) preview.video_preview.stopPlayback()
        if (controller != null && controller!!.isShowing) controller!!.hide()

        if (file.extension.contains("jpg", true)) {
            preview.image_preview.visibility = View.VISIBLE
            preview.video_preview.visibility = View.GONE
            if (context != null) {
                Glide.with(context!!)
                        .load(file)
                        .into(preview.image_preview)
            }
        } else {
            preview.video_preview.visibility = View.VISIBLE
            preview.image_preview.visibility = View.GONE
            preview.video_preview.setVideoPath(file.absolutePath)

            mSlideShowTime = appUtil.getVideoDuration(file) / 1000F
            Log.i("SlideShowTime", "Meta Data Video Duration is $mSlideShowTime")
            preview.video_preview.setOnPreparedListener {
                Log.i("SlideShowTime", "Normal Video Duration is ${(it.duration.toFloat() / 1000F)}")
            }
            preview.video_preview.start()

            if (mShowVideoControls) {
                if (context == null) return
                controller = MediaController(context)
                preview.video_preview.setMediaController(controller)
                controller?.show(5)
                preview.video_preview.setPlayPauseListener(object : StatusVideoView.PlayPauseListener {
                    override fun onPlay() {
                        if (progressAnimator != null && progressAnimator!!.isPaused) {
                            progressAnimator!!.resume()
                        }
                    }

                    override fun onPause() {
                        if (progressAnimator != null && progressAnimator!!.isRunning) {
                            progressAnimator!!.pause()
                        }
                    }

                })
            }
        }
        preview.toolbar_progressBar.visibility = View.INVISIBLE
        if (mSlideShow) {
            preview.toolbar_progressBar.visibility = View.VISIBLE
            animateSlider(preview, statusList.indexOf(file))
        }
    }

    override fun onPreviewReady(pos: Int) {
        if (preview == null) return
        setupPreview(preview!!)
    }

    @SuppressLint("SetTextI18n")
    private fun setupPreview(preview: View) {
        selectPreviewType(preview, statusList.elementAt(position))
        setListenersForMedia(preview)


        preview.toolbar_time_left.text = if (fromAllStatus) {
            "${previewTitles[position]} left"
        } else {
            previewTitles[position]
        }
        preview.preview_download.setOnClickListener {
            if (progressAnimator != null && progressAnimator!!.isRunning) {
                progressAnimator!!.pause()
                Log.i("OnResponse", "Progress paused...")
            }

            val responseListener = object : AppUtil.OnUserDialogResponse {
                override fun onResponse(status: Int) {
                    if (status == ResponseStatus.SUCCESSFUL) Toast.makeText(context!!, "Status Downloaded Successfully", Toast.LENGTH_SHORT).show()
                    if (progressAnimator != null && progressAnimator!!.isPaused) {
                        progressAnimator!!.resume()
                        Log.i("OnResponse", "Progress resumed...")
                    }
                }
            }
            appUtil.renameFileAndDownload(position, responseListener)
        }
        if (fromAllStatus) {
            preview.preview_download.visibility = View.VISIBLE
        } else {
            preview.preview_download.visibility = View.GONE
        }
        preview.toolbar_action_back.setOnClickListener {
            gotoMainActivity()
        }
        preview.preview_action_menu.setOnClickListener {
            val menu = PopupMenu(context, it)
            val inflater = menu.menuInflater
            inflater.inflate(R.menu.preview_menu, menu.menu)
            menu.show()
            progressAnimator?.pause()
            menu.setOnMenuItemClickListener {
                if (it.itemId == R.id.menu_share) {
                    val shareTitle = "Share File With"
                    ShareCompat.IntentBuilder.from(activity)
                            .setChooserTitle(shareTitle)
                            .setType("*/*")
                            .setStream(FileProvider.getUriForFile(context!!, BuildConfig.APPLICATION_ID + ".provider", statusList.elementAt(position)))
                            .startChooser()
                }
                true
            }
            menu.setOnDismissListener {
                progressAnimator?.resume()
            }

        }
    }

    private fun setListenersForMedia(preview: View) {
        preview.video_preview.setOnTouchListener { _, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                preview.preview_toolbar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                controller?.show()
                progressAnimator?.pause()
            } else if (event.action == KeyEvent.ACTION_UP) {
                preview.preview_toolbar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
                if (preview.video_preview.currentPosition.toFloat() == mSlideShowTime / 1000F) progressAnimator?.end()
                if (preview.video_preview.isPlaying) progressAnimator?.resume()
            }
            true
        }
        preview.image_preview.setOnTouchListener { _, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                preview.preview_toolbar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                progressAnimator?.pause()
            } else if (event.action == KeyEvent.ACTION_UP) {
                preview.preview_toolbar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
                progressAnimator?.resume()
            }
            true
        }

//        preview.preview_media_wrapper.setOnClickListener {
//            progressAnimator?.end()
//        }
    }

    private fun gotoMainActivity() {
        activity!!.finish()
    }

    private fun animateSlider(preview: View, pos: Int) {
        progressAnimator = ObjectAnimator.ofInt(preview.toolbar_progressBar, "progress", 0, 100)
        progressAnimator?.duration = (mSlideShowTime * 1000).toLong()
        progressAnimator?.interpolator = DecelerateInterpolator()

        progressAnimator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isUserSlide) {
                    if (pos >= statusList.size - 1) {
                        showInterstitial()
                    }
                    mSlideCompleteListener?.onSlideComplete(pos)
                    if (pos < statusList.size) preview.toolbar_progressBar.progress = 0
                }
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
        progressAnimator?.start()

    }

    private fun showInterstitial() {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            Log.i("ADMOB_AD", "Ad wasn't loaded.")
        }
    }

    private fun loadFullScreenAd() {
        if (!mInterstitialAd.isLoading && !mInterstitialAd.isLoaded) {
            val adRequest = AdRequest.Builder()
                    .addTestDevice("EE61FFC39B2F91254A201499649C0082")
                    .build()
            mInterstitialAd.loadAd(adRequest)
        }
    }

    interface OnSlideCompleteListener {
        fun onSlideComplete(pos: Int)
    }

    companion object {
        fun newInstance(position: Int, fromAll: Boolean, listener: OnSlideCompleteListener): PreviewFragment {
            val pf = PreviewFragment()
            val args = Bundle()
            args.putInt("POSITION", position)
            args.putBoolean("USE_ALL_STATUS", fromAll)
            pf.arguments = args
            pf.mSlideCompleteListener = listener
            return pf
        }
    }
}