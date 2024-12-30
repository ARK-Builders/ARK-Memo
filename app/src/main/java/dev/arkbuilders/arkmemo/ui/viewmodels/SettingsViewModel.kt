package dev.arkbuilders.arkmemo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val memoPreferences: MemoPreferences,
    ) : ViewModel() {
        fun storeCrashReportEnabled(enabled: Boolean) {
            viewModelScope.launch {
                memoPreferences.storeCrashReportEnabled(enabled)
            }
        }

        fun getCrashReportEnabled(): Boolean {
            return memoPreferences.getCrashReportEnabled()
        }
    }
