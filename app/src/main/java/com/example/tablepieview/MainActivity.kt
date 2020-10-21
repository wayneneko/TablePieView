package com.example.tablepieview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val colors = arrayOf("#448AFF", "#D81234", "#991AFF", "#FDD835")
        val angles = arrayOf(40f, 90f, 100f, 80f)
        pieView?.setPieData(arrayListOf(*colors), arrayListOf(*colors), arrayListOf(*angles))
        pieView?.setCenterLayout(R.layout.layout_table_pie_text)


        val xAxisTexts = arrayOf("8/10", "8/11", "8/12", "8/13", "8/14", "8/15")
        val yAxisValue = arrayOf(4, 20, 35, 40, 80, 56)
        tableView?.setData(arrayListOf(*xAxisTexts), arrayListOf(*yAxisValue), 100)

        println("add commit")
        println("add commit222222")
        println("modify on git test ")
        println("this is git demo")
        println("this is git push demo")


        println("test demo fuck ")


    }
}
