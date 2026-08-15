package com.example.veritabani

import kotlinx.coroutines.flow.Flow

class AnalizDeposu(private val dao: AnalizKayitDao) {
    val tumKayitlar: Flow<List<AnalizKayitEntity>> = dao.tumKayitlariGetir()

    suspend fun kaydet(kayit: AnalizKayitEntity): Long {
        return dao.kayitEkle(kayit)
    }

    suspend fun idIleGetir(id: Long): AnalizKayitEntity? {
        return dao.idIleKayitGetir(id)
    }

    suspend fun sil(id: Long) {
        dao.idIleKayitSil(id)
    }

    suspend fun tumunuTemizle() {
        dao.tumKayitlariTemizle()
    }
}
