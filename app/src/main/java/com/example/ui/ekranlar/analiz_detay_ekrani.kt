package com.example.ui.ekranlar

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApkAnalizSonucu
import com.example.ui.bilesenler.CyberCard
import com.example.ui.bilesenler.KopyalanabilirVeriSatiri
import com.example.ui.bilesenler.MetrikKutusu
import com.example.ui.bilesenler.RiskRozeti
import com.example.ui.bilesenler.RiskSayaci
import com.example.ui.bilesenler.TerminalKutusu
import com.example.ui.bilesenler.riskRenginiGetir
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardStroke
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberPurpleLight
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
fun AnalizDetayEkrani(
    sonuc: ApkAnalizSonucu,
    viewModel: AnalizViewModel,
    onGeri: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var seciliSekme by remember { mutableIntStateOf(0) }
    val sekmeler = listOf(
        "Özet & Tehdit",
        "İzinler (${sonuc.izinBulgulari.size})",
        "Manifest",
        "DEX & API",
        "Ağ & IoC",
        "Sertifika & Packer",
        "Ham Rapor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
    ) {
        // Üst Başlık Çubuğu
        Surface(
            color = CyberNavy,
            border = BorderStroke(1.dp, CyberCardStroke),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onGeri,
                    modifier = Modifier.testTag("detay_geri_butonu")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri Dön",
                        tint = CyberCyan
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sonuc.uygulamaAdi,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${sonuc.paketAdi} • v${sonuc.surumAdi}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = { viewModel.raporu_paylas(context, sonuc) },
                    modifier = Modifier.testTag("raporu_paylas_butonu")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Raporu Paylaş",
                        tint = CyberCyan
                    )
                }
            }
        }

        // Sekme Çubuğu
        ScrollableTabRow(
            selectedTabIndex = seciliSekme,
            containerColor = CyberNavySurface,
            contentColor = CyberCyan,
            edgePadding = 12.dp
        ) {
            sekmeler.forEachIndexed { indeks, baslik ->
                Tab(
                    selected = seciliSekme == indeks,
                    onClick = { seciliSekme = indeks },
                    text = {
                        Text(
                            text = baslik,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (seciliSekme == indeks) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Sekme İçeriği
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (seciliSekme) {
                0 -> OzetTehditPaneli(sonuc)
                1 -> IzinlerPaneli(sonuc)
                2 -> ManifestPaneli(sonuc)
                3 -> DexApiPaneli(sonuc)
                4 -> AgGostergeleriPaneli(sonuc)
                5 -> SertifikaPackerPaneli(sonuc)
                6 -> HamRaporPaneli(sonuc, viewModel)
            }
        }
    }
}

@Composable
private fun OzetTehditPaneli(sonuc: ApkAnalizSonucu) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CyberCard(
                title = "Statik Risk Değerlendirmesi",
                icon = Icons.Default.Security,
                accentColor = riskRenginiGetir(sonuc.riskDegerlendirmesi.seviye)
            ) {
                RiskSayaci(
                    puan = sonuc.riskDegerlendirmesi.toplamPuan,
                    seviye = sonuc.riskDegerlendirmesi.seviye
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = sonuc.riskDegerlendirmesi.ozet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetrikKutusu(
                        etiket = "Tehlikeli İzin",
                        deger = "${sonuc.izinBulgulari.count { it.tehlikeliMi }}/${sonuc.izinBulgulari.size}",
                        vurguRengi = if (sonuc.izinBulgulari.any { it.tehlikeliMi }) RiskYuksek else RiskTemiz
                    )
                    MetrikKutusu(
                        etiket = "Hassas API",
                        deger = "${sonuc.dexBulgulari.apiBulgulari.size}",
                        vurguRengi = if (sonuc.dexBulgulari.apiBulgulari.isNotEmpty()) RiskOrta else RiskTemiz
                    )
                    MetrikKutusu(
                        etiket = "Açık Bileşen",
                        deger = "${sonuc.manifestBulgulari.acikBilesenler.size}",
                        vurguRengi = if (sonuc.manifestBulgulari.acikBilesenler.isNotEmpty()) RiskYuksek else RiskTemiz
                    )
                }
            }
        }

        item {
            CyberCard(title = "Risk Faktörleri Dağılımı", icon = Icons.Default.Warning) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    sonuc.riskDegerlendirmesi.puanDetaylari.forEach { puanDetayi ->
                        val oran = if (puanDetayi.maxPuan > 0) puanDetayi.alinanPuan.toFloat() / puanDetayi.maxPuan.toFloat() else 0f
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = puanDetayi.kategori,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${puanDetayi.alinanPuan} / ${puanDetayi.maxPuan} puan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { oran },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = when {
                                    oran >= 0.7f -> RiskKritik
                                    oran >= 0.4f -> RiskOrta
                                    else -> RiskDusuk
                                },
                                trackColor = CyberNavySurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = puanDetayi.aciklama,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        if (sonuc.riskDegerlendirmesi.mitreTaktikleri.isNotEmpty()) {
            item {
                CyberCard(title = "MITRE ATT&CK® for Mobile Eşleşmeleri", icon = Icons.Default.BugReport) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sonuc.riskDegerlendirmesi.mitreTaktikleri.forEach { taktik ->
                            Surface(
                                color = CyberNavyDark,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, CyberPurpleLight.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(CyberPurpleLight)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = taktik,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            CyberCard(title = "Güvenlik Araştırmacısı Notları", icon = Icons.Default.Code) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sonuc.riskDegerlendirmesi.oneriler.forEachIndexed { idx, oneri ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${idx + 1}. ",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = oneri,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            CyberCard(title = "Dosya Bütünlüğü ve Hashler", icon = Icons.Default.Lock) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KopyalanabilirVeriSatiri(etiket = "SHA-256", deger = sonuc.dosyaHashleri["SHA-256"] ?: "-")
                    KopyalanabilirVeriSatiri(etiket = "SHA-1", deger = sonuc.dosyaHashleri["SHA-1"] ?: "-")
                    KopyalanabilirVeriSatiri(etiket = "MD5", deger = sonuc.dosyaHashleri["MD5"] ?: "-")
                    KopyalanabilirVeriSatiri(etiket = "APK Boyutu", deger = "${sonuc.apkBoyutuBayt} bayt (${"%.2f".format(sonuc.apkBoyutuBayt / (1024.0 * 1024.0))} MB)")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun IzinlerPaneli(sonuc: ApkAnalizSonucu) {
    var filtre by remember { mutableStateOf("Tümü") }
    val filtrelenmis = remember(filtre, sonuc.izinBulgulari) {
        when (filtre) {
            "Tehlikeli" -> sonuc.izinBulgulari.filter { it.tehlikeliMi }
            "Özel" -> sonuc.izinBulgulari.filter { it.ozelMi }
            else -> sonuc.izinBulgulari
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                listOf("Tümü", "Tehlikeli", "Özel").forEach { secenek ->
                    FilterChip(
                        selected = filtre == secenek,
                        onClick = { filtre = secenek },
                        label = { Text(secenek, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan,
                            containerColor = CyberNavySurface,
                            labelColor = TextSecondary
                        ),
                        border = BorderStroke(1.dp, if (filtre == secenek) CyberCyan else CyberCardStroke)
                    )
                }
            }
        }

        if (filtrelenmis.isEmpty()) {
            item {
                Surface(
                    color = CyberNavySurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text(
                        text = "Seçilen filtrede izin bulunamadı.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (izin.tehlikeliMi) RiskYuksek else RiskTemiz)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = izin.kisaAd,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = if (izin.tehlikeliMi) RiskYuksek.copy(alpha = 0.15f) else CyberNavyDark,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (izin.tehlikeliMi) RiskYuksek else CyberCardStroke)
                        ) {
                            Text(
                                text = "+${izin.riskPuani} Risk",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (izin.tehlikeliMi) RiskYuksek else TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = izin.izinAdi,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyanDim,
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "MITRE: ${izin.mitreEtiketi}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberPurpleLight,
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
private fun ManifestPaneli(sonuc: ApkAnalizSonucu) {
    val m = sonuc.manifestBulgulari
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CyberCard(title = "Güvenlik Bayrakları", icon = Icons.Default.Security) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BayrakSatiri("android:debuggable", m.debugEdilebilirMi, "Hata ayıklayıcı bağlanabilir (Riskli)")
                    BayrakSatiri("android:allowBackup", m.yedeklemeAktifMi, "ADB yedekleme ile veri çekilebilir")
                    BayrakSatiri("android:usesCleartextTraffic", m.sifresizAgTrafigiAktifMi, "Şifresiz HTTP trafiğine izin verir")
                }
            }
        }

        item {
            CyberCard(title = "Bileşen Sayımları & Saldırı Yüzeyi", icon = Icons.Default.FolderZip) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MetrikKutusu(etiket = "Aktivite", deger = "${m.toplamAktivite} (${m.disariAcikAktiviteSayisi} açık)")
                    MetrikKutusu(etiket = "Servis", deger = "${m.toplamServis} (${m.disariAcikServisSayisi} açık)")
                    MetrikKutusu(etiket = "Alıcı (Receiver)", deger = "${m.toplamAlici} (${m.disariAcikAliciSayisi} açık)")
                    MetrikKutusu(etiket = "Sağlayıcı", deger = "${m.toplamSaglayici} (${m.disariAcikSaglayiciSayisi} açık)")
                }
            }
        }

        if (m.acikBilesenler.isNotEmpty()) {
            item {
                CyberCard(title = "Dışa Açık Bileşenler (Exported=true)", icon = Icons.Default.Public) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        m.acikBilesenler.forEach { bilesen ->
                            Surface(
                                color = CyberNavySurface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, RiskYuksek.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = bilesen.sinifAdi,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (bilesen.riskNotu != null) {
                                        Text(
                                            text = bilesen.riskNotu,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RiskOrta,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun BayrakSatiri(etiket: String, aktifMi: Boolean, riskAciklamasi: String) {
    Surface(
        color = CyberNavySurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (aktifMi) RiskYuksek.copy(alpha = 0.4f) else CyberCardStroke),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = etiket,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = riskAciklamasi,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
            Surface(
                color = if (aktifMi) RiskYuksek.copy(alpha = 0.2f) else RiskTemiz.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (aktifMi) "TRUE" else "FALSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (aktifMi) RiskYuksek else RiskTemiz,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DexApiPaneli(sonuc: ApkAnalizSonucu) {
    val d = sonuc.dexBulgulari
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CyberCard(title = "DEX Baytkod Göstergeleri", icon = Icons.Default.Code) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BayrakSatiri("Dinamik Kod Yükleme (DexClassLoader)", d.dinamikKodYuklemeVarMi, "Bellekte veya harici dex dosyası yükleme")
                    BayrakSatiri("Java Reflection Çağrıları", d.reflectionKullanimiVarMi, "Gizli metod ve sınıf çağrıları")
                    BayrakSatiri("Root ve Shell Komut Yürütme", d.rootKontroluVarMi || d.komutCalistirmaVarMi, "Runtime.exec veya su ikilisi tespiti")
                    BayrakSatiri("Erişilebilirlik Servisi İstismarı", d.erisilebilirlikSuiistimaliVarMi, "performGlobalAction / Tuş kaydetme riski")
                    BayrakSatiri("Gizli SMS İletimi", d.smsKontroluVarMi, "SmsManager arka plan mesajlaşması")
                    BayrakSatiri("Zayıf Kriptografi", d.zayifKriptoVarMi, "DES / AES ECB zayıf şifreleme blokları")
                }
            }
        }

        if (d.apiBulgulari.isNotEmpty()) {
            item {
                CyberCard(title = "Tespit Edilen Kritik API Referansları", icon = Icons.Default.Warning) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        d.apiBulgulari.forEach { api ->
                            Surface(
                                color = CyberNavySurface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, CyberCardStroke),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = api.apiKategorisi,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyberCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "[${api.riskSeviyesi}] +${api.riskPuani}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (api.riskSeviyesi == "KRİTİK") RiskKritik else RiskOrta,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = api.metodReferansi,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = api.aciklama,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun AgGostergeleriPaneli(sonuc: ApkAnalizSonucu) {
    val ag = sonuc.agGostergeleri
    val str = sonuc.stringBulgulari

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CyberCard(title = "Ağ ve İletişim Göstergeleri (IoC)", icon = Icons.Default.Lan) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "IP Adresleri (${ag.ipAdresleri.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    if (ag.ipAdresleri.isEmpty()) {
                        Text("Doğrudan IP adresi bulunamadı.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    } else {
                        ag.ipAdresleri.forEach { ip ->
                            KopyalanabilirVeriSatiri(etiket = "IP Adresi", deger = ip)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Etki Alanları & URL'ler (${ag.alanAdlari.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    if (ag.alanAdlari.isEmpty()) {
                        Text("Ağ bağlantı URL'si bulunamadı.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    } else {
                        ag.alanAdlari.forEach { host ->
                            KopyalanabilirVeriSatiri(etiket = "Domain", deger = host)
                        }
                    }
                }
            }
        }

        if (str.sistemDosyaYollari.isNotEmpty() || str.komutSatirlari.isNotEmpty()) {
            item {
                CyberCard(title = "Şüpheli Sistem Yolları & Komutlar", icon = Icons.Default.Code) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        str.sistemDosyaYollari.forEach { yol ->
                            KopyalanabilirVeriSatiri(etiket = "Dosya Yolu", deger = yol)
                        }
                        str.komutSatirlari.forEach { komut ->
                            KopyalanabilirVeriSatiri(etiket = "Komut", deger = komut)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun SertifikaPackerPaneli(sonuc: ApkAnalizSonucu) {
    val cert = sonuc.sertifikaBulgulari
    val obf = sonuc.obfuscationBulgulari

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CyberCard(title = "X.509 Dijital İmza Sertifikası", icon = Icons.Default.Lock) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KopyalanabilirVeriSatiri(etiket = "İmza Durumu", deger = if (cert.imzaliMi) "İmzalı" else "İmzasız")
                    KopyalanabilirVeriSatiri(etiket = "Debug Anahtarı Mı", deger = if (cert.debugSertifikasiMi) "EVET (Güvensiz Test İmzası)" else "HAYIR (Üretim İmzası)")
                    KopyalanabilirVeriSatiri(etiket = "Yayımcı (Issuer)", deger = cert.yayimci)
                    KopyalanabilirVeriSatiri(etiket = "Sahip (Subject)", deger = cert.sahip)
                    KopyalanabilirVeriSatiri(etiket = "Geçerlilik", deger = "${cert.gecerlilikBaslangic} - ${cert.gecerlilikBitis}")
                    KopyalanabilirVeriSatiri(etiket = "Algoritma", deger = cert.algoritma)
                    KopyalanabilirVeriSatiri(etiket = "SHA-256 Parmak İzi", deger = cert.sha256ParmakIzi)
                }
            }
        }

        item {
            CyberCard(title = "Obfuscation ve Packer Tespiti", icon = Icons.Default.Security) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Karartma (Obfuscation) Puanı", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text("${obf.obfuscationPuani}/100", style = MaterialTheme.typography.titleMedium, color = CyberCyan, fontFamily = FontFamily.Monospace)
                    }
                    LinearProgressIndicator(
                        progress = { obf.obfuscationPuani / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = CyberPurpleLight,
                        trackColor = CyberNavySurface
                    )
                    KopyalanabilirVeriSatiri(
                        etiket = "Tespit Edilen Packer",
                        deger = obf.packerAdi ?: "Packer Tespit Edilmedi (Standart / ProGuard)"
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun HamRaporPaneli(sonuc: ApkAnalizSonucu, viewModel: AnalizViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { viewModel.raporu_paylas(context, sonuc) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberNavyDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("ham_rapor_paylas_butonu")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Raporu Paylaş / Dışa Aktar", fontWeight = FontWeight.Bold)
            }
        }

        TerminalKutusu(
            baslik = "k7~ STATİK ANALİZ GÜVENLİK RAPORU",
            metin = sonuc.genelRaporMetni,
            modifier = Modifier.fillMaxSize()
        )
    }
}
