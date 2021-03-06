package com.example.movieapp.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

fun formatStringLong(value: Long): String {
    val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
    formatter.applyPattern("#,###")
    return formatter.format(value)
}

fun formatStringInt(value: Int): String {
    val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
    formatter.applyPattern("#,###")
    return formatter.format(value)
}