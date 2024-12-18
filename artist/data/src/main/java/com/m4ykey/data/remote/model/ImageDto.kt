package com.m4ykey.data.remote.model

import androidx.annotation.Keep

@Keep
data class ImageDto(
    val height: Int?,
    val url: String?,
    val width: Int?
)