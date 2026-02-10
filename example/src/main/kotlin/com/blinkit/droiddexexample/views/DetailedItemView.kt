package com.blinkit.droiddexexample.views

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updatePadding
import com.blinkit.droiddex.models.PerformanceLevel
import com.blinkit.droiddexexample.R
import com.blinkit.droiddexexample.databinding.LayoutItemDetailedBinding
import com.blinkit.droiddexexample.models.MetricItem
import com.blinkit.droiddexexample.models.PerformanceMetrics
import com.blinkit.droiddexexample.utils.dpToPx
import com.blinkit.droiddexexample.utils.getColor

class DetailedItemView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
): ConstraintLayout(context, attrs, defStyleAttr) {

	private val binding: LayoutItemDetailedBinding by lazy { 
		LayoutItemDetailedBinding.inflate(LayoutInflater.from(context), this) 
	}


	init {
		layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
			updatePadding(bottom = 8.dpToPx().toInt())
		}

		background = GradientDrawable().apply {
			shape = GradientDrawable.RECTANGLE
			setColor(context.getColor(android.R.color.white))
			cornerRadius = 12.dpToPx()
		}

	}

	fun set(level: PerformanceLevel, classText: String, metrics: PerformanceMetrics? = null) {
		// Set level text with performance-based color
		binding.level.text = level.name.lowercase().replaceFirstChar { it.uppercase() }
		binding.level.setTextColor(level.getColor(context))
		binding.level.setTextAppearance(R.style.TextAppearanceLight)

		// Set class name
		binding.className.text = classText
		binding.className.setTextAppearance(R.style.TextAppearanceMedium)

		// Always show metrics container and update detailed metrics if provided
		binding.detailsContainer.visibility = View.VISIBLE
		metrics?.let { updateMetrics(it) }
	}

	private fun updateMetrics(metrics: PerformanceMetrics) {
		binding.metricsList.removeAllViews()
		
		metrics.getDisplayMetrics().forEach { metric ->
			val metricView = createMetricView(metric)
			binding.metricsList.addView(metricView)
		}
	}

	private fun createMetricView(metric: MetricItem): View {
		val metricContainer = LinearLayout(context).apply {
			orientation = LinearLayout.HORIZONTAL
			setPadding(0, 4.dpToPx().toInt(), 0, 4.dpToPx().toInt())
		}

		val labelView = TextView(context).apply {
			text = metric.label
			setTextAppearance(R.style.TextAppearanceMedium)
			textSize = 10f
			layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
		}

		val valueView = TextView(context).apply {
			text = "${metric.value} ${metric.unit}".trim()
			setTextAppearance(R.style.TextAppearanceLight)
			textSize = 10f
			textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
			layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
		}

		metricContainer.addView(labelView)
		metricContainer.addView(valueView)

		return metricContainer
	}


}