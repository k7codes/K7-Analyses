package com.example.analizciler

import com.example.model.SertifikaBulgusu
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Locale

object SertifikaAnalizcisi {

    fun sertifikayi_cozumle(cert: X509Certificate?): SertifikaBulgusu {
        if (cert == null) {
            return SertifikaBulgusu(
                imzaliMi = false,
                yayimci = "Bilinmiyor / İmzasız",
                sahip = "Bilinmiyor",
                gecerlilikBaslangic = "-",
                gecerlilikBitis = "-",
                seriNumarasi = "-",
                sha256ParmakIzi = "-",
                sha1ParmakIzi = "-",
                md5ParmakIzi = "-",
                debugSertifikasiMi = false,
                algoritma = "Bilinmiyor"
            )
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val baslangic = try { dateFormat.format(cert.notBefore) } catch (_: Exception) { "-" }
        val bitis = try { dateFormat.format(cert.notAfter) } catch (_: Exception) { "-" }
        val yayimci = cert.issuerDN?.name ?: "Bilinmiyor"
        val sahip = cert.subjectDN?.name ?: "Bilinmiyor"
        val seriNo = cert.serialNumber?.toString(16) ?: "-"
        val algoritma = cert.sigAlgName ?: "Bilinmiyor"

        val sha256 = parmak_izi_hesapla(cert.encoded, "SHA-256")
        val sha1 = parmak_izi_hesapla(cert.encoded, "SHA-1")
        val md5 = parmak_izi_hesapla(cert.encoded, "MD5")

        val debugMi = yayimci.contains("Android Debug", ignoreCase = true) ||
                sahip.contains("Android Debug", ignoreCase = true) ||
                yayimci.contains("Android", ignoreCase = true) && yayimci.contains("Debug", ignoreCase = true)

        return SertifikaBulgusu(
            imzaliMi = true,
            yayimci = yayimci,
            sahip = sahip,
            gecerlilikBaslangic = baslangic,
            gecerlilikBitis = bitis,
            seriNumarasi = seriNo,
            sha256ParmakIzi = sha256,
            sha1ParmakIzi = sha1,
            md5ParmakIzi = md5,
            debugSertifikasiMi = debugMi,
            algoritma = algoritma
        )
    }

    private fun parmak_izi_hesapla(bytes: ByteArray, algo: String): String {
        return try {
            val md = MessageDigest.getInstance(algo)
            val digest = md.digest(bytes)
            digest.joinToString(":") { "%02X".format(it) }
        } catch (_: Exception) {
            "-"
        }
    }
}
