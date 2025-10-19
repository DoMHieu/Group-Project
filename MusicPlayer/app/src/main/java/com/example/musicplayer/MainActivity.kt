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
import com.example.musicplayer.Home.HomeFragment
import com.example.musicplayer.Playlist.PlaylistFragment
import com.example.musicplayer.playbackcontrol.PlayerViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    // Đổi tên `viewModel` thành `mainViewModel` cho rõ ràng
    private val mainViewModel: MainViewModel by viewModels()
    // KHAI BÁO PlayerViewModel còn thiếu
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
        // Lấy view của mini_player_fragment từ layout
        val miniPlayerView = supportFragmentManager.findFragmentById(R.id.mini_player_fragment)?.view

        // Xử lý sự kiện cho Bottom Navigation
        bottomNavView.setOnItemSelectedListener { item ->
            mainViewModel.setSelectedTab(item.itemId)
            true
        }

        // Lắng nghe các thay đổi từ ViewModel
        lifecycleScope.launch {
            // Lắng nghe trạng thái của trình phát nhạc toàn màn hình
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
            // Lắng nghe tab được chọn để điều hướng
            mainViewModel.selectedTabId.collectLatest { tabId ->
                if(bottomNavView.selectedItemId != tabId) {
                    bottomNavView.selectedItemId = tabId
                }
                // Chỉ điều hướng khi player to không hiển thị
                if (!playerViewModel.isFullScreenPlayerVisible.value) {
                    navigateToFragment(tabId)
                }
            }
        }

        // Lắng nghe sự kiện nút Back để cập nhật trạng thái
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