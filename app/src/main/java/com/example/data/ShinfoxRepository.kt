package com.example.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class ShinfoxRepository(private val db: AppDatabase) {
    private val saldoDao = db.saldoDao()
    private val transaksiDao = db.transaksiDao()
    private val hutangDao = db.hutangDao()
    private val pengaturanDao = db.pengaturanDao()
    private val riwayatSaldoDao = db.riwayatSaldoDao()

    // Flow getters
    fun getSaldoFlow(): Flow<Saldo?> = saldoDao.getSaldoFlow()
    fun getPengaturanFlow(): Flow<Pengaturan?> = pengaturanDao.getPengaturanFlow()
    fun getAllTransaksi(): Flow<List<Transaksi>> = transaksiDao.getAllTransaksi()
    fun getTransaksiFromTime(start: Long): Flow<List<Transaksi>> = transaksiDao.getTransaksiFromTime(start)
    fun getAllHutang(): Flow<List<Hutang>> = hutangDao.getAllHutang()
    fun getAllRiwayatSaldo(): Flow<List<RiwayatSaldo>> = riwayatSaldoDao.getAllRiwayatSaldo()

    // Direct fetch
    suspend fun getPengaturanDirect(): Pengaturan? = pengaturanDao.getPengaturanDirect()

    // Setup Flow
    suspend fun saveInitialSetup(namaKonter: String, saldoAwal: Double, batasSaldoRendah: Double) {
        db.withTransaction {
            val config = Pengaturan(
                id = 1,
                namaKonter = namaKonter,
                batasSaldoRendah = batasSaldoRendah,
                setupComplete = true
            )
            pengaturanDao.upsertPengaturan(config)

            val initialSaldo = Saldo(id = 1, jumlah = saldoAwal)
            saldoDao.upsertSaldo(initialSaldo)

            val log = RiwayatSaldo(
                tipe = "Tambah Saldo",
                jumlah = saldoAwal,
                saldoLama = 0.0,
                saldoBaru = saldoAwal,
                catatan = "Setup Awal Konter"
            )
            riwayatSaldoDao.insertRiwayatSaldo(log)
        }
    }

    // Save Settings
    suspend fun updatePengaturan(pengaturan: Pengaturan) {
        pengaturanDao.upsertPengaturan(pengaturan)
    }

    // Add Balance (Tambah Saldo)
    suspend fun tambahSaldo(jumlah: Double, catatan: String) {
        db.withTransaction {
            val current = saldoDao.getSaldoDirect()
            val oldVal = current?.jumlah ?: 0.0
            val newVal = oldVal + jumlah

            saldoDao.upsertSaldo(Saldo(id = 1, jumlah = newVal))
            riwayatSaldoDao.insertRiwayatSaldo(
                RiwayatSaldo(
                    tipe = "Tambah Saldo",
                    jumlah = jumlah,
                    saldoLama = oldVal,
                    saldoBaru = newVal,
                    catatan = catatan
                )
            )
        }
    }

    // Create Transaction (Transaksi Baru) with Auto-Deduct logic and validation!
    // Out: Boolean (success). Under Room's default execution inside withTransaction.
    suspend fun insertTransaksi(transaksi: Transaksi): Boolean {
        return db.withTransaction {
            val current = saldoDao.getSaldoDirect()
            val currentSaldo = current?.jumlah ?: 0.0

            // The business logic states the saldo must decrease by the modal (capital spent from local/server balance)
            // If we don't have enough saldo, prevent creation.
            if (currentSaldo < transaksi.modal) {
                false
            } else {
                val newSaldoValue = currentSaldo - transaksi.modal
                saldoDao.upsertSaldo(Saldo(id = 1, jumlah = newSaldoValue))

                transaksiDao.insertTransaksi(transaksi)

                riwayatSaldoDao.insertRiwayatSaldo(
                    RiwayatSaldo(
                        tipe = "Transaksi",
                        jumlah = -transaksi.modal,
                        saldoLama = currentSaldo,
                        saldoBaru = newSaldoValue,
                        catatan = "${transaksi.kategori} - ${transaksi.catatan.ifEmpty { "Tanpa catatan" }}"
                    )
                )
                true
            }
        }
    }

    // Edit and Delete Transactions
    suspend fun deleteTransaksi(transaksi: Transaksi) {
        db.withTransaction {
            // Revert the saldo if we delete the transaction
            val current = saldoDao.getSaldoDirect()
            val currentSaldo = current?.jumlah ?: 0.0
            val newSaldoValue = currentSaldo + transaksi.modal

            saldoDao.upsertSaldo(Saldo(id = 1, jumlah = newSaldoValue))
            transaksiDao.deleteTransaksi(transaksi)

            riwayatSaldoDao.insertRiwayatSaldo(
                RiwayatSaldo(
                    tipe = "Koreksi",
                    jumlah = transaksi.modal,
                    saldoLama = currentSaldo,
                    saldoBaru = newSaldoValue,
                    catatan = "Hapus Transaksi ID #${transaksi.id}"
                )
            )
        }
    }

    // Debt Operations
    suspend fun insertHutang(hutang: Hutang) {
        hutangDao.insertHutang(hutang)
    }

    suspend fun updateHutang(hutang: Hutang) {
        hutangDao.updateHutang(hutang)
    }

    suspend fun deleteHutang(hutang: Hutang) {
        hutangDao.deleteHutang(hutang)
    }
}
