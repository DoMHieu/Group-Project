package com.example.musicplayer

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.home.HomeFragment
import com.example.musicplayer.Playlist.PlaylistFragment
import com.example.musicplayer.playbackcontrol.PlayerViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val miniPlayerView = supportFragmentManager.findFragmentById(R.id.mini_player_fragment)?.view

        bottomNavView.setOnItemSelectedListener { item ->
            mainViewModel.setSelectedTab(item.itemId)
            true
        }

        lifecycleScope.launch {
            playerViewModel.isFullScreenPlayerVisible.collect { isVisible ->
                if (isVisible) {
                    bottomNavView.visibility = View.GONE
                    miniPlayerView?.visibility = View.GONE
                } else {
                    bottomNavView.visibility = View.VISIBLE
                    miniPlayerView?.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            mainViewModel.selectedTabId.collectLatest { tabId ->
                if(bottomNavView.selectedItemId != tabId) {
                    bottomNavView.selectedItemId = tabId
                }
                if (!playerViewModel.isFullScreenPlayerVisible.value) {
                    navigateToFragment(tabId)
                }
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val isFullScreenPlayerShowing = supportFragmentManager.findFragmentById(R.id.full_player_container) != null
            playerViewModel.setFullScreenPlayerVisibility(isFullScreenPlayerShowing)
        }
    }

    private fun navigateToFragment(tabId: Int) {
        val fragmentToShow: Fragment = when(tabId) {
            R.id.home -> HomeFragment()
            R.id.playlist -> PlaylistFragment()
            else -> HomeFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragmentToShow)
            .setReorderingAllowed(true)
            .commit()
    }
}