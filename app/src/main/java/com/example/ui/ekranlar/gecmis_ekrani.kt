package com.example.ui.ekranlar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RiskSeviyesi
import com.example.ui.bilesenler.RiskRozeti
import com.example.ui.bilesenler.riskRenginiGetir
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardStroke
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.RiskKritik
import com.example.ui.theme.RiskYuksek
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnalizViewModel
import com.example.veritabani.AnalizKayitEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GecmisEkrani(
    viewModel: AnalizViewModel,
    onKayitSec: (AnalizKayitEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val kayitlar by viewModel.analizGecmisi.collectAsState()
    var aramaMetni by remember { mutableStateOf("") }
    var temizleOnayDiyalogu by remember { mutableStateOf(false) }

    val filtrelenmis = remember(aramaMetni, kayitlar) {
        if (aramaMetni.isBlank()) kayitlar
        else kayitlar.filter {
            it.uygulamaAdi.contains(aramaMetni, ignoreCase = true) ||
                    it.paketAdi.contains(aramaMetni, ignoreCase = true) ||
                    it.riskSeviyesi.contains(aramaMetni, ignoreCase = true)
        }
    }

    val tarihFormati = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ANALİZ GEÇMİŞİ",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${kayitlar.size} kayıtlı statik rapor",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            if (kayitlar.isNotEmpty()) {
                IconButton(
                    onClick = { temizleOnayDiyalogu = true },
                    modifier = Modifier.testTag("tum_gecmisi_temizle_butonu")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Geçmişi Temizle",
                        tint = RiskYuksek
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = aramaMetni,
            onValueChange = { aramaMetni = it },
            placeholder = { Text("Geçmişte ara (ad, paket, risk seviyesi)...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gecmis_arama_girdi"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CyberCardStroke,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CyberNavySurface,
                unfocusedContainerColor = CyberNavySurface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtrelenmis.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (kayitlar.isEmpty()) "Henüz kayıtlı analiz geçmişi yok." else "Aramaya uygun kayıt bulunamadı.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtrelenmis, key = { it.id }) { kayit ->
                    val seviyeEnum = when (kayit.riskSeviyesi) {
                        "TEMİZ" -> RiskSeviyesi.TEMIZ
                        "DÜŞÜK RİSK" -> RiskSeviyesi.DUSUK_RISK
                        "ORTA RİSK" -> RiskSeviyesi.ORTA_RISK
                        "YÜKSEK RİSK" -> RiskSeviyesi.YUKSEK_RISK
                        else -> RiskSeviyesi.KRITIK_RISK
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        border = BorderStroke(1.dp, CyberCardStroke),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onKayitSec(kayit) }
                            .testTag("gecmis_kayit_${kayit.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = kayit.uygulamaAdi,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = kayit.paketAdi,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.gecmis_kaydi_sil(kayit.id) },
                                    modifier = Modifier.size(28.dp).testTag("sil_kayit_${kayit.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Kaydı Sil",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RiskRozeti(seviye = seviyeEnum, puan = kayit.riskPuani)

                                Text(
                                    text = tarihFormati.format(Date(kayit.analizZamani)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "İzinler: ${kayit.tehlikeliIzinSayisi}/${kayit.toplamIzinSayisi} tehlikeli",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = "Dışa Açık: ${kayit.disariAcikBilesenSayisi}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = "İmza: ${kayit.arastirmaciImzasi}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }

    if (temizleOnayDiyalogu) {
        AlertDialog(
            onDismissRequest = { temizleOnayDiyalogu = false },
            containerColor = CyberNavySurface,
            title = {
                Text(
                    text = "Geçmişi Temizle",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Kayıtlı tüm statik analiz raporları kalıcı olarak silinecek. Onaylıyor musunuz?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.tum_gecmisi_temizle()
                        temizleOnayDiyalogu = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RiskKritik)
                ) {
                    Text("Tümünü Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { temizleOnayDiyalogu = false }) {
                    Text("İptal", color = TextSecondary)
                }
            }
        )
    }
}
