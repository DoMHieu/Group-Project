package com.example.musicplayer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.musicplayer.home.HomeFragment
import com.example.musicplayer.home.SearchFragment
import com.example.musicplayer.playback.PlayerFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), HomeFragment.OnSearchClickListener {

    private val homeFragment = HomeFragment()
    private val playerFragment = PlayerFragment()
    private var activeFragment: Fragment = homeFragment
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        actionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        val container = findViewById<View>(R.id.container)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

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
            supportFragmentManager.beginTransaction().apply {
                add(R.id.container, playerFragment, "2").hide(playerFragment)
                add(R.id.container, homeFragment, "1")
            }.commit()
        }
        supportFragmentManager.addOnBackStackChangedListener {
            val currentVisibleFragment = supportFragmentManager.fragments.find { it.isVisible }
            if (currentVisibleFragment is HomeFragment || currentVisibleFragment is PlayerFragment) {
                bottomNavigationView.visibility = View.VISIBLE
            } else {
                bottomNavigationView.visibility = View.GONE
            }
        }

        bottomNavigationView?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction().hide(activeFragment).show(homeFragment).commit()
                    activeFragment = homeFragment
                    true
                }
                R.id.control -> {
                    supportFragmentManager.beginTransaction().hide(activeFragment).show(playerFragment).commit()
                    activeFragment = playerFragment
                    true
                }
                else -> false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("active_fragment_tag", activeFragment.tag)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val activeTag = savedInstanceState.getString("active_fragment_tag")
        val fragmentToShow = supportFragmentManager.findFragmentByTag(activeTag) ?: homeFragment
        val fragmentToHide1 = if (fragmentToShow != homeFragment) homeFragment else playerFragment
        val fragmentToHide2 = if (fragmentToShow != playerFragment) playerFragment else homeFragment
        supportFragmentManager.beginTransaction()
            .hide(fragmentToHide1)
            .hide(fragmentToHide2)
            .show(fragmentToShow)
            .commit()
        activeFragment = fragmentToShow
    }

    override fun onSearchClicked() {
        bottomNavigationView?.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .add(R.id.container, SearchFragment())
            .hide(activeFragment)
            .addToBackStack(null)
            .commit()
    }

}