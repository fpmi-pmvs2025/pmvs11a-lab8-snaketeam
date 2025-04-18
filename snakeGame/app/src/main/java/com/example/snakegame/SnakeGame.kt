package com.example.snakegame
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import kotlin.math.max

class SnakeGame(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), Runnable {

    //private val gameLock = Any()

    private var thread: Thread? = null
    private var running = false

    //
    private var gameListener: ((Int, Int) -> Unit)? = null

    fun setGameListener(listener: (Int, Int) -> Unit) {
        gameListener = listener
    }
    //

    val snake = ArrayList<Point>()
    private var food: Point? = null
    //
    private var foodBitmap: Bitmap? = null
    private lateinit var headBitmap: Bitmap
    private lateinit var bodyBitmap: Bitmap
    private val headMatrix = Matrix()

    //
    private var direction = Direction.RIGHT
    private var nextDirection = Direction.RIGHT
    private var score = 0
    var highScore = 0
    private var moveDelay = 200L

    private val targetFPS = 5
    private var idealFrameTime = moveDelay //1000 / targetFPS
    private var lastFrameTime = 0L

    private var viewWidth = 0
    private var viewHeight = 0

    private var gameOverTriggered = false

    private val blockSize = 40

    private val weatherFoodMap = mapOf(
        "sunny" to R.drawable.sunny,
        "cloudy" to R.drawable.cloud,
        "rain" to R.drawable.rain,
        "snow" to R.drawable.snow,
        "fog" to R.drawable.fog
    )



    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                bodyBitmap = getBitmapFromDrawable(R.drawable.ic_snake_body)
                headBitmap = getBitmapFromDrawable(R.drawable.ic_snake_head)
                resetGame()
                startGame()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                viewWidth = width
                viewHeight = height
            }
            override fun surfaceDestroyed(holder: SurfaceHolder) { stopGame() }
        })
    }

    private fun resetGame() {
        snake.clear()
        snake.add(Point(5, 5))
        snake.add(Point(4, 5))
        snake.add(Point(3, 5))
        direction = Direction.RIGHT
        nextDirection = Direction.RIGHT
        score = 0
        moveDelay = 200
        spawnFood()
    }

    private fun getFoodDrawable(weatherCategory: String?): Int {
        return weatherFoodMap[weatherCategory] ?: R.drawable.cloud
    }
    private fun calculateSampleSize(scale: Float): Int {
        return when {
            scale <= 0.25 -> 4
            scale <= 0.5 -> 2
            else -> 1
        }
    }
    private fun spawnFood() {

        //if (viewWidth == 0 || viewHeight == 0) return
        val random = java.util.Random()
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val weatherCategory = prefs.getString("current_weather", "sunny")
        val foodDrawableId = getFoodDrawable(weatherCategory)


        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, foodDrawableId, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            Log.e("SnakeGame", "Invalid food bitmap dimensions")
            return
        }
        // Calculate scaling
        val scale = if (blockSize > 0) {
            blockSize.toFloat() / max(options.outWidth, options.outHeight)
        } else {
            1f // Fallback scale if blockSize not initialized
        }
        options.inJustDecodeBounds = false
        options.inSampleSize = calculateSampleSize(scale)
        val sourceBitmap = BitmapFactory.decodeResource(resources, foodDrawableId, options)

        sourceBitmap?.let {
            foodBitmap = Bitmap.createScaledBitmap(
                it,
                (options.outWidth * scale).toInt().coerceAtLeast(1),
                (options.outHeight * scale).toInt().coerceAtLeast(1),
                true
            )
        } ?: run {
            Log.e("SnakeGame", "Failed to decode food bitmap")
            foodBitmap = null
        }

        while (true) {
            val newFood = Point(
                random.nextInt(width / blockSize),
                random.nextInt(height / blockSize)
            )
            if (!snake.contains(newFood)) {
                food = newFood
                break
            }
        }
    }

    fun update() {
        if (gameOverTriggered) return

        direction = nextDirection

        if (snake.isEmpty()) return
        val head = snake[0]
        val newHead = Point(head.x, head.y)

        val now = System.currentTimeMillis()
        if (now - lastFrameTime < idealFrameTime) return
        lastFrameTime = now


        when (direction) {
            Direction.UP -> newHead.y--
            Direction.DOWN -> newHead.y++
            Direction.LEFT -> newHead.x--
            Direction.RIGHT -> newHead.x++
        }

        // Check collisions
        if (newHead.x < 0 || newHead.x >= width / blockSize ||
            newHead.y < 0 || newHead.y >= height / blockSize ||
            snake.contains(newHead)
        ) {
            gameOverTriggered = true
            gameOver()
            return
        }

        snake.add(0, newHead)

        if (newHead == food) {
            score += 10
            spawnFood()
            moveDelay -= 10
            idealFrameTime = moveDelay
        } else {
            snake.removeAt(snake.size - 1)
        }
    }

    private fun getBitmapFromDrawable(drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)!!
        val bitmap = Bitmap.createBitmap(
            blockSize,
            blockSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private val gridPaint = Paint().apply {
        color = Color.argb(50, 255, 255, 255) // Semi-transparent white
        strokeWidth = 1f
    }
    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
    }
    private fun drawGame(canvas: Canvas) {
       // canvas.drawColor(Color.BLACK)
        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            Color.parseColor("#1a2a3a"),  // Dark teal
            Color.parseColor("#0d1b2a"),  // Deeper navy
            Shader.TileMode.CLAMP
        )
        backgroundPaint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Adjust grid color to match new background
        gridPaint.color = Color.argb(50, 100, 200, 255)  // Light teal grid

        // Draw grid
        for (i in 1 until width step blockSize) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), height.toFloat(), gridPaint)
        }
        for (i in 1 until height step blockSize) {
            canvas.drawLine(0f, i.toFloat(), width.toFloat(), i.toFloat(), gridPaint)
        }

        // Draw snake
        snake.subList(1, snake.size).forEach { point ->
            bodyBitmap.let { bitmap ->
                canvas.drawBitmap(
                    bitmap,
                    point.x * blockSize.toFloat(),
                    point.y * blockSize.toFloat(),
                    null
                )
            }
        }
        snake.firstOrNull()?.let { head ->
            headBitmap?.let { bitmap ->
                val rotation = when (direction) {
                    Direction.RIGHT -> 270f
                    Direction.DOWN -> 0f
                    Direction.LEFT -> 90f
                    Direction.UP -> 180f
                }

                headMatrix.reset()
                headMatrix.postRotate(
                    rotation,
                    bitmap.width / 2f,
                    bitmap.height / 2f
                )
                headMatrix.postTranslate(
                    head.x * blockSize.toFloat(),
                    head.y * blockSize.toFloat()
                )

                canvas.drawBitmap(bitmap, headMatrix, null)
            }
        }
        // Draw food
        food?.let {
            foodBitmap?.let { bitmap ->
                canvas.drawBitmap(
                    bitmap,
                    (it.x * blockSize).toFloat(),
                    (it.y * blockSize).toFloat(),
                    null
                )
            }
        }
    }

    fun changeDirection(newDirection: Direction) {
        if (!direction.isOpposite(newDirection)) {
            nextDirection = newDirection
        }
    }
    private fun gameOver() {
        stopGame()
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        highScore = prefs.getInt("high_score", 0)
        if (score > highScore) {
            prefs.edit().putInt("high_score", score).apply()
            highScore = score
        }
        post {
            gameListener?.invoke(score, highScore)
        }
    }


    override fun run() {
        while (running) {
            if (holder.surface.isValid) {
                val canvas = holder.lockCanvas()
                update()
                drawGame(canvas)
                holder.unlockCanvasAndPost(canvas)

                Thread.sleep(moveDelay)
            }
        }
    }

    fun startGame() {
        running = true
        thread = Thread(this)
        thread?.start()
    }

    fun stopGame() {
        running = false
        thread?.join()
    }

    enum class Direction {
        UP, DOWN, LEFT, RIGHT;

        fun isOpposite(other: Direction): Boolean {
            return when (this) {
                UP -> other == DOWN
                DOWN -> other == UP
                LEFT -> other == RIGHT
                RIGHT -> other == LEFT
            }
        }
    }
}