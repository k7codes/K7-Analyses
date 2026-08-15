package com.example.ui.ekranlar

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.model.ApkAnalizSonucu
import com.example.ui.bilesenler.CyberCard
import com.example.ui.bilesenler.MetrikKutusu
import com.example.ui.bilesenler.RiskRozeti
import com.example.ui.bilesenler.RiskSayaci
import com.example.ui.bilesenler.riskRenginiGetir
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardStroke
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberPurpleLight
import com.example.ui.theme.RiskKritik
import com.example.ui.theme.RiskTemiz
import com.example.ui.theme.RiskYuksek
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnalizDurumu
import com.example.ui.viewmodel.AnalizViewModel
import com.example.ui.viewmodel.YukluUygulamaOgesi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaEkran(
    viewModel: AnalizViewModel,
    modifier: Modifier = Modifier
) {
    var seciliNavIndeks by remember { mutableIntStateOf(0) }
    var detayGorunurMu by remember { mutableStateOf(false) }
    var uygulamalarSheetAcikMi by remember { mutableStateOf(false) }

    val aktifSonuc by viewModel.aktifSonuc.collectAsState()
    val analizDurumu by viewModel.analizDurumu.collectAsState()
    val ornekProfiller by viewModel.ornekProfiller.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uri_apk_analiz_et(it)
            detayGorunurMu = true
        }
    }

    if (detayGorunurMu && aktifSonuc != null) {
        AnalizDetayEkrani(
            sonuc = aktifSonuc!!,
            viewModel = viewModel,
            onGeri = { detayGorunurMu = false }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = CyberNavy,
                    contentColor = CyberCyan
                ) {
                    NavigationBarItem(
                        selected = seciliNavIndeks == 0,
                        onClick = { seciliNavIndeks = 0 },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Analiz") },
                        label = { Text("Analiz Laboratuvarı", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_analiz")
                    )
                    NavigationBarItem(
                        selected = seciliNavIndeks == 1,
                        onClick = { seciliNavIndeks = 1 },
                        icon = { Icon(Icons.Default.Build, contentDescription = "Araçlar") },
                        label = { Text("Güvenlik Araçları", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_araclar")
                    )
                    NavigationBarItem(
                        selected = seciliNavIndeks == 2,
                        onClick = { seciliNavIndeks = 2 },
                        icon = { Icon(Icons.Default.History, contentDescription = "Geçmiş") },
                        label = { Text("Rapor Geçmişi", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_gecmis")
                    )
                }
            }
        ) { icBosluk ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(icBosluk)
                    .background(CyberNavyDark)
            ) {
                when (seciliNavIndeks) {
                    0 -> AnalizPaneli(
                        viewModel = viewModel,
                        aktifSonuc = aktifSonuc,
                        ornekProfiller = ornekProfiller,
                        onApkSec = { filePickerLauncher.launch("*/*") },
                        onUygulamalariAc = { uygulamalarSheetAcikMi = true },
                        onDetayaGit = { detayGorunurMu = true }
                    )
                    1 -> LaboratuvarAraclariEkrani(viewModel = viewModel)
                    2 -> GecmisEkrani(
                        viewModel = viewModel,
                        onKayitSec = { kayit ->
                            val profiller = viewModel.ornekProfiller.value
                            val eslesen = profiller.find { it.paketAdi == kayit.paketAdi }
                            if (eslesen != null) {
                                viewModel.aktif_sonucu_belirle(eslesen)
                            }
                            detayGorunurMu = true
                        }
                    )
                }

                // Analiz İlerleme Durumu Katmanı (Loading Overlay)
                if (analizDurumu is AnalizDurumu.Calisiyor) {
                    val calisiyor = analizDurumu as AnalizDurumu.Calisiyor
                    Surface(
                        color = Color.Black.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(32.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                                border = BorderStroke(1.dp, CyberCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = CyberCyan,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "k7~ STATİK ANALİZ YÜRÜTÜLÜYOR",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = calisiyor.adim,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    LinearProgressIndicator(
                                        progress = { calisiyor.ilerlemeYuzdesi / 100f },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = CyberCyan,
                                        trackColor = CyberNavyDark
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "%${calisiyor.ilerlemeYuzdesi} Tamamlandı",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uygulamalarSheetAcikMi) {
        UygulamaSeciciSheet(
            viewModel = viewModel,
            onKapat = { uygulamalarSheetAcikMi = false },
            onUygulamaSecildi = { paketAdi ->
                uygulamalarSheetAcikMi = false
                viewModel.yuklu_uygulamayi_analiz_et(paketAdi)
                detayGorunurMu = true
            }
        )
    }
}

@Composable
private fun AnalizPaneli(
    viewModel: AnalizViewModel,
    aktifSonuc: ApkAnalizSonucu?,
    ornekProfiller: List<ApkAnalizSonucu>,
    onApkSec: () -> Unit,
    onUygulamalariAc: () -> Unit,
    onDetayaGit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Üst Başlık ve İmza
        item {
            Surface(
                color = CyberNavy,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberCardStroke),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "k7~",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ANALYSES",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Android APK Güvenlik Analiz Laboratuvarı",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        color = CyberNavyDark,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "LAB İMZASI: k7~",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Hızlı Eylem Butonları
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onApkSec,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("apk_sec_butonu"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberNavyDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("APK Seç", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onUygulamalariAc,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("uygulamalari_tara_butonu"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    border = BorderStroke(1.dp, CyberCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cihazı Tara", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Aktif Hedef ve Risk Göstergesi
        if (aktifSonuc != null) {
            item {
                val sonuc = aktifSonuc
                CyberCard(
                    title = "Aktif Analiz Hedefi",
                    icon = Icons.Default.Security,
                    accentColor = riskRenginiGetir(sonuc.riskDegerlendirmesi.seviye)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sonuc.uygulamaAdi,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${sonuc.paketAdi} • v${sonuc.surumAdi}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }

                        RiskRozeti(seviye = sonuc.riskDegerlendirmesi.seviye, puan = sonuc.riskDegerlendirmesi.toplamPuan)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    RiskSayaci(
                        puan = sonuc.riskDegerlendirmesi.toplamPuan,
                        seviye = sonuc.riskDegerlendirmesi.seviye
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = sonuc.riskDegerlendirmesi.ozet,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetrikKutusu(
                            etiket = "İzinler",
                            deger = "${sonuc.izinBulgulari.size}",
                            vurguRengi = if (sonuc.izinBulgulari.any { it.tehlikeliMi }) RiskYuksek else CyberCyan
                        )
                        MetrikKutusu(
                            etiket = "API İmzası",
                            deger = "${sonuc.dexBulgulari.apiBulgulari.size}",
                            vurguRengi = if (sonuc.dexBulgulari.apiBulgulari.isNotEmpty()) RiskYuksek else CyberCyan
                        )
                        MetrikKutusu(
                            etiket = "IoC / Ağ",
                            deger = "${sonuc.agGostergeleri.ipAdresleri.size + sonuc.agGostergeleri.alanAdlari.size}",
                            vurguRengi = if (sonuc.agGostergeleri.ipAdresleri.isNotEmpty()) RiskKritik else CyberCyan
                        )
                        MetrikKutusu(
                            etiket = "Dışa Açık",
                            deger = "${sonuc.manifestBulgulari.acikBilesenler.size}",
                            vurguRengi = if (sonuc.manifestBulgulari.acikBilesenler.isNotEmpty()) RiskYuksek else CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onDetayaGit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("detayli_rapor_butonu"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberNavyDark, contentColor = CyberCyan),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ayrıntılı Güvenlik Raporunu İncele", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Laboratuvar Benchmark Tehdit Profilleri
        item {
            Text(
                text = "LABORATUVAR TEHDİT VE TEST PROFİLLERİ",
                style = MaterialTheme.typography.titleSmall,
                color = CyberCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Statik motor analiz algoritmalarını test etmek için önceden hazırlanmış güvenlik profilleri:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ornekProfiller) { profil ->
                    val seviye = profil.riskDegerlendirmesi.seviye
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        border = BorderStroke(1.dp, if (aktifSonuc?.paketAdi == profil.paketAdi) CyberCyan else CyberCardStroke),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(260.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.ornek_profili_sec(profil)
                            }
                            .testTag("ornek_profil_${profil.paketAdi}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RiskRozeti(seviye = seviye, puan = profil.riskDegerlendirmesi.toplamPuan)
                                if (aktifSonuc?.paketAdi == profil.paketAdi) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Seçili",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = profil.uygulamaAdi,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = profil.paketAdi,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = profil.riskDegerlendirmesi.ozet,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UygulamaSeciciSheet(
    viewModel: AnalizViewModel,
    onKapat: () -> Unit,
    onUygulamaSecildi: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val uygulamalar by viewModel.yukluUygulamalar.collectAsState()
    val aramaMetni by viewModel.uygulamaAramaMetni.collectAsState()
    val sistemUygulamalariGoster by viewModel.sistemUygulamalariniGoster.collectAsState()

    val filtrelenmis = remember(uygulamalar, aramaMetni, sistemUygulamalariGoster) {
        uygulamalar.filter { app ->
            (sistemUygulamalariGoster || !app.sistemUygulamasiMi) &&
                    (aramaMetni.isBlank() ||
                            app.uygulamaAdi.contains(aramaMetni, ignoreCase = true) ||
                            app.paketAdi.contains(aramaMetni, ignoreCase = true))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onKapat,
        sheetState = sheetState,
        containerColor = CyberNavySurface,
        dragHandle = null
    ) {
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
                    text = "CİHAZDA YÜKLÜ UYGULAMALAR",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                IconButton(
                    onClick = {
                        scope.launch { sheetState.hide() }
                        onKapat()
                    },
                    modifier = Modifier.testTag("sheet_kapat_butonu")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = aramaMetni,
                onValueChange = { viewModel.arama_metni_guncelle(it) },
                placeholder = { Text("Uygulama veya paket adı ara...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("uygulama_arama_girdi"),
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

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.sistem_uygulamalari_gecis() }
            ) {
                Checkbox(
                    checked = sistemUygulamalariGoster,
                    onCheckedChange = { viewModel.sistem_uygulamalari_gecis() },
                    colors = CheckboxDefaults.colors(checkedColor = CyberCyan)
                )
                Text(
                    text = "Sistem ve çekirdek uygulamalarını da listele",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtrelenmis, key = { it.paketAdi }) { app ->
                    Surface(
                        color = CyberNavyDark,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberCardStroke),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                scope.launch { sheetState.hide() }
                                onUygulamaSecildi(app.paketAdi)
                            }
                            .testTag("uygulama_sec_${app.paketAdi}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberNavySurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = if (app.sistemUygulamasiMi) TextMuted else CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.uygulamaAdi,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${app.paketAdi} • v${app.surumAdi}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }

                            Surface(
                                color = CyberCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "ANALİZ ET",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
