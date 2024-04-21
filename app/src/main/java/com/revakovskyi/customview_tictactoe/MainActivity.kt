package com.revakovskyi.customview_tictactoe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.revakovskyi.customview_tictactoe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private var isFirstPlayer = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.apply {
            ticTacToeView.cellListener = { row, column, field ->
                val cell = field.getCell(row, column)
                if (cell == Cell.EMPTY) {
                    if (isFirstPlayer) field.setCell(row, column, Cell.PLAYER_1)
                    else field.setCell(row, column, Cell.PLAYER_2)

                    isFirstPlayer = !isFirstPlayer
                }
            }

            generateBut.setOnClickListener {
                ticTacToeView.gameField = TicTacToeField(
                    rows = (3..10).random(),
                    columns = (3..10).random()
                )
            }
        }
    }

}