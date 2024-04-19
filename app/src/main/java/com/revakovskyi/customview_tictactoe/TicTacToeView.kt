package com.revakovskyi.customview_tictactoe

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class TicTacToeView(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    /**
     * Set a default style for the view if the Global attrStyle
     * (in our case it is a "ticTacToeFieldStyle") wasn't set up
     */
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.DefaultTicTacToeFieldStyle)

    /**
     * Set a global style for the custom view in the current app
     */
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.ticTacToeFieldStyle)

    /**
     * Set a constructor to create a view from the code with minimal params
     */
    constructor(context: Context) : this(context, null)


    private var player1Color by Delegates.notNull<Int>()
    private var player2Color by Delegates.notNull<Int>()
    private var gridColor by Delegates.notNull<Int>()

    private val fieldRect = RectF(0f, 0f, 0f, 0f)
    private var cellSize = 0f
    private var cellPadding = 0f

    /**
     * Setter needs to set for the ticTacToeField variable a new value - "value"
     *
     * field?.listeners?.remove(listener) - unsubscribe the listener from the old field to prevent leakMemory
     * field?.listeners?.add(listener) - subscribe a new listener to the new field
     *
     * @see requestLayout - If we want to change the size of the field on the layout: e.g. the field was the
     * size of 3x4 and we wanna make it 5x7 we should call this fun
     *
     * @see updateViewSizes - update view sizes if we changed the field size
     *
     * @see invalidate - to redraw the TicTacToeField and show updated state of the variable ticTacToeField
     */
    private var ticTacToeField: TicTacToeField? = null
        set(value) {
            field?.listeners?.remove(listener)
            field = value
            field?.listeners?.add(listener)
            updateViewSizes()
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

    /**
     * In this function, the container agrees with our view what dimensions the container can give for the
     * view and what dimensions the view requires for itself.
     * As a result, after we indicate the desired sizes and pass them to the
     * @see resolveSize function, it determines whether it is possible to use the desired size
     * and returns us one of the sent size options, which is physically possible for this layout and
     * for the view
     *
     * @see TypedValue.applyDimension - converts dp into the px
     *
     * @see setMeasuredDimension - necessary function to set the dimens for the view
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        val desiredCellSizeInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DESIRED_CELL_SIZE, resources.displayMetrics).toInt()

        val rows = ticTacToeField?.getRowsAndColumns()?.first ?: 0
        val columns = ticTacToeField?.getRowsAndColumns()?.second ?: 0

        val desiredWidth = max(minWidth, (rows * desiredCellSizeInPx + paddingLeft + paddingRight))
        val desiredHeight = max(minHeight, (columns * desiredCellSizeInPx + paddingTop + paddingBottom))

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec),
        )
    }

    /**
     * This fun calls after onMeasure when the container has already given some dimens in which we can
     * draw our view. By using these dimens we can calculate main view sizes by calling the fun
     * @see updateViewSizes
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSizes()
    }

    private fun updateViewSizes() {
        val field = this.ticTacToeField ?: return

        val safeWidth = width - paddingLeft - paddingRight
        val safeHeight = height - paddingTop - paddingBottom

        val cellWidth = (safeWidth / field.getRowsAndColumns().second).toFloat()
        val cellHeight = (safeHeight / field.getRowsAndColumns().first).toFloat()
        cellSize = min(cellWidth, cellHeight)

        cellPadding = cellSize * 0.2f
        val fieldWidth = cellSize * field.getRowsAndColumns().second
        val fieldHeight = cellSize * field.getRowsAndColumns().first

        fieldRect.left = paddingLeft + (safeWidth - fieldWidth) / 2
        fieldRect.top = paddingTop + (safeHeight - fieldHeight) / 2
        fieldRect.right = fieldRect.left + fieldWidth
        fieldRect.bottom = fieldRect.top + fieldHeight
    }


    companion object {
        // These colors will be used if we don't set up the DefaultTicTacToeFieldStyle and GlobalTicTacToeFieldStyle
        private const val PLAYER_1_DEFAULT_COLOR = Color.GREEN
        private const val PLAYER_2_DEFAULT_COLOR = Color.RED
        private const val GRID_DEFAULT_COLOR = Color.GRAY

        private const val DESIRED_CELL_SIZE = 60f    // size in dp
    }

}