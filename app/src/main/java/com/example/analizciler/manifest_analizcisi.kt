package com.example.analizciler

import com.example.model.BilesenDetayi
import com.example.model.ManifestBulgusu

object ManifestAnalizcisi {

    fun manifesti_oku(
        aktiviteler: List<BilesenDetayi>,
        servisler: List<BilesenDetayi>,
        alicilar: List<BilesenDetayi>,
        saglayicilar: List<BilesenDetayi>,
        debugEdilebilir: Boolean,
        yedeklemeAktif: Boolean,
        sifresizAg: Boolean,
        ozelIzinler: List<String>
    ): ManifestBulgusu {
        val disariAcikAktivite = aktiviteler.count { it.disariyaAcik }
        val disariAcikServis = servisler.count { it.disariyaAcik }
        val disariAcikAlici = alicilar.count { it.disariyaAcik }
        val disariAcikSaglayici = saglayicilar.count { it.disariyaAcik }

        val tumAcikBilesenler = (aktiviteler + servisler + alicilar + saglayicilar).filter { it.disariyaAcik }

        return ManifestBulgusu(
            toplamAktivite = aktiviteler.size,
            toplamServis = servisler.size,
            toplamAlici = alicilar.size,
            toplamSaglayici = saglayicilar.size,
            disariAcikAktiviteSayisi = disariAcikAktivite,
            disariAcikServisSayisi = disariAcikServis,
            disariAcikAliciSayisi = disariAcikAlici,
            disariAcikSaglayiciSayisi = disariAcikSaglayici,
            debugEdilebilirMi = debugEdilebilir,
            yedeklemeAktifMi = yedeklemeAktif,
            sifresizAgTrafigiAktifMi = sifresizAg,
            acikBilesenler = tumAcikBilesenler,
            ozelIzinler = ozelIzinler
        )
    }

    fun manifest_risk_puani_hesapla(bulgu: ManifestBulgusu): Pair<Int, List<String>> {
        var puan = 0
        val uyarilar = mutableListOf<String>()

        if (bulgu.debugEdilebilirMi) {
            puan += 10
            uyarilar.add("android:debuggable=\"true\" aktif: Uygulama bellek denetimi ve debugger saldırılarına açıktır.")
        }
        if (bulgu.sifresizAgTrafigiAktifMi) {
            puan += 6
            uyarilar.add("android:usesCleartextTraffic=\"true\" aktif: Şifresiz HTTP trafiği Man-in-the-Middle (MitM) saldırılarına zemin hazırlar.")
        }
        if (bulgu.yedeklemeAktifMi) {
            puan += 2
            uyarilar.add("android:allowBackup=\"true\" aktif: ADB üzerinden yerel uygulama verisi çekilebilir.")
        }
        if (bulgu.disariAcikSaglayiciSayisi > 0) {
            puan += bulgu.disariAcikSaglayiciSayisi * 3
            uyarilar.add("${bulgu.disariAcikSaglayiciSayisi} adet ContentProvider izinsiz dışarıya açık: Veri tabanı sızıntısı riski.")
        }
        if (bulgu.disariAcikServisSayisi > 0) {
            puan += bulgu.disariAcikServisSayisi * 2
            uyarilar.add("${bulgu.disariAcikServisSayisi} adet Servis dış erişime açık.")
        }
        if (bulgu.disariAcikAliciSayisi > 0) {
            puan += bulgu.disariAcikAliciSayisi * 1
        }

        return Pair(puan.coerceAtMost(25), uyarilar)
    }
}
