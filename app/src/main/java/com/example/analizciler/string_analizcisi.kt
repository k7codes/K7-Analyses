package com.example.analizciler

import com.example.model.StringBulgusu

object StringAnalizcisi {

    private val SISTEM_YOLLARI_DESENLERI = listOf(
        "/data/local/tmp",
        "/system/bin/su",
        "/system/xbin/su",
        "/proc/net/tcp",
        "/data/data/",
        "/system/etc/hosts",
        "/system/framework/",
        "/dev/socket/",
        "/system/app/"
    )

    private val KOMUT_DESENLERI = listOf(
        "chmod 777",
        "chmod 755",
        "pm install",
        "rm -rf",
        "mount -o remount",
        "iptables",
        "setprop",
        "getprop",
        "insmod",
        "chown root"
    )

    private val BAZ64_REGEX = Regex("^[A-Za-z0-9+/]{24,}={0,2}$")

    fun stringleri_cikar(cikarilanStringHavuzu: List<String>): StringBulgusu {
        val supheliStringler = mutableListOf<String>()
        val sistemYollari = mutableListOf<String>()
        val baz64Listesi = mutableListOf<String>()
        val komutListesi = mutableListOf<String>()

        for (metin in cikarilanStringHavuzu) {
            val temiz = metin.trim()
            if (temiz.length < 4) continue

            for (yol in SISTEM_YOLLARI_DESENLERI) {
                if (temiz.contains(yol, ignoreCase = true)) {
                    sistemYollari.add(temiz)
                    break
                }
            }

            for (komut in KOMUT_DESENLERI) {
                if (temiz.contains(komut, ignoreCase = true)) {
                    komutListesi.add(temiz)
                    break
                }
            }

            if (BAZ64_REGEX.matches(temiz) && temiz.length in 28..256) {
                baz64Listesi.add(temiz)
            }

            if (temiz.contains("AESKey", ignoreCase = true) ||
                temiz.contains("private_key", ignoreCase = true) ||
                temiz.contains("c2_server", ignoreCase = true) ||
                temiz.contains("bot_token", ignoreCase = true) ||
                temiz.contains("telegram.org/bot", ignoreCase = true)
            ) {
                supheliStringler.add(temiz)
            }
        }

        return StringBulgusu(
            supheliStringler = supheliStringler.distinct().take(30),
            sistemDosyaYollari = sistemYollari.distinct().take(30),
            baz64Metinler = baz64Listesi.distinct().take(30),
            komutSatirlari = komutListesi.distinct().take(30)
        )
    }
}
