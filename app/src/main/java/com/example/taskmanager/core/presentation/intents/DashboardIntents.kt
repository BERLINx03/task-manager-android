
package com.example.taskmanager.core.presentation.intents

sealed class DashboardIntents {
    data object GetAdminsCount : DashboardIntents()
    data object Refresh: DashboardIntents()
}
