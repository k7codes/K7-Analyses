package com.example.analizciler

import com.example.model.ApkAnalizSonucu
import com.example.model.BilesenDetayi
import com.example.model.ObfuscationBulgusu
import com.example.model.SertifikaBulgusu
import com.example.model.StringBulgusu

object OrnekProfiller {

    fun ornek_profilleri_getir(): List<ApkAnalizSonucu> {
        return listOf(
            trojan_bankacilik_profili(),
            casus_yazilim_profili(),
            fidye_yazilimi_profili(),
            temiz_arac_profili(),
            reklam_izleyici_profili(),
            root_istismar_profili()
        )
    }

    private fun trojan_bankacilik_profili(): ApkAnalizSonucu {
        val izinler = IzinAnalizcisi.izinleri_incele(
            listOf(
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.RECEIVE_BOOT_COMPLETED",
                "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.BIND_ACCESSIBILITY_SERVICE",
                "android.permission.QUERY_ALL_PACKAGES",
                "android.permission.READ_PHONE_STATE",
                "android.permission.RECEIVE_SMS",
                "android.permission.SEND_SMS",
                "android.permission.REQUEST_INSTALL_PACKAGES"
            )
        )

        val aktiviteler = listOf(
            BilesenDetayi("com.secpay.update.MainActivity", true, false),
            BilesenDetayi("com.secpay.update.OverlayActivity", true, false, "Sahte bankacılık arayüz katmanı"),
            BilesenDetayi("com.secpay.update.SettingsActivity", false, true)
        )
        val servisler = listOf(
            BilesenDetayi("com.secpay.update.AccessibilityCoreService", true, false, "Erişilebilirlik Suiistimal Servisi"),
            BilesenDetayi("com.secpay.update.BackgroundSyncService", true, false)
        )
        val alicilar = listOf(
            BilesenDetayi("com.secpay.update.BootReceiver", true, false, "Kalıcılık tetikleyicisi"),
            BilesenDetayi("com.secpay.update.SmsInterceptor", true, false, "SMS OTP Yakalayıcı")
        )
        val saglayicilar = emptyList<BilesenDetayi>()

        val manifest = ManifestAnalizcisi.manifesti_oku(
            aktiviteler = aktiviteler,
            servisler = servisler,
            alicilar = alicilar,
            saglayicilar = saglayicilar,
            debugEdilebilir = false,
            yedeklemeAktif = false,
            sifresizAg = true,
            ozelIzinler = listOf("com.secpay.permission.PAYLOAD")
        )

        val dexMetinler = listOf(
            "dalvik/system/DexClassLoader",
            "performGlobalAction",
            "sendTextMessage",
            "getDeviceId",
            "getSubscriberId",
            "java/lang/reflect/Method;->invoke",
            "https://gate.darkpay99.top/c2/register",
            "http://185.220.101.5:4444/bot/checkin",
            "AES/ECB",
            "chmod 777",
            "/data/local/tmp/payload.jar"
        )

        val dex = DexAnalizcisi.guvenlik_apilerini_tara(dexMetinler, 245, 1890)
        val strings = StringAnalizcisi.stringleri_cikar(dexMetinler + listOf("aW5qZWN0X2Jhbmspbmdfb3ZlcmxheQ==", "telegram.org/bot772819:AAEf/sendMsg"))
        val ag = AgGostergeAnalizcisi.ag_gostergelerini_bul(dexMetinler)
        val obfuscation = ObfuscationAnalizcisi.obfuscation_puanla(
            listOf("a.a.a", "a.a.b", "b.c.d", "com.secpay.Core", "c.d.e", "f.g.h"),
            listOf("libSecShell.so")
        )
        val sertifika = SertifikaAnalizcisi.sertifikayi_cozumle(null).copy(
            imzaliMi = true,
            yayimci = "CN=Android Debug, O=Android, C=US",
            sahip = "CN=Android Debug, O=Android, C=US",
            gecerlilikBaslangic = "01.01.2023 10:00",
            gecerlilikBitis = "01.01.2053 10:00",
            seriNumarasi = "7F82BC194DA",
            sha256ParmakIzi = "8E:4A:12:DF:99:3B:11:02:AA:BC:EE:55:12:44:88:99:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF",
            debugSertifikasiMi = true,
            algoritma = "SHA256withRSA"
        )

        val risk = RiskMotoru.risk_puanini_hesapla(izinler, manifest, dex, ag, obfuscation, sertifika)

        val sonuc = ApkAnalizSonucu(
            paketAdi = "com.secpay.flashupdate",
            uygulamaAdi = "Flash Player & Güvenlik Güncelleyici",
            surumAdi = "2.4.1",
            surumKodu = 241,
            minSdk = 24,
            targetSdk = 33,
            apkBoyutuBayt = 4850200,
            dosyaHashleri = mapOf(
                "MD5" to "e4d909c290d0fb1ca068ffaddf22cbd0",
                "SHA-1" to "2aae6c35c94fcfb415dbe95f408b9ce91ee846ed",
                "SHA-256" to "8b1a9953c4611296a827abf8c47804d7ecd3f4b01e4a3b8d4f4e24efb5ef601a"
            ),
            analizZamani = System.currentTimeMillis() - 3600000,
            izinBulgulari = izinler,
            manifestBulgulari = manifest,
            dexBulgulari = dex,
            stringBulgulari = strings,
            agGostergeleri = ag,
            obfuscationBulgulari = obfuscation,
            sertifikaBulgulari = sertifika,
            riskDegerlendirmesi = risk,
            genelRaporMetni = "",
            arastirmaciImzasi = "k7~"
        )
        return sonuc.copy(genelRaporMetni = RaporOlusturucu.raporu_olustur(sonuc))
    }

