package com.revakovskyi.customview_tictactoe

enum class Cell { PLAYER_1, PLAYER_2, EMPTY }

typealias OnFieldChangeListener = (field: TicTacToeField) -> Unit


class TicTacToeField(
    private val rows: Int,
    private val columns: Int,
) {

    private val cells = Array(rows) { Array(columns) { Cell.EMPTY } }
    val listeners = mutableSetOf<OnFieldChangeListener>()

    fun getCell(row: Int, column: Int): Cell {
        return if (row in 0..rows && column in 0..columns) cells[row][column] else Cell.EMPTY
    }

    fun setCell(row: Int, column: Int, cell: Cell) {
        if (row in 0..rows && column in 0..columns) {
            if (cells[row][column] != cell) {
                cells[row][column] = cell
                listeners.forEach { it.invoke(this) }
            }
        } else return
    }

}