package com.example.analizciler

import com.example.model.AgGostergesi

object AgGostergeAnalizcisi {

    private val IP_REGEX = Regex("""\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b""")
    private val URL_REGEX = Regex("""https?://[^\s<>"'{}|\\^`]+""")
    private val SUPHELI_TLD_LISTESI = listOf(".onion", ".xyz", ".top", ".tk", ".ml", ".ga", ".cf", ".gq", "duckdns.org", "ngrok.io", "pastebin.com")
    private val SUPHELI_PORTLAR = listOf(4444, 1337, 6667, 8080, 8888, 9001, 9999)

    fun ag_gostergelerini_bul(metinHavuzu: List<String>): AgGostergesi {
        val ipler = mutableListOf<String>()
        val alanAdlari = mutableListOf<String>()
        val sifresizHttp = mutableListOf<String>()
        val acikPortlar = mutableListOf<Int>()
        var supheliUrlSayisi = 0

        val birlestirilmis = metinHavuzu.joinToString("\n")

        // IP adresleri
        IP_REGEX.findAll(birlestirilmis).forEach { match ->
            val ip = match.value
            // Yerel ve broadcast IP'leri eleyebiliriz veya filtreleyebiliriz
            if (ip != "0.0.0.0" && ip != "127.0.0.1" && ip != "255.255.255.255") {
                ipler.add(ip)
            }
        }

        // URL'ler
        URL_REGEX.findAll(birlestirilmis).forEach { match ->
            val url = match.value
            if (url.startsWith("http://", ignoreCase = true)) {
                sifresizHttp.add(url)
            }
            try {
                val host = url.substringAfter("://").substringBefore("/").substringBefore(":")
                alanAdlari.add(host)

                if (SUPHELI_TLD_LISTESI.any { host.contains(it, ignoreCase = true) }) {
                    supheliUrlSayisi++
                }
            } catch (_: Exception) {}
        }

        // Portlar
        for (port in SUPHELI_PORTLAR) {
            if (birlestirilmis.contains(":$port") || birlestirilmis.contains("port $port") || birlestirilmis.contains("port=$port")) {
                acikPortlar.add(port)
            }
        }

        return AgGostergesi(
            ipAdresleri = ipler.distinct().take(30),
            alanAdlari = alanAdlari.distinct().take(30),
            supheliUrlSayisi = supheliUrlSayisi,
            acikPortlar = acikPortlar.distinct(),
            sifresizHttpBaglantilari = sifresizHttp.distinct().take(30)
        )
    }

    fun ag_risk_puani_hesapla(ag: AgGostergesi): Pair<Int, List<String>> {
        var puan = 0
        val uyarilar = mutableListOf<String>()

        if (ag.ipAdresleri.isNotEmpty()) {
            puan += (ag.ipAdresleri.size * 3).coerceAtMost(12)
            uyarilar.add("${ag.ipAdresleri.size} adet doğrudan IP adresi referansı tespit edildi (C2 şüphesi).")
        }

        if (ag.supheliUrlSayisi > 0) {
            puan += (ag.supheliUrlSayisi * 5).coerceAtMost(15)
            uyarilar.add("${ag.supheliUrlSayisi} adet şüpheli dinamik DNS veya C2 etki alanı göstergesi saptandı.")
        }

        if (ag.sifresizHttpBaglantilari.size > 2) {
            puan += 4
            uyarilar.add("${ag.sifresizHttpBaglantilari.size} adet şifresiz HTTP bağlantısı bulundu.")
        }

        if (ag.acikPortlar.any { it == 4444 || it == 1337 || it == 9001 }) {
            puan += 8
            uyarilar.add("Tipik saldırı/ters kabuk portları (4444, 1337 vb.) tespit edildi.")
        }

        return Pair(puan.coerceAtMost(25), uyarilar)
    }
}
