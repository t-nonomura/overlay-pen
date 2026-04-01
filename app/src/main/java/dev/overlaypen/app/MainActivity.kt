package dev.overlaypen.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.compose.ui.res.stringResource
import dev.overlaypen.app.overlay.OverlayService

class MainActivity : ComponentActivity() {
    private var overlayPermissionGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayPermissionGranted = Settings.canDrawOverlays(this)

        setContent {
            OverlayPenTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    OverlayPenHome(
                        overlayPermissionGranted = overlayPermissionGranted,
                        onRequestPermission = ::openOverlayPermissionSettings,
                        onStartOverlay = ::startOverlay,
                        onStopOverlay = ::stopOverlay,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overlayPermissionGranted = Settings.canDrawOverlays(this)
    }

    private fun openOverlayPermissionSettings() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri(),
            ),
        )
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, R.string.overlay_permission_required, Toast.LENGTH_SHORT).show()
            openOverlayPermissionSettings()
            return
        }
        ContextCompat.startForegroundService(
            this,
            OverlayService.createIntent(this, OverlayService.ACTION_START),
        )
        Toast.makeText(this, R.string.overlay_started, Toast.LENGTH_SHORT).show()
    }

    private fun stopOverlay() {
        startService(OverlayService.createIntent(this, OverlayService.ACTION_STOP))
        Toast.makeText(this, R.string.overlay_stopped, Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun OverlayPenTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = Color(0xFF005F73),
        onPrimary = Color.White,
        secondary = Color(0xFFEE9B00),
        background = Color(0xFFF5F1E8),
        surface = Color(0xFFFFFBF3),
        onSurface = Color(0xFF1C1B1A),
    )
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content,
    )
}

@Composable
private fun OverlayPenHome(
    overlayPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5F1E8), Color(0xFFE6F1F2)),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )

            StatusCard(overlayPermissionGranted = overlayPermissionGranted)
            SpecificationCard()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                Button(
                    onClick = if (overlayPermissionGranted) onStartOverlay else onRequestPermission,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        stringResource(
                            if (overlayPermissionGranted) {
                                R.string.home_start_overlay
                            } else {
                                R.string.home_request_permission
                            },
                        ),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onStopOverlay,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.home_stop_overlay))
                }
            }
        }
    }
}

@Composable
private fun StatusCard(overlayPermissionGranted: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.status_card_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    if (overlayPermissionGranted) {
                        R.string.status_card_granted
                    } else {
                        R.string.status_card_missing
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SpecificationCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.spec_card_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            SpecLine(stringResource(R.string.spec_line_1))
            SpecLine(stringResource(R.string.spec_line_2))
            SpecLine(stringResource(R.string.spec_line_3))
            SpecLine(stringResource(R.string.spec_line_4))
            SpecLine(stringResource(R.string.spec_line_5))
            SpecLine(stringResource(R.string.spec_line_6))
            SpecLine(stringResource(R.string.spec_line_7))
            SpecLine(stringResource(R.string.spec_line_8))
        }
    }
}

@Composable
private fun SpecLine(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "*",
            color = Color(0xFFF0B429),
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
