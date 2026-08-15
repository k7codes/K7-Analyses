package com.example.model

enum class RiskSeviyesi(
    val baslik: String,
    val minPuan: Int,
    val maxPuan: Int,
    val aciklama: String
) {
    TEMIZ("TEMİZ", 0, 20, "Kritik bir güvenlik tehdidi tespit edilmedi. Standart izin ve bileşen yapısı gözlemlendi."),
    DUSUK_RISK("DÜŞÜK RİSK", 21, 40, "Sınırlı sayıda hassas izin veya olağan API kullanımı mevcut. Standart davranış modeli."),
    ORTA_RISK("ORTA RİSK", 41, 60, "Dikkat çeken izin kombinasyonları veya dışa açık bileşenler mevcut. Ayrıntılı denetim önerilir."),
    YUKSEK_RISK("YÜKSEK RİSK", 61, 80, "Şüpheli ağ göstergeleri, dinamik kod yükleme veya yoğun tehlikeli izin kullanımı tespit edildi."),
    KRITIK_RISK("KRİTİK RİSK", 81, 100, "Zararlı yazılım karakteristikleri (C2 bağlantısı, root yürütme, gizli SMS/erişilebilirlik suiistimali) saptandı.")
}

data class PuanDetayi(
    val kategori: String,
    val alinanPuan: Int,
    val maxPuan: Int,
    val aciklama: String
)

data class RiskDegerlendirmesi(
    val toplamPuan: Int,
    val seviye: RiskSeviyesi,
    val ozet: String,
    val puanDetaylari: List<PuanDetayi>,
    val oneriler: List<String>,
    val mitreTaktikleri: List<String>
)

data class IzinBulgusu(
    val izinAdi: String,
    val kisaAd: String,
    val tehlikeliMi: Boolean,
    val ozelMi: Boolean,
    val riskPuani: Int,
    val kategori: String,
    val aciklama: String,
    val mitreEtiketi: String? = null
)

data class BilesenDetayi(
    val sinifAdi: String,
    val disariyaAcik: Boolean,
    val izinKorumali: Boolean,
    val riskNotu: String? = null
)

data class ManifestBulgusu(
    val toplamAktivite: Int,
    val toplamServis: Int,
    val toplamAlici: Int,
    val toplamSaglayici: Int,
    val disariAcikAktiviteSayisi: Int,
    val disariAcikServisSayisi: Int,
    val disariAcikAliciSayisi: Int,
    val disariAcikSaglayiciSayisi: Int,
    val debugEdilebilirMi: Boolean,
    val yedeklemeAktifMi: Boolean,
    val sifresizAgTrafigiAktifMi: Boolean,
    val acikBilesenler: List<BilesenDetayi>,
    val ozelIzinler: List<String>
)

data class ApiKullanimDetayi(
    val apiKategorisi: String,
    val metodReferansi: String,
    val riskSeviyesi: String,
    val riskPuani: Int,
    val aciklama: String
)

data class DexBulgusu(
    val sinifSayisi: Int,
    val metodSayisi: Int,
    val dinamikKodYuklemeVarMi: Boolean,
    val reflectionKullanimiVarMi: Boolean,
    val rootKontroluVarMi: Boolean,
    val komutCalistirmaVarMi: Boolean,
    val zayifKriptoVarMi: Boolean,
    val erisilebilirlikSuiistimaliVarMi: Boolean,
    val smsKontroluVarMi: Boolean,
    val apiBulgulari: List<ApiKullanimDetayi>
)

data class StringBulgusu(
    val supheliStringler: List<String>,
    val sistemDosyaYollari: List<String>,
    val baz64Metinler: List<String>,
    val komutSatirlari: List<String>
)

data class AgGostergesi(
    val ipAdresleri: List<String>,
    val alanAdlari: List<String>,
    val supheliUrlSayisi: Int,
    val acikPortlar: List<Int>,
    val sifresizHttpBaglantilari: List<String>
)

data class ObfuscationBulgusu(
    val obfuscationPuani: Int,
    val paketKarartmaVarMi: Boolean,
    val sinifKarakterOrani: Float,
    val packerAdi: String?,
    val karartilmisSinifOrnegi: List<String>
)

data class SertifikaBulgusu(
    val imzaliMi: Boolean,
    val yayimci: String,
    val sahip: String,
    val gecerlilikBaslangic: String,
    val gecerlilikBitis: String,
    val seriNumarasi: String,
    val sha256ParmakIzi: String,
    val sha1ParmakIzi: String,
    val md5ParmakIzi: String,
    val debugSertifikasiMi: Boolean,
    val algoritma: String
)

data class ApkAnalizSonucu(
    val paketAdi: String,
    val uygulamaAdi: String,
    val surumAdi: String,
    val surumKodu: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val apkBoyutuBayt: Long,
    val dosyaHashleri: Map<String, String>,
    val analizZamani: Long,
    val izinBulgulari: List<IzinBulgusu>,
    val manifestBulgulari: ManifestBulgusu,
    val dexBulgulari: DexBulgusu,
    val stringBulgulari: StringBulgusu,
    val agGostergeleri: AgGostergesi,
    val obfuscationBulgulari: ObfuscationBulgusu,
    val sertifikaBulgulari: SertifikaBulgusu,
    val riskDegerlendirmesi: RiskDegerlendirmesi,
    val genelRaporMetni: String,
    val arastirmaciImzasi: String = "k7~"
)
