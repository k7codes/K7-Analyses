package com.example.ui.ekranlar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analizciler.IzinAnalizcisi
import com.example.ui.bilesenler.CyberCard
import com.example.ui.bilesenler.KopyalanabilirVeriSatiri
import com.example.ui.bilesenler.TerminalKutusu
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardStroke
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.RiskDusuk
import com.example.ui.theme.RiskKritik
import com.example.ui.theme.RiskOrta
import com.example.ui.theme.RiskTemiz
import com.example.ui.theme.RiskYuksek
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnalizViewModel

@Composable
fun LaboratuvarAraclariEkrani(
    viewModel: AnalizViewModel,
    modifier: Modifier = Modifier
) {
    var seciliSekme by remember { mutableIntStateOf(0) }
    val sekmeler = listOf("Hash Hesaplayıcı", "İzin Tehdit Matrisi", "Motor Kuralları")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
    ) {
        ScrollableTabRow(
            selectedTabIndex = seciliSekme,
            containerColor = CyberNavySurface,
            contentColor = CyberCyan,
            edgePadding = 12.dp
        ) {
            sekmeler.forEachIndexed { idx, baslik ->
                Tab(
                    selected = seciliSekme == idx,
                    onClick = { seciliSekme = idx },
                    text = {
                        Text(
                            text = baslik,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (seciliSekme == idx) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (seciliSekme) {
                0 -> HashHesaplayiciPaneli(viewModel)
                1 -> IzinTehditMatrisiPaneli()
                2 -> MotorKurallariPaneli()
            }
        }
    }
}

@Composable
private fun HashHesaplayiciPaneli(viewModel: AnalizViewModel) {
    var girdiMetni by remember { mutableStateOf("") }
    val hashler = remember(girdiMetni) { viewModel.metin_hash_hesapla(girdiMetni) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CyberCard(title = "Kriptografik Hash Hesaplayıcı", icon = Icons.Default.Calculate) {
                Text(
                    text = "Herhangi bir dize, imza anahtarı veya string için anlık SHA-256, SHA-1 ve MD5 hashlerini yerel olarak hesaplar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = girdiMetni,
                    onValueChange = { girdiMetni = it },
                    placeholder = { Text("Hashlenecek metin veya gösterge girin...", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hash_hesaplayici_girdi"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberCardStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CyberNavyDark,
                        unfocusedContainerColor = CyberNavyDark
                    ),
                    singleLine = false,
                    maxLines = 4
                )
            }
        }

        if (girdiMetni.isNotEmpty()) {
            item {
                CyberCard(title = "Hesaplanan Kriptografik Hashler", icon = Icons.Default.Lock) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        KopyalanabilirVeriSatiri(etiket = "SHA-256", deger = hashler["SHA-256"] ?: "-")
                        KopyalanabilirVeriSatiri(etiket = "SHA-1", deger = hashler["SHA-1"] ?: "-")
                        KopyalanabilirVeriSatiri(etiket = "MD5", deger = hashler["MD5"] ?: "-")
                    }
                }
            }
        }
    }
}

@Composable
private fun IzinTehditMatrisiPaneli() {
    var arama by remember { mutableStateOf("") }
    val tumIzinler = remember {
        IzinAnalizcisi.izinleri_incele(
            listOf(
                "android.permission.SEND_SMS",
                "android.permission.RECEIVE_SMS",
                "android.permission.READ_SMS",
                "android.permission.READ_CONTACTS",
                "android.permission.WRITE_CONTACTS",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_BACKGROUND_LOCATION",
                "android.permission.RECORD_AUDIO",
                "android.permission.CAMERA",
                "android.permission.READ_PHONE_STATE",
                "android.permission.READ_CALL_LOG",
                "android.permission.PROCESS_OUTGOING_CALLS",
                "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.REQUEST_INSTALL_PACKAGES",
                "android.permission.BIND_ACCESSIBILITY_SERVICE",
                "android.permission.BIND_DEVICE_ADMIN",
                "android.permission.WRITE_SETTINGS",
                "android.permission.RECEIVE_BOOT_COMPLETED",
                "android.permission.QUERY_ALL_PACKAGES",
                "android.permission.PACKAGE_USAGE_STATS",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.INTERNET",
                "android.permission.FOREGROUND_SERVICE"
            )
        )
    }

    val filtrelenmis = remember(arama, tumIzinler) {
        if (arama.isBlank()) tumIzinler
        else tumIzinler.filter {
            it.kisaAd.contains(arama, ignoreCase = true) ||
                    it.izinAdi.contains(arama, ignoreCase = true) ||
                    it.aciklama.contains(arama, ignoreCase = true)
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            OutlinedTextField(
                value = arama,
                onValueChange = { arama = it },
                placeholder = { Text("İzin adı veya tehdit ara...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("izin_arama_girdi"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberCardStroke,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CyberNavyDark,
                    unfocusedContainerColor = CyberNavyDark
                ),
                singleLine = true
            )
        }

        items(filtrelenmis) { izin ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, if (izin.tehlikeliMi) RiskYuksek.copy(alpha = 0.5f) else CyberCardStroke),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = izin.kisaAd,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = if (izin.tehlikeliMi) RiskYuksek.copy(alpha = 0.15f) else CyberNavyDark,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (izin.tehlikeliMi) RiskYuksek else CyberCardStroke)
                        ) {
                            Text(
                                text = "+${izin.riskPuani} Risk Puanı",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (izin.tehlikeliMi) RiskYuksek else RiskTemiz,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        text = izin.izinAdi,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = izin.aciklama,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    if (izin.mitreEtiketi != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MITRE: ${izin.mitreEtiketi}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RiskDusuk,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun MotorKurallariPaneli() {
    val motorAciklama = """
k7~ DETERMINİSTİK STATİK ANALİZ VE RİSK MOTORU
----------------------------------------------------------------------
Bu motor, Android APK paketlerini ve baytkodlarını yerel olarak inceleyen
kural tabanlı bir güvenlik analiz sistemidir.

1. ÇALIŞMA PRENSİBİ
- APK dosyası içindeki AndroidManifest.xml, classes.dex, META-INF sertifikaları
  ve yerel kütüphaneler (.so) ayrıştırılır.
- Hiçbir veri bulut servisine veya harici sunucuya iletilmez.

2. AĞIRLIKLANDIRILMIŞ PUANLAMA TABLOSU
- Tehlikeli İzinler ve Kombinasyonlar : Azami 25 Puan
- DEX Güvenlik ve İstismar API'leri   : Azami 30 Puan
- Ağ Göstergeleri ve C2 Sinyalleri   : Azami 20 Puan
- Manifest Zafiyetleri (Exported/Dbg): Azami 15 Puan
- Obfuscation ve Packer Varlığı      : Azami 10 Puan
- Sertifika Doğrulaması (Debug Anahtar): Azami 10 Puan

3. RİSK SINIFLANDIRMASI (0 - 100)
- 00 - 20 : TEMİZ
- 21 - 40 : DÜŞÜK RİSK
- 41 - 60 : ORTA RİSK
- 61 - 80 : YÜKSEK RİSK
- 81 - 100: KRİTİK RİSK

4. TEMEL GÜVENLİK KURALI
Tek bir izin veya API çağrısı nedeniyle bir uygulama doğrudan zararlı
ilan edilmez. Analiz motoru çoklu davranış göstergelerini birlikte
değerlendirir.

İmza: k7~
    """.trimIndent()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TerminalKutusu(
                baslik = "k7~ STATİK MOTOR DOKÜMANTASYONU",
                metin = motorAciklama,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
