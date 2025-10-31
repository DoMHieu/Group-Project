package com.example.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.musicplayer.home.HomeFragment
import com.example.musicplayer.playback.MiniPlayerFragment
import com.example.musicplayer.playback.MusicQueueManager
import com.example.musicplayer.playback.PlayerFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.isVisible
import com.example.musicplayer.playlist.FavoriteList
import com.example.musicplayer.playlist.PlaylistFragment

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG_HOME = "home"
        private const val TAG_MINI = "mini"
        private const val TAG_PLAYER = "player"
        private const val TAG_PLAYLIST = "playlist"
        private const val KEY_ACTIVE_TAG = "active_fragment_tag"
        private const val LOG = "MainActivity"
    }

    private lateinit var playlistFragment: PlaylistFragment
    private lateinit var homeFragment: HomeFragment
    private lateinit var miniPlayerFragment: MiniPlayerFragment
    private lateinit var playerFragment: PlayerFragment
    private var activeFragment: Fragment? = null
    private var activeTag: String = TAG_HOME

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var miniPlayerContainer: View

    private val musicStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val currentSong = MusicQueueManager.getCurrent()
            val shouldBeVisible = (currentSong != null)
            val isVisible = (miniPlayerContainer.isVisible)
            Log.d(LOG, "onReceive: currentSong=$currentSong shouldBeVisible=$shouldBeVisible isVisible=$isVisible")
            if (shouldBeVisible && !isVisible) {
                miniPlayerContainer.visibility = View.VISIBLE
                miniPlayerContainer.post {
                    miniPlayerContainer.translationY = miniPlayerContainer.height.toFloat()
                    miniPlayerContainer.animate()
                        .translationY(0f)
                        .setDuration(300)
                        .start()
                }
            } else if (!shouldBeVisible && isVisible) {
                miniPlayerContainer.animate()
                    .translationY(miniPlayerContainer.height.toFloat())
                    .setDuration(300)
                    .withEndAction {
                        miniPlayerContainer.visibility = View.GONE
                    }
                    .start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FavoriteList.load(this)
        supportActionBar?.hide()
        actionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FavoriteList.load(this)
        setContentView(R.layout.activity_main)
        val container = findViewById<View>(R.id.container)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        miniPlayerContainer = findViewById(R.id.mini_player_container)

        val fullScreenContainer = findViewById<View>(R.id.full_screen_container)
        ViewCompat.setOnApplyWindowInsetsListener(fullScreenContainer) { v, insets -> insets }

        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            v.setPadding(0, cutoutInsets.top, 0, 0)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView) { v, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(0, 0, 0, navBarInsets.bottom)
            insets
        }

        if (savedInstanceState == null) {
            homeFragment = HomeFragment()
            playlistFragment = PlaylistFragment()
            miniPlayerFragment = MiniPlayerFragment()
            playerFragment = PlayerFragment()

            supportFragmentManager.beginTransaction().apply {
                add(R.id.container, homeFragment, TAG_HOME)
                add(R.id.container, playlistFragment, TAG_PLAYLIST).hide(playlistFragment)
                add(R.id.mini_player_container, miniPlayerFragment, TAG_MINI)
                add(R.id.full_screen_container, playerFragment, TAG_PLAYER).hide(playerFragment)
            }.commit()
            activeFragment = homeFragment
            activeTag = TAG_HOME

        } else {
            activeTag = savedInstanceState.getString(KEY_ACTIVE_TAG, TAG_HOME)

            homeFragment = supportFragmentManager.findFragmentByTag(TAG_HOME) as HomeFragment
            playlistFragment = supportFragmentManager.findFragmentByTag(TAG_PLAYLIST) as PlaylistFragment
            playerFragment = supportFragmentManager.findFragmentByTag(TAG_PLAYER) as PlayerFragment
            miniPlayerFragment = supportFragmentManager.findFragmentByTag(TAG_MINI) as MiniPlayerFragment
            activeFragment = supportFragmentManager.findFragmentByTag(activeTag)

            supportFragmentManager.beginTransaction().apply {
                if (activeFragment?.tag != TAG_HOME) hide(homeFragment)
                if (activeFragment?.tag != TAG_PLAYLIST) hide(playlistFragment)
                if (activeFragment?.tag != TAG_PLAYER) hide(playerFragment)
            }.commit()
        }

        val currentSong = MusicQueueManager.getCurrent()
        miniPlayerContainer.visibility = if (currentSong != null) View.VISIBLE else View.GONE

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            }
            when (item.itemId) {
                R.id.home -> {
                    switchFragment(TAG_HOME, homeFragment)
                    true
                }
                R.id.playlist -> {
                    switchFragment(TAG_PLAYLIST, playlistFragment)
                    true
                }
                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (playerFragment.isVisible) {
                    playerFragment.dismissWithAnimation()
                } else if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    private fun switchFragment(tag: String, fragment: Fragment) {
        if (activeFragment?.tag == tag) return
        val ft = supportFragmentManager.beginTransaction()
        if (activeFragment != null) {
            ft.hide(activeFragment!!)
        }
        ft.show(fragment)
        ft.commit()
        activeFragment = fragment
        activeTag = tag
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("MUSIC_PROGRESS_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(musicStateReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                this,
                musicStateReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(musicStateReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_ACTIVE_TAG, activeTag)
    }
}