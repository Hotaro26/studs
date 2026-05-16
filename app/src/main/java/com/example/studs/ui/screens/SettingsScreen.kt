package com.example.studs.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.Dialog
import com.example.studs.data.repository.ColorSchemeType
import com.example.studs.data.repository.ThemeMode
import com.example.studs.ui.components.MorphingSurface
import com.example.studs.ui.viewmodels.SettingsViewModel

private enum class SettingsSubScreen { MAIN, APPEARANCE, SUPPORT, ABOUT }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val windowSizeClass = calculateWindowSizeClass(LocalContext.current as ComponentActivity)
    val isTablet = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
    
    var currentSubScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }
    val themeMode by viewModel.themeMode.collectAsState()
    val colorScheme by viewModel.colorScheme.collectAsState()
    val uriHandler = LocalUriHandler.current

    if (isTablet) {
        // Tablet Layout: Two-Pane
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Pane: Navigation List
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                SettingsSection {
                    SettingsItem(
                        icon = Icons.Default.Style,
                        title = "Appearance",
                        subtitle = "Theme, Color scheme",
                        selected = currentSubScreen == SettingsSubScreen.APPEARANCE,
                        onClick = { currentSubScreen = SettingsSubScreen.APPEARANCE }
                    )
                }
                SettingsSection {
                    SettingsItem(
                        icon = Icons.Default.VolunteerActivism,
                        title = "Support",
                        subtitle = "Support the developer",
                        selected = currentSubScreen == SettingsSubScreen.SUPPORT,
                        onClick = { currentSubScreen = SettingsSubScreen.SUPPORT }
                    )
                }
                SettingsSection {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About",
                        subtitle = "Studs 1.0.0",
                        selected = currentSubScreen == SettingsSubScreen.ABOUT,
                        onClick = { currentSubScreen = SettingsSubScreen.ABOUT }
                    )
                }
            }

            // Right Pane: Detail View
            Surface(
                modifier = Modifier.weight(0.6f).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                shape = MaterialTheme.shapes.extraLarge.copy(
                    bottomEnd = CornerSize(0.dp),
                    topEnd = CornerSize(0.dp)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentSubScreen) {
                        SettingsSubScreen.MAIN -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Select a setting to view details", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        SettingsSubScreen.APPEARANCE -> AppearanceSettingsView(
                            themeMode = themeMode,
                            colorScheme = colorScheme,
                            onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                            onSetTheme = viewModel::setThemeMode,
                            onSetColorScheme = viewModel::setColorScheme,
                            hideHeader = true
                        )
                        SettingsSubScreen.SUPPORT -> SupportSettingsView(
                            onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                            hideHeader = true
                        )
                        SettingsSubScreen.ABOUT -> AboutSettingsView(
                            onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                            hideHeader = true
                        )
                    }
                }
            }
        }
    } else {
        // Mobile Layout: Single-Pane with Transitions
        BackHandler(enabled = currentSubScreen != SettingsSubScreen.MAIN) {
            currentSubScreen = SettingsSubScreen.MAIN
        }

        AnimatedContent(
            targetState = currentSubScreen,
            transitionSpec = {
                if (targetState != SettingsSubScreen.MAIN) {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it / 2 } + fadeOut()
                } else {
                    slideInHorizontally { -it / 2 } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "SettingsNavigation"
        ) { subScreen ->
            when (subScreen) {
                SettingsSubScreen.MAIN -> MainSettingsView(onNavigate = { currentSubScreen = it })
                SettingsSubScreen.APPEARANCE -> AppearanceSettingsView(
                    themeMode = themeMode,
                    colorScheme = colorScheme,
                    onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                    onSetTheme = viewModel::setThemeMode,
                    onSetColorScheme = viewModel::setColorScheme
                )
                SettingsSubScreen.SUPPORT -> SupportSettingsView(onBack = { currentSubScreen = SettingsSubScreen.MAIN })
                SettingsSubScreen.ABOUT -> AboutSettingsView(onBack = { currentSubScreen = SettingsSubScreen.MAIN })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainSettingsView(onNavigate: (SettingsSubScreen) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Settings", 
                            style = MaterialTheme.typography.headlineMedium, 
                            fontWeight = FontWeight.Bold
                        ) 
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SettingsSection {
                SettingsItem(icon = Icons.Default.Style, title = "Appearance", subtitle = "Theme, Color scheme", onClick = { onNavigate(SettingsSubScreen.APPEARANCE) })
            }
            SettingsSection {
                SettingsItem(icon = Icons.Default.VolunteerActivism, title = "Support", subtitle = "Support the developer", onClick = { onNavigate(SettingsSubScreen.SUPPORT) })
            }
            SettingsSection {
                SettingsItem(icon = Icons.Default.Info, title = "About", subtitle = "Studs 1.0.0", onClick = { onNavigate(SettingsSubScreen.ABOUT) })
            }
        }
    }
}

@Composable
private fun AppearanceSettingsView(
    themeMode: ThemeMode,
    colorScheme: ColorSchemeType,
    onBack: () -> Unit,
    onSetTheme: (ThemeMode) -> Unit,
    onSetColorScheme: (ColorSchemeType) -> Unit,
    hideHeader: Boolean = false
) {
    Scaffold(
        topBar = {
            if (!hideHeader) SubScreenHeader(title = "Appearance", subtitle = "Settings", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (hideHeader) Text("Appearance", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 8.dp))
            
            SettingsSection(title = "Theme Mode") {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SettingsItem(title = mode.name, onClick = { onSetTheme(mode) }, trailing = { RadioButton(selected = themeMode == mode, onClick = { onSetTheme(mode) }) })
                    if (index < ThemeMode.entries.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            SettingsSection(title = "Color Scheme") {
                ColorSchemeType.entries.forEachIndexed { index, scheme ->
                    SettingsItem(title = scheme.name, onClick = { onSetColorScheme(scheme) }, trailing = { RadioButton(selected = colorScheme == scheme, onClick = { onSetColorScheme(scheme) }) })
                    if (index < ColorSchemeType.entries.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun SupportSettingsView(onBack: () -> Unit, hideHeader: Boolean = false) {
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = {
            if (!hideHeader) SubScreenHeader(title = "Support", subtitle = "Donation", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (hideHeader) Text("Support", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 8.dp))
            
            SettingsSection(title = "Donate via UPI") {
                SettingsItem(
                    icon = Icons.Default.Payments, 
                    title = "UPI ID", 
                    subtitle = "9693703723@yesfam", 
                    onClick = { 
                        try {
                            uriHandler.openUri("upi://pay?pa=9693703723@yesfam&pn=Hotaro")
                        } catch (e: Exception) {
                            // Fallback if no UPI app
                        }
                    }
                )
            }
            Text("If this app helped you in your studies, consider supporting me! Every donation keeps the app alive and free.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AboutSettingsView(onBack: () -> Unit, hideHeader: Boolean = false) {
    val uriHandler = LocalUriHandler.current
    var showDiscordDialog by remember { mutableStateOf(false) }

    if (showDiscordDialog) {
        AlertDialog(
            onDismissRequest = { showDiscordDialog = false },
            title = { Text("Discord") },
            text = { Text("Discord ID: oi.hotaro") },
            confirmButton = {
                TextButton(onClick = { showDiscordDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (!hideHeader) SubScreenHeader(title = "About", subtitle = "Studs", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (hideHeader) Text("About", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 8.dp))
            
            SettingsSection {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(80.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(48.dp)) }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text("Studs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("1.0.0 (1)", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            SettingsSection(title = "Developer") {
                SettingsItem(
                    icon = Icons.Default.Person, 
                    title = "Hotaro", 
                    subtitle = "Lead Developer", 
                    onClick = { }
                ) { 
                    Text("Hotaro26", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary) 
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItem(
                    icon = null, 
                    title = "GitHub", 
                    subtitle = "github.com/Hotaro26", 
                    onClick = { uriHandler.openUri("https://github.com/Hotaro26") },
                    leading = {
                        Icon(
                            painter = painterResource(id = com.example.studs.R.drawable.ic_github),
                            contentDescription = "GitHub",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItem(
                    icon = null, 
                    title = "Pinterest", 
                    subtitle = "pinterest.com/hotaro344", 
                    onClick = { uriHandler.openUri("https://www.pinterest.com/hotaro344") },
                    leading = {
                        Icon(
                            painter = painterResource(id = com.example.studs.R.drawable.ic_pinterest),
                            contentDescription = "Pinterest",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFFE60023)
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItem(
                    icon = null, 
                    title = "Discord", 
                    subtitle = "oi.hotaro", 
                    onClick = { showDiscordDialog = true },
                    leading = {
                        Icon(
                            painter = painterResource(id = com.example.studs.R.drawable.ic_discord),
                            contentDescription = "Discord",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF5865F2)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubScreenHeader(title: String, subtitle: String, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                )
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
        },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
    )
}

@Composable
fun SettingsSection(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth()) { Column(content = content) }
    }
}

@Composable
fun SettingsItem(
    title: String, 
    icon: ImageVector? = null, 
    subtitle: String? = null, 
    selected: Boolean = false, 
    onClick: () -> Unit, 
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    MorphingSurface(
        onClick = onClick,
        initialCornerRadius = 0.dp, // No round initially to fit in the card
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium, 
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant, 
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { leading() }
                }
                Spacer(modifier = Modifier.width(16.dp))
            } else if (icon != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium, 
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant, 
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            }
            if (trailing != null) trailing()
            else if (!selected) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
