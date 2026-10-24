package com.focuslock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Color.Black, surface = Color.Black, primary = Color(0xFF7C5CFF))) {
                Surface(Modifier.fillMaxSize(), color = Color.Black) { App(Store(this)) }
            }
        }
    }
}

@Composable
fun App(st: Store) {
    var user by remember { mutableStateOf(st.currentUser) }
    if (user == null) Login(st) { user = it } else Home(st) { st.currentUser = null; user = null }
}

@Composable
fun Login(st: Store, onDone: (String) -> Unit) {
    var u by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var err by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("🔒 FocusLock", fontSize = 32.sp, color = Color.White)
        Text("Lock distractions. Finish your study time.", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(u, { u = it }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(p, { p = it }, label = { Text("Password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Text(err, color = Color(0xFFFF7A7A))
        Button({ st.login(u, p)?.let { err = it } ?: onDone(u) }, Modifier.fillMaxWidth()) { Text("Log in") }
        OutlinedButton({ st.signUp(u, p)?.let { err = it } ?: onDone(u) }, Modifier.fillMaxWidth()) { Text("Create account") }
    }
}

fun serviceEnabled(ctx: Context): Boolean =
    (Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "")
        .contains(ctx.packageName)

@Composable
fun Home(st: Store, onLogout: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(500) } }
    val active = now < st.endsAt
    BackHandler(enabled = active) { /* no escaping during a session */ }

    var mode by remember { mutableStateOf(st.modeKey) }
    var mins by remember { mutableStateOf("25") }
    var q by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("") }
    var picked by remember { mutableStateOf(st.custom) }
    val apps = remember {
        val pm = ctx.packageManager
        pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .map { it.loadLabel(pm).toString() to it.activityInfo.packageName }
            .filter { it.second != ctx.packageName }.distinct().sortedBy { it.first.lowercase() }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("🔒 FocusLock", fontSize = 28.sp, color = Color.White)

        if (!serviceEnabled(ctx)) {
            Text("Step 1: turn on the FocusLock Blocker so apps can be blocked.", color = Color(0xFFFFB454))
            Button({ ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, Modifier.fillMaxWidth()) {
                Text("Open Accessibility settings")
            }
        }

        if (active) {
            Text("FOCUS MODE ACTIVE — cannot be cancelled", color = Color.Gray)
            Text(fmt(st.endsAt - now), fontSize = 56.sp, color = Color(0xFF7C5CFF))
            Text("Mode: ${MODES.first { it.key == st.modeKey }.label}", color = Color.White)
        } else {
            Text("Choose a focus mode", color = Color.Gray)
            MODES.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { m ->
                        val sel = mode == m.key
                        Button({ mode = m.key }, colors = ButtonDefaults.buttonColors(
                            containerColor = if (sel) Color(0xFF7C5CFF) else Color(0xFF222222))) { Text(m.label) }
                    }
                }
            }
            if (mode == "custom") {
                Text("Pick apps to block:", color = Color.Gray)
                Column(Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState())) {
                    apps.forEach { (label, pkg) ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(pkg in picked, { on ->
                                picked = if (on) picked + pkg else picked - pkg; st.custom = picked
                            })
                            Text(label, color = Color.White)
                        }
                    }
                }
            }
            OutlinedTextField(mins, { mins = it.filter(Char::isDigit) }, label = { Text("Minutes") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Button({
                val m = mins.toIntOrNull() ?: 0
                if (m in 1..480) st.start(mode, m)
            }, Modifier.fillMaxWidth()) { Text("Start locked session") }
        }

        HorizontalDivider(color = Color(0xFF222222))
        Text("🤖 AI Study Coach", color = Color.White)
        OutlinedTextField(q, { q = it }, label = { Text("e.g. Plan 3 hours of maths") }, modifier = Modifier.fillMaxWidth())
        OutlinedButton({
            reply = "Thinking..."
            scope.launch { reply = Ai.ask(q, st.history) }
        }, Modifier.fillMaxWidth()) { Text("Ask") }
        Text(reply, color = Color(0xFFCCCCCC))

        HorizontalDivider(color = Color(0xFF222222))
        Text("Recent sessions", color = Color.White)
        Text(st.history.joinToString("\n").ifBlank { "None yet" }, color = Color.Gray)
        if (!active) TextButton(onLogout) { Text("Log out") }
    }
}
