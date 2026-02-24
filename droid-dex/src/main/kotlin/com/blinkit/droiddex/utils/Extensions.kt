package com.blinkit.droiddex.utils

import kotlin.math.pow

internal fun Float.roundToPrecision(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return Math.round(this.toDouble() * factor) / factor
}

internal fun Double.roundToPrecision(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return Math.round(this * factor) / factor
}
