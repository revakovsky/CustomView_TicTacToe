package com.revakovskyi.customview_tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.floor
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
     * if there is no style, global style, color attributes ->
     * then use values from DefaultTicTacToeFieldStyle
     */
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.DefaultTicTacToeFieldStyle)


    /**
     * if global style is defined in app theme by using ticTacToeFieldStyle attribute ->
     * all TicTacToe views in the project will use that style
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
    private val cellRect = RectF(0f, 0f, 0f, 0f)

    private var cellSize = 0f
    private var cellPadding = 0f

    // Row and Columns to detect by using physical keyBoard
    private var currentCellRow = -1
    private var currentCellColumn = -1

    private lateinit var player1Paint: Paint
    private lateinit var player2Paint: Paint
    private lateinit var focusedCellPaint: Paint
    private lateinit var gridPaint: Paint

    // When some data changes in the field -> need to redraw the view
    private val listener: OnFieldChangeListener = { invalidate() }

    // Assign cell listener in order to listen user actions with this view in the activity/fragment
    var cellListener: OnCellActionListener? = null

    /**
     * Field data is stored here. Setter needs to set for the ticTacToeField variable a new value - "value"
     *
     * field?.listeners?.remove(listener) - unsubscribe the listener from the old field to prevent leakMemory
     * field?.listeners?.add(listener) - subscribe a new listener to the new field
     *
     * @see requestLayout - If we want to change the size of the field on the layout: e.g. the field was the
     * size of 3x4 and we wanna make it 5x7 we should call this fun. In case of using wrap_content,
     * view size may also be changed
     *
     * @see updateViewSizes - If new field may have another number of rows/columns, another cells,
     * so need to update safe zone rect, cell size, cell padding
     *
     * @see invalidate - to redraw the TicTacToeField and show updated state of the variable ticTacToeField
     */
    var gameField: TicTacToeField? = null
        set(value) {
            field?.listeners?.remove(listener)
            field = value
            field?.listeners?.add(listener)
            updateViewSizes()
            requestLayout()
            invalidate()
        }


    /**
     * @see isInEditMode - a param which check is it a debug mode or not and if it is - we can set up
     * some values and create some objects directly in this check to see the drawing progress right
     * away. It's like a helper function to create some object and to see it on the canvas
     */
    init {
        if (attributeSet != null) initAttributes(attributeSet, defStyleAttr, defStyleRes)
        else initDefaultColors()

        initPaints()

        // Here we can initialize some data for component preview in Android Studio
        if (isInEditMode) {
            gameField = TicTacToeField(rows = 8, columns = 6)
            gameField?.setCell(4, 2, Cell.PLAYER_1)
            gameField?.setCell(3, 1, Cell.PLAYER_2)
        }

        // Make our view to work on devices without touchscreen
        isFocusable = true
        isClickable = true
    }

    private fun initAttributes(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TicTacToeView, defStyleAttr, defStyleRes)

        // parsing XML attributes
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
     * @param Paint.ANTI_ALIAS_FLAG - needs to create a smooth pixel line while using a paint to draw view.
     * This flag can slow down a drawing process a bit but the picture will be smoother
     *
     * @see TypedValue.applyDimension - convert dp into the px
     */
    private fun initPaints() {
        player1Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = player1Color
            style = Paint.Style.STROKE
            strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics)
        }

        player2Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = player2Color
            style = Paint.Style.STROKE
            strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics)
        }

        focusedCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = CURRENT_CELL_DEFAULT_COLOR
            style = Paint.Style.FILL
        }

        gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gridColor
            style = Paint.Style.STROKE
            strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Start listening field data changes
        gameField?.listeners?.add(listener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        // Stop listening field data changes
        gameField?.listeners?.remove(listener)
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
        // Min size of our view
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        // Calculating desired size of view
        val desiredCellSizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DESIRED_CELL_SIZE,
            resources.displayMetrics
        ).toInt()

        val rows = gameField?.getRowsAndColumns()?.first ?: 0
        val columns = gameField?.getRowsAndColumns()?.second ?: 0

        val desiredWidth = max(minWidth, (rows * desiredCellSizeInPx + paddingLeft + paddingRight))
        val desiredHeight = max(minHeight, (columns * desiredCellSizeInPx + paddingTop + paddingBottom))

        // Submit view size
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec),
        )
    }

    /**
     * This fun calls after onMeasure when the container has already given some dimens in which we can
     * draw our view. Here we have the real view size after all calculations;
     * By using these dimens we can calculate main view sizes by calling the fun
     * @see updateViewSizes
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSizes()
    }

    private fun updateViewSizes() {
        val field = this.gameField ?: return

        val safeWidth = width - paddingLeft - paddingRight
        val safeHeight = height - paddingTop - paddingBottom

        val rowsNumber = field.getRowsAndColumns().first
        val columnsNumber = field.getRowsAndColumns().second

        val cellWidth = (safeWidth / columnsNumber).toFloat()
        val cellHeight = (safeHeight / rowsNumber).toFloat()
        cellSize = min(cellWidth, cellHeight)

        cellPadding = cellSize * 0.2f
        val fieldWidth = cellSize * columnsNumber
        val fieldHeight = cellSize * rowsNumber

        fieldRect.left = paddingLeft + (safeWidth - fieldWidth) / 2
        fieldRect.top = paddingTop + (safeHeight - fieldHeight) / 2
        fieldRect.right = fieldRect.left + fieldWidth
        fieldRect.bottom = fieldRect.top + fieldHeight
    }

    // To see what we are drawing we need to build a project!
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (canNotStartDrawing()) return
        drawGrid(canvas)
        drawFocusedCell(canvas)
        drawPlayerSingInCell(canvas)
    }

    private fun canNotStartDrawing(): Boolean {
        return gameField == null ||
                cellSize == 0f ||
                fieldRect.width() <= 0f ||
                fieldRect.height() <= 0f
    }

    private fun drawGrid(canvas: Canvas) {
        val field = gameField ?: return

        val xStart = fieldRect.left
        val xEnd = fieldRect.right
        for (i in 0..field.getRowsAndColumns().first) {
            val y = fieldRect.top + cellSize * i
            canvas.drawLine(xStart, y, xEnd, y, gridPaint)
        }

        val yStart = fieldRect.top
        val yEnd = fieldRect.bottom
        for (i in 0..field.getRowsAndColumns().second) {
            val x = fieldRect.left + cellSize * i
            canvas.drawLine(x, yStart, x, yEnd, gridPaint)
        }
    }

    private fun drawFocusedCell(canvas: Canvas) {
        val field = this.gameField ?: return

        if (currentCellRow < 0 || currentCellColumn < 0 ||
            currentCellRow >= field.getRowsAndColumns().first ||
            currentCellColumn >= field.getRowsAndColumns().second) return

        val cellRect = getCellRect(currentCellColumn, currentCellRow)
        canvas.drawRect(
            cellRect.left - cellPadding,
            cellRect.top - cellPadding,
            cellRect.right + cellPadding,
            cellRect.bottom + cellPadding,
            focusedCellPaint
        )
    }

    private fun drawPlayerSingInCell(canvas: Canvas) {
        val field = gameField ?: return

        for (row in 0 until field.getRowsAndColumns().first) {
            for (column in 0 until field.getRowsAndColumns().second) {
                val cell = field.getCell(row, column)
                when (cell) {
                    Cell.PLAYER_1 -> drawPlayer1(canvas, row, column)
                    Cell.PLAYER_2 -> drawPlayer2(canvas, row, column)
                    else -> Unit
                }
            }
        }
    }

    private fun drawPlayer1(canvas: Canvas, row: Int, column: Int) {
        val cellRect = getCellRect(column, row)
        canvas.drawLine(cellRect.left, cellRect.top, cellRect.right, cellRect.bottom, player1Paint)
        canvas.drawLine(cellRect.right, cellRect.top, cellRect.left, cellRect.bottom, player1Paint)
    }

    private fun drawPlayer2(canvas: Canvas, row: Int, column: Int) {
        val cellRect = getCellRect(column, row)
        canvas.drawCircle(
            cellRect.centerX(),
            cellRect.centerY(),
            cellRect.width() / 2,
            player2Paint
        )
    }

    private fun getCellRect(column: Int, row: Int): RectF {
        cellRect.left = fieldRect.left + column * cellSize + cellPadding
        cellRect.top = fieldRect.top + row * cellSize + cellPadding
        cellRect.right = cellRect.left + cellSize - cellPadding * 2
        cellRect.bottom = cellRect.top + cellSize - cellPadding * 2
        return cellRect
    }

    /**
     * Instead of process the user click on the cell on the event - MotionEvent.ACTION_UP we call the
     * fun performClick() - it gives us an opportunity to process the user click not only with a touch
     * event but also with click events e.g. the click of keyboard or right mouse button
     * Same way we have used the performClick() in the onTouchEvent fun and dismiss the warning!
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                updateFocusedCell(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                updateFocusedCell(event)
                return true
            }
            MotionEvent.ACTION_UP ->  return performClick()
        }
        return false
    }

    private fun updateFocusedCell(event: MotionEvent) {
        val field = this.gameField ?: return
        val row = getRow(event)
        val col = getColumn(event)

        if (
            row in 0 until field.getRowsAndColumns().first &&
            col in 0 until field.getRowsAndColumns().second
        ) {
            if (currentCellRow != row || currentCellColumn != col) {
                currentCellRow = row
                currentCellColumn = col
                invalidate()
            }
        } else {
            // clearing current cell if user moves out from the view
            currentCellRow = -1
            currentCellColumn = -1
            invalidate()
        }
    }

    override fun performClick(): Boolean {
        super.performClick()

        val field = this.gameField ?: return false
        val row = currentCellRow
        val col = currentCellColumn

        if (
            row in 0 until field.getRowsAndColumns().first &&
            col in 0 until field.getRowsAndColumns().second
        ) {
            cellListener?.invoke(row, col, field)
            return true
        }
        return false
    }

    /**
     * @see floor - is better then simple rounding to int in our case because it rounds to an integer
     * towards negative infinity. Examples:
     *         1) -0.3.toInt() = 0
     *         2) floor(-0.3) = -1
     */
    private fun getRow(event: MotionEvent): Int {
        return floor((event.y - fieldRect.top) / cellSize).toInt()
    }

    private fun getColumn(event: MotionEvent): Int {
        return floor((event.x - fieldRect.left) / cellSize).toInt()
    }


    companion object {
        // These colors will be used if we don't set up the DefaultTicTacToeFieldStyle and GlobalTicTacToeFieldStyle
        private const val PLAYER_1_DEFAULT_COLOR = Color.GREEN
        private const val PLAYER_2_DEFAULT_COLOR = Color.RED
        private const val CURRENT_CELL_DEFAULT_COLOR = Color.LTGRAY
        private const val GRID_DEFAULT_COLOR = Color.GRAY

        private const val DESIRED_CELL_SIZE = 60f    // size in dp
    }

}