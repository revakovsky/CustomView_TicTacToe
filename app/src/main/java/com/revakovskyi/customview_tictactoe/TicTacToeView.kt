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


    // Set a default style for the view if the Global attrStyle (in our case it is a "ticTacToeFieldStyle") wasn't set up
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.DefaultTicTacToeFieldStyle)

    // Set a global style for the custom view in the current app
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.ticTacToeFieldStyle)

    // Set a constructor to create a view from the code with minimal params
    constructor(context: Context) : this(context, null)


    private var player1Color by Delegates.notNull<Int>()
    private var player2Color by Delegates.notNull<Int>()
    private var gridColor by Delegates.notNull<Int>()

    /**
     * field?.listeners?.remove(listener) - unsubscribe the listener from the old field to prevent leakMemory
     * Set to the ticTacToeField variable a new value - "value" and redraw the TicTacToeField by using the
     * @see invalidate()
     * field?.listeners?.add(listener) - subscribe a new listener to the new field
     * If we want to change the size of the field on the layout: e.g. the field was the
     * size of 3x4 and we wanna make it 5x7
     * @see requestLayout()
     */
    private var ticTacToeField: TicTacToeField? = null
        set(value) {
            field?.listeners?.remove(listener)
            field = value
            field?.listeners?.add(listener)
            requestLayout()
            invalidate()
        }

    private val listener: OnFieldChangeListener = {

    }


    init {
        if (attributeSet != null) initAttributes(attributeSet, defStyleAttr, defStyleRes)
        else initDefaultColors()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ticTacToeField?.listeners?.add(listener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ticTacToeField?.listeners?.remove(listener)
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