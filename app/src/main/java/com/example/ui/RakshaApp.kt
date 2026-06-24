package com.example.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarning

// Room and API engine imports
import com.example.data.ScamHistoryEntity
import com.example.data.ScamReportEntity
import com.example.data.BlacklistItemEntity
import com.example.api.ScamScanEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RakshaApp(viewModel: RakshaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isLoggedIn && currentScreen != "splash" && currentScreen != "onboarding" && currentScreen != "emergency") {
                RakshaBottomNavigation(currentScreen = currentScreen, userRole = userRole) { screen ->
                    viewModel.navigateTo(screen)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberBackground)
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "splash" -> SplashScreen {
                    if (isLoggedIn) {
                        viewModel.navigateTo("home")
                    } else {
                        viewModel.navigateTo("onboarding")
                    }
                }
                "onboarding" -> OnboardingScreen {
                    viewModel.navigateTo("login")
                }
                "login" -> LoginScreen { name, phone, role ->
                    viewModel.login(name, phone, role)
                }
                "home" -> HomeScreen(viewModel)
                "call_scanner" -> CallScannerScreen(viewModel)
                "sms_scanner" -> SmsScannerScreen(viewModel)
                "whatsapp_scanner" -> WhatsAppScannerScreen(viewModel)
                "url_scanner" -> UrlScannerScreen(viewModel)
                "payment_scanner" -> PaymentScannerScreen(viewModel)
                "explainer" -> ExplainerScreen(viewModel)
                "emergency" -> EmergencyScreen(viewModel)
                "report" -> ReportScreen(viewModel)
                "history" -> HistoryScreen(viewModel)
                "settings" -> SettingsScreen(viewModel)
                "admin" -> AdminPanelScreen(viewModel)
                else -> HomeScreen(viewModel)
            }
        }
    }
}

