package com.example.veritabani

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analiz_kayitlari")
data class AnalizKayitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val paketAdi: String,
    val uygulamaAdi: String,
    val surumAdi: String,
    val surumKodu: Long,
    val apkBoyutuBayt: Long,
    val sha256Hash: String,
    val riskPuani: Int,
    val riskSeviyesi: String,
    val tehlikeliIzinSayisi: Int,
    val toplamIzinSayisi: Int,
    val disariAcikBilesenSayisi: Int,
    val agGostergeSayisi: Int,
    val analizZamani: Long,
    val raporOzeti: String,
    val tamJsonRaporu: String,
    val arastirmaciImzasi: String = "k7~"
)
