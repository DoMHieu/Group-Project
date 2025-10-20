package com.example.musicplayer.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.musicplayer.R
import com.example.musicplayer.home.SearchFragment
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val openSearchButton = view.findViewById<MaterialButton>(R.id.search_button)

        openSearchButton.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                add(R.id.main, SearchFragment())
                addToBackStack(null)
            }
        }
    }
}