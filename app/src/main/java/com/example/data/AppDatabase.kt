package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Saldo::class,
        Transaksi::class,
        Hutang::class,
        Pengaturan::class,
        RiwayatSaldo::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun saldoDao(): SaldoDao
    abstract fun transaksiDao(): TransaksiDao
    abstract fun hutangDao(): HutangDao
    abstract fun pengaturanDao(): PengaturanDao
    abstract fun riwayatSaldoDao(): RiwayatSaldoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shinfox_store_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
