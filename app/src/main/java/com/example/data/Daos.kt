package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SaldoDao {
    @Query("SELECT * FROM saldo WHERE id = 1 LIMIT 1")
    fun getSaldoFlow(): Flow<Saldo?>

    @Query("SELECT * FROM saldo WHERE id = 1 LIMIT 1")
    suspend fun getSaldoDirect(): Saldo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSaldo(saldo: Saldo)
}

@Dao
interface TransaksiDao {
    @Query("SELECT * FROM transaksi ORDER BY tanggal DESC")
    fun getAllTransaksi(): Flow<List<Transaksi>>

    @Query("SELECT * FROM transaksi WHERE tanggal >= :startOfDay ORDER BY tanggal DESC")
    fun getTransaksiFromTime(startOfDay: Long): Flow<List<Transaksi>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksi(transaksi: Transaksi)

    @Update
    suspend fun updateTransaksi(transaksi: Transaksi)

    @Delete
    suspend fun deleteTransaksi(transaksi: Transaksi)
}

@Dao
interface HutangDao {
    @Query("SELECT * FROM hutang ORDER BY status DESC, tanggal DESC")
    fun getAllHutang(): Flow<List<Hutang>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHutang(hutang: Hutang)

    @Update
    suspend fun updateHutang(hutang: Hutang)

    @Delete
    suspend fun deleteHutang(hutang: Hutang)
}

@Dao
interface PengaturanDao {
    @Query("SELECT * FROM pengaturan WHERE id = 1 LIMIT 1")
    fun getPengaturanFlow(): Flow<Pengaturan?>

    @Query("SELECT * FROM pengaturan WHERE id = 1 LIMIT 1")
    suspend fun getPengaturanDirect(): Pengaturan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPengaturan(pengaturan: Pengaturan)
}

@Dao
interface RiwayatSaldoDao {
    @Query("SELECT * FROM riwayat_saldo ORDER BY tanggal DESC")
    fun getAllRiwayatSaldo(): Flow<List<RiwayatSaldo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiwayatSaldo(riwayatSaldo: RiwayatSaldo)
}
