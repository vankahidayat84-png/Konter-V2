package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saldo")
data class Saldo(
    @PrimaryKey val id: Int = 1,
    val jumlah: Double
)

@Entity(tableName = "transaksi")
data class Transaksi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tanggal: Long = System.currentTimeMillis(),
    val kategori: String, // Pulsa, Top Up E-Wallet, Transfer Bank, Token PLN, Pembayaran BPJS
    val namaPelanggan: String?,
    val nomorHP: String?,
    val nominal: Double,
    val modal: Double,
    val hargaJual: Double,
    val catatan: String,
    val keuntungan: Double // Auto-calculated in model / viewmodel: hargaJual - modal
)

@Entity(tableName = "hutang")
data class Hutang(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaPelanggan: String,
    val nomorHP: String,
    val jumlahHutang: Double,
    val tanggal: Long = System.currentTimeMillis(),
    val catatan: String,
    val status: String // "Belum Lunas", "Lunas"
)

@Entity(tableName = "pengaturan")
data class Pengaturan(
    @PrimaryKey val id: Int = 1,
    val namaKonter: String = "Shinfox Store",
    val batasSaldoRendah: Double = 100000.0,
    val logoKonterUri: String? = null,
    val darkTheme: Boolean = true,
    val setupComplete: Boolean = false
)

@Entity(tableName = "riwayat_saldo")
data class RiwayatSaldo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tanggal: Long = System.currentTimeMillis(),
    val tipe: String, // "Tambah Saldo", "Transaksi", "Koreksi"
    val jumlah: Double,
    val saldoLama: Double,
    val saldoBaru: Double,
    val catatan: String
)
