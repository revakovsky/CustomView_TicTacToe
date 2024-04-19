package com.revakovskyi.customview_tictactoe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.revakovskyi.customview_tictactoe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.apply {
            ticTacToeView.gameField = TicTacToeField(rows = 10, columns = 10)

            generateBut.setOnClickListener {
                ticTacToeView.gameField = TicTacToeField(
                    rows = (3..10).random(),
                    columns = (3..10).random()
                )
            }
        }
    }

}