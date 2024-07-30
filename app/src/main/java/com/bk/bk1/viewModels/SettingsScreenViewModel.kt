package com.bk.bk1.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import com.bk.bk1.states.SettingsScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    application: Application
) : ViewModel() {

    val state = MutableStateFlow(SettingsScreenState())

    private val sharedPreferences = application
        .getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Checks the value of DCI record limit.
    // If it equals 0 it means it has never been set and then it is set to 300.
    init {
        var currentDciRecordLimit = sharedPreferences
            .getInt("dciRecordLimit", 0)
        if (currentDciRecordLimit == 0) {
            currentDciRecordLimit = 300
            setDciRecordLimit(currentDciRecordLimit)
        }
        state.update { it.copy(dciRecordLimit = currentDciRecordLimit) }
    }

    // Toggles the DCI record limit dialog window
    fun toggleDciRecordLimitDialog() {
        state.update { it.copy(showSetDciLimitDialog = !state.value.showSetDciLimitDialog) }
    }

    // Updates the DCI record limit in preferences and state.
    fun setDciRecordLimit(value: Int) {
        with (sharedPreferences.edit()) {
            putInt("dciRecordLimit", value)
            apply()
        }
        state.update { it.copy(dciRecordLimit = value) }
    }

}