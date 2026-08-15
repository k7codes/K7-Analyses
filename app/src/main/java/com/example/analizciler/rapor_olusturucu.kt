package com.example.analizciler

import com.example.model.ApkAnalizSonucu
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RaporOlusturucu {

    fun raporu_olustur(sonuc: ApkAnalizSonucu): String {
        val tarihFormati = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val analizTarihi = tarihFormati.format(Date(sonuc.analizZamani))
        val sb = StringBuilder()

        sb.appendLine("================================================================================")
        sb.appendLine("                     k7~ ANALYSES — STATİK GÜVENLİK RAPORU                      ")
        sb.appendLine("                 Android APK Güvenlik Analiz Laboratuvarı                       ")
        sb.appendLine("================================================================================")
        sb.appendLine("Rapor Tarihi       : $analizTarihi")
        sb.appendLine("Araştırmacı İmzası : ${sonuc.arastirmaciImzasi}")
        sb.appendLine("Analiz Motoru      : k7~ Kural Tabanlı Deterministik Statik Motor")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("1. HEDEF METADATA BİLGİLERİ")
        sb.appendLine("  Uygulama Adı     : ${sonuc.uygulamaAdi}")
        sb.appendLine("  Paket Adı        : ${sonuc.paketAdi}")
        sb.appendLine("  Sürüm Bilgisi    : ${sonuc.surumAdi} (Kod: ${sonuc.surumKodu})")
        sb.appendLine("  SDK Hedefleri    : Min SDK ${sonuc.minSdk} | Target SDK ${sonuc.targetSdk}")
        sb.appendLine("  APK Boyutu       : ${"%.2f".format(sonuc.apkBoyutuBayt / (1024.0 * 1024.0))} MB (${sonuc.apkBoyutuBayt} bayt)")
        sb.appendLine("  SHA-256 Hash     : ${sonuc.dosyaHashleri["SHA-256"] ?: "-"}")
        sb.appendLine("  MD5 Hash         : ${sonuc.dosyaHashleri["MD5"] ?: "-"}")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("2. RİSK VE TEHDİT DEĞERLENDİRMESİ")
        sb.appendLine("  Nihai Risk Puanı : ${sonuc.riskDegerlendirmesi.toplamPuan} / 100")
        sb.appendLine("  Tehdit Seviyesi  : [ ${sonuc.riskDegerlendirmesi.seviye.baslik} ]")
        sb.appendLine("  Yönetici Özeti   : ${sonuc.riskDegerlendirmesi.ozet}")
        sb.appendLine()
        sb.appendLine("  Risk Faktörleri Dağılımı:")
        for (detay in sonuc.riskDegerlendirmesi.puanDetaylari) {
            sb.appendLine("    * ${detay.kategori.padEnd(24)} : ${detay.alinanPuan}/${detay.maxPuan} puan | ${detay.aciklama}")
        }
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("3. MITRE ATT&CK® FOR MOBILE EŞLEŞMELERİ")
        if (sonuc.riskDegerlendirmesi.mitreTaktikleri.isEmpty()) {
            sb.appendLine("  Belirgin MITRE saldırı tekniği deseni saptanmadı.")
        } else {
            for (taktik in sonuc.riskDegerlendirmesi.mitreTaktikleri) {
                sb.appendLine("  [!] $taktik")
            }
        }
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("4. İZİN MATRİSİ (${sonuc.izinBulgulari.size} adet)")
        val tehlikeliIzinler = sonuc.izinBulgulari.filter { it.tehlikeliMi }
        sb.appendLine("  Tehlikeli / Kritik İzin Sayısı: ${tehlikeliIzinler.size}")
        for (izin in sonuc.izinBulgulari) {
            val isaret = if (izin.tehlikeliMi) "[!]" else "[+]"
            sb.appendLine("  $isaret ${izin.kisaAd.padEnd(26)} : ${izin.izinAdi} (${izin.kategori})")
            sb.appendLine("      Açıklama: ${izin.aciklama}")
        }
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("5. MANIFEST BİLEŞENLERİ VE GÜVENLİK BAYRAKLARI")
        sb.appendLine("  Aktiviteler      : Toplam ${sonuc.manifestBulgulari.toplamAktivite} (Dışa Açık: ${sonuc.manifestBulgulari.disariAcikAktiviteSayisi})")
        sb.appendLine("  Servisler        : Toplam ${sonuc.manifestBulgulari.toplamServis} (Dışa Açık: ${sonuc.manifestBulgulari.disariAcikServisSayisi})")
        sb.appendLine("  Alıcılar         : Toplam ${sonuc.manifestBulgulari.toplamAlici} (Dışa Açık: ${sonuc.manifestBulgulari.disariAcikAliciSayisi})")
        sb.appendLine("  Sağlayıcılar     : Toplam ${sonuc.manifestBulgulari.toplamSaglayici} (Dışa Açık: ${sonuc.manifestBulgulari.disariAcikSaglayiciSayisi})")
        sb.appendLine("  Debuggable       : ${sonuc.manifestBulgulari.debugEdilebilirMi}")
        sb.appendLine("  AllowBackup      : ${sonuc.manifestBulgulari.yedeklemeAktifMi}")
        sb.appendLine("  CleartextTraffic : ${sonuc.manifestBulgulari.sifresizAgTrafigiAktifMi}")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("6. DEX BAYT KOD VE KRİTİK API ÇAĞRILARI")
        sb.appendLine("  Toplam Sınıf     : ${sonuc.dexBulgulari.sinifSayisi}")
        sb.appendLine("  Dinamik Kod      : ${sonuc.dexBulgulari.dinamikKodYuklemeVarMi}")
        sb.appendLine("  Reflection       : ${sonuc.dexBulgulari.reflectionKullanimiVarMi}")
        sb.appendLine("  Root Kontrolü    : ${sonuc.dexBulgulari.rootKontroluVarMi}")
        sb.appendLine("  Komut Yürütme    : ${sonuc.dexBulgulari.komutCalistirmaVarMi}")
        sb.appendLine("  Erişilebilirlik  : ${sonuc.dexBulgulari.erisilebilirlikSuiistimaliVarMi}")
        sb.appendLine("  Gizli SMS        : ${sonuc.dexBulgulari.smsKontroluVarMi}")
        if (sonuc.dexBulgulari.apiBulgulari.isNotEmpty()) {
            sb.appendLine("  Tespit Edilen API İmzaları:")
            for (api in sonuc.dexBulgulari.apiBulgulari) {
                sb.appendLine("    - [${api.riskSeviyesi}] ${api.apiKategorisi}: ${api.metodReferansi}")
            }
        }
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("7. AĞ VE TELEMETRİ GÖSTERGELERİ (IoC)")
        sb.appendLine("  IP Adresleri     : ${if (sonuc.agGostergeleri.ipAdresleri.isEmpty()) "Yok" else sonuc.agGostergeleri.ipAdresleri.joinToString(", ")}")
        sb.appendLine("  Etki Alanları    : ${if (sonuc.agGostergeleri.alanAdlari.isEmpty()) "Yok" else sonuc.agGostergeleri.alanAdlari.joinToString(", ")}")
        sb.appendLine("  Şüpheli URL'ler  : ${sonuc.agGostergeleri.supheliUrlSayisi}")
        sb.appendLine("  Şifresiz HTTP    : ${sonuc.agGostergeleri.sifresizHttpBaglantilari.size} bağlantı")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("8. SERTİFİKA VE İMZA DOĞRULAMA")
        sb.appendLine("  İmzalı Durumu    : ${sonuc.sertifikaBulgulari.imzaliMi}")
        sb.appendLine("  Yayımcı (Issuer) : ${sonuc.sertifikaBulgulari.yayimci}")
        sb.appendLine("  Debug İmzası Mı  : ${sonuc.sertifikaBulgulari.debugSertifikasiMi}")
        sb.appendLine("  SHA-256 Parmak   : ${sonuc.sertifikaBulgulari.sha256ParmakIzi}")
        sb.appendLine("  Geçerlilik       : ${sonuc.sertifikaBulgulari.gecerlilikBaslangic} - ${sonuc.sertifikaBulgulari.gecerlilikBitis}")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("9. GÜVENLİK ARAŞTIRMACISI TAVSİYELERİ")
        for ((indeks, oneri) in sonuc.riskDegerlendirmesi.oneriler.withIndex()) {
            sb.appendLine("  ${indeks + 1}. $oneri")
        }
        sb.appendLine()
        sb.appendLine("  NOT: Bu değerlendirme kural tabanlı yerel statik analiz sonuçlarına dayanmaktadır.")
        sb.appendLine("  Tekil bir izin veya API çağrısı tek başına kötü amaçlı yazılım kanıtı değildir.")
        sb.appendLine("================================================================================")
        sb.appendLine("Rapor Sonu — İmzacı: k7~")
        return sb.toString()
    }
}
