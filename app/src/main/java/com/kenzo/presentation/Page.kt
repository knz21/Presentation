package com.kenzo.presentation

sealed interface Page {

    object Title : Page

    class Profile(val profile: Data.Profile) : Page

    class Timeline(val timeline: Data.Timeline) : Page

    object End : Page

    companion object {

        fun find(page: Int, data: Data): Page {
            val profilePage = page - 1
            val timelinePage = profilePage - data.profiles.size
            return when {
                page == 0 -> Title
                profilePage in 0 until data.profiles.size -> Profile(data.profiles[profilePage])
                timelinePage in 0 until data.timelines.size -> Timeline(data.timelines[timelinePage])
                else -> End
            }
        }
    }
}