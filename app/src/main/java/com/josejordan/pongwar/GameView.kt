package com.josejordan.pongwar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private lateinit var gameThread: Thread
    private var playing = false

    private val ballPaint = Paint()
    private val squarePaint = Paint()
    private val scorePaint = Paint()

    private var ball1: Ball? = null
    private var ball2: Ball? = null

    private val squareSize = 25f
    private var squares: Array<Array<String>>? = null
    private var scoreDay = 0
    private var scoreNight = 0

    init {
        holder.addCallback(this)
        ballPaint.color = Color.BLACK
        squarePaint.color = Color.WHITE
        scorePaint.color = Color.BLUE
        scorePaint.textSize = 30f
    }

    override fun run() {
        while (playing) {
            update()
            draw()
            control()
        }
    }

    private fun update() {
        ball1?.updatePosition(width, height)
        ball2?.updatePosition(width, height)
        updateScores()
    }

    private fun draw() {
        if (holder.surface.isValid) {
            val canvas = holder.lockCanvas()
            drawSquares(canvas)
            ball1?.let { drawBall(canvas, it) }
            ball2?.let { drawBall(canvas, it) }
            drawScore(canvas)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun control() {
        try {
            Thread.sleep(17) // Aproximadamente 60 FPS
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun drawBall(canvas: Canvas, ball: Ball) {
        canvas.drawCircle(ball.x, ball.y, ball.radius, ballPaint)
    }

    private fun drawSquares(canvas: Canvas) {
        squares?.let { squaresArray ->
            for (i in squaresArray.indices) {
                for (j in squaresArray[i].indices) {
                    squarePaint.color = if (squaresArray[i][j] == "DAY") Color.WHITE else Color.BLACK
                    canvas.drawRect(
                        i * squareSize,
                        j * squareSize,
                        (i + 1) * squareSize,
                        (j + 1) * squareSize,
                        squarePaint
                    )
                }
            }
        }
    }

    private fun drawScore(canvas: Canvas) {
        canvas.drawText("Day: $scoreDay | Night: $scoreNight", 10f, 50f, scorePaint)
    }

    fun pause() {
        playing = false
        gameThread.join()
    }

    fun resume() {
        playing = true
        gameThread = Thread(this)
        gameThread.start()
    }

    inner class Ball(var x: Float, var y: Float, var dx: Float = 5f, var dy: Float = 5f, var color: String) {
        val radius = squareSize / 2

        fun updatePosition(width: Int, height: Int) {
            x += dx
            y += dy
            checkCollision(this)
        }
    }

    private fun checkCollision(ball: Ball) {
        if (ball.x - ball.radius < 0 || ball.x + ball.radius > width) ball.dx = -ball.dx
        if (ball.y - ball.radius < 0 || ball.y + ball.radius > height) ball.dy = -ball.dy

        val squareX = (ball.x / squareSize).toInt()
        val squareY = (ball.y / squareSize).toInt()
        squares?.let {
            if (squareX in it.indices && squareY in it[0].indices) {
                if (it[squareX][squareY] != ball.color) {
                    ball.color = if (ball.color == "DAY") "NIGHT" else "DAY"
                    it[squareX][squareY] = ball.color
                }
            }
        }
    }

    private fun updateScores() {
        scoreDay = 0
        scoreNight = 0
        squares?.let {
            for (row in it) {
                for (color in row) {
                    if (color == "DAY") scoreDay++ else scoreNight++
                }
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Implementación opcional si es necesario manejar cambios en la superficie
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        ball1 = Ball(x = width / 4f, y = height / 2f, color = "DAY")
        ball2 = Ball(x = width * 3f / 4f, y = height / 2f, color = "NIGHT")

        initializeSquares(width, height)
        resume() // Comenzar el juego cuando la superficie esté creada
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pause() // Pausar el juego cuando la superficie sea destruida
    }

    private fun initializeSquares(width: Int, height: Int) {
        val numSquaresX = (width / squareSize).toInt()
        val numSquaresY = (height / squareSize).toInt()
        squares = Array(numSquaresX) { Array(numSquaresY) { "DAY" } }
    }
}