    private fun casus_yazilim_profili(): ApkAnalizSonucu {
        val izinler = IzinAnalizcisi.izinleri_incele(
            listOf(
                "android.permission.INTERNET",
                "android.permission.RECORD_AUDIO",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_BACKGROUND_LOCATION",
                "android.permission.READ_CONTACTS",
                "android.permission.READ_CALL_LOG",
                "android.permission.CAMERA",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.RECEIVE_BOOT_COMPLETED"
            )
        )
        val aktiviteler = listOf(BilesenDetayi("com.monitor.systemservice.MainActivity", false, false))
        val servisler = listOf(BilesenDetayi("com.monitor.systemservice.TrackingCoreService", true, false, "Arka plan izleme motoru"))
        val alicilar = listOf(BilesenDetayi("com.monitor.systemservice.BootReceiver", true, false))
        val saglayicilar = emptyList<BilesenDetayi>()

        val manifest = ManifestAnalizcisi.manifesti_oku(aktiviteler, servisler, alicilar, saglayicilar, false, true, false, emptyList())
        val dexMetinler = listOf(
            "AudioRecord;->startRecording",
            "getDeviceId",
            "getSubscriberId",
            "https://api.spyendpoint.xyz/upload/audio",
            "https://duckdns.org/sync_telemetry",
            "/data/local/tmp/dump.raw",
            "/system/bin/su"
        )
        val dex = DexAnalizcisi.guvenlik_apilerini_tara(dexMetinler, 180, 1200)
        val strings = StringAnalizcisi.stringleri_cikar(dexMetinler)
        val ag = AgGostergeAnalizcisi.ag_gostergelerini_bul(dexMetinler)
        val obfuscation = ObfuscationAnalizcisi.obfuscation_puanla(
            listOf("a.a.a", "a.b.c", "com.monitor.systemservice.Core"),
            emptyList()
        )
        val sertifika = SertifikaAnalizcisi.sertifikayi_cozumle(null).copy(
            imzaliMi = true,
            yayimci = "CN=System Utilities Dev, OU=Mobile, O=Global Corp, C=US",
            sahip = "CN=System Utilities Dev, OU=Mobile, O=Global Corp, C=US",
            gecerlilikBaslangic = "10.05.2021 12:00",
            gecerlilikBitis = "10.05.2046 12:00",
            seriNumarasi = "4C91A00281F",
            sha256ParmakIzi = "12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF",
            debugSertifikasiMi = false,
            algoritma = "SHA256withRSA"
        )
        val risk = RiskMotoru.risk_puanini_hesapla(izinler, manifest, dex, ag, obfuscation, sertifika)
        val sonuc = ApkAnalizSonucu(
            paketAdi = "com.system.deviceguard.core",
            uygulamaAdi = "System Device Guard",
            surumAdi = "1.0.4",
            surumKodu = 104,
            minSdk = 26,
            targetSdk = 34,
            apkBoyutuBayt = 3120000,
            dosyaHashleri = mapOf(
                "MD5" to "7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d",
                "SHA-1" to "3f4e5d6c7b8a90123456789abcdef0123456789a",
                "SHA-256" to "9f8e7d6c5b4a32109876543210fedcba9876543210fedcba9876543210fedcba"
            ),
            analizZamani = System.currentTimeMillis() - 7200000,
            izinBulgulari = izinler,
            manifestBulgulari = manifest,
            dexBulgulari = dex,
            stringBulgulari = strings,
            agGostergeleri = ag,
            obfuscationBulgulari = obfuscation,
            sertifikaBulgulari = sertifika,
            riskDegerlendirmesi = risk,
            genelRaporMetni = "",
            arastirmaciImzasi = "k7~"
        )
        return sonuc.copy(genelRaporMetni = RaporOlusturucu.raporu_olustur(sonuc))
    }

