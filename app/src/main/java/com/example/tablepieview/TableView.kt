package com.example.tablepieview

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import kotlin.math.min


/**
 * Created by sk on 2019-08-02.
 */

class TableView constructor(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //第一条横线对于顶部的距离(Y轴单位距离的1/2)
    private var firstHorLineToTop = 0f
    //文字和横纵坐标间距偏移量
//    private val xyOffset = 20

    //X轴文字距离坐标轴的偏移量
    private val xTextOffset = 0f
    //Y轴文字距离坐标轴的偏移量
    private val yTextOffset = 0f
    //最后一条横线和第一条横线Y的差值
    private var diffY = 0f

    private var xTotal = 100
    //Y轴是否显示0
    private var isShow0: Boolean = false
    private var horizontalLineCount = 5//行
    private var xAxisCount = 1//列的个数
    private var centerX: Int = 0
    private var centerY: Int = 0
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private val defaultHeight: Int = 300
    //最后一根垂直线距离右边的偏移量
    private var marginEndOffset: Float = 0f
    //X轴文字的高度
    private var xAxisTextHeight = 0
    //保存数据坐标点
    private var dataXyList = ArrayList<DataXyBean>()
    //第一条水平线的Y
    private var firstLineY = 0f
    //最后一条水平线的Y
    private var finalLineY = 0f
    //原点的半径
    private var circleRadius = 20f
    //填充区域path
    private var areaPath = Path()
    //X轴文字size
    private var xAxisTextSize: Float
    //Y轴文字size
    private var yAxisTextSize: Float
    //XY轴文字颜色
    private var xAxisTextColor: Int
    private var yAxisTextColor: Int
    //数据圆点底色
    private var pointCircleBgColor: Int
    //数据点圆环颜色
    private var pointCircleColor: Int
    //数据点圆环线条宽度
    private var pointCircleWidth: Float
    //数据点连线颜色
    private var pointLineColor: Int
    //数据点连线线条宽度
    private var pointLineWidth: Float
    //填充区域颜色
    private var areaColor: Int
    //坐标轴实线颜色
    private var solidLineColor: Int
    //坐标轴虚线颜色
    private var dashLineColor: Int
    //坐标轴线宽度
    private var axisLineWidth: Float

    private var metrics = Paint.FontMetrics()
    private var dashLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var solidLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var circleBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var dataLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textYPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textXPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var areaPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var xAxisTexts = ArrayList<String>()
    private var yAxisValue = ArrayList<Int>()

    init {
        setLayerType(RelativeLayout.LAYER_TYPE_SOFTWARE, null)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TableView)
        xAxisTextColor = typedArray.getColor(R.styleable.TableView_table_x_text_color, Color.BLACK)
        yAxisTextColor = typedArray.getColor(R.styleable.TableView_table_y_text_color, Color.BLACK)
        xAxisTextSize = typedArray.getDimension(R.styleable.TableView_table_x_text_size, sp2px(18f))
        yAxisTextSize = typedArray.getDimension(R.styleable.TableView_table_y_text_size, sp2px(18f))
        pointCircleBgColor = typedArray.getColor(R.styleable.TableView_table_point_circle_bg_color, Color.WHITE)
        pointCircleColor = typedArray.getColor(R.styleable.TableView_table_point_circle_color, Color.BLACK)
        pointCircleWidth = typedArray.getDimension(R.styleable.TableView_table_point_circle_width, dp2px(5f))
        pointLineColor = typedArray.getColor(R.styleable.TableView_table_point_line_color, Color.BLACK)
        pointLineWidth = typedArray.getDimension(R.styleable.TableView_table_point_line_width, dp2px(5f))
        areaColor = typedArray.getColor(R.styleable.TableView_table_area_color, Color.parseColor("#3365C9FF"))
        solidLineColor = typedArray.getColor(R.styleable.TableView_table_solid_line_color, Color.BLACK)
        dashLineColor = typedArray.getColor(R.styleable.TableView_table_dash_line_color, Color.BLACK)
        axisLineWidth = typedArray.getDimension(R.styleable.TableView_table_axis_line_width, dp2px(5f))
        circleRadius = typedArray.getDimension(R.styleable.TableView_table_point_radius, dp2px(5f))
        typedArray.recycle()
        initPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        viewWidth = getViewSize(widthMeasureSpec)
        viewHeight = getViewSize(heightMeasureSpec)
        marginEndOffset = (viewWidth / 15).toFloat()
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //中心坐标
        centerX = w / 2
        centerY = h / 2
        super.onSizeChanged(w, h, oldw, oldh)
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

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawTable(canvas)
        }
    }

    private fun initPaint() {
        //数据点背景
        circleBgPaint.color = pointCircleBgColor
        circleBgPaint.style = Paint.Style.FILL
        //数据点外圈
        circlePaint.color = pointCircleColor
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = pointCircleWidth
        //数据点连线
        dataLinePaint.color = pointLineColor
        dataLinePaint.style = Paint.Style.STROKE
        dataLinePaint.strokeWidth = pointLineWidth
        //填充区域
        areaPaint.color = areaColor
        areaPaint.style = Paint.Style.FILL
        //XY轴文字
        textYPaint.color = xAxisTextColor
        textYPaint.textSize = yAxisTextSize
        textXPaint.color = yAxisTextColor
        textXPaint.textSize = xAxisTextSize
        //实线
        solidLinePaint.color = solidLineColor
        solidLinePaint.style = Paint.Style.STROKE
        solidLinePaint.strokeWidth = axisLineWidth
        //虚线
        dashLinePaint.color = dashLineColor
        dashLinePaint.style = Paint.Style.STROKE
        dashLinePaint.strokeWidth = axisLineWidth
        dashLinePaint.pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
    }

    private fun drawTable(canvas: Canvas) {
        if (xAxisTexts.size > 0 && yAxisValue.size > 0) {

            //纵坐标间隔单位（约定纵坐标为数字）
            val xUnit = xTotal / horizontalLineCount
            //获取Y轴文字的最大宽度
            val yMaxWidth = getMaxWidth(xUnit)
            //获取X轴文字的高度
            xAxisTextHeight = getTextHeight(xAxisTexts[0])

            drawHorizontalLine(canvas, xUnit, yMaxWidth)
            drawVerticalLine(canvas, yMaxWidth)
        }
    }

    private fun drawHorizontalLine(canvas: Canvas, xUnit: Int, xMaxWidth: Int) {
        //Y轴横线单位距离(上面有0.5单位的间距)，控件高度-X轴文字高度-文字距离最下面一根线的偏移量
        val spaceY =
            ((viewHeight - xAxisTextHeight - yTextOffset - paddingTop - paddingBottom) / (horizontalLineCount + 0.5)).toFloat()
        val startX = 0f + paddingStart
        var startY = 0f
        val endX = viewWidth.toFloat() - paddingEnd

        firstHorLineToTop = spaceY / 2

        for (i in 0..horizontalLineCount) {
            startY = spaceY / 2 + spaceY * i + paddingTop
            //画字Y轴
            textYPaint.textAlign = Paint.Align.RIGHT
            textYPaint.getFontMetrics(metrics)
            //高度偏移量，为了使文字垂直方向剧中
            val offset = (metrics.ascent + metrics.descent) / 2f
            //纵坐标现实文字（例如：总数100，count为5，纵坐标文字为20，40，60，80，100，0特殊）
            if (isShow0 || i != horizontalLineCount) {
                canvas.drawText(
                    (xUnit * (horizontalLineCount - i)).toString(),
                    startX + xMaxWidth,
                    startY - offset,
                    textYPaint
                )
            }

            //水平线的起始X
            val realX = startX + xMaxWidth + xTextOffset
            //画线
            if (i == horizontalLineCount) {
                //最后一条水平线的Y
                finalLineY = startY
                //最下面一条是实线
                canvas.drawLine(realX, startY, endX, startY, solidLinePaint)
                areaPath.moveTo(realX, startY)
            } else {
                if (i == 0) {
                    //第一条水平线的Y
                    firstLineY = startY
                }
                canvas.drawLine(realX, startY, endX, startY, dashLinePaint)
            }
        }
        diffY = finalLineY - firstLineY
    }

    private fun drawVerticalLine(canvas: Canvas, xMaxWidth: Int) {
        //文字宽度（最宽）+ 文字和第一根垂直线距离
        val xOffsetToVerticalLine = xMaxWidth + xTextOffset
        xAxisCount = min(xAxisTexts.size, yAxisValue.size)
        val verticalCount = if (xAxisCount == 1) xAxisCount else xAxisCount - 1
        val spaceX = ((viewWidth - marginEndOffset - xOffsetToVerticalLine - paddingStart - paddingEnd) / verticalCount)
        var startX = 0f
        val startY = 0f + paddingTop
        val endY = viewHeight.toFloat() - xAxisTextHeight - yTextOffset - paddingBottom

        var lastPointX = 0f
        var lastPointY = 0f

        dataXyList.clear()

        for (j in 0..verticalCount) {
            startX = spaceX * j + paddingStart + xOffsetToVerticalLine
            //画字(X轴文字)
            textXPaint.textAlign = Paint.Align.CENTER
            textXPaint.getFontMetrics(metrics)
            //高度偏移量，为了使文字垂直方向剧中
            val offset = (metrics.ascent + metrics.descent) / 2f

            //特殊处理X轴只有一个数据的时候
            if (xAxisCount != 1 || j != xAxisCount) {
                canvas.drawText(
                    xAxisTexts[j],
                    startX,
                    viewHeight + offset - paddingBottom,
                    textXPaint
                )
            }
            //垂直线
            canvas.drawLine(startX, startY, startX, endY, solidLinePaint)

            //数据点Y轴坐标
            val circleY =
                diffY / xTotal * (xTotal - yAxisValue[if (xAxisCount == 1) 0 else j]) + firstHorLineToTop + paddingTop

            //两点相连线（处理X轴只有一条数据的情况）
            if (xAxisCount != 1 || j != xAxisCount) {
                if (j > 0) {
                    canvas.drawLine(lastPointX, lastPointY, startX, circleY, dataLinePaint)
                }
            }
            //填充区域path连线
            areaPath.lineTo(startX, circleY)

            //记录上一次数据点，为了连线
            lastPointX = startX
            lastPointY = circleY

            //保存数据坐标点
            val dataXyBean = DataXyBean()
            dataXyBean.x = startX
            dataXyBean.y = circleY
            dataXyList.add(dataXyBean)
        }

        //区域填充
        areaPath.lineTo(startX, finalLineY)
        areaPath.close()
        canvas.drawPath(areaPath, areaPaint)

        //单独画圆(数据点)
        var circleStartX = 0f
        for (i in 0..verticalCount) {
            if (xAxisCount != 1 || i != xAxisCount) {
                circleStartX = spaceX * i + paddingStart + xOffsetToVerticalLine
                val circleY = diffY / xTotal * (xTotal - yAxisValue[i]) + firstHorLineToTop + paddingTop
                //画数据点（X轴与垂直线一致）
                canvas.drawCircle(circleStartX, circleY, circleRadius, circleBgPaint)
                canvas.drawCircle(circleStartX, circleY, circleRadius, circlePaint)
            }
        }
    }

    private fun getMaxWidth(xUnit: Int): Int {
        val strings: ArrayList<String> = ArrayList<String>()
        for (i in 0..horizontalLineCount) {
            strings.add((xUnit * (horizontalLineCount - i)).toString())
        }
        return calcMaxTextLength(strings)
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
    private fun calcMaxTextLength(strings: ArrayList<String>): Int {
        var maxWidth = 0
        for (i in strings.indices) {
            val textWidth = textYPaint.measureText(strings[i])
            if (textWidth > maxWidth) {
                maxWidth = textWidth.toInt()
            }
        }
        return maxWidth
    }

    private fun getTextHeight(str: String): Int {
        val fontMetrics = textXPaint.fontMetrics
        val height = fontMetrics.bottom - fontMetrics.top
        return height.toInt()
    }


    fun setData(xTexts: ArrayList<String>, yValue: ArrayList<Int>, xMax: Int) {
        xTotal = xMax
        xAxisTexts.clear()
        xAxisTexts.addAll(xTexts)
        yAxisValue.clear()
        yAxisValue.addAll(yValue)
        areaPath.reset()
        invalidate()
    }

    private class DataXyBean {
        var x = 0f
        var y = 0f
    }

}