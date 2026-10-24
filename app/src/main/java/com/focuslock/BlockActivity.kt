package com.focuslock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Black screen shown when a blocked app is opened. */
class BlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val st = Store(this)
        setContent {
            var left by remember { mutableLongStateOf(st.endsAt - System.currentTimeMillis()) }
            LaunchedEffect(Unit) {
                while (left > 0) { delay(500); left = st.endsAt - System.currentTimeMillis() }
                finish()
            }
            Column(
                Modifier.fillMaxSize().background(Color.Black),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔒 Stay focused", color = Color.White, fontSize = 28.sp)
                Spacer(Modifier.height(12.dp))
                Text(fmt(left), color = Color(0xFF7C5CFF), fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text("This app is blocked until your timer ends.", color = Color.Gray)
            }
        }
    }
}

fun fmt(ms: Long): String {
    val s = (ms.coerceAtLeast(0) / 1000)
    return "%02d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
}
