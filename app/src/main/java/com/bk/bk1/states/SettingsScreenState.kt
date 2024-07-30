package com.bk.bk1.states

data class SettingsScreenState(
    val showSetDciLimitDialog: Boolean = false,
    val dciRecordLimit: Int = 0,
)