package com.example.snakegame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment


class StartScreenFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        view.findViewById<Button>(R.id.btnPlay).setOnClickListener {
            (activity as MainActivity).navigateToGame()
        }

        view.findViewById<Button>(R.id.btnSettings).setOnClickListener {
            (activity as MainActivity).navigateToSettings()
        }

        return view
    }
}