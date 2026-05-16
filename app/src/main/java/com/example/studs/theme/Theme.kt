package com.example.studs.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.studs.data.repository.ColorSchemeType
import com.example.studs.data.repository.ThemeMode

private val DarkColorScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val LightColorScheme = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

private val DraculaScheme = darkColorScheme(
    primary = DraculaPrimary,
    secondary = DraculaSecondary,
    background = DraculaBackground,
    surface = DraculaBackground
)

private val MochaScheme = darkColorScheme(
    primary = MochaPrimary,
    secondary = MochaSecondary,
    background = MochaBackground,
    surface = MochaBackground
)

private val MonoDarkScheme = darkColorScheme(
    primary = MonoDarkPrimary,
    secondary = MonoDarkSecondary,
    background = MonoDarkBackground,
    surface = MonoDarkBackground
)

private val MonoLightScheme = lightColorScheme(
    primary = MonoLightPrimary,
    secondary = MonoLightSecondary,
    background = MonoLightBackground,
    surface = MonoLightBackground
)

@Composable
fun StudsTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  colorSchemeType: ColorSchemeType = ColorSchemeType.DEFAULT,
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeMode) {
      ThemeMode.SYSTEM -> isSystemInDarkTheme()
      ThemeMode.LIGHT -> false
      ThemeMode.DARK -> true
  }

  val colorScheme = when (colorSchemeType) {
      ColorSchemeType.DRACULA -> DraculaScheme
      ColorSchemeType.MOCHA -> MochaScheme
      ColorSchemeType.MONOCHROME -> if (darkTheme) MonoDarkScheme else MonoLightScheme
      ColorSchemeType.DEFAULT -> {
          when {
              dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                  val context = LocalContext.current
                  if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
              }
              darkTheme -> DarkColorScheme
              else -> LightColorScheme
          }
      }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
