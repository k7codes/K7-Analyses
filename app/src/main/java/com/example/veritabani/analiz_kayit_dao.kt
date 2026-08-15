package com.example.veritabani

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalizKayitDao {
    @Query("SELECT * FROM analiz_kayitlari ORDER BY analizZamani DESC")
    fun tumKayitlariGetir(): Flow<List<AnalizKayitEntity>>

    @Query("SELECT * FROM analiz_kayitlari WHERE id = :id LIMIT 1")
    suspend fun idIleKayitGetir(id: Long): AnalizKayitEntity?

    @Query("SELECT * FROM analiz_kayitlari WHERE paketAdi = :paketAdi ORDER BY analizZamani DESC LIMIT 1")
    suspend fun paketAdiIleKayitGetir(paketAdi: String): AnalizKayitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun kayitEkle(kayit: AnalizKayitEntity): Long

    @Query("DELETE FROM analiz_kayitlari WHERE id = :id")
    suspend fun idIleKayitSil(id: Long)

    @Query("DELETE FROM analiz_kayitlari")
    suspend fun tumKayitlariTemizle()
}
