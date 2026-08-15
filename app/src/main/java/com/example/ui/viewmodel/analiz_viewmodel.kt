package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.analizciler.ApkAnalizcisi
import com.example.analizciler.OrnekProfiller
import com.example.model.ApkAnalizSonucu
import com.example.veritabani.AnalizDeposu
import com.example.veritabani.AnalizKayitEntity
import com.example.veritabani.AnalizVeritabani
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest

data class YukluUygulamaOgesi(
    val paketAdi: String,
    val uygulamaAdi: String,
    val surumAdi: String,
    val sistemUygulamasiMi: Boolean,
    val iconBitmap: Bitmap? = null
)

sealed interface AnalizDurumu {
    data object Bosta : AnalizDurumu
    data class Calisiyor(val adim: String, val ilerlemeYuzdesi: Int) : AnalizDurumu
    data class Tamamlandi(val sonuc: ApkAnalizSonucu) : AnalizDurumu
    data class Hata(val mesaj: String) : AnalizDurumu
}

class AnalizViewModel(application: Application) : AndroidViewModel(application) {

    private val depo: AnalizDeposu? = try {
        val db = AnalizVeritabani.veritabaniniGetir(application)
        AnalizDeposu(db.analizKayitDao())
    } catch (_: Exception) {
        null
    }

    private val _analizGecmisiYedek = MutableStateFlow<List<AnalizKayitEntity>>(emptyList())
    val analizGecmisi: StateFlow<List<AnalizKayitEntity>> = (depo?.tumKayitlar ?: _analizGecmisiYedek)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _analizDurumu = MutableStateFlow<AnalizDurumu>(AnalizDurumu.Bosta)
    val analizDurumu: StateFlow<AnalizDurumu> = _analizDurumu.asStateFlow()

    private val _aktifSonuc = MutableStateFlow<ApkAnalizSonucu?>(null)
    val aktifSonuc: StateFlow<ApkAnalizSonucu?> = _aktifSonuc.asStateFlow()

    private val _yukluUygulamalar = MutableStateFlow<List<YukluUygulamaOgesi>>(emptyList())
    val yukluUygulamalar: StateFlow<List<YukluUygulamaOgesi>> = _yukluUygulamalar.asStateFlow()

    private val _uygulamaAramaMetni = MutableStateFlow("")
    val uygulamaAramaMetni: StateFlow<String> = _uygulamaAramaMetni.asStateFlow()

    private val _sistemUygulamalariniGoster = MutableStateFlow(false)
    val sistemUygulamalariniGoster: StateFlow<Boolean> = _sistemUygulamalariniGoster.asStateFlow()

    private val _ornekProfiller = MutableStateFlow<List<ApkAnalizSonucu>>(emptyList())
    val ornekProfiller: StateFlow<List<ApkAnalizSonucu>> = _ornekProfiller.asStateFlow()

    init {
        try {
            val profiller = OrnekProfiller.ornek_profilleri_getir()
            _ornekProfiller.value = profiller
            if (profiller.isNotEmpty()) {
                _aktifSonuc.value = profiller.first()
            }
        } catch (_: Exception) {}

        yuklu_uygulamalari_tara()
    }

    fun arama_metni_guncelle(metin: String) {
        _uygulamaAramaMetni.value = metin
    }

    fun sistem_uygulamalari_gecis() {
        _sistemUygulamalariniGoster.value = !_sistemUygulamalariniGoster.value
    }

    fun yuklu_uygulamalari_tara() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val pm = context.packageManager
            val packages: List<PackageInfo> = try {
                pm.getInstalledPackages(PackageManager.GET_META_DATA)
            } catch (_: Exception) {
                emptyList()
            }

