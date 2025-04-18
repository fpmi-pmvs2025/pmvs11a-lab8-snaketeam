package com.example.snakegame


import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), GameFragment.GameListener {
    private lateinit var gestureDetector: GestureDetector
    //private lateinit var tvScore: TextView
    //private lateinit var tvHighScore: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            navigateToStart()
        }

        //tvScore = findViewById(R.id.tvScore)
        //tvHighScore = findViewById(R.id.tvHighScore)

        // Load high score
        val prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        val highScore = prefs.getInt("high_score", 0)
        //tvHighScore.text = "High Score: $highScore"

    }



    fun updateScore(score: Int) {
        runOnUiThread {
            //tvScore.text = "Score: $score"
      }
    }

    fun navigateToStart() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, StartScreenFragment())
            .commit()
    }

    fun navigateToGame() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GameFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToSettings() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SettingsFragment())
            .addToBackStack(null)
            .commit()
    }


    override fun onGameOver(score: Int, highScore: Int) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, GameOverFragment().apply {
                arguments = Bundle().apply {
                    putInt("score", score)
                    putInt("highScore", highScore)
                }
            })
            .commit()
    }
    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
  }
}