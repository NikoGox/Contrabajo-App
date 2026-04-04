package com.movil.contrabajo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.movil.contrabajo.ui.ContrabajoApp
import com.movil.contrabajo.ui.theme.ContrabajoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContrabajoTheme {
                ContrabajoApp()
            }
        }
    }
}