            val liste = packages.map { pkg ->
                val appInfo = pkg.applicationInfo
                val isSystem = if (appInfo != null) {
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                            (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                } else false

                val appName = try {
                    appInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
                } catch (_: Exception) {
                    pkg.packageName
                }

                YukluUygulamaOgesi(
                    paketAdi = pkg.packageName,
                    uygulamaAdi = appName,
                    surumAdi = pkg.versionName ?: "1.0",
                    sistemUygulamasiMi = isSystem,
                    iconBitmap = null
                )
            }.sortedBy { it.uygulamaAdi.lowercase() }

            _yukluUygulamalar.value = liste
        }
    }

    fun yuklu_uygulamayi_analiz_et(paketAdi: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _analizDurumu.value = AnalizDurumu.Calisiyor("Paket meta verileri ve izin tablosu okunuyor...", 15)
                delay(180)
                _analizDurumu.value = AnalizDurumu.Calisiyor("Manifest bileşenleri ve dışa açık servisler taranıyor...", 35)
                delay(180)
                _analizDurumu.value = AnalizDurumu.Calisiyor("DEX baytkod yapısı ve hassas API çağrıları ayrıştırılıyor...", 60)
                delay(220)
                _analizDurumu.value = AnalizDurumu.Calisiyor("Ağ göstergeleri, IP adresleri ve IoC göstergeleri taranıyor...", 80)
                delay(180)
                _analizDurumu.value = AnalizDurumu.Calisiyor("k7~ Kural Tabanlı Risk Motoru puanlamayı hesaplıyor...", 95)
                delay(150)

                val context = getApplication<Application>()
                val sonuc = ApkAnalizcisi.yuklu_uygulamayi_analiz_et(context, paketAdi)
                _aktifSonuc.value = sonuc
                _analizDurumu.value = AnalizDurumu.Tamamlandi(sonuc)

                kaydi_veritabanina_yaz(sonuc)
            } catch (e: Exception) {
                _analizDurumu.value = AnalizDurumu.Hata("Statik analiz sırasında hata: ${e.localizedMessage ?: "Bilinmeyen hata"}")
            }
        }
    }

    fun uri_apk_analiz_et(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _analizDurumu.value = AnalizDurumu.Calisiyor("APK arşiv dosyası ayrıştırılıyor...", 20)
                delay(200)
                _analizDurumu.value = AnalizDurumu.Calisiyor("DEX sınıfları ve AndroidManifest.xml taranıyor...", 55)
                delay(250)
                _analizDurumu.value = AnalizDurumu.Calisiyor("Dijital imza, hash ve şüpheli stringler taranıyor...", 80)
                delay(200)
                _analizDurumu.value = AnalizDurumu.Calisiyor("k7~ Risk Motoru deterministik raporu hazırlıyor...", 95)
                delay(150)

                val context = getApplication<Application>()
                val sonuc = ApkAnalizcisi.uri_apk_analiz_et(context, uri)
                _aktifSonuc.value = sonuc
                _analizDurumu.value = AnalizDurumu.Tamamlandi(sonuc)

                kaydi_veritabanina_yaz(sonuc)
            } catch (e: Exception) {
                _analizDurumu.value = AnalizDurumu.Hata("APK dosyası incelenirken hata: ${e.localizedMessage ?: "Dosya açılamadı"}")
            }
        }
    }

    fun ornek_profili_sec(sonuc: ApkAnalizSonucu) {
        _aktifSonuc.value = sonuc
        _analizDurumu.value = AnalizDurumu.Tamamlandi(sonuc)
        viewModelScope.launch(Dispatchers.IO) {
            kaydi_veritabanina_yaz(sonuc)
        }
    }

    fun aktif_sonucu_belirle(sonuc: ApkAnalizSonucu) {
        _aktifSonuc.value = sonuc
        _analizDurumu.value = AnalizDurumu.Tamamlandi(sonuc)
    }

    fun durumu_sifirla() {
        _analizDurumu.value = AnalizDurumu.Bosta
    }

    private suspend fun kaydi_veritabanina_yaz(sonuc: ApkAnalizSonucu) {
        try {
            val entity = AnalizKayitEntity(
                paketAdi = sonuc.paketAdi,
                uygulamaAdi = sonuc.uygulamaAdi,
                surumAdi = sonuc.surumAdi,
                surumKodu = sonuc.surumKodu,
                apkBoyutuBayt = sonuc.apkBoyutuBayt,
                sha256Hash = sonuc.dosyaHashleri["SHA-256"] ?: "-",
                riskPuani = sonuc.riskDegerlendirmesi.toplamPuan,
                riskSeviyesi = sonuc.riskDegerlendirmesi.seviye.baslik,
                tehlikeliIzinSayisi = sonuc.izinBulgulari.count { it.tehlikeliMi },
                toplamIzinSayisi = sonuc.izinBulgulari.size,
                disariAcikBilesenSayisi = sonuc.manifestBulgulari.acikBilesenler.size,
                agGostergeSayisi = sonuc.agGostergeleri.ipAdresleri.size + sonuc.agGostergeleri.alanAdlari.size,
                analizZamani = sonuc.analizZamani,
                raporOzeti = sonuc.riskDegerlendirmesi.ozet,
                tamJsonRaporu = sonuc_to_json(sonuc),
                arastirmaciImzasi = "k7~"
            )
            depo?.kaydet(entity)
        } catch (_: Exception) {}
    }

    fun gecmis_kaydi_sil(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                depo?.sil(id)
            } catch (_: Exception) {}
        }
    }

    fun tum_gecmisi_temizle() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                depo?.tumunuTemizle()
            } catch (_: Exception) {}
        }
    }

    fun metin_hash_hesapla(metin: String): Map<String, String> {
        if (metin.isEmpty()) return emptyMap()
        val bytes = metin.toByteArray(Charsets.UTF_8)
        return mapOf(
            "MD5" to hash_bayt(bytes, "MD5"),
            "SHA-1" to hash_bayt(bytes, "SHA-1"),
            "SHA-256" to hash_bayt(bytes, "SHA-256")
        )
    }

    private fun hash_bayt(bytes: ByteArray, algo: String): String {
        return try {
            val md = MessageDigest.getInstance(algo)
            md.digest(bytes).joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            "-"
        }
    }

    fun raporu_paylas(context: Context, sonuc: ApkAnalizSonucu) {
        try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, sonuc.genelRaporMetni)
                putExtra(Intent.EXTRA_TITLE, "k7~ Güvenlik Analiz Raporu: ${sonuc.uygulamaAdi}")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "k7~ Analiz Raporunu Paylaş")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (_: Exception) {}
    }

    private fun sonuc_to_json(sonuc: ApkAnalizSonucu): String {
        return try {
            val obj = JSONObject()
            obj.put("paketAdi", sonuc.paketAdi)
            obj.put("uygulamaAdi", sonuc.uygulamaAdi)
            obj.put("surumAdi", sonuc.surumAdi)
            obj.put("surumKodu", sonuc.surumKodu)
            obj.put("riskPuani", sonuc.riskDegerlendirmesi.toplamPuan)
            obj.put("riskSeviyesi", sonuc.riskDegerlendirmesi.seviye.baslik)
            obj.put("analizZamani", sonuc.analizZamani)
            obj.put("arastirmaciImzasi", "k7~")
            obj.toString()
        } catch (_: Exception) {
            "{}"
        }
    }
}

