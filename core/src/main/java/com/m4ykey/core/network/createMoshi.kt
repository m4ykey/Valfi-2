package com.m4ykey.core.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

fun createMoshi() : Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()