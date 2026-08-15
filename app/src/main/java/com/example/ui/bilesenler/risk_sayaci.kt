package com.example.ui.bilesenler

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RiskSeviyesi
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun RiskSayaci(
    puan: Int,
    seviye: RiskSeviyesi,
    modifier: Modifier = Modifier
) {
    val hedefOran = (puan.toFloat() / 100f).coerceIn(0f, 1f)
    val animasyonluOran by animateFloatAsState(
        targetValue = hedefOran,
        animationSpec = tween(durationMillis = 800),
        label = "risk_sayac_anim"
    )

    val anaRenk = riskRenginiGetir(seviye)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            Canvas(modifier = Modifier.size(150.dp)) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                val arcSize = Size(radius * 2, radius * 2)

                // 240 derecelik yay (150'den başlayıp 390'a)
                val startAngle = 150f
                val sweepAngle = 240f

                // Arka plan çizgisi
                drawArc(
                    color = Color(0xFF162544),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Aktif risk göstergesi
                val currentSweep = sweepAngle * animasyonluOran
                if (currentSweep > 0) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0.0f to Color(0xFF00E5FF),
                            0.3f to Color(0xFFFFB703),
                            0.7f to Color(0xFFFF5722),
                            1.0f to Color(0xFFFF1744)
                        ),
                        startAngle = startAngle,
                        sweepAngle = currentSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$puan",
                    style = MaterialTheme.typography.displayMedium,
                    color = anaRenk,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 36.sp
                )
                Text(
                    text = "RİSK PUANI",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        RiskRozeti(seviye = seviye, puan = puan)
    }
}
