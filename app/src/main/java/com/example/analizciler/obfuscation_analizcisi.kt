package com.example.analizciler

import com.example.model.ObfuscationBulgusu

object ObfuscationAnalizcisi {

    private val BILINEN_PACKERLAR = mapOf(
        "libsecexe.so" to "Bangcle (SecNeo)",
        "libSecShell.so" to "SecShell",
        "libprotectClass.so" to "Qihoo 360",
        "libjiagu.so" to "360 Jiagu",
        "libstub.so" to "梆梆安全 (Bangcle)",
        "libnesec.so" to "NetEase (网易易盾)",
        "libDexHelper.so" to "Secenh (Baidu Protect)",
        "libshell.so" to "Tencent Legu (乐固)",
        "dexguard" to "DexGuard",
        "allatori" to "Allatori Obfuscator"
    )

    fun obfuscation_puanla(
        sinifIsimleri: List<String>,
        yerelKutuphaneler: List<String>
    ): ObfuscationBulgusu {
        var tespitEdilenPacker: String? = null

        // Packer kontrolü
        for (so in yerelKutuphaneler) {
            val packer = BILINEN_PACKERLAR[so]
            if (packer != null) {
                tespitEdilenPacker = packer
                break
            }
        }

        if (tespitEdilenPacker == null) {
            for (sinif in sinifIsimleri) {
                for ((imza, packer) in BILINEN_PACKERLAR) {
                    if (sinif.contains(imza, ignoreCase = true)) {
                        tespitEdilenPacker = packer
                        break
                    }
                }
                if (tespitEdilenPacker != null) break
            }
        }

        // Karartma istatistiği: Tek harfli veya anlamsız sınıf adları (a, b, c, aa, o.o, vb.)
        val karartilmisSiniflar = sinifIsimleri.filter { sinif ->
            val kisaAd = sinif.substringAfterLast(".").substringAfterLast("/")
            kisaAd.length in 1..2 || kisaAd.matches(Regex("^[a-z0-9]$")) || kisaAd.matches(Regex("^[a-z]{1,2}[0-9]?$"))
        }

        val oran = if (sinifIsimleri.isNotEmpty()) {
            (karartilmisSiniflar.size.toFloat() / sinifIsimleri.size.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        var puan = (oran * 70).toInt()
        if (tespitEdilenPacker != null) {
            puan = (puan + 30).coerceAtMost(100)
        }

        return ObfuscationBulgusu(
            obfuscationPuani = puan,
            paketKarartmaVarMi = oran > 0.35f,
            sinifKarakterOrani = oran,
            packerAdi = tespitEdilenPacker,
            karartilmisSinifOrnegi = karartilmisSiniflar.take(15)
        )
    }
}
