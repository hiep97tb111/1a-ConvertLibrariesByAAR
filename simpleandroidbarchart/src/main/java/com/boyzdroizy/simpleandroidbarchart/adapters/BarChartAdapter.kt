package com.boyzdroizy.simpleandroidbarchart.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.boyzdroizy.simpleandroidbarchart.R
import com.boyzdroizy.simpleandroidbarchart.utils.*
import com.boyzdroizy.simpleandroidbarchart.utils.Utils.getLocationOnScreen
import kotlinx.android.synthetic.main.adapter_bar_chart.view.*
import kotlin.random.Random

class BarChartAdapter(
    private val items: MutableList<Int>,
    private val chartInterface: ChartInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectedView: View? = null
    var listDay = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_bar_chart, parent, false)
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ViewHolder)?.bind(items[position], position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: Int, position: Int) {
            itemView.let { view ->
//                view.chart_value.text = position.inc().toString()
                view.chart_value.text = listDay[position]
                initProgressBar(view.progressBar, item)
                displayIndicatorFunc(view)
            }
        }

        private fun initProgressBar(processBar: ProgressBar, value: Int) {
            processBar.progress = 0
            processBar.max = 10000

            ProgressBarAnimation(processBar, PROGRESS_ANIMATION).apply {
                setProgress(value)
            }
        }

        private fun displayIndicatorFunc(view: View) {
            view.container.setClickListenerWithDelay(Constants.DELAY_DEBOUNCE) {

                view.indicator.postDelayed({
                    if (selectedView != view) {
                        view.indicator.alphaShow()
                        selectedView?.indicator?.alphaHide()
                        selectedView = view
                    } else {
                        if (view.indicator.alpha == 0f) view.indicator.alphaShow()
                        else view.indicator.alphaHide()
                    }


                    val location = getLocationOnScreen(view.indicator)
                    chartInterface.onChartValueSelected(
                        location.x,
                        view.indicator.alpha,
                        view.progressBar.progress
                    )
                }, 50)
            }
        }
    }

    interface ChartInterface {
        fun onChartValueSelected(x: Int, alpha: Float, value: Int)
    }

    companion object {
        const val PROGRESS_ANIMATION = 2000L
    }
}