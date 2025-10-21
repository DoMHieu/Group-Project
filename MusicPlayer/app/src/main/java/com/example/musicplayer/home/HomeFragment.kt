package com.example.musicplayer.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.musicplayer.R
import android.content.Context

class HomeFragment : Fragment() {
    interface OnSearchClickListener {
        fun onSearchClicked()
    }
    private var listener: OnSearchClickListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnSearchClickListener) {
            listener=context
        } else {
            throw RuntimeException("$context ???")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val searchButton = view.findViewById<View>(R.id.search_button)
        searchButton.setOnClickListener {
            listener?.onSearchClicked()
        }
    }
}