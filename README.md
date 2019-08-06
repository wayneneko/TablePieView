# TablePieView
pie图和折线图

工作这么久第一次项目提交github，多多包涵

![image](https://github.com/cl1992/TablePieView/blob/master/images/device-2019-08-06-153744.png)

左边是pie图，右边是折线图

pie图中间使用`setCenterLayout(centerLayoutId)`方法设置动态布局,但是宽高设定是不能超过内圆半径

### pie图参数：

| 参数 | 含义 |
| :-: | :-: |
| pie_bg_circle_background | 背景圆环的颜色 |
| pie_bg_circle_width | 背景圆环宽度 |
| pie_main_circle_width | pie环宽度 |
| pie_spread_line_length_start | 延长线第一段长度 |
| pie_spread_line_length_end | 延长线第二段长度 |
| pie_spread_line_radius | 延长线拐点圆角 |
| pie_is_line_dash | 延长线是否是虚线 |
| pie_is_line_rounded | 延长线是否是圆角 |
| pie_is_line_circle | 延长线末端是否需要原点 |
| pie_spread_text | 延长线文字 |
| pie_spread_text_offset_horizontal | 延长线文字水平偏移量 |
| pie_spread_text_offset_vertical | 延长线文字垂直偏移量 |
| pie_spread_text_size | 延长线文字大小 |
| pie_spread_text_direction | 延长线文字所处方向 |
| pie_start_angle | pie起始角度 |


### 折线图参数

| table_x_text_size | 图表X轴文字大小 |
| :-: | :-: |
| table_y_text_size | 图表Y轴文字大小 |
| table_x_text_color | 图表X轴文字颜色 |
| table_y_text_color | 图表Y轴文字颜色 |
| table_point_circle_bg_color | 坐标点圆圈的背景颜色 |
| table_point_circle_color | 坐标点圆圈的颜色 |
| table_point_circle_width | 坐标点圆圈的线宽 |
| table_point_line_color | 坐标点连线的颜色 |
| table_point_line_width | 坐标点连线的线宽 |
| table_area_color | 填充区域颜色 |
| table_solid_line_color | 实线颜色 |
| table_dash_line_color | 虚线颜色 |
| table_axis_line_width | 横纵坐标线的宽度(不管虚实) |


### 使用示例

```
val colors = arrayOf("#448AFF", "#D81234", "#991AFF", "#FDD835")
val angles = arrayOf(40f, 90f, 100f, 80f)
pieView?.setPieData(arrayListOf(*colors),arrayListOf(*colors),arrayListOf(*angles)
pieView?.setCenterLayout(R.layout.layout_table_pie_text)

val xAxisTexts = arrayOf("8/10", "8/11", "8/12", "8/13", "8/14", "8/15")
val yAxisValue = arrayOf(4, 20, 35, 40, 80, 56)
tableView?.setData(arrayListOf(*xAxisTexts), arrayListOf(*yAxisValue),100)
```


