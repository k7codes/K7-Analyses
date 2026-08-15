package com.example.analizciler

import com.example.model.AgGostergesi
import com.example.model.DexBulgusu
import com.example.model.IzinBulgusu
import com.example.model.ManifestBulgusu
import com.example.model.ObfuscationBulgusu
import com.example.model.PuanDetayi
import com.example.model.RiskDegerlendirmesi
import com.example.model.RiskSeviyesi
import com.example.model.SertifikaBulgusu

object RiskMotoru {

    fun risk_puanini_hesapla(
        izinler: List<IzinBulgusu>,
        manifest: ManifestBulgusu,
        dex: DexBulgusu,
        ag: AgGostergesi,
        obfuscation: ObfuscationBulgusu,
        sertifika: SertifikaBulgusu
    ): RiskDegerlendirmesi {
        var hamPuan = 0
        val puanDetaylari = mutableListOf<PuanDetayi>()
        val oneriler = mutableListOf<String>()
        val mitreTaktikleri = mutableSetOf<String>()

        // 1. İzin Analizi Puanlaması (Maksimum 25 Puan)
        val tehlikeliIzinSayisi = izinler.count { it.tehlikeliMi }
        var izinPuani = 0
        for (izin in izinler) {
            if (izin.tehlikeliMi) {
                izinPuani += izin.riskPuani
                izin.mitreEtiketi?.let { mitreTaktikleri.add(it) }
            }
        }
        val izinKombinasyonUyarilari = IzinAnalizcisi.risk_kombinasyonlarini_kontrol_et(izinler)
        if (izinKombinasyonUyarilari.isNotEmpty()) {
            izinPuani += izinKombinasyonUyarilari.size * 6
            oneriler.addAll(izinKombinasyonUyarilari)
        }
        val normalizeIzinPuani = (izinPuani).coerceIn(0, 25)
        puanDetaylari.add(
            PuanDetayi("İzin Riski", normalizeIzinPuani, 25, "$tehlikeliIzinSayisi tehlikeli izin, ${izinKombinasyonUyarilari.size} riskli kombinasyon.")
        )
        hamPuan += normalizeIzinPuani

        // 2. DEX ve API Güvenlik Puanlaması (Maksimum 30 Puan)
        var apiPuani = 0
        if (dex.dinamikKodYuklemeVarMi) {
            apiPuani += 15
            mitreTaktikleri.add("Defense Evasion / Dynamic Execution (T1407)")
            oneriler.add("Dinamik Kod Yükleme (DexClassLoader) tespit edildi. Bellekte harici kod çalıştırılması incelenmeli.")
        }
        if (dex.komutCalistirmaVarMi || dex.rootKontroluVarMi) {
            apiPuani += 10
            mitreTaktikleri.add("Execution / Rooting (T1406)")
            oneriler.add("Sistem kabuk komutu ve/veya root ikilileri arayışı tespit edildi.")
        }
        if (dex.erisilebilirlikSuiistimaliVarMi) {
            apiPuani += 10
            mitreTaktikleri.add("Privilege Escalation (T1417)")
            oneriler.add("Erişilebilirlik servisi otomasyon çağrıları bulundu.")
        }
        if (dex.smsKontroluVarMi) {
            apiPuani += 8
            mitreTaktikleri.add("Collection (T1412)")
            oneriler.add("Programatik SMS gönderme API referansı tespit edildi.")
        }
        if (dex.reflectionKullanimiVarMi) {
            apiPuani += 5
            mitreTaktikleri.add("Defense Evasion / Reflection (T1407)")
        }
        if (dex.zayifKriptoVarMi) {
            apiPuani += 5
            oneriler.add("Güvensiz şifreleme blok modu (DES/ECB) kullanımı saptandı.")
        }
        val normalizeApiPuani = apiPuani.coerceIn(0, 30)
        puanDetaylari.add(
            PuanDetayi("DEX ve API Çağrıları", normalizeApiPuani, 30, "${dex.apiBulgulari.size} adet hassas güvenlik API referansı.")
        )
        hamPuan += normalizeApiPuani

        // 3. Ağ ve C2 Göstergeleri Puanlaması (Maksimum 20 Puan)
        val (agPuani, agUyarilari) = AgGostergeAnalizcisi.ag_risk_puani_hesapla(ag)
        val normalizeAgPuani = agPuani.coerceIn(0, 20)
        if (ag.ipAdresleri.isNotEmpty() || ag.supheliUrlSayisi > 0) {
            mitreTaktikleri.add("Command and Control (T1437)")
        }
        oneriler.addAll(agUyarilari)
        puanDetaylari.add(
            PuanDetayi("Ağ ve C2 Göstergeleri", normalizeAgPuani, 20, "${ag.ipAdresleri.size} IP adresi, ${ag.alanAdlari.size} etki alanı.")
        )
        hamPuan += normalizeAgPuani

        // 4. Manifest ve Dışa Açık Bileşenler (Maksimum 15 Puan)
        val (manifestPuani, manifestUyarilari) = ManifestAnalizcisi.manifest_risk_puani_hesapla(manifest)
        val normalizeManifestPuani = manifestPuani.coerceIn(0, 15)
        if (manifest.acikBilesenler.isNotEmpty()) {
            mitreTaktikleri.add("Initial Access (T1476)")
        }
        oneriler.addAll(manifestUyarilari)
        puanDetaylari.add(
            PuanDetayi("Manifest Güvenliği", normalizeManifestPuani, 15, "${manifest.acikBilesenler.size} dışa açık bileşen, debug: ${manifest.debugEdilebilirMi}.")
        )
        hamPuan += normalizeManifestPuani

        // 5. Obfuscation ve Packer (Maksimum 10 Puan)
        val normalizeObfuscationPuani = ((obfuscation.obfuscationPuani * 10) / 100).coerceIn(0, 10)
        if (obfuscation.packerAdi != null) {
            mitreTaktikleri.add("Defense Evasion / Software Packing (T1407)")
            oneriler.add("Packer/Koruma tespit edildi: ${obfuscation.packerAdi}")
        }
        puanDetaylari.add(
            PuanDetayi("Obfuscation & Paketleme", normalizeObfuscationPuani, 10, "Puan: ${obfuscation.obfuscationPuani}/100, Packer: ${obfuscation.packerAdi ?: "Yok"}.")
        )
        hamPuan += normalizeObfuscationPuani

        // 6. Sertifika Güvenliği (Maksimum 10 Puan)
        var sertifikaPuani = 0
        if (!sertifika.imzaliMi) {
            sertifikaPuani += 10
            oneriler.add("APK geçerli bir dijital sertifika ile imzalanmamış.")
        } else if (sertifika.debugSertifikasiMi) {
            sertifikaPuani += 8
            oneriler.add("APK test/debug imza anahtarı ile imzalanmış (Android Debug Key).")
        }
        val normalizeSertifikaPuani = sertifikaPuani.coerceIn(0, 10)
        puanDetaylari.add(
            PuanDetayi("Sertifika Doğrulaması", normalizeSertifikaPuani, 10, if (sertifika.debugSertifikasiMi) "Debug İmzası" else "Üretim İmzası")
        )
        hamPuan += normalizeSertifikaPuani

        // Kural: Tek bir izin veya API nedeniyle APK kesinlikle zararlı ilan edilmemeli.
        // Birden fazla gösterge birlikte değerlendirilmeli.
        val toplamPuan = hamPuan.coerceIn(0, 100)

        val seviye = when (toplamPuan) {
            in 0..20 -> RiskSeviyesi.TEMIZ
            in 21..40 -> RiskSeviyesi.DUSUK_RISK
            in 41..60 -> RiskSeviyesi.ORTA_RISK
            in 61..80 -> RiskSeviyesi.YUKSEK_RISK
            else -> RiskSeviyesi.KRITIK_RISK
        }

        val ozet = when (seviye) {
            RiskSeviyesi.TEMIZ -> "Statik analizde şüpheli zararlı yazılım imzası tespit edilmedi. Standart izin ve güvenli bileşen konfigürasyonu."
            RiskSeviyesi.DUSUK_RISK -> "Sınırlı sayıda hassas izin veya olağan geliştirici fonksiyonu mevcut. Kritik bir istismar göstergesi yok."
            RiskSeviyesi.ORTA_RISK -> "Dışa açık bileşenler ve/veya hassas izin kombinasyonları tespit edildi. Uygulama yetkilerinin gözden geçirilmesi önerilir."
            RiskSeviyesi.YUKSEK_RISK -> "APK içerisinde yüksek riskli davranış göstergeleri (Dinamik kod, şüpheli ağ göstergesi veya agresif izinler) saptandı. Ayrıntılı laboratuvar analizi önerilir."
            RiskSeviyesi.KRITIK_RISK -> "Kritik güvenlik tehditleri ve çoklu saldırı vektörleri saptandı. C2 bağlantısı, gizli komut yürütme veya cihaz manipülasyonu bulguları mevcut."
        }

        if (oneriler.isEmpty()) {
            oneriler.add("Uygulama temel güvenlik standartlarına uygundur.")
        }

        return RiskDegerlendirmesi(
            toplamPuan = toplamPuan,
            seviye = seviye,
            ozet = ozet,
            puanDetaylari = puanDetaylari,
            oneriler = oneriler.distinct(),
            mitreTaktikleri = mitreTaktikleri.toList()
        )
    }
}