    private fun fidye_yazilimi_profili(): ApkAnalizSonucu {
        val izinler = IzinAnalizcisi.izinleri_incele(
            listOf(
                "android.permission.INTERNET",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.BIND_DEVICE_ADMIN",
                "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.RECEIVE_BOOT_COMPLETED"
            )
        )
        val aktiviteler = listOf(BilesenDetayi("com.locker.ransom.LockActivity", true, false, "Kilit Ekranı"))
        val servisler = listOf(BilesenDetayi("com.locker.ransom.CryptService", false, false))
        val alicilar = listOf(BilesenDetayi("com.locker.ransom.AdminReceiver", true, false, "Cihaz Yönetici Alıcısı"))
        val manifest = ManifestAnalizcisi.manifesti_oku(aktiviteler, servisler, alicilar, emptyList(), false, false, false, emptyList())

        val dexMetinler = listOf(
            "DevicePolicyManager;->lockNow",
            "DevicePolicyManager;->wipeData",
            "Cipher;->getInstance(\"AES/CBC\")",
            "https://ransom-pay.onion/wallet/btc",
            "http://194.26.29.112:1337/key",
            "/system/bin/su"
        )
        val dex = DexAnalizcisi.guvenlik_apilerini_tara(dexMetinler, 95, 620)
        val strings = StringAnalizcisi.stringleri_cikar(dexMetinler)
        val ag = AgGostergeAnalizcisi.ag_gostergelerini_bul(dexMetinler)
        val obfuscation = ObfuscationAnalizcisi.obfuscation_puanla(listOf("a.a", "a.b", "b.a"), emptyList())
        val sertifika = SertifikaAnalizcisi.sertifikayi_cozumle(null).copy(
            imzaliMi = true,
            yayimci = "CN=Unknown Developer",
            sahip = "CN=Unknown Developer",
            gecerlilikBaslangic = "01.01.2024",
            gecerlilikBitis = "01.01.2025",
            seriNumarasi = "11223344",
            sha256ParmakIzi = "FF:EE:DD:CC:BB:AA:99:88:77:66:55:44:33:22:11:00:FF:EE:DD:CC:BB:AA:99:88:77:66:55:44:33:22:11:00",
            debugSertifikasiMi = false,
            algoritma = "SHA1withRSA"
        )
        val risk = RiskMotoru.risk_puanini_hesapla(izinler, manifest, dex, ag, obfuscation, sertifika)
        val sonuc = ApkAnalizSonucu(
            paketAdi = "com.crypto.devicecleaner",
            uygulamaAdi = "Ultra Memory Booster & Cleaner",
            surumAdi = "3.1.0",
            surumKodu = 310,
            minSdk = 23,
            targetSdk = 31,
            apkBoyutuBayt = 1950000,
            dosyaHashleri = mapOf(
                "MD5" to "45c829e1f37e42d7a9b01c34ef567890",
                "SHA-1" to "ab12cd34ef567890ab12cd34ef567890ab12cd34",
                "SHA-256" to "a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f60718293a4b5c6d7e8f90"
            ),
            analizZamani = System.currentTimeMillis() - 14400000,
            izinBulgulari = izinler,
            manifestBulgulari = manifest,
            dexBulgulari = dex,
            stringBulgulari = strings,
            agGostergeleri = ag,
            obfuscationBulgulari = obfuscation,
            sertifikaBulgulari = sertifika,
            riskDegerlendirmesi = risk,
            genelRaporMetni = "",
            arastirmaciImzasi = "k7~"
        )
        return sonuc.copy(genelRaporMetni = RaporOlusturucu.raporu_olustur(sonuc))
    }

    private fun temiz_arac_profili(): ApkAnalizSonucu {
        val izinler = IzinAnalizcisi.izinleri_incele(
            listOf(
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.POST_NOTIFICATIONS"
            )
        )
        val aktiviteler = listOf(
            BilesenDetayi("org.foss.notes.MainActivity", true, false),
            BilesenDetayi("org.foss.notes.SettingsActivity", false, false)
        )
        val servisler = emptyList<BilesenDetayi>()
        val alicilar = emptyList<BilesenDetayi>()
        val saglayicilar = emptyList<BilesenDetayi>()

        val manifest = ManifestAnalizcisi.manifesti_oku(aktiviteler, servisler, alicilar, saglayicilar, false, true, false, emptyList())
        val dexMetinler = listOf(
            "https://api.github.com/repos/notes/app",
            "https://foss-notes.org/sync"
        )
        val dex = DexAnalizcisi.guvenlik_apilerini_tara(dexMetinler, 120, 850)
        val strings = StringAnalizcisi.stringleri_cikar(dexMetinler)
        val ag = AgGostergeAnalizcisi.ag_gostergelerini_bul(dexMetinler)
        val obfuscation = ObfuscationAnalizcisi.obfuscation_puanla(
            listOf("org.foss.notes.MainActivity", "org.foss.notes.data.Note", "org.foss.notes.ui.Theme"),
            emptyList()
        )
        val sertifika = SertifikaAnalizcisi.sertifikayi_cozumle(null).copy(
            imzaliMi = true,
            yayimci = "CN=FOSS Open Source Release, OU=Development, O=Community, C=DE",
            sahip = "CN=FOSS Open Source Release, OU=Development, O=Community, C=DE",
            gecerlilikBaslangic = "01.01.2022 00:00",
            gecerlilikBitis = "01.01.2047 00:00",
            seriNumarasi = "90AB3312FE",
            sha256ParmakIzi = "55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44",
            debugSertifikasiMi = false,
            algoritma = "SHA256withECDSA"
        )
        val risk = RiskMotoru.risk_puanini_hesapla(izinler, manifest, dex, ag, obfuscation, sertifika)
        val sonuc = ApkAnalizSonucu(
            paketAdi = "org.foss.securenotes",
            uygulamaAdi = "Secure Notes FOSS",
            surumAdi = "1.8.2",
            surumKodu = 182,
            minSdk = 26,
            targetSdk = 34,
            apkBoyutuBayt = 2840000,
            dosyaHashleri = mapOf(
                "MD5" to "11223344556677889900aabbccddeeff",
                "SHA-1" to "1234567890123456789012345678901234567890",
                "SHA-256" to "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
            ),
            analizZamani = System.currentTimeMillis() - 25000000,
            izinBulgulari = izinler,
            manifestBulgulari = manifest,
            dexBulgulari = dex,
            stringBulgulari = strings,
            agGostergeleri = ag,
            obfuscationBulgulari = obfuscation,
            sertifikaBulgulari = sertifika,
            riskDegerlendirmesi = risk,
            genelRaporMetni = "",
            arastirmaciImzasi = "k7~"
        )
        return sonuc.copy(genelRaporMetni = RaporOlusturucu.raporu_olustur(sonuc))
    }

    private fun reklam_izleyici_profili(): ApkAnalizSonucu {
        val izinler = IzinAnalizcisi.izinleri_incele(
            listOf(
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.READ_PHONE_STATE",
                "android.permission.WAKE_LOCK"
            )
        )
        val aktiviteler = listOf(
            BilesenDetayi("com.adnetwork.game.GameActivity", true, false),
            BilesenDetayi("com.adnetwork.game.AdActivity", true, false)
        )
        val servisler = listOf(BilesenDetayi("com.adnetwork.game.TrackingService", true, false))
        val alicilar = emptyList<BilesenDetayi>()
        val manifest = ManifestAnalizcisi.manifesti_oku(aktiviteler, servisler, alicilar, emptyList(), false, true, true, emptyList())

        val dexMetinler = listOf(
            "getDeviceId",
            "https://ads.tracker-network.xyz/bid",
            "http://stat.analytics-telemetry.top/pixel.gif",
            "http://104.244.42.1:8080/log"
        )
        val dex = DexAnalizcisi.guvenlik_apilerini_tara(dexMetinler, 160, 1100)
        val strings = StringAnalizcisi.stringleri_cikar(dexMetinler)
        val ag = AgGostergeAnalizcisi.ag_gostergelerini_bul(dexMetinler)
        val obfuscation = ObfuscationAnalizcisi.obfuscation_puanla(
            listOf("com.adnetwork.a", "com.adnetwork.b", "com.adnetwork.c"),
            emptyList()
        )
        val sertifika = SertifikaAnalizcisi.sertifikayi_cozumle(null).copy(
            imzaliMi = true,
            yayimci = "CN=Game Studios Mobile Ltd, O=Mobile, C=US",
            sahip = "CN=Game Studios Mobile Ltd, O=Mobile, C=US",
            gecerlilikBaslangic = "15.03.2023",
            gecerlilikBitis = "15.03.2048",
            seriNumarasi = "88AA77BB",
            sha256ParmakIzi = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99",
            debugSertifikasiMi = false,
            algoritma = "SHA256withRSA"
        )
        val risk = RiskMotoru.risk_puanini_hesapla(izinler, manifest, dex, ag, obfuscation, sertifika)
        val sonuc = ApkAnalizSonucu(
            paketAdi = "com.casual.puzzle.blockrunner",
            uygulamaAdi = "Block Runner 3D Puzzle",
            surumAdi = "4.2.0",
            surumKodu = 420,
            minSdk = 24,
            targetSdk = 33,
            apkBoyutuBayt = 18400000,
            dosyaHashleri = mapOf(
                "MD5" to "a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4",
                "SHA-1" to "0987654321fedcba0987654321fedcba09876543",
                "SHA-256" to "bb22cc33dd44ee55ff6600112233445566778899aabbccddeeff001122334455"
            ),
            analizZamani = System.currentTimeMillis() - 36000000,
            izinBulgulari = izinler,
            manifestBulgulari = manifest,
            dexBulgulari = dex,
            stringBulgulari = strings,
            agGostergeleri = ag,
            obfuscationBulgulari = obfuscation,
            sertifikaBulgulari = sertifika,
            riskDegerlendirmesi = risk,
            genelRaporMetni = "",
            arastirmaciImzasi = "k7~"
        )
        return sonuc.copy(genelRaporMetni = RaporOlusturucu.raporu_olustur(sonuc))
    }

    private fun root_istismar_profili(): ApkAnalizSonucu {
        val izinler = IzinAnalizcisi.izinleri_incele(
            listOf(
                "android.permission.INTERNET",
                "android.permission.WRITE_SETTINGS",
                "android.permission.REQUEST_INSTALL_PACKAGES",
                "android.permission.WRITE_EXTERNAL_STORAGE"
            )
        )
        val aktiviteler = listOf(BilesenDetayi("com.roottools.exploit.MainExploitActivity", true, false))
        val servisler = listOf(BilesenDetayi("com.roottools.exploit.RootDaemonService", false, false))
        val manifest = ManifestAnalizcisi.manifesti_oku(aktiviteler, servisler, emptyList(), emptyList(), true, false, true, emptyList())

        val dexMetinler = listOf(
            "java/lang/Runtime;->exec",
            "java/lang/ProcessBuilder",
            "/system/bin/su",
            "/system/xbin/su",
            "chmod 777 /data/local/tmp/dirtycow",
            "mount -o remount,rw /system",
            "c2_server=198.51.100.23:9001"
        )
        val dex = DexAnalizcisi.guvenlik_apilerini_tara(dexMetinler, 110, 780)
        val strings = StringAnalizcisi.stringleri_cikar(dexMetinler)
        val ag = AgGostergeAnalizcisi.ag_gostergelerini_bul(dexMetinler)
        val obfuscation = ObfuscationBulgusu(65, true, 0.45f, "DexGuard", listOf("a.a", "a.b", "b.c"))
        val sertifika = SertifikaBulgusu(
            imzaliMi = true,
            yayimci = "CN=Android Debug, O=Android, C=US",
            sahip = "CN=Android Debug, O=Android, C=US",
            gecerlilikBaslangic = "01.01.2023",
            gecerlilikBitis = "01.01.2053",
            seriNumarasi = "99887766",
            sha256ParmakIzi = "11:22:33:44:55:66:77:88:99:00:AA:BB:CC:DD:EE:FF:11:22:33:44:55:66:77:88:99:00:AA:BB:CC:DD:EE:FF",
            sha1ParmakIzi = "11:22:33:44:55:66:77:88:99:00:AA:BB:CC:DD:EE:FF:11:22:33:44",
            md5ParmakIzi = "11:22:33:44:55:66:77:88:99:00:AA:BB:CC:DD",
            debugSertifikasiMi = true,
            algoritma = "SHA256withRSA"
        )
        val risk = RiskMotoru.risk_puanini_hesapla(izinler, manifest, dex, ag, obfuscation, sertifika)
        val sonuc = ApkAnalizSonucu(
            paketAdi = "com.roottools.quickroot.pro",
            uygulamaAdi = "OneClick SuperRoot Installer",
            surumAdi = "5.0.1",
            surumKodu = 501,
            minSdk = 21,
            targetSdk = 29,
            apkBoyutuBayt = 6420000,
            dosyaHashleri = mapOf(
                "MD5" to "d41d8cd98f00b204e9800998ecf8427e",
                "SHA-1" to "da39a3ee5e6b4b0d3255bfef95601890afd80709",
                "SHA-256" to "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
            ),
            analizZamani = System.currentTimeMillis() - 48000000,
            izinBulgulari = izinler,
            manifestBulgulari = manifest,
            dexBulgulari = dex,
            stringBulgulari = strings,
            agGostergeleri = ag,
            obfuscationBulgulari = obfuscation,
            sertifikaBulgulari = sertifika,
            riskDegerlendirmesi = risk,
            genelRaporMetni = "",
            arastirmaciImzasi = "k7~"
        )
        return sonuc.copy(genelRaporMetni = RaporOlusturucu.raporu_olustur(sonuc))
    }
}
