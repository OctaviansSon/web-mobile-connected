package com.example.taskboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.taskboard.ui.TaskBoardApp
import com.example.taskboard.ui.theme.TaskBoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskBoardTheme {
                TaskBoardApp(application as PlannerApp)
            }
        }
    }
}
