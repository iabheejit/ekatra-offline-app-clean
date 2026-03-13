package org.ekatra.alfred

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import org.ekatra.alfred.data.AppSettings
import javax.inject.Inject

/**
 * Privacy consent screen — shown before collecting analytics.
 * Required for GDPR / India's DPDP Act compliance.
 */
@AndroidEntryPoint
class PrivacyConsentActivity : ComponentActivity() {

    @Inject lateinit var settings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF2563EB),
                    background = Color(0xFFF6F6F8)
                )
            ) {
                PrivacyConsentScreen(
                    onAccept = {
                        settings.setAnalyticsConsent(true)
                        finish()
                    },
                    onDecline = {
                        settings.setAnalyticsConsent(false)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun PrivacyConsentScreen(onAccept: () -> Unit, onDecline: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F8))
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("🔒", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("Your Privacy Matters", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("What we collect:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                BulletPoint("Chat session statistics (message counts, response times)")
                BulletPoint("Subject preferences and usage patterns")
                BulletPoint("Device model for performance optimization")
                BulletPoint("Crash reports to fix bugs")

                Spacer(Modifier.height(16.dp))
                Text("What we DON'T collect:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                BulletPoint("Your actual chat messages or questions")
                BulletPoint("Personal photos, contacts, or files")
                BulletPoint("Location data")

                Spacer(Modifier.height(16.dp))
                Text("Your rights:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                BulletPoint("Export all your data anytime from Settings")
                BulletPoint("Delete all cloud data anytime from Settings")
                BulletPoint("The app works fully offline if you decline")
                BulletPoint("You can change this choice later in Settings")

                Spacer(Modifier.height(16.dp))
                Text(
                    "Compliant with India's DPDP Act and GDPR.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onAccept,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("Accept & Continue")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onDecline,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue without analytics", color = Color(0xFF6B7280))
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", color = Color(0xFF6B7280))
        Text(text, fontSize = 14.sp, color = Color(0xFF374151))
    }
}
