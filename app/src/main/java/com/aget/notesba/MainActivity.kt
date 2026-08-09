package com.aget.notesba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aget.notesba.presentation.navigation.AppNavigation
import com.aget.notesba.ui.theme.NotesBaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {

            NotesBaTheme {

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {

                    AppNavigation()
                }
            }
        }
    }
}