// Bottom navigation bar matching Cybertheme
@Composable
fun RakshaBottomNavigation(
    currentScreen: String,
    userRole: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface, RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = mutableListOf(
                Triple("home", "Home", Icons.Default.Home),
                Triple("history", "History", Icons.Default.History),
                Triple("report", "Report", Icons.Default.Feedback)
            )
            if (userRole == "ADMIN") {
                navItems.add(Triple("admin", "Admin", Icons.Default.AdminPanelSettings))
            }
            navItems.add(Triple("settings", "Settings", Icons.Default.Settings))

            navItems.forEach { (screen, label, icon) ->
                val selected = currentScreen == screen
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(screen) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) CyberPrimary else CyberTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        color = if (selected) CyberPrimary else CyberTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        kotlinx.coroutines.delay(2500)
        onTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // glowing shield logo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(150.dp)
                .alpha(alphaAnim)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val brush = Brush.radialGradient(
                    colors = listOf(CyberPrimary.copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.width / 1.5f
                )
                drawCircle(brush = brush, radius = size.width / 2)
            }
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Shield Logo",
                tint = CyberPrimary,
                modifier = Modifier.size(100.dp)
            )
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security Core Logo",
                tint = CyberSecondary,
                modifier = Modifier.size(45.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "RAKSHA AI",
            color = CyberPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "AI SCAM SHIELD",
            color = CyberSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 6.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        CircularProgressIndicator(
            color = CyberPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
        )
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val pages = listOf(
        Triple(
            "Call Scam Shield",
            "Real-time call screening and transcript threat analysis utilizing Gemini models to spot social engineering instantly before money is lost.",
            Icons.Default.Call
        ),
        Triple(
            "SMS & Phishing filter",
            "Automatic SMS audit for bank fraud alerts, high-risk links, and digital arrest text patterns to secure first-time internet users.",
            Icons.Default.Message
        ),
        Triple(
            "UPI receipt audit",
            "Upload payment screenshots to extract transaction IDs, check fonts, verify merchant data, and identify tampered receipts automatically.",
            Icons.Default.QrCodeScanner
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Skip",
                color = CyberSecondary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clickable { onGetStarted() }
                    .padding(8.dp)
                    .testTag("skip_onboarding")
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = listOf(CyberPrimary.copy(alpha = 0.2f), CyberSecondary.copy(alpha = 0.2f))
                            ),
                            radius = size.width / 2
                        )
                    }
            ) {
                Icon(
                    imageVector = pages[currentPage].third,
                    contentDescription = null,
                    tint = CyberPrimary,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = pages[currentPage].first,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = pages[currentPage].second,
                color = CyberTextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.Center) {
                pages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (currentPage == index) CyberPrimary else CyberTextSecondary.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (currentPage < pages.lastIndex) {
                        currentPage++
                    } else {
                        onGetStarted()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("next_onboarding_button")
            ) {
                Text(
                    text = if (currentPage == pages.lastIndex) "SECURE MY DEVICE" else "NEXT MODULE",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// 3. LOGIN SCREEN
@Composable
fun LoginScreen(onLogin: (name: String, phone: String, role: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("USER") } // USER, ADMIN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Shield",
            tint = CyberPrimary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ACTIVATE ASSISTANT",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        Text(
            text = "RakshaAI acts as your local cyber safety agent",
            color = CyberTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            placeholder = { Text("e.g. Rajesh Kumar") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                focusedLabelColor = CyberPrimary,
                unfocusedLabelColor = CyberTextSecondary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_name_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Mobile Number (OTP secure)") },
            placeholder = { Text("e.g. +91 98765 01234") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                focusedLabelColor = CyberPrimary,
                unfocusedLabelColor = CyberTextSecondary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_phone_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Role Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Persona",
                    color = CyberSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { selectedRole = "USER" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == "USER") CyberPrimary else CyberBackground
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .testTag("role_user")
                    ) {
                        Text(
                            "User Shield",
                            color = if (selectedRole == "USER") Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { selectedRole = "ADMIN" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == "ADMIN") CyberSecondary else CyberBackground
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .testTag("role_admin")
                    ) {
                        Text(
                            "Admin Shield",
                            color = if (selectedRole == "ADMIN") Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onLogin(name, phone, selectedRole) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("login_submit")
        ) {
            Text(
                text = "SECURE DEVICE",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// 4. HOME DASHBOARD
@Composable
fun HighDensityHeader(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Gradient square "R" logo
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(CyberPrimary, CyberSecondary)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    color = CyberBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column {
                Text(
                    text = "RakshaAI",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "AI SCAM SHIELD",
                    color = CyberPrimary.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Language selector pill
            Row(
                modifier = Modifier
                    .background(CyberSurface, shape = RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("EN", color = CyberPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("తె", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("हि", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // User Profile badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(CyberSurface, shape = CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "👤",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CircularSafetyScoreMeter(blockedThreatsCount: Int) {
    val safetyScore = if (blockedThreatsCount > 5) 85 else if (blockedThreatsCount > 2) 94 else 98
    val riskStatus = if (safetyScore >= 95) "Device Secure" else "Caution Advised"
    val riskColor = if (safetyScore >= 95) CyberPrimary else CyberWarning
    val riskDesc = if (blockedThreatsCount == 0) {
        "No active threats detected. Real-time monitoring active."
    } else {
        "$blockedThreatsCount threats blocked in the last 24 hours. Real-time monitoring active."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Blurred radial gradient element at the top-right
            Canvas(modifier = Modifier.matchParentSize()) {
                val brush = Brush.radialGradient(
                    colors = listOf(CyberPrimary.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(size.width * 0.9f, 0f),
                    radius = size.width * 0.4f
                )
                drawCircle(brush = brush, radius = size.width * 0.4f)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 8.dp.toPx()
                        val diameter = size.width - strokeWidth
                        val radius = diameter / 2
                        
                        // Background track
                        drawCircle(
                            color = Color(0xFF1F2937),
                            radius = radius,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                        )
                        
                        // Active progress arc
                        val sweepAngle = (safetyScore / 100f) * 360f
                        drawArc(
                            color = riskColor,
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = safetyScore.toString(),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Safety Score",
                            color = CyberTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = riskStatus,
                    color = riskColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = riskDesc,
                    color = CyberTextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun HighDensityStatsGrid(scansToday: Int, blockedThreats: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Protected / Scans Card
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberPrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 16.sp)
                }
                Column {
                    Text(
                        text = scansToday.toString(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "PROTECTED",
                        color = CyberTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Scams Blocked Card
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberSecondary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚫", fontSize = 16.sp)
                }
                Column {
                    Text(
                        text = blockedThreats.toString(),
                        color = CyberSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SCAMS BLOCKED",
                        color = CyberTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveShieldsList() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ActiveShieldRow(title = "Call Scam Detection", icon = "📞")
            Divider(color = Color.White.copy(alpha = 0.05f))
            ActiveShieldRow(title = "WhatsApp Link Monitor", icon = "💬")
            Divider(color = Color.White.copy(alpha = 0.05f))
            ActiveShieldRow(title = "UPI Payment Verify", icon = "💳")
        }
    }
}

@Composable
fun ActiveShieldRow(title: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(icon, fontSize = 16.sp)
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        
        // Green active toggle indicator
        Box(
            modifier = Modifier
                .size(32.dp, 16.dp)
                .background(CyberPrimary.copy(alpha = 0.3f), CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(CyberPrimary, CircleShape)
            )
        }
    }
}

@Composable
fun SafetyTicker() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "ticker")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "tickerAlpha"
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .alpha(alpha)
                .background(CyberDanger, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "New KYC scam detected in New Delhi • Stay Alert",
            color = CyberTextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HomeScreen(viewModel: RakshaViewModel) {
    val history by viewModel.historyState.collectAsStateWithLifecycle()
    val reports by viewModel.reportsState.collectAsStateWithLifecycle()
    val language by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val name by viewModel.userName.collectAsStateWithLifecycle()

    val scansToday = history.size
    val blockedThreats = history.count { it.riskLevel == "HIGH" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Language Switcher
        item {
            HighDensityHeader(userName = name)
        }

        // Circular Safety Score Meter
        item {
            CircularSafetyScoreMeter(blockedThreatsCount = blockedThreats)
        }

        // Quick Stats Grid
        item {
            HighDensityStatsGrid(scansToday = scansToday, blockedThreats = blockedThreats)
        }

        // Active Shields List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ACTIVE SHIELDS",
                    color = CyberSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                ActiveShieldsList()
            }
        }

        // Emergency red button matching High Density style exactly
        item {
            Button(
                onClick = { viewModel.navigateTo("emergency") },
                colors = ButtonDefaults.buttonColors(containerColor = CyberDanger),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("emergency_alert_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🆘",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "I THINK I AM BEING SCAMMED NOW",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Feature Header
        item {
            Text(
                text = "CYBER SAFETY SCAN ENGINES",
                color = CyberSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Grid of Scan Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScanModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "Call Scam Guard",
                        desc = "Analyze caller transcript & keywords",
                        icon = Icons.Default.Call,
                        color = CyberPrimary,
                        onClick = { viewModel.navigateTo("call_scanner") },
                        tag = "module_call"
                    )
                    ScanModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "SMS Audit",
                        desc = "Screen fake bank & KYC texts",
                        icon = Icons.Default.Message,
                        color = CyberSecondary,
                        onClick = { viewModel.navigateTo("sms_scanner") },
                        tag = "module_sms"
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScanModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "WhatsApp Filter",
                        desc = "Detect work-from-home trap",
                        icon = Icons.Default.Smartphone,
                        color = CyberWarning,
                        onClick = { viewModel.navigateTo("whatsapp_scanner") },
                        tag = "module_whatsapp"
                    )
                    ScanModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "Scam URL Checker",
                        desc = "Verify links & phishing lists",
                        icon = Icons.Default.Link,
                        color = CyberPrimary,
                        onClick = { viewModel.navigateTo("url_scanner") },
                        tag = "module_url"
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScanModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "UPI Receipt Auditor",
                        desc = "Upload & verify success screenshots",
                        icon = Icons.Default.QrCodeScanner,
                        color = CyberSecondary,
                        onClick = { viewModel.navigateTo("payment_scanner") },
                        tag = "module_payment"
                    )
                    ScanModuleCard(
                        modifier = Modifier.weight(1f),
                        title = "AI Safety Explainer",
                        desc = "Ask questions or explain threats",
                        icon = Icons.Default.Info,
                        color = CyberPrimary,
                        onClick = { viewModel.navigateTo("explainer") },
                        tag = "module_explainer"
                    )
                }
            }
        }

        // Recent warnings Header
        item {
            Text(
                text = "RECENT SCAN LOGS",
                color = CyberSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (history.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface)
                ) {
                    Text(
                        text = "Your device is secured. Run any of the scanners above to start auditing security.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
        } else {
            items(history.take(3)) { item ->
                ScamLogCard(item = item, onDelete = { viewModel.deleteHistoryItem(item.id) })
            }
        }

        // High Density Safety Ticker at the bottom
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SafetyTicker()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = CyberTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun ScanModuleCard(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = modifier
            .height(105.dp)
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    color = CyberTextSecondary,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun ScamLogCard(item: ScamHistoryEntity, onDelete: () -> Unit) {
    val riskColor = when (item.riskLevel.uppercase()) {
        "HIGH" -> CyberDanger
        "MEDIUM" -> CyberWarning
        else -> CyberPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = when (item.type) {
                    "CALL" -> Icons.Default.Call
                    "SMS" -> Icons.Default.Message
                    "WHATSAPP" -> Icons.Default.Smartphone
                    "URL" -> Icons.Default.Link
                    "PAYMENT" -> Icons.Default.QrCodeScanner
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                tint = riskColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.category,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.riskLevel} RISK",
                        color = riskColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(riskColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (item.input.contains("|")) item.input.substringAfter("|") else item.input,
                    color = CyberTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = CyberTextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 5. CALL SCANNER SCREEN
@Composable
fun CallScannerScreen(viewModel: RakshaViewModel) {
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()

    var number by remember { mutableStateOf("") }
    var transcript by remember { mutableStateOf("") }

    val presetTranscripts = listOf(
        Pair("+91 98765 43210", "Hello. I am the Commissioner of Police from Cyber Crime Department. Your Aadhaar card was found in illegal drug packaging in Mumbai. Transfer 2 lakhs immediately as caution money to secure bail or you will face immediate Digital Arrest on video call."),
        Pair("+91 90123 45678", "Dear customer, your State Bank card is blocked due to non-KYC completion. To reactivate, click the link and verify. Please tell me your one-time password OTP so I can manually approve your PAN card update in the server right now."),
        Pair("+91 99999 88888", "Hi Dad, I lost my phone and borrowing my friend's number. I have an emergency and need to pay my college form fee today. Please send 5000 Rupees to this UPI ID: emergency@okicici. Thank you.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = "CALL SCAM SHIELD", onBack = { viewModel.navigateTo("home") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Screen caller details and suspicious social engineering pitches over active voice calls instantly using RakshaAI learning engine.",
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SIMULATOR CALL AUDIT",
                            color = CyberSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = number,
                            onValueChange = { number = it },
                            label = { Text("Caller Mobile Number") },
                            placeholder = { Text("e.g. +91 98765 43210") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = transcript,
                            onValueChange = { transcript = it },
                            label = { Text("Call Transcript / Suspicious Pitch") },
                            placeholder = { Text("What did the caller say? e.g. KYC blocked, Police, OTP, Parcel arrested...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.runCallScan(number, transcript) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("run_call_scan_button"),
                            enabled = !isScanning && number.isNotBlank() && transcript.isNotBlank()
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Text("AUDIT METADATA & TRANSCRIPT", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SELECT COMMON FRAUD PATTERNS",
                    color = CyberSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(presetTranscripts) { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            number = preset.first
                            transcript = preset.second
                        },
                    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberTextSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = preset.first,
                            color = CyberSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.second,
                            color = CyberTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Results UI
            if (scanResult != null && scanResult?.type == "CALL") {
                item {
                    ScanResultWidget(result = scanResult!!, onReport = {
                        viewModel.submitReport("NUMBER", number, scanResult!!.category, "Flagged: ${scanResult!!.explanation}")
                    })
                }
            }
        }
    }
}

// 6. SMS SCANNER SCREEN
@Composable
fun SmsScannerScreen(viewModel: RakshaViewModel) {
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()

    var customSender by remember { mutableStateOf("") }
    var customText by remember { mutableStateOf("") }

    val mockInbox = listOf(
        Pair("AD-SBIINF", "Your HDFC / SBI net banking access has been blocked tonight due to pending pan card update. Please update immediately on: http://sbi-kyc-verify.com to avoid disconnection."),
        Pair("VK-PWRGRD", "Urgent notice: Your electricity power bill of Rs. 492 is overdue. Electric supply will be disconnected at 9:30 PM tonight. Contact power officer immediately at +91 88888 77777."),
        Pair("KBC-ALERT", "Congratulations! You won Rs 25,00,000 in KBC Lucky Draw. To process your prize claim, transfer Rs. 12,500 file charges to Bank account 902148019 immediately."),
        Pair("INF-BANK", "Dear User, 98204 is your security verification One Time Password (OTP) for transaction of Rs. 14,000 on paytm. Never share your OTP with bank manager.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = "SMS PHISHING DETECTOR", onBack = { viewModel.navigateTo("home") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Detect and isolate phishing links, fake bank alerts, and emergency-based utility scam messages.",
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CUSTOM SMS SHIELD AUDIT",
                            color = CyberSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customSender,
                            onValueChange = { customSender = it },
                            label = { Text("Sender Header (e.g. AD-SBIINF)") },
                            placeholder = { Text("e.g. AD-SBIINF") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            label = { Text("SMS Message Content") },
                            placeholder = { Text("Copy paste suspicious text here...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.runSmsScan(customSender, customText) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("run_sms_scan_button"),
                            enabled = !isScanning && customSender.isNotBlank() && customText.isNotBlank()
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Text("AUDIT MESSAGE THREATS", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SIMULATED PHONE INBOX",
                    color = CyberSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(mockInbox) { sms ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            customSender = sms.first
                            customText = sms.second
                        },
                    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberTextSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sms.first,
                                color = CyberPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Scam Risk",
                                tint = CyberDanger,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sms.second,
                            color = CyberTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Results UI
            if (scanResult != null && scanResult?.type == "SMS") {
                item {
                    ScanResultWidget(result = scanResult!!, onReport = {
                        viewModel.submitReport("SMS", customText, scanResult!!.category, "Flagged: ${scanResult!!.explanation}")
                    })
                }
            }
        }
    }
}

// 7. WHATSAPP SCANNER SCREEN
@Composable
fun WhatsAppScannerScreen(viewModel: RakshaViewModel) {
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()

    var chatText by remember { mutableStateOf("") }

    val presetChats = listOf(
        "Hi! YouTube video liking part-time task available. Earn Rs. 1500 to Rs. 5000 daily from home. No deposit required. Contact executive on Telegram to start tasks immediately.",
        "URGENT: Official emergency instant loan up to Rs. 5,00,000 approved without doc verification. Low interest rate. Install this APK and apply instantly inside app: http://instant-loan-fast.apk",
        "Hello sir, I am calling from KBC Lucky Draw department. Your mobile number has won Rs. 25,00,000 cash prize. Please deposit GST processing charges Rs. 15,000 to get file clearance. Call +91 99999 88888."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = "WHATSAPP FRAUD DETECTOR", onBack = { viewModel.navigateTo("home") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Analyze shared texts or screenshots from WhatsApp to isolate Task Fraud, part-time jobs, and loan traps.",
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PASTE WHATSAPP CONVERSATION",
                            color = CyberSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = chatText,
                            onValueChange = { chatText = it },
                            label = { Text("WhatsApp Message") },
                            placeholder = { Text("Paste suspicious job offers, loan offers, or prize text here...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.runWhatsAppScan(chatText) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("run_whatsapp_scan_button"),
                            enabled = !isScanning && chatText.isNotBlank()
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Text("AUDIT WHATSAPP CHAT", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SELECT COMMON WHATSAPP FRAUDS",
                    color = CyberSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(presetChats) { chat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { chatText = chat },
                    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberTextSecondary.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = chat,
                        color = CyberTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Results UI
            if (scanResult != null && scanResult?.type == "WHATSAPP") {
                item {
                    ScanResultWidget(result = scanResult!!, onReport = {
                        viewModel.submitReport("SMS", chatText, scanResult!!.category, "Flagged on WhatsApp: ${scanResult!!.explanation}")
                    })
                }
            }
        }
    }
}

// 8. URL SCANNER SCREEN
@Composable
fun UrlScannerScreen(viewModel: RakshaViewModel) {
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()

    var urlText by remember { mutableStateOf("") }

    val presetUrls = listOf(
        "http://sbi-kyc-verify.com",
        "https://verify-pan-income-tax.net",
        "http://parttimejobs-earn-now.in",
        "https://google.com"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = "SCAM URL CHECKER", onBack = { viewModel.navigateTo("home") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Submit any link to audit HTTPS security, domain spelling tricks, banking impersonation, or confirmed threat lists.",
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "URL VERIFICATION ENGINE",
                            color = CyberSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = urlText,
                            onValueChange = { urlText = it },
                            label = { Text("Paste Website Link / URL") },
                            placeholder = { Text("e.g. http://sbi-kyc-verify.com") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.runUrlScan(urlText) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("run_url_scan_button"),
                            enabled = !isScanning && urlText.isNotBlank()
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Text("CHECK WEBSITE SAFETY", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SUSPICIOUS DEFI BLACKLIST LINKS",
                    color = CyberSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(presetUrls) { link ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { urlText = link },
                    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberTextSecondary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = link,
                            color = if (link.contains("google.com")) CyberPrimary else CyberDanger,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (link.contains("google.com")) Icons.Default.CheckCircle else Icons.Default.Block,
                            contentDescription = null,
                            tint = if (link.contains("google.com")) CyberPrimary else CyberDanger,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Results UI
            if (scanResult != null && scanResult?.type == "URL") {
                item {
                    ScanResultWidget(result = scanResult!!, onReport = {
                        viewModel.submitReport("WEBSITE", urlText, scanResult!!.category, "Flagged url: ${scanResult!!.explanation}")
                    })
                }
            }
        }
    }
}

// 9. PAYMENT SCANNER SCREEN (UPI Receipt Auditor)
@Composable
fun PaymentScannerScreen(viewModel: RakshaViewModel) {
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()

    var selectedScenario by remember { mutableStateOf<String?>(null) }

    val scenarios = listOf(
        Triple("GPay Successful (Genuine)", "Extracted TransID: 9402949019\nAmount: Rs 15,000\nMerchant: Shiva Kirana Store\nStatus: 100% Genuine. Match verified on power network.", Color(0xFF00E676)),
        Triple("PayTM Successful (TAMPERED)", "Extracted TransID: 000219491\nAmount: Rs 5,000\nMerchant: Shiva Kirana Store\nALERT: Font spacing & digital layout inconsistencies detected. Font style mismatch in transaction ID and time digits. Highly likely generated via fake paytm bill generator apps.", Color(0xFFFF3D00)),
        Triple("PhonePe Pending (SPOOF)", "Extracted TransID: 4920491901\nAmount: Rs 8,500\nMerchant: Shiva Kirana Store\nALERT: Background color grid spoofed. Success indicator banner lacks transaction token. Legitimate funds have not left sender's account. Likely fake screenshot.", Color(0xFFFF3D00))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = "UPI RECEIPT AUDITOR", onBack = { viewModel.navigateTo("home") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Upload UPI success receipts/screenshots to verify transaction numbers, detect fake bill apps, font overrides, and alignment anomalies before parting with physical goods.",
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .border(width = 2.dp, color = CyberSecondary, shape = RoundedCornerShape(12.dp))
                                .background(CyberBackground, shape = RoundedCornerShape(12.dp))
                                .clickable {
                                    // Generate a mock bitmap and launch scan
                                    val dummyBitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(dummyBitmap)
                                    val paint = Paint().apply {
                                        color = CyberSecondary.toArgb()
                                        strokeWidth = 4f
                                    }
                                    canvas.drawRect(0f, 0f, 150f, 150f, paint)
                                    viewModel.runPaymentScan(dummyBitmap)
                                    selectedScenario = "GPay Successful (Genuine)"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Upload",
                                    tint = CyberSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("CHOOSE FILE", color = CyberSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "AUDIT SIMULATOR SCREENSHOTS",
                    color = CyberSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(scenarios) { scenario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val dummyBitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                            viewModel.runPaymentScan(dummyBitmap)
                            selectedScenario = scenario.first
                        },
                    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, scenario.third.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = scenario.first,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = scenario.second.substringBefore("\n"),
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = scenario.third,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (scanResult != null && scanResult?.type == "PAYMENT") {
                item {
                    val matchingScenario = scenarios.firstOrNull { it.first == selectedScenario }
                    val isGenuine = matchingScenario?.first?.contains("Genuine") == true
                    val severityColor = if (isGenuine) CyberPrimary else CyberDanger

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, severityColor.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "screenshot audited".uppercase(),
                                    color = severityColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (isGenuine) "VERIFIED" else "TAMPERED ALERT",
                                    color = severityColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(severityColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = matchingScenario?.second ?: "No detailed analysis",
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 10. AI SAFETY EXPLAINER SCREEN
@Composable
fun ExplainerScreen(viewModel: RakshaViewModel) {
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }

    val presetQueries = listOf(
        "Why do scammers ask for bank OTP?",
        "How do I recognize a fake website link?",
        "What is digital arrest fraud?",
        "Can I get my money back after a UPI scam?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = "AI SAFETY EXPLAINER", onBack = { viewModel.navigateTo("home") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Submit screenshots, call logs, messages, or ask questions. RakshaAI translates risk profiles and advises safety recommendations.",
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ASK RAKSHA SECURITY AI",
                            color = CyberSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Your Safety Question") },
                            placeholder = { Text("e.g. Legitimate banks never ask for credentials...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.runSafetyExplainer(query) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("run_explainer_button"),
                            enabled = !isScanning && query.isNotBlank()
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Text("EXPLAIN SECURITY RISK", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "TRENDING SECURITY CONCEPTS",
                    color = CyberSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(presetQueries) { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { query = preset },
                    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberTextSecondary.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = preset,
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Results UI
            if (scanResult != null && scanResult?.type == "EXPLAINER") {
                item {
                    ScanResultWidget(result = scanResult!!, onReport = {})
                }
            }
        }
    }
}

// 11. EMERGENCY SCREEN (Red alert)
@Composable
fun EmergencyScreen(viewModel: RakshaViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(CyberDanger.copy(alpha = 0.6f), CyberBackground)))
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "EMERGENCY SAFE MODE",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberDanger.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "IMMEDIATE ESCAPE PROTOCOL",
                        color = CyberDanger,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "1. DISCONNECT IMMEDIATELY: Hang up the phone. Legitimate police, banks, or customs never demand instant video calls or online money deposits.\n\n" +
                                "2. FREEZE TRANSACTIONS: If you sent money via UPI, call your Bank app immediately to block your card and halt the transfer token.\n\n" +
                                "3. HELPLINE: Dial 1930 immediately. It is the national cybercrime coordination portal of India to block fraud transactions.",
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930"))
                    context.startActivity(dialIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberDanger),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("dial_1930_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("DIAL 1930 HELPLINE", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cybercrime.gov.in"))
                    context.startActivity(browserIntent)
                },
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("cybercrime.gov.in PORTAL", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// 12. SCAM REPORTING CENTER
@Composable
fun ReportScreen(viewModel: RakshaViewModel) {
    val reports by viewModel.reportsState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var reportType by remember { mutableStateOf("NUMBER") } // NUMBER, WEBSITE, SMS
    var target by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = "SCAM REPORTING CENTER", onBack = { viewModel.navigateTo("home") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Report suspicious numbers, links, or texts anonymously. Your submission dynamically updates the on-device blacklist for immediate offline protection.",
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "FILE ANONYMOUS FRAUD REPORT",
                            color = CyberSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Type selector
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("NUMBER", "WEBSITE", "SMS").forEach { type ->
                                Button(
                                    onClick = { reportType = type },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (reportType == type) CyberPrimary else CyberBackground
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .testTag("report_type_$type")
                                ) {
                                    Text(
                                        text = type,
                                        color = if (reportType == type) Color.Black else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = target,
                            onValueChange = { target = it },
                            label = { Text(if (reportType == "NUMBER") "Mobile Number" else if (reportType == "WEBSITE") "Website Link" else "SMS sender ID") },
                            placeholder = { Text("e.g. +91 99999 88888") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Fraud Category") },
                            placeholder = { Text("e.g. Electricity Scam, Aadhaar Arrest, Part-time job") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Describe the Fraud Pitch") },
                            placeholder = { Text("e.g. Threatened to block SIM unless KYC completed...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberTextSecondary.copy(alpha = 0.5f),
                                focusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.submitReport(reportType, target, category, description)
                                Toast.makeText(context, "Report filed anonymously! Device blacklist updated.", Toast.LENGTH_LONG).show()
                                target = ""
                                category = ""
                                description = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_report_button"),
                            enabled = target.isNotBlank() && category.isNotBlank()
                        ) {
                            Text("SUBMIT ANONYMOUS REPORT", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ACTIVE USER-SUBMITTED REPORTS",
                    color = CyberSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (reports.isEmpty()) {
                item {
                    Text(
                        text = "No custom reports submitted yet. Submit reported numbers above.",
                        color = CyberTextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(reports) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.reportType}: ${item.target}",
                                    color = CyberPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                IconButton(onClick = { viewModel.deleteReportItem(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberDanger, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Category: ${item.category}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = item.description, color = CyberTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

// 13. HISTORIC THREAT LOGS
@Composable
fun HistoryScreen(viewModel: RakshaViewModel) {
    val history by viewModel.historyState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "THREAT LOG HISTORY",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (history.isNotEmpty()) {
                Text(
                    text = "Clear All",
                    color = CyberDanger,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable { viewModel.clearAllHistory() }
                        .padding(8.dp)
                        .testTag("clear_history_all")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = CyberPrimary.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "History is empty. Securely audit call, SMS, website links, or receipts to build safety history.",
                        color = CyberTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { item ->
                    var isExpanded by remember { mutableStateOf(false) }
                    val riskColor = when (item.riskLevel.uppercase()) {
                        "HIGH" -> CyberDanger
                        "MEDIUM" -> CyberWarning
                        else -> CyberPrimary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (item.type) {
                                            "CALL" -> Icons.Default.Call
                                            "SMS" -> Icons.Default.Message
                                            "WHATSAPP" -> Icons.Default.Smartphone
                                            "URL" -> Icons.Default.Link
                                            "PAYMENT" -> Icons.Default.QrCodeScanner
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = null,
                                        tint = riskColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.category,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = item.riskLevel,
                                    color = riskColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(riskColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Audited: ${if (item.input.contains("|")) item.input.substringAfter("|") else item.input}",
                                color = CyberTextSecondary,
                                fontSize = 11.sp,
                                maxLines = if (isExpanded) 10 else 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = CyberTextSecondary.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "RAKSHA AI SAFETY RISK EVALUATION:",
                                    color = CyberSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.explanation,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = { viewModel.deleteHistoryItem(item.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberDanger)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 14. SETTINGS & LANGUAGE
@Composable
fun SettingsScreen(viewModel: RakshaViewModel) {
    val language by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val name by viewModel.userName.collectAsStateWithLifecycle()
    val phone by viewModel.userPhone.collectAsStateWithLifecycle()
    val biometric by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val role by viewModel.userRole.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "SHIELD SYSTEM SETTINGS",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language selection card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = CyberPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MULTILINGUAL AI AGENT LANGUAGE",
                                color = CyberPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("ENGLISH", "HINDI", "TELUGU").forEach { lang ->
                                Button(
                                    onClick = { viewModel.setLanguage(lang) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (language.uppercase() == lang) CyberPrimary else CyberBackground
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("lang_$lang")
                                ) {
                                    Text(
                                        text = lang,
                                        color = if (language.uppercase() == lang) Color.Black else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Profile Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DEVICE SECURE ACCOUNT",
                            color = CyberSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Name: $name", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Secure Phone: $phone", color = CyberTextSecondary, fontSize = 12.sp)
                        Text(text = "Device Access Role: $role", color = CyberPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Biometric Toggle & Reset
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberTextSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Biometric Login Protection", color = Color.White, fontSize = 13.sp)
                            Button(
                                onClick = { viewModel.toggleBiometrics() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (biometric) CyberPrimary else CyberDanger
                                )
                            ) {
                                Text(if (biometric) "ACTIVE" else "DISABLED", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = CyberTextSecondary.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDanger),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("DEACTIVATE SECURITY SHIELD", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

// 15. ADMIN PANEL SCREEN
@Composable
fun AdminPanelScreen(viewModel: RakshaViewModel) {
    val reports by viewModel.reportsState.collectAsStateWithLifecycle()
    val blacklist by viewModel.blacklistState.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("REPORTS") } // REPORTS, BLACKLIST
    var newBlockValue by remember { mutableStateOf("") }
    var newBlockCategory by remember { mutableStateOf("") }
    var newBlockReason by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = "SECURE ADMIN SHIELD", onBack = { viewModel.navigateTo("home") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Global Cyber Safety Dashboard. Monitor reported scams and manage the local offline blacklist directly.",
                    color = CyberTextSecondary,
                    fontSize = 12.sp
                )
            }

            // Quick Stats
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(modifier = Modifier.weight(1f), title = "TOTAL REPORTS", value = reports.size.toString(), color = CyberSecondary)
                    StatCard(modifier = Modifier.weight(1f), title = "BLACKLIST COUNT", value = blacklist.size.toString(), color = CyberPrimary)
                }
            }

            // Admin tab selection
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { activeTab = "REPORTS" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == "REPORTS") CyberSecondary else CyberSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("User Fraud Reports", color = if (activeTab == "REPORTS") Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { activeTab = "BLACKLIST" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == "BLACKLIST") CyberPrimary else CyberSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Manage Blacklist", color = if (activeTab == "BLACKLIST") Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (activeTab == "REPORTS") {
                if (reports.isEmpty()) {
                    item {
                        Text(text = "No scam reports registered by users yet.", color = CyberTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    items(reports) { report ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CyberSurface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${report.reportType}: ${report.target}",
                                        color = CyberSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    IconButton(onClick = { viewModel.deleteReportItem(report.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberDanger)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Category: ${report.category}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = report.description, color = CyberTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                // ADD BLACKLIST FORM
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "ADD DIRECT BLOCKLIST ITEM", color = CyberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newBlockValue,
                                onValueChange = { newBlockValue = it },
                                label = { Text("Number or Web link") },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPrimary, focusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newBlockCategory,
                                onValueChange = { newBlockCategory = it },
                                label = { Text("Scam Category") },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPrimary, focusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newBlockReason,
                                onValueChange = { newBlockReason = it },
                                label = { Text("Scam Reason details") },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPrimary, focusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.addToBlacklist("NUMBER", newBlockValue, newBlockCategory, newBlockReason)
                                    newBlockValue = ""
                                    newBlockCategory = ""
                                    newBlockReason = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("INSERT BLACKLIST ITEM", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Blacklist listing
                items(blacklist) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.value, color = CyberPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(text = "Reason: ${item.reason}", color = CyberTextSecondary, fontSize = 11.sp)
                            }
                            IconButton(onClick = { viewModel.deleteBlacklistItem(item.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberDanger)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Reusable custom widgets
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ScanResultWidget(result: ScamScanEngine.ScanResult, onReport: () -> Unit) {
    val riskColor = when (result.riskLevel.uppercase()) {
        "HIGH" -> CyberDanger
        "MEDIUM" -> CyberWarning
        else -> CyberPrimary
    }

    var reportSubmitted by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI SAFETY PROFILE REPORT",
                    color = CyberSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${result.riskLevel} RISK",
                    color = riskColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(riskColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Scam Category: ${result.category}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Confidence Level: ${result.confidenceScore}%",
                color = CyberTextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = CyberTextSecondary.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = result.explanation,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (result.riskLevel == "HIGH" && result.type != "EXPLAINER") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onReport()
                        reportSubmitted = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberDanger),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !reportSubmitted
                ) {
                    Text(
                        text = if (reportSubmitted) "REPORTER BLACKLIST UPDATED" else "REPORT & BLOCK SENDER DIRECTLY",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
