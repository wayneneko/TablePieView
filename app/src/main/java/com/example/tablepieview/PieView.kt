package com.example.tablepieview

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


/**
 * Created by sk on 2019-07-31.
 */

const val DIR_OUT = 1
const val DIR_TOP = 2
const val DIR_BOTTOM = 3

class PieView constructor(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private var minSize: Int = 0
    //pie圆弧起点角度(3点钟方向为0度)
    private var pieStartAngle: Float
    //底圆颜色
    private var baseCircleBg: Int
    //底圆宽度
    private var baseCircleWidth: Float
    //pie圆弧宽度
    private var arcsWidth: Float
    //延长线的两段的长度
    private var spreadLineStartLength: Float
    private var spreadLineEndLength: Float
    //延长线拐点圆弧
    private var spreadLineRadius: Float
    //延长线是否需要虚线
    private var isLineDash: Boolean
    //延长线拐点是否需要圆角
    private var isLineRounded: Boolean
    //延长线末端是否需要圆球
    private var isLineCircle: Boolean
    //延长线描述文字偏移量
    private var spreadLineTextOffsetHorizontal: Float
    private var spreadLineTextOffsetVertical: Float
    //延长线描述文字大小
    private var spreadLineTextSize: Float
    //延长线描述文字所在方向（out为延长线外侧且居中对齐）
    private var spreadLineTextDirection: Int
    //中心布局
    private var layoutId: Int = 0

    private val defaultHeight = 300
    private var radius: Float = 0f
    private var centerY: Int = 0
    private var centerX: Int = 0
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    //pie绘制范围
    private var rectF = RectF()
    private var metrics = Paint.FontMetrics()
    //最长文字长度
    private var maxTextWidth: Int = 0

    private var baseCirclePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var arcsPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)


    //延长线描述文字
    private var spreadLineTexts = ArrayList<String>()
    private var pieColors = ArrayList<String>()
    private var pieAngles = ArrayList<Float>()
    //    private var pieColors = arrayOf("#448AFF", "#D81234", "#991AFF", "#FDD835")
