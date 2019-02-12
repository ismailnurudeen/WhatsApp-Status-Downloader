package xyz.ismailnurudeen.whatsappstatusaver

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.ListPreference
import android.preference.Preference
import android.preference.SwitchPreference
import android.support.design.widget.AppBarLayout
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout


class SettingsActivity : AppCompatPreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    var useDefaultNameKey = ""
    var doSlideShowKey = ""
    var showVideoControls = ""
    var slideShowTime = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        addPreferencesFromResource(R.xml.pref_general)

        useDefaultNameKey = this.getString(R.string.use_default_name_key)
        doSlideShowKey = this.getString(R.string.do_slide_show_key)
        slideShowTime = this.getString(R.string.slide_show_time_key)
        showVideoControls = this.getString(R.string.show_video_controls_key)

        val sharedPreferences = preferenceScreen.sharedPreferences
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val prefScreen = preferenceScreen
        val count = prefScreen.preferenceCount
        findPreference(slideShowTime).isEnabled = sharedPreferences.getBoolean(this.getString(R.string.do_slide_show_key), false)
        for (i in 0 until count) {
            val pref = prefScreen.getPreference(i)
            if (!(pref is SwitchPreference) && !(pref is CheckBoxPreference)) {
                val value = sharedPreferences.getString(pref.key, "")
                setPreferenceSummary(pref, value)
            }
        }
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    @SuppressLint("NewApi")
    private fun setupActionBar() {
        val root = findViewById<View>(android.R.id.list).parent.parent.parent as LinearLayout
        val appBar = LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false) as AppBarLayout
        val toolbar = appBar.findViewById<android.support.v7.widget.Toolbar>(R.id.settings_toolbar)
        root.addView(appBar, 0)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setPreferenceSummary(pref: Preference, value: String) {
        if (pref is ListPreference) {
            val prefIndex = pref.findIndexOfValue(value)
            if (prefIndex >= 0) {
                pref.summary = pref.entries[prefIndex]
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val pref = findPreference(key)
        if (null != pref) {
            if (key.equals(doSlideShowKey)) {
                findPreference(slideShowTime).isEnabled = sharedPreferences.getBoolean(doSlideShowKey, false)
            }
        }
    }
}
