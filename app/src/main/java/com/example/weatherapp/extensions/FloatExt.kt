package com.example.weatherapp.extensions

import kotlin.math.roundToInt

fun Float.tempToFormattedString(): String = "${roundToInt()}°C"