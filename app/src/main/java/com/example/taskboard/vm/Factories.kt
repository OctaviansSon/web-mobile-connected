package com.example.taskboard.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Small helper: a ViewModel factory from a lambda (our ViewModels take constructor arguments). */
fun <VM : ViewModel> factoryOf(builder: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = builder() as T
    }
