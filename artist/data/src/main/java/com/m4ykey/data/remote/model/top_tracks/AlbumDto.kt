package com.m4ykey.data.remote.model.top_tracks

import androidx.annotation.Keep
import com.m4ykey.data.remote.model.ImageDto

@Keep
data class AlbumDto(
    val id: String?,
    val images: List<ImageDto>?,
    val name: String?
)