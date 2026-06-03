package com.sentinel.x

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════
//  1. CORE PERSONA ARCHITECTURE
// ═══════════════════════════════════════════════════════════

enum class Persona(
    val id: String,
    val themeColor: Color,
    val secondaryColor: Color,
    val font: FontFamily,
    val aura: String
) {
    GEEN("GEEN_SEC", Color(0xFF00FF88), Color(0xFF001A0A), FontFamily.Monospace, "TERMINAL_ACTIVE"),
    APEX("APEX_STRAT", Color(0xFFFFCC00), Color(0xFF1A1200), FontFamily.SansSerif, "MARKET_SYNCED"),
    JARVIS("JARVIS_AI", Color(0xFF00FFFF), Color(0xFF001A1A), FontFamily.Serif, "NEURAL_LINKED")
}

data class Message(val content: String, val isUser: Boolean, val persona: Persona)
data class BrainNode(val offset: Offset, val size: Float, val pulse: Float)

// ═══════════════════════════════════════════════════════════
//  2. THE CENTRAL INTELLIGENCE (VIEWMODEL)
// ═══════════════════════════════════════════════════════════

class SentinelViewModel : ViewModel() {
    var activePersona by mutableStateOf(Persona.JARVIS)
    var isWarRoomActive by mutableStateOf(false)
    var isBooting by mutableStateOf(true)
    var statusMessage by mutableStateOf("CALIBRATING...")
    
    val chatHistory = mutableStateListOf<Message>()
    val brainMap = mutableStateListOf<BrainNode>()

    init {
        // Build Initial Neural Map Structure
        repeat(20) {
            brainMap.add(BrainNode(
                Offset((100..900).random().toFloat(), (100..1500).random().toFloat()),
                (5..25).random().toFloat(),
                (0.1f..1f).random()
            ))
        }
    }

    fun executeInput(text: String) {
        if (text.isBlank()) return
        chatHistory.add(Message(text, true, activePersona))
        
        // Logical Triggers
        if (text.uppercase().contains("WAR ROOM")) {
            isWarRoomActive = true
        } else {
            // Simulated AI reasoning chain
            chatHistory.add(Message("Executing ${activePersona.id} protocols... Thinking deeply.", false, activePersona))
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  3. THE SENTINEL-X INTERFACE
// ═══════════════════════════════════════════════════════════

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val system: SentinelViewModel = viewModel()

            // STARTUP PROTOCOL
            LaunchedEffect(Unit) {
                delay(1000); system.statusMessage = "LOADING GEEN INTELLIGENCE..."
                delay(1200); system.statusMessage = "APEX STRATEGY ONLINE..."
                delay(800); system.statusMessage = "J.A.R.V.I.S. ENGAGED."
                delay(500); system.isBooting = false
            }

            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (system.isBooting) {
                    InitializationDisplay(system.statusMessage)
                } else {
                    DashboardLayout(system)
                }

                // EMERGENCY OVERLAY
                if (system.isWarRoomActive) {
                    WarRoomRedAlert { system.isWarRoomActive = false }
                }
            }
        }
    }
}

@Composable
fun InitializationDisplay(status: String) {
    Column(modifier = Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = Color(0xFF00FFFF), strokeWidth = 1.dp)
        Spacer(Modifier.height(30.dp))
        Text(status, color = Color(0xFF00FFFF), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardLayout(system: SentinelViewModel) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val current = system.activePersona

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP METRIC BAR
        Row(modifier = Modifier.fillMaxWidth().background(current.secondaryColor).padding(6.dp)) {
            Text(
                "SENTINEL_X >> CORE: ${current.id} | AURA: ${current.aura} | XP: 4200",
                color = current.themeColor, fontSize = 9.sp, fontFamily = current.font
            )
        }

        // PERSONA HOT-SWAP
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), Arrangement.SpaceEvenly) {
            Persona.values().forEach { p ->
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (system.activePersona == p) p.themeColor else Color(0xFF111111))
                    .border(1.dp, if (system.activePersona == p) Color.White else Color.DarkGray)
                    .clickable { system.activePersona = p }
                    .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(p.id, color = if (system.activePersona == p) Color.Black else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // MAIN VIEWPORT (SWIPE BETWEEN CHAT & BRAIN)
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            if (page == 0) ChatTerminal(system) else NeuralMap(system)
        }
        
        // INPUT BAR
        InputSection { system.executeInput(it) }
    }
}

@Composable
fun ChatTerminal(system: SentinelViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(system.chatHistory) { msg ->
            Text(
                text = if (msg.isUser) "USER_CMD> ${msg.content}" else "[${msg.persona.id}]> ${msg.content}",
                color = if (msg.isUser) Color.White else msg.persona.themeColor,
                fontFamily = msg.persona.font,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun NeuralMap(system: SentinelViewModel) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        system.brainMap.forEach { node ->
            drawCircle(system.activePersona.themeColor, radius = node.size, center = node.offset, alpha = 0.3f)
            drawCircle(system.activePersona.themeColor, radius = node.size, center = node.offset, style = Stroke(0.5f))
        }
    }
    Text("PROCEDURAL_NEURAL_MAP_V4", color = Color.DarkGray, fontSize = 10.sp, modifier = Modifier.padding(16.dp))
}

@Composable
fun InputSection(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(focusedTextColor = Color.White, containerColor = Color(0xFF111111)),
            placeholder = { Text("Enter Command...", color = Color.DarkGray, fontSize = 12.sp) }
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { onSend(text); text = "" }) {
            Text("▶", color = Color.White)
        }
    }
}

@Composable
fun WarRoomRedAlert(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xCC770000)).clickable { onClose() }) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PROTOCOL: RED ALERT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text("THREAT DETECTED - WAR ROOM ACTIVE", color = Color.White, fontSize = 12.sp)
            Spacer(Modifier.height(20.dp))
            Text("TAP TO DISMISS", color = Color.Gray, fontSize = 10.sp)
        }
    }
}