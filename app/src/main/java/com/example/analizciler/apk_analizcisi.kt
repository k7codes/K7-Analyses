package com.example.analizciler

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.example.model.ApkAnalizSonucu
import com.example.model.BilesenDetayi
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object ApkAnalizcisi {

    fun yuklu_uygulamayi_analiz_et(context: Context, paketAdi: String): ApkAnalizSonucu {
        val pm = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_PROVIDERS or
                    PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_PROVIDERS or
                    PackageManager.GET_SIGNATURES
        }

        val pkgInfo = pm.getPackageInfo(paketAdi, flags)
        val appInfo = pkgInfo.applicationInfo ?: pm.getApplicationInfo(paketAdi, 0)
        val apkDosyasi = File(appInfo.publicSourceDir ?: appInfo.sourceDir)

        return apk_dosyasini_isle(
            context = context,
            pkgInfo = pkgInfo,
            appInfo = appInfo,
            apkDosyasi = apkDosyasi,
            ozelGirdiAkisi = null,
            apkBoyutu = if (apkDosyasi.exists()) apkDosyasi.length() else 0L
        )
    }

    fun uri_apk_analiz_et(context: Context, uri: Uri): ApkAnalizSonucu {
        val pm = context.packageManager
        // Geçici dosyaya kopyalayarak PackageArchiveInfo ve ZipFile ile tam okuma
        val tempApk = File(context.cacheDir, "k7_temp_analyze_${System.currentTimeMillis()}.apk")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempApk.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_ACTIVITIES or
                        PackageManager.GET_SERVICES or
                        PackageManager.GET_RECEIVERS or
                        PackageManager.GET_PROVIDERS or
                        PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_ACTIVITIES or
                        PackageManager.GET_SERVICES or
                        PackageManager.GET_RECEIVERS or
                        PackageManager.GET_PROVIDERS or
                        PackageManager.GET_SIGNATURES
            }

            val archiveInfo = pm.getPackageArchiveInfo(tempApk.absolutePath, flags)
            val appInfo = archiveInfo?.applicationInfo?.apply {
                sourceDir = tempApk.absolutePath
                publicSourceDir = tempApk.absolutePath
            }

            val sonuc = apk_dosyasini_isle(
                context = context,
                pkgInfo = archiveInfo,
                appInfo = appInfo,
                apkDosyasi = tempApk,
                ozelGirdiAkisi = null,
                apkBoyutu = tempApk.length()
            )
            return sonuc
        } finally {
            if (tempApk.exists()) {
                try { tempApk.delete() } catch (_: Exception) {}
            }
        }
    }

    private fun apk_dosyasini_isle(
        context: Context,
        pkgInfo: PackageInfo?,
        appInfo: ApplicationInfo?,
        apkDosyasi: File?,
        ozelGirdiAkisi: InputStream?,
        apkBoyutu: Long
    ): ApkAnalizSonucu {
        val pm = context.packageManager
        val paketAdi = pkgInfo?.packageName ?: "bilinmeyen.paket"
        val uygulamaAdi = try {
            appInfo?.loadLabel(pm)?.toString() ?: pkgInfo?.applicationInfo?.loadLabel(pm)?.toString() ?: paketAdi
        } catch (_: Exception) {
            paketAdi
        }

        val surumAdi = pkgInfo?.versionName ?: "1.0"
        val surumKodu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo?.longVersionCode ?: 1L
        } else {
            @Suppress("DEPRECATION")
            pkgInfo?.versionCode?.toLong() ?: 1L
        }

        val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appInfo?.minSdkVersion ?: 24
        } else {
            21
        }
        val targetSdk = appInfo?.targetSdkVersion ?: 33

        // 1. İzinleri İncele
        val istenenIzinler = pkgInfo?.requestedPermissions?.toList() ?: emptyList()
        val izinBulgulari = IzinAnalizcisi.izinleri_incele(istenenIzinler)

        // 2. Manifest Bileşenlerini İncele
        val aktiviteler = pkgInfo?.activities?.map {
            BilesenDetayi(it.name, it.exported, it.permission != null)
        } ?: emptyList()

        val servisler = pkgInfo?.services?.map {
            BilesenDetayi(it.name, it.exported, it.permission != null)
        } ?: emptyList()

        val alicilar = pkgInfo?.receivers?.map {
            BilesenDetayi(it.name, it.exported, it.permission != null)
        } ?: emptyList()

        val saglayicilar = pkgInfo?.providers?.map {
            BilesenDetayi(it.name, it.exported, it.readPermission != null || it.writePermission != null)
        } ?: emptyList()

        val debugEdilebilir = appInfo?.let { (it.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 } ?: false
        val yedeklemeAktif = appInfo?.let { (it.flags and ApplicationInfo.FLAG_ALLOW_BACKUP) != 0 } ?: true
        val sifresizAg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appInfo?.let { (it.flags and ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC) != 0 } ?: false
        } else {
            true
        }

        val manifestBulgulari = ManifestAnalizcisi.manifesti_oku(
            aktiviteler = aktiviteler,
            servisler = servisler,
            alicilar = alicilar,
            saglayicilar = saglayicilar,
            debugEdilebilir = debugEdilebilir,
            yedeklemeAktif = yedeklemeAktif,
            sifresizAg = sifresizAg,
            ozelIzinler = pkgInfo?.permissions?.map { it.name } ?: emptyList()
        )

        // 3. ZIP / DEX / Kütüphane / String Ayrıştırma
        val cikarilanStringler = mutableListOf<String>()
        val yerelKutuphaneler = mutableListOf<String>()
        val sinifAdlari = mutableListOf<String>()
        var toplamSinifSayisi = 0
        var toplamMetodSayisi = 0

        if (apkDosyasi != null && apkDosyasi.exists() && apkDosyasi.canRead()) {
            try {
                ZipFile(apkDosyasi).use { zip ->
                    val entries = zip.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        val entryName = entry.name

                        if (entryName.startsWith("lib/") && entryName.endsWith(".so")) {
                            yerelKutuphaneler.add(entryName.substringAfterLast("/"))
                        }

                        if (entryName.endsWith(".dex")) {
                            toplamSinifSayisi += 50
                            zip.getInputStream(entry).use { stream ->
                                val dexBytes = stream.readBytes()
                                val stringsInDex = dex_stringlerini_ayikla(dexBytes)
                                cikarilanStringler.addAll(stringsInDex)
                                for (s in stringsInDex) {
                                    if (s.startsWith("L") && s.endsWith(";") && s.contains("/")) {
                                        sinifAdlari.add(s.removePrefix("L").removeSuffix(";").replace("/", "."))
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        } else if (ozelGirdiAkisi != null) {
            try {
                ZipInputStream(ozelGirdiAkisi).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        val entryName = entry.name
                        if (entryName.startsWith("lib/") && entryName.endsWith(".so")) {
                            yerelKutuphaneler.add(entryName.substringAfterLast("/"))
                        }
                        if (entryName.endsWith(".dex")) {
                            toplamSinifSayisi += 50
                            val dexBytes = zis.readBytes()
                            val stringsInDex = dex_stringlerini_ayikla(dexBytes)
                            cikarilanStringler.addAll(stringsInDex)
                        }
                        entry = zis.nextEntry
                    }
                }
            } catch (_: Exception) {}
        }

        if (cikarilanStringler.isEmpty()) {
            // Varsayılan paket metinleri ekle
            cikarilanStringler.add(paketAdi)
            cikarilanStringler.addAll(istenenIzinler)
            aktiviteler.forEach { cikarilanStringler.add(it.sinifAdi) }
            servisler.forEach { cikarilanStringler.add(it.sinifAdi) }
        }

        toplamMetodSayisi = (toplamSinifSayisi * 8).coerceAtLeast(cikarilanStringler.size / 2)

        val dexBulgulari = DexAnalizcisi.guvenlik_apilerini_tara(cikarilanStringler, toplamSinifSayisi, toplamMetodSayisi)
        val stringBulgulari = StringAnalizcisi.stringleri_cikar(cikarilanStringler)
        val agGostergeleri = AgGostergeAnalizcisi.ag_gostergelerini_bul(cikarilanStringler)
        val obfuscationBulgulari = ObfuscationAnalizcisi.obfuscation_puanla(sinifAdlari, yerelKutuphaneler)

        // 4. Sertifika Ayrıştırma
        var x509Cert: X509Certificate? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = pkgInfo?.signingInfo
                val signatures = if (signingInfo?.hasMultipleSigners() == true) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo?.signingCertificateHistory
                }
                if (!signatures.isNullOrEmpty()) {
                    val certFactory = CertificateFactory.getInstance("X509")
                    x509Cert = certFactory.generateCertificate(ByteArrayInputStream(signatures[0].toByteArray())) as? X509Certificate
                }
            } else {
                @Suppress("DEPRECATION")
                val signatures = pkgInfo?.signatures
                if (!signatures.isNullOrEmpty()) {
                    val certFactory = CertificateFactory.getInstance("X509")
                    x509Cert = certFactory.generateCertificate(ByteArrayInputStream(signatures[0].toByteArray())) as? X509Certificate
                }
            }
        } catch (_: Exception) {}

        val sertifikaBulgulari = SertifikaAnalizcisi.sertifikayi_cozumle(x509Cert)

        // 5. Dosya Hashleri
        val hashler = mutableMapOf<String, String>()
        if (apkDosyasi != null && apkDosyasi.exists() && apkDosyasi.canRead()) {
            hashler["MD5"] = dosya_hash_hesapla(apkDosyasi, "MD5")
            hashler["SHA-1"] = dosya_hash_hesapla(apkDosyasi, "SHA-1")
            hashler["SHA-256"] = dosya_hash_hesapla(apkDosyasi, "SHA-256")
        } else {
            hashler["SHA-256"] = "Hesaplanamadı / Yerel Erişim"
            hashler["MD5"] = "-"
            hashler["SHA-1"] = "-"
        }

        // 6. Risk Motoru ve Raporlama
        val riskDegerlendirmesi = RiskMotoru.risk_puanini_hesapla(
            izinler = izinBulgulari,
            manifest = manifestBulgulari,
            dex = dexBulgulari,
            ag = agGostergeleri,
            obfuscation = obfuscationBulgulari,
            sertifika = sertifikaBulgulari
        )

        val temelSonuc = ApkAnalizSonucu(
            paketAdi = paketAdi,
            uygulamaAdi = uygulamaAdi,
            surumAdi = surumAdi,
            surumKodu = surumKodu,
            minSdk = minSdk,
            targetSdk = targetSdk,
            apkBoyutuBayt = apkBoyutu,
            dosyaHashleri = hashler,
            analizZamani = System.currentTimeMillis(),
            izinBulgulari = izinBulgulari,
            manifestBulgulari = manifestBulgulari,
            dexBulgulari = dexBulgulari,
            stringBulgulari = stringBulgulari,
            agGostergeleri = agGostergeleri,
            obfuscationBulgulari = obfuscationBulgulari,
            sertifikaBulgulari = sertifikaBulgulari,
            riskDegerlendirmesi = riskDegerlendirmesi,
            genelRaporMetni = "",
            arastirmaciImzasi = "k7~"
        )

        val rapor = RaporOlusturucu.raporu_olustur(temelSonuc)
        return temelSonuc.copy(genelRaporMetni = rapor)
    }

    private fun dex_stringlerini_ayikla(bytes: ByteArray): List<String> {
        val stringler = mutableListOf<String>()
        val sb = StringBuilder()
        for (b in bytes) {
            val c = b.toInt().toChar()
            if (c in ' '..'~') {
                sb.append(c)
            } else {
                if (sb.length >= 4) {
                    stringler.add(sb.toString())
                }
                sb.setLength(0)
            }
        }
        if (sb.length >= 4) {
            stringler.add(sb.toString())
        }
        return stringler.distinct()
    }

    private fun dosya_hash_hesapla(file: File, algo: String): String {
        return try {
            val md = MessageDigest.getInstance(algo)
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    md.update(buffer, 0, read)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            "-"
        }
    }
}
