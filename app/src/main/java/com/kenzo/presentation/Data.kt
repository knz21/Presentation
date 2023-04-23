package com.kenzo.presentation

import android.graphics.Color
import java.io.Serializable

data class Data(
    val title: String = "",
    val themeColor: String = "#FFFFFF",
    val iconUrl: String = "",
    val birthYear: Int = 1700,
    val profiles: List<Profile> = emptyList(),
    val timelines: List<Timeline> = emptyList()
) : Serializable {

    val pageCount: Int get() = profiles.size + timelines.size

    val color: Int = Color.parseColor(themeColor)

    data class Profile(
        val name: String,
        val items: List<String>
    )

    data class Timeline(
        val title: String,
        val items: List<Item>,
        val memo: String = "",
        val startYear: Int,
        val endYear: Int? = null,
        val useConvertedYear: Boolean = false,
        val yearStrict: Boolean = false
    )

    data class Item(
        val name: String,
        val texts: List<String>,
        val startYear: Int,
        val endYear: Int? = null
    )
}