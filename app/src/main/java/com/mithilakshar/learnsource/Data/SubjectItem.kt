package com.mithilakshar.learnsource.Data

import androidx.annotation.DrawableRes

data class SubjectItem(
    val id: String,
    val name: String,
    @DrawableRes val iconResId: Int
)