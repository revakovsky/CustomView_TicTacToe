package com.revakovskyi.customview_tictactoe

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class TicTacToeView(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attributeSet,
        defStyleAttr,
        // Set a default style for the view if the Global attrStyle (in our case it is a "ticTacToeFieldStyle") wasn't set up
        R.style.DefaultTicTacToeFieldStyle
    )
    constructor(context: Context, attributeSet: AttributeSet?) : this(
        context,
        attributeSet,
        // Set a global style for the custom view in the current app
        R.attr.ticTacToeFieldStyle
    )
    constructor(context: Context) : this(context, null)

    private var player1Color by Delegates.notNull<Int>()
    private var player2Color by Delegates.notNull<Int>()
    private var gridColor by Delegates.notNull<Int>()

    init {
        if (attributeSet != null) initAttributes(attributeSet, defStyleAttr, defStyleRes)
        else initDefaultColors()
    }

    private fun initAttributes(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TicTacToeView, defStyleAttr, defStyleRes)
        player1Color = typedArray.getColor(R.styleable.TicTacToeView_player1Color, PLAYER_1_DEFAULT_COLOR)
        player2Color = typedArray.getColor(R.styleable.TicTacToeView_player2Color, PLAYER_2_DEFAULT_COLOR)
        gridColor = typedArray.getColor(R.styleable.TicTacToeView_gridColor, GRID_DEFAULT_COLOR)

        // It needs to release unnecessary resources
        typedArray.recycle()
    }

    private fun initDefaultColors() {
        player1Color = PLAYER_1_DEFAULT_COLOR
        player2Color = PLAYER_2_DEFAULT_COLOR
        gridColor = GRID_DEFAULT_COLOR
    }


    companion object {
        // These colors will be used if we don't set up the DefaultTicTacToeFieldStyle and GlobalTicTacToeFieldStyle
        private const val PLAYER_1_DEFAULT_COLOR = Color.GREEN
        private const val PLAYER_2_DEFAULT_COLOR = Color.RED
        private const val GRID_DEFAULT_COLOR = Color.GRAY
    }

}