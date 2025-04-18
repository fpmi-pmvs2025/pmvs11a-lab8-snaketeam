package com.example.snakegame

import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlin.math.abs

class GameFragment : Fragment() {
    interface GameListener {
        fun onGameOver(score: Int, highScore: Int)
    }

    private lateinit var snakeGame: SnakeGame
    private var gameListener: GameListener? = null
    private lateinit var gestureDetector: GestureDetector

    override fun onAttach(context: Context) {
        super.onAttach(context)
        gameListener = context as? GameListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        snakeGame = SnakeGame(requireContext(), null).apply {
            setGameListener { score, highScore ->
                activity?.runOnUiThread {
                    gameListener?.onGameOver(score, highScore)
                }
            }
        }
        setupGestureDetection()
        return snakeGame
    }

    private fun setupGestureDetection() {
        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val deltaX = e2.x - e1.x
                val deltaY = e2.y - e1.y

                if (abs(deltaX) > abs(deltaY)) {
                    if (abs(deltaX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (deltaX > 0) snakeGame.changeDirection(SnakeGame.Direction.RIGHT)
                        else snakeGame.changeDirection(SnakeGame.Direction.LEFT)
                        return true
                    }
                } else {
                    if (abs(deltaY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (deltaY > 0) snakeGame.changeDirection(SnakeGame.Direction.DOWN)
                        else snakeGame.changeDirection(SnakeGame.Direction.UP)
                        return true
                    }
                }
                return false
            }
        })

        snakeGame.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }
    override fun onPause() {
        super.onPause()
        snakeGame.stopGame()
    }

    override fun onResume() {
        super.onResume()
        snakeGame.startGame()
    }

    override fun onDestroyView() {
        snakeGame.stopGame()
        snakeGame.setOnTouchListener(null)
        super.onDestroyView()
    }
}