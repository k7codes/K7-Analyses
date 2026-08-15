package com.example.analizciler

import com.example.model.IzinBulgusu

object IzinAnalizcisi {

    private val BILINEN_IZINLER = mapOf(
        "android.permission.SEND_SMS" to Triple("SMS Gönderme", 5, "Arka planda gizli SMS gönderimi ve ücretli servis suiistimali riski."),
        "android.permission.RECEIVE_SMS" to Triple("SMS Alma", 5, "Gelen SMS doğrulama kodlarını (OTP) ve 2FA bildirimlerini ele geçirme riski."),
        "android.permission.READ_SMS" to Triple("SMS Okuma", 5, "Kullanıcıya ait özel SMS mesajlarının okunması ve veri sızıntısı."),
        "android.permission.READ_CONTACTS" to Triple("Rehber Okuma", 4, "Rehber verisinin toplanarak C2 sunucusuna sızdırılması riski."),
        "android.permission.WRITE_CONTACTS" to Triple("Rehber Değiştirme", 3, "Rehber verilerinin manipülasyonu."),
        "android.permission.ACCESS_FINE_LOCATION" to Triple("Hassas Konum", 4, "GPS tabanlı tam konum takibi."),
        "android.permission.ACCESS_COARSE_LOCATION" to Triple("Yaklaşık Konum", 3, "Ağ tabanlı konum takibi."),
        "android.permission.ACCESS_BACKGROUND_LOCATION" to Triple("Arka Plan Konum", 6, "Uygulama kapalıyken dahi sürekli coğrafi konum izleme."),
        "android.permission.RECORD_AUDIO" to Triple("Ses Kaydı", 5, "Ortam dinlemesi ve casus yazılım ses kaydı."),
        "android.permission.CAMERA" to Triple("Kamera Erişimi", 4, "Gizli fotoğraf/video çekimi riski."),
        "android.permission.READ_PHONE_STATE" to Triple("Telefon Durumu Okuma", 4, "IMEI, IMSI ve cihaz kimlik bilgilerinin ele geçirilmesi."),
        "android.permission.READ_CALL_LOG" to Triple("Arama Kaydı Okuma", 5, "Arama geçmişi ve iletişim trafiğinin gözetlenmesi."),
        "android.permission.PROCESS_OUTGOING_CALLS" to Triple("Giden Aramaları İzleme", 5, "Giden aramaları yönlendirme veya engelleme riski."),
        "android.permission.SYSTEM_ALERT_WINDOW" to Triple("Pencere Üzerine Çizim", 6, "Overlay/katman saldırıları (bankacılık sahte giriş ekranları)."),
        "android.permission.REQUEST_INSTALL_PACKAGES" to Triple("Paket Kurulum Talebi", 7, "Dışarıdan zararlı APK indirme ve gizlice kurma (Dropper)."),
        "android.permission.BIND_ACCESSIBILITY_SERVICE" to Triple("Erişilebilirlik Servisi", 8, "Ekran okuma, tuş kaydetme (keylogging) ve otomatik tıklama."),
        "android.permission.BIND_DEVICE_ADMIN" to Triple("Cihaz Yöneticisi", 8, "Cihazı kilitleme, şifre sıfırlama veya silme yetkisi."),
        "android.permission.WRITE_SETTINGS" to Triple("Sistem Ayarları Değiştirme", 5, "Sistem güvenlik ayarlarının manipülasyonu."),
        "android.permission.RECEIVE_BOOT_COMPLETED" to Triple("Açılışta Otomatik Başlama", 3, "Cihaz yeniden başladığında otomatik kalıcılık (Persistence)."),
        "android.permission.QUERY_ALL_PACKAGES" to Triple("Tüm Paketleri Sorgulama", 4, "Cihazda yüklü bankacılık/kripto uygulamalarını listeleme."),
        "android.permission.PACKAGE_USAGE_STATS" to Triple("Kullanım İstatistikleri", 5, "Hangi uygulamanın ne zaman açıldığını gözetleme."),
        "android.permission.READ_EXTERNAL_STORAGE" to Triple("Harici Depolama Okuma", 3, "Cihazdaki fotoğraflar ve belgelerin okunması."),
        "android.permission.WRITE_EXTERNAL_STORAGE" to Triple("Harici Depolama Yazma", 4, "Dosya şifreleme (Ransomware) veya tahrifat riski."),
        "android.permission.INTERNET" to Triple("İnternet Erişimi", 1, "Standart ağ iletişimi."),
        "android.permission.FOREGROUND_SERVICE" to Triple("Ön Plan Servisi", 2, "Kalıcı arka plan operasyonları yürütme.")
    )

    fun izinleri_incele(izinListesi: List<String>): List<IzinBulgusu> {
        return izinListesi.map { izin ->
            val bilinen = BILINEN_IZINLER[izin]
            val kisaAd = bilinen?.first ?: izin.substringAfterLast(".")
            val riskPuani = bilinen?.second ?: if (izin.startsWith("android.permission.")) 1 else 3
            val aciklama = bilinen?.third ?: "Özel veya üreticiye özgü izin tanımı."
            val tehlikeliMi = riskPuani >= 4

            val mitre = when {
                izin.contains("SMS") -> "Credential Access / Collection (T1412)"
                izin.contains("RECORD_AUDIO") || izin.contains("CAMERA") -> "Collection (T1429)"
                izin.contains("LOCATION") -> "Collection / Discovery (T1430)"
                izin.contains("BOOT_COMPLETED") -> "Persistence (T1624)"
                izin.contains("ACCESSIBILITY") -> "Privilege Escalation / Keylogging (T1417)"
                izin.contains("SYSTEM_ALERT_WINDOW") -> "Defense Evasion / Phishing (T1411)"
                izin.contains("INSTALL_PACKAGES") -> "Initial Access / Dropper (T1476)"
                izin.contains("QUERY_ALL_PACKAGES") -> "Discovery (T1418)"
                else -> null
            }

            IzinBulgusu(
                izinAdi = izin,
                kisaAd = kisaAd,
                tehlikeliMi = tehlikeliMi,
                ozelMi = !izin.startsWith("android.permission."),
                riskPuani = riskPuani,
                kategori = when {
                    riskPuani >= 6 -> "KRİTİK"
                    riskPuani >= 4 -> "YÜKSEK"
                    riskPuani >= 2 -> "ORTA"
                    else -> "DÜŞÜK"
                },
                aciklama = aciklama,
                mitreEtiketi = mitre
            )
        }.sortedByDescending { it.riskPuani }
    }

    fun risk_kombinasyonlarini_kontrol_et(bulgular: List<IzinBulgusu>): List<String> {
        val izinAdlari = bulgular.map { it.izinAdi }.toSet()
        val uyariListesi = mutableListOf<String>()

        if (izinAdlari.contains("android.permission.SYSTEM_ALERT_WINDOW") &&
            (izinAdlari.contains("android.permission.BIND_ACCESSIBILITY_SERVICE") ||
             izinAdlari.contains("android.permission.QUERY_ALL_PACKAGES"))
        ) {
            uyariListesi.add("Bankacılık Truva Atı Deseni: Overlay pencere izni ve Erişilebilirlik/Paket sorgulama kombinasyonu tespit edildi.")
        }

        if (izinAdlari.contains("android.permission.RECORD_AUDIO") &&
            izinAdlari.contains("android.permission.ACCESS_FINE_LOCATION") &&
            izinAdlari.contains("android.permission.READ_CONTACTS")
        ) {
            uyariListesi.add("Casus Yazılım (Spyware) Deseni: Mikrofon kaydı, hassas GPS konumu ve rehber erişimi bir arada istenmiş.")
        }

        if (izinAdlari.contains("android.permission.RECEIVE_BOOT_COMPLETED") &&
            izinAdlari.contains("android.permission.SEND_SMS") &&
            izinAdlari.contains("android.permission.RECEIVE_SMS")
        ) {
            uyariListesi.add("SMS Casusu / Botnet Deseni: Açılışta başlama ve tam SMS denetimi yetkisi.")
        }

        if (izinAdlari.contains("android.permission.REQUEST_INSTALL_PACKAGES") &&
            izinAdlari.contains("android.permission.WRITE_EXTERNAL_STORAGE")
        ) {
            uyariListesi.add("Yükleyici (Dropper) Deseni: Depolamaya yazma ve dışarıdan paket kurma yetkisi.")
        }

        return uyariListesi
    }
}
