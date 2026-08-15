package com.example.analizciler

import com.example.model.ApiKullanimDetayi
import com.example.model.DexBulgusu

object DexAnalizcisi {

    private val KRITIK_API_IMZALARI = listOf(
        Pair(
            "dalvik/system/DexClassLoader",
            ApiKullanimDetayi("Dinamik Kod Yükleme", "DexClassLoader -> Harici dex/jar yükleme", "KRİTİK", 15, "Çalışma anında şifreli veya gizli ek kod modülü çalıştırma.")
        ),
        Pair(
            "dalvik/system/InMemoryDexClassLoader",
            ApiKullanimDetayi("Dinamik Kod Yükleme", "InMemoryDexClassLoader -> Bellekten kod çalıştırma", "KRİTİK", 15, "Dosya sistemine dokunmadan doğrudan RAM'den payload yürütme.")
        ),
        Pair(
            "java/lang/reflect/Method;->invoke",
            ApiKullanimDetayi("Reflection", "Method.invoke -> Dinamik yansıma çağrısı", "ORTA", 5, "Statik tespiti atlatmak için gizli metod çalıştırma.")
        ),
        Pair(
            "java/lang/Class;->forName",
            ApiKullanimDetayi("Reflection", "Class.forName -> Dinamik sınıf çözümleme", "DÜŞÜK", 3, "Dinamik sınıf yükleme mekanizması.")
        ),
        Pair(
            "java/lang/Runtime;->exec",
            ApiKullanimDetayi("Shell / Komut", "Runtime.exec -> Sistem kabuk komutu", "KRİTİK", 12, "Root veya Linux komut satırı üzerinden yetkisiz işlem yürütme.")
        ),
        Pair(
            "java/lang/ProcessBuilder",
            ApiKullanimDetayi("Shell / Komut", "ProcessBuilder -> Alt işlem başlatma", "YÜKSEK", 8, "Harici ikili dosya (binary) veya script çalıştırma.")
        ),
        Pair(
            "DES/CBC",
            ApiKullanimDetayi("Zayıf Kriptografi", "Cipher -> DES Şifreleme", "ORTA", 6, "Kırılması kolay eski şifreleme algoritması.")
        ),
        Pair(
            "AES/ECB",
            ApiKullanimDetayi("Zayıf Kriptografi", "Cipher -> AES ECB Modu", "ORTA", 5, "ECB modu örüntü sızıntısına neden olan güvensiz şifreleme blok modu.")
        ),
        Pair(
            "getDeviceId",
            ApiKullanimDetayi("Cihaz Casusluğu", "TelephonyManager.getDeviceId", "YÜKSEK", 7, "Kullanıcı rızası olmadan donanım IMEI/cihaz kimliğini alma.")
        ),
        Pair(
            "getSubscriberId",
            ApiKullanimDetayi("Cihaz Casusluğu", "TelephonyManager.getSubscriberId", "YÜKSEK", 7, "IMSI ve SIM kart kimlik verisi çekme.")
        ),
        Pair(
            "sendTextMessage",
            ApiKullanimDetayi("SMS Manipülasyonu", "SmsManager.sendTextMessage", "KRİTİK", 10, "Kullanıcı fark etmeden arka planda SMS gönderme.")
        ),
        Pair(
            "performGlobalAction",
            ApiKullanimDetayi("Erişilebilirlik Suiistimali", "AccessibilityService.performGlobalAction", "KRİTİK", 12, "Otomatik geri tuşu, ev tuşu basma veya ekran tıklatma simülasyonu.")
        ),
        Pair(
            "DevicePolicyManager;->lockNow",
            ApiKullanimDetayi("Cihaz Kilitleme", "DevicePolicyManager.lockNow", "KRİTİK", 14, "Fidye yazılımları (Ransomware) tarafından ekranı kilitleme amacıyla kullanılır.")
        ),
        Pair(
            "DevicePolicyManager;->wipeData",
            ApiKullanimDetayi("Veri İmha", "DevicePolicyManager.wipeData", "KRİTİK", 15, "Cihazı fabrika ayarlarına sıfırlama ve verileri silme tehdidi.")
        ),
        Pair(
            "AudioRecord;->startRecording",
            ApiKullanimDetayi("Ses Dinleme", "AudioRecord.startRecording", "YÜKSEK", 8, "Mikrofon akışını doğrudan dinleme ve kaydetme.")
        )
    )

    fun guvenlik_apilerini_tara(dexMetinHavuzu: List<String>, toplamSinif: Int, toplamMetod: Int): DexBulgusu {
        val bulunanApiler = mutableListOf<ApiKullanimDetayi>()
        val tumMetin = dexMetinHavuzu.joinToString(" ")

        var dinamikKod = false
        var reflection = false
        var rootKomut = false
        var komutCalistirma = false
        var zayifKripto = false
        var erisilebilirlik = false
        var smsKontrol = false

        for ((anahtar, detay) in KRITIK_API_IMZALARI) {
            if (tumMetin.contains(anahtar, ignoreCase = false) || dexMetinHavuzu.any { it.contains(anahtar) }) {
                bulunanApiler.add(detay)
                when (detay.apiKategorisi) {
                    "Dinamik Kod Yükleme" -> dinamikKod = true
                    "Reflection" -> reflection = true
                    "Shell / Komut" -> {
                        komutCalistirma = true
                        if (anahtar.contains("Runtime") || tumMetin.contains("/system/bin/su")) rootKomut = true
                    }
                    "Zayıf Kriptografi" -> zayifKripto = true
                    "Erişilebilirlik Suiistimali" -> erisilebilirlik = true
                    "SMS Manipülasyonu" -> smsKontrol = true
                }
            }
        }

        if (tumMetin.contains("/system/bin/su") || tumMetin.contains("/system/xbin/su") || tumMetin.contains("which su")) {
            rootKomut = true
            if (bulunanApiler.none { it.metodReferansi.contains("su") }) {
                bulunanApiler.add(
                    ApiKullanimDetayi("Root Tespiti / Yetki Alma", "su binary kontrolü", "YÜKSEK", 9, "Cihazın rootlu olup olmadığını kontrol etme veya root kabuk açma.")
                )
            }
        }

        return DexBulgusu(
            sinifSayisi = toplamSinif.coerceAtLeast(1),
            metodSayisi = toplamMetod.coerceAtLeast(1),
            dinamikKodYuklemeVarMi = dinamikKod,
            reflectionKullanimiVarMi = reflection,
            rootKontroluVarMi = rootKomut,
            komutCalistirmaVarMi = komutCalistirma,
            zayifKriptoVarMi = zayifKripto,
            erisilebilirlikSuiistimaliVarMi = erisilebilirlik,
            smsKontroluVarMi = smsKontrol,
            apiBulgulari = bulunanApiler
        )
    }
}
