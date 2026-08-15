package com.example.ui.bilesenler

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RiskSeviyesi
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardStroke
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.RiskDusuk
import com.example.ui.theme.RiskKritik
import com.example.ui.theme.RiskOrta
import com.example.ui.theme.RiskTemiz
import com.example.ui.theme.RiskYuksek
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

fun riskRenginiGetir(seviye: RiskSeviyesi): Color {
    return when (seviye) {
        RiskSeviyesi.TEMIZ -> RiskTemiz
        RiskSeviyesi.DUSUK_RISK -> RiskDusuk
        RiskSeviyesi.ORTA_RISK -> RiskOrta
        RiskSeviyesi.YUKSEK_RISK -> RiskYuksek
        RiskSeviyesi.KRITIK_RISK -> RiskKritik
    }
}

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    accentColor: Color = CyberCyan,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.dp, CyberCardStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            content()
        }
    }
}

@Composable
fun RiskRozeti(
    seviye: RiskSeviyesi,
    puan: Int? = null,
    modifier: Modifier = Modifier
) {
    val renk = riskRenginiGetir(seviye)
    Surface(
        modifier = modifier.clip(RoundedCornerShape(6.dp)),
        color = renk.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, renk.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(renk)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (puan != null) "${seviye.baslik} • $puan/100" else seviye.baslik,
                style = MaterialTheme.typography.labelSmall,
                color = renk,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun MetrikKutusu(
    etiket: String,
    deger: String,
    vurguRengi: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = CyberNavySurface,
        border = BorderStroke(1.dp, CyberCardStroke.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = etiket.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = deger,
                style = MaterialTheme.typography.titleMedium,
                color = vurguRengi,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun KopyalanabilirVeriSatiri(
    etiket: String,
    deger: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(etiket, deger))
                Toast.makeText(context, "$etiket kopyalandı", Toast.LENGTH_SHORT).show()
            },
        color = CyberNavySurface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, CyberCardStroke.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = etiket,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = deger,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(etiket, deger))
                    Toast.makeText(context, "$etiket kopyalandı", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("kopyala_$etiket")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "$etiket Kopyala",
                    tint = CyberCyan.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun TerminalKutusu(
    baslik: String = "k7~ LABORATUVAR KONSOLU",
    metin: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = TerminalBackground,
        border = BorderStroke(1.dp, CyberCardStroke)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TerminalGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = baslik,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = metin,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}