//    private var angles = floatArrayOf(40f, 20f, 100f, 80f)

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
//        setBackgroundColor(Color.DKGRAY)
        //viewGroup默认不调用onDraw
        setWillNotDraw(false)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieView)
        baseCircleBg = typedArray.getColor(R.styleable.PieView_pie_bg_circle_background, Color.parseColor("#F6F5F5"))
        baseCircleWidth = typedArray.getDimension(R.styleable.PieView_pie_bg_circle_width, dp2px(100f))
        arcsWidth = typedArray.getDimension(R.styleable.PieView_pie_main_circle_width, dp2px(130f))
        spreadLineStartLength = typedArray.getDimension(R.styleable.PieView_pie_spread_line_length_start, dp2px(50f))
        spreadLineEndLength = typedArray.getDimension(R.styleable.PieView_pie_spread_line_length_end, dp2px(120f))
        spreadLineRadius = typedArray.getDimension(R.styleable.PieView_pie_spread_line_radius, dp2px(30f))
        isLineDash = typedArray.getBoolean(R.styleable.PieView_pie_is_line_dash, true)
        isLineRounded = typedArray.getBoolean(R.styleable.PieView_pie_is_line_rounded, true)
        isLineCircle = typedArray.getBoolean(R.styleable.PieView_pie_is_line_circle, true)
        spreadLineTextOffsetHorizontal =
            typedArray.getDimension(R.styleable.PieView_pie_spread_text_offset_horizontal, dp2px(20f))
        spreadLineTextOffsetVertical =
            typedArray.getDimension(R.styleable.PieView_pie_spread_text_offset_vertical, dp2px(0f))
        spreadLineTextSize = typedArray.getDimension(R.styleable.PieView_pie_spread_text_size, sp2px(18f))
        spreadLineTextDirection = typedArray.getInt(R.styleable.PieView_pie_spread_text_direction, DIR_OUT)
        pieStartAngle = typedArray.getInt(R.styleable.PieView_pie_start_angle, 0).toFloat()

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        viewWidth = getViewSize(widthMeasureSpec)
        viewHeight = getViewSize(heightMeasureSpec)
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //边距重新计算（包含文字长度）
        radius =
            min(h, w) / 2 * 0.9f - arcsWidth - maxTextWidth - spreadLineStartLength - spreadLineEndLength - min(
                spreadLineTextOffsetHorizontal,
                spreadLineTextOffsetVertical
            )
        //中心坐标
        centerX = w / 2
        centerY = h / 2

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        Log.d("TAG", "width radius = $radius")

        invalidate()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawBaseCircle(it)
            drawArcs(it)
        }
        if (layoutId != 0) {
            setCenterLayout(layoutId)
        }
    }

    fun setCenterLayout(@LayoutRes centerLayoutId: Int) {
        //中间的布局
        layoutId = centerLayoutId
        val view = LayoutInflater.from(context).inflate(centerLayoutId, null)
        val width = (radius).toInt()
        val height = (radius).toInt()
        if (view != null) {
            val layoutParams = LayoutParams(width, height)
            layoutParams.addRule(CENTER_IN_PARENT)
            addView(view, layoutParams)
            invalidate()
        }
    }

    private fun drawArcs(canvas: Canvas) {
        var startAngles = pieStartAngle
        for (i in 0 until minSize) {
            arcsPaint.style = Paint.Style.STROKE
            arcsPaint.strokeWidth = arcsWidth
            arcsPaint.color = Color.parseColor(pieColors[i])
            canvas.drawArc(rectF, startAngles, pieAngles[i], false, arcsPaint)
            drawSpreadLine(canvas, startAngles, i)
            startAngles += pieAngles[i]
        }
    }

    //延长线
    private fun drawSpreadLine(canvas: Canvas, startAngles: Float, index: Int) {
        linePaint.color = Color.parseColor(pieColors[index])
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 5f

        val lineLocation = getLineLocation(startAngles, index)

        //折线(两段) path
        val path = Path()
        path.moveTo(lineLocation.lineStartX, lineLocation.lineStartY)
        path.lineTo(lineLocation.linePartOneEndX, lineLocation.lineFinalEndY)
        path.lineTo(lineLocation.lineFinalEndX, lineLocation.lineFinalEndY)

        judgeDashAndRound(canvas, path)
        drawLineEndCircle(canvas, lineLocation, index)
        drawLineText(canvas, lineLocation, index)
    }

    //延长线文字绘制
    private fun drawLineText(canvas: Canvas, lineLocation: LineLocation, index: Int) {
        textPaint.color = Color.parseColor(pieColors[index])
        textPaint.textSize = spreadLineTextSize
        textPaint.getFontMetrics(metrics)

        var spreadLineText = ""
        if (index < spreadLineTexts.size) {
            spreadLineText = spreadLineTexts[index]
        }
        //高度偏移量，为了使文字垂直方向剧中
        val offset = (metrics.ascent + metrics.descent) / 2f
        //文字宽度
        val textWidth = textPaint.measureText(spreadLineText)
        when (spreadLineTextDirection) {
            DIR_OUT -> {
                canvas.drawText(
                    spreadLineText,
                    if (isLocatedLeft(lineLocation.lineStartX)) lineLocation.lineFinalEndX - spreadLineTextOffsetHorizontal - textWidth else lineLocation.lineFinalEndX + spreadLineTextOffsetHorizontal,
                    lineLocation.lineFinalEndY - offset - spreadLineTextOffsetVertical,
                    textPaint
                )
            }
            DIR_TOP -> {
                canvas.drawText(
                    spreadLineText,
                    if (isLocatedLeft(lineLocation.lineStartX)) lineLocation.lineFinalEndX + spreadLineTextOffsetHorizontal else lineLocation.lineFinalEndX - spreadLineTextOffsetHorizontal - textWidth,
                    lineLocation.lineFinalEndY - spreadLineTextOffsetVertical,
                    textPaint
                )
            }
            DIR_BOTTOM -> {
                canvas.drawText(
                    spreadLineText,
                    if (isLocatedLeft(lineLocation.lineStartX)) lineLocation.lineFinalEndX + spreadLineTextOffsetHorizontal else lineLocation.lineFinalEndX - spreadLineTextOffsetHorizontal - textWidth,
                    lineLocation.lineFinalEndY + spreadLineTextOffsetVertical - offset * 2,
                    textPaint
                )
            }
        }


    }

    //延长线末端的圆球
    private fun drawLineEndCircle(canvas: Canvas, lineLocation: LineLocation, index: Int) {
        if (isLineCircle) {
            linePaint.color = Color.parseColor(pieColors[index])
            linePaint.style = Paint.Style.FILL
            canvas.drawCircle(
                lineLocation.lineFinalEndX,
                lineLocation.lineFinalEndY, 10f, linePaint
            )
        }
    }

    private fun judgeDashAndRound(canvas: Canvas, path: Path) {
        //是否虚线是否圆角的判断
        if (isLineDash && isLineRounded) {
            //组合虚线+圆角(注意顺序)
            val effect =
                ComposePathEffect(DashPathEffect(floatArrayOf(10f, 5f), 0f), CornerPathEffect(spreadLineRadius))
            linePaint.pathEffect = effect
            canvas.drawPath(path, linePaint)
            linePaint.pathEffect = null
        } else {
            if (isLineDash) {
                //虚线
                linePaint.pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
                canvas.drawPath(path, linePaint)
                linePaint.pathEffect = null
            }
            if (isLineRounded) {
                //圆角
                linePaint.pathEffect = CornerPathEffect(spreadLineRadius)
                canvas.drawPath(path, linePaint)
                linePaint.pathEffect = null
            }
        }
    }

    private fun getLineLocation(startAngles: Float, index: Int): LineLocation {
        //中心到圆弧最边缘(真正半径)
        val outWidth = radius + arcsWidth / 2
        //角度对应的圆弧上的中心点坐标(lineStartX,lineStartY)
        val x = cos(Math.toRadians((startAngles + pieAngles[index] / 2).toDouble())).toFloat() * outWidth
        val y = sin(Math.toRadians((startAngles + pieAngles[index] / 2).toDouble())).toFloat() * outWidth
        val lineStartX = x + centerX
        val lineStartY = y + centerY
        val linePartOneEndX =
            lineStartX + cos(Math.toRadians((startAngles + pieAngles[index] / 2).toDouble())).toFloat() * spreadLineStartLength
        val lineFinalEndX =
            if (isLocatedLeft(lineStartX)) linePartOneEndX - spreadLineEndLength else linePartOneEndX + spreadLineEndLength
        val lineFinalEndY =
            lineStartY + sin(Math.toRadians((startAngles + pieAngles[index] / 2).toDouble())).toFloat() * spreadLineStartLength

        val lineLocation = LineLocation()
        lineLocation.lineStartX = lineStartX
        lineLocation.lineStartY = lineStartY
        lineLocation.linePartOneEndX = linePartOneEndX
        lineLocation.lineFinalEndX = lineFinalEndX
        lineLocation.lineFinalEndY = lineFinalEndY

        return lineLocation
    }

    private fun drawBaseCircle(canvas: Canvas) {
        baseCirclePaint.color = Color.parseColor("#F6F5F5")
        baseCirclePaint.style = Paint.Style.STROKE
        baseCirclePaint.strokeWidth = baseCircleWidth
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius, baseCirclePaint)
    }

    private fun getViewSize(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> min(defaultHeight, specSize)
            MeasureSpec.UNSPECIFIED -> defaultHeight
            else -> defaultHeight
        }
    }

    //是否在pie左边
    private fun isLocatedLeft(x: Float): Boolean = x < centerX

    fun setPieData(titles: ArrayList<String>, colors: ArrayList<String>, angles: ArrayList<Float>) {
        minSize = minOf(titles.size, colors.size, angles.size)
        this.spreadLineTexts.clear()
        this.spreadLineTexts.addAll(titles)
        maxTextWidth = calcMaxTextLength(titles)

        this.pieColors.clear()
        this.pieColors.addAll(colors)

        this.pieAngles.clear()
        this.pieAngles.addAll(angles)
        invalidate()
    }


    /**
     * dp转px
     */
    private fun dp2px(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)

    /**
     * sp转px
     */
    private fun sp2px(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().displayMetrics)

    /**
     * 最长文字长度
     */
    private fun calcMaxTextLength(titles: ArrayList<String>): Int {
        var maxWidth = 0
        for (i in titles.indices) {
            val tp = TextPaint()
            val rect = Rect()
            val strTxt = titles[i]
            tp.getTextBounds(strTxt, 0, strTxt.length, rect)
            if (rect.width() > maxWidth) {
                maxWidth = rect.width()
            }
        }
        return maxWidth
    }

    private class LineLocation {
        var lineStartX: Float = 0f
        var lineStartY: Float = 0f
        var linePartOneEndX: Float = 0f
        var lineFinalEndX: Float = 0f
        var lineFinalEndY: Float = 0f
    }
}


