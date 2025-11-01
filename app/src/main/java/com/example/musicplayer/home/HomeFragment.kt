package com.example.musicplayer.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import com.example.musicplayer.R
@SuppressLint("SetTextI18n")
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchButton = view.findViewById<AppCompatImageButton>(R.id.search_button)
        searchButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.container, SearchFragment())
                .hide(this)
                .addToBackStack("search")
                .commit()
        }
    }
}