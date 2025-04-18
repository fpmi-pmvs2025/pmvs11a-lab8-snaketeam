package com.example.snakegame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class GameOverFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game_over, container, false)

        arguments?.let {
            view.findViewById<TextView>(R.id.tvFinalScore).text = "Score: ${it.getInt("score")}"
            view.findViewById<TextView>(R.id.tvHighScore).text = "High Score: ${it.getInt("highScore")}"
        }

        view.findViewById<Button>(R.id.btnMenu).setOnClickListener {
            (activity as MainActivity).navigateToStart()
        }

        return view
    }
}