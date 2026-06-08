package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import androidx.room.withTransaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ShinfoxViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ShinfoxRepository(db)

    // Raw flows from Database
    val saldo: StateFlow<Saldo?> = repository.getSaldoFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pengaturan: StateFlow<Pengaturan?> = repository.getPengaturanFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val semuaTransaksi: StateFlow<List<Transaksi>> = repository.getAllTransaksi()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val semuaHutang: StateFlow<List<Hutang>> = repository.getAllHutang()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val semuaRiwayatSaldo: StateFlow<List<RiwayatSaldo>> = repository.getAllRiwayatSaldo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Dashboard Calculations
    val dashboardStats = combine(semuaTransaksi, semuaHutang, saldo) { transactions, debts, currentSaldo ->
        val startOfToday = getStartOfToday()
        val todayTransactions = transactions.filter { it.tanggal >= startOfToday }

        val omzetHariIni = todayTransactions.sumOf { it.hargaJual }
        val keuntunganHariIni = todayTransactions.sumOf { it.keuntungan }
        val jumlahTransaksiHariIni = todayTransactions.size

        val hutangAktif = debts.filter { it.status == "Belum Lunas" }.sumOf { it.jumlahHutang }

        DashboardStats(
            saldoKonter = currentSaldo?.jumlah ?: 0.0,
            omzetHariIni = omzetHariIni,
            keuntunganHariIni = keuntunganHariIni,
            jumlahTransaksiHariIni = jumlahTransaksiHariIni,
            hutangAktif = hutangAktif
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Live Smart Insights (Wawasan Pintar)
    val wawasanPintar = combine(dashboardStats, semuaHutang, semuaTransaksi, pengaturan) { stats, debts, transactions, settings ->
        val insights = mutableListOf<InsightMessage>()
        val lowBatas = settings?.batasSaldoRendah ?: 100000.0

        // 1. Saldo Rendah
        if (stats.saldoKonter < lowBatas) {
            insights.add(
                InsightMessage(
                    title = "Peringatan Saldo Rendah!",
                    message = "Saldo konter tersisa Rp ${formatRupiah(stats.saldoKonter)} dan berada di bawah batas minimum Rp ${formatRupiah(lowBatas)}.",
                    isCritical = true
                )
            )
        } else {
            insights.add(
                InsightMessage(
                    title = "Saldo Aman",
                    message = "Saldo digital Anda sebesar Rp ${formatRupiah(stats.saldoKonter)} dalam kondisi aman di atas batas batas minimal.",
                    isCritical = false
                )
            )
        }

        // 2. Hutang Menumpuk
        val unpaidDebtsCount = debts.filter { it.status == "Belum Lunas" }.size
        val totalUnpaidAmount = debts.filter { it.status == "Belum Lunas" }.sumOf { it.jumlahHutang }
        if (totalUnpaidAmount > 200000.0) {
            insights.add(
                InsightMessage(
                    title = "Hutang Menumpuk!",
                    message = "Ada $unpaidDebtsCount pelanggan belum membayar hutang dengan total Rp ${formatRupiah(totalUnpaidAmount)}. Segera tagih untuk kelancaran modal.",
                    isCritical = true
                )
            )
        }

        // 3. Kategori Terlaris
        if (transactions.isNotEmpty()) {
            val bestKategori = transactions.groupBy { it.kategori }
                .maxByOrNull { it.value.size }?.key
            if (bestKategori != null) {
                insights.add(
                    InsightMessage(
                        title = "Kategori Terlaris",
                        message = "Kategori penjualan terpopuler konter Anda saat ini adalah: $bestKategori.",
                        isCritical = false
                    )
                )
            }
        }

        // 4. Analisis Tren (Harian dibanding Hari Sebelumnya)
        val today = getStartOfToday()
        val yesterday = today - 24 * 60 * 60 * 1000
        val todayCount = transactions.filter { it.tanggal >= today }.size
        val yesterdayCount = transactions.filter { it.tanggal in yesterday until today }.size

        if (todayCount > yesterdayCount && yesterdayCount > 0) {
            insights.add(
                InsightMessage(
                    title = "Transaksi Meningkat",
                    message = "Tren penjualan meningkat! Hari ini ada $todayCount transaksi dibandingkan kemarin sebanyak $yesterdayCount.",
                    isCritical = false
                )
            )
        } else if (todayCount < yesterdayCount && yesterdayCount > 5) {
            insights.add(
                InsightMessage(
                    title = "Transaksi Menurun",
                    message = "Penjualan hari ini agak sepi ($todayCount transaksi) dibanding kemarin ($yesterdayCount transaksi). Cobalah tawarkan promo menarik!",
                    isCritical = false
                )
            )
        }

        insights
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Setup Actions
    fun completeSetup(namaKonter: String, saldoAwal: Double, batasSaldoRendah: Double) {
        viewModelScope.launch {
            repository.saveInitialSetup(namaKonter, saldoAwal, batasSaldoRendah)
        }
    }

    // Settings Updates
    fun updateBatasSaldoRendah(batas: Double) {
        viewModelScope.launch {
            val current = repository.getPengaturanDirect() ?: Pengaturan()
            repository.updatePengaturan(current.copy(batasSaldoRendah = batas))
        }
    }

    fun updateNamaKonter(nama: String) {
        viewModelScope.launch {
            val current = repository.getPengaturanDirect() ?: Pengaturan()
            repository.updatePengaturan(current.copy(namaKonter = nama))
        }
    }

    fun updateDarkTheme(theme: Boolean) {
        viewModelScope.launch {
            val current = repository.getPengaturanDirect() ?: Pengaturan()
            repository.updatePengaturan(current.copy(darkTheme = theme))
        }
    }

    // Transaction Actions
    suspend fun tambahTransaksi(
        kategori: String,
        namaPelanggan: String,
        nomorHP: String,
        nominal: Double,
        modal: Double,
        hargaJual: Double,
        catatan: String
    ): Boolean {
        val trx = Transaksi(
            kategori = kategori,
            namaPelanggan = namaPelanggan.trim().ifEmpty { null },
            nomorHP = nomorHP.trim().ifEmpty { null },
            nominal = nominal,
            modal = modal,
            hargaJual = hargaJual,
            catatan = catatan.trim(),
            keuntungan = hargaJual - modal
        )
        return repository.insertTransaksi(trx)
    }

    fun hapusTransaksi(transaksi: Transaksi) {
        viewModelScope.launch {
            repository.deleteTransaksi(transaksi)
        }
    }

    // Balance Actions
    fun tambahSaldoKonter(jumlah: Double, catatan: String) {
        viewModelScope.launch {
            repository.tambahSaldo(jumlah, catatan)
        }
    }

    // Debt Actions
    fun tambahHutang(nama: String, hp: String, jumlah: Double, catatan: String) {
        viewModelScope.launch {
            val h = Hutang(
                namaPelanggan = nama.trim().ifEmpty { "Pelanggan Tanpa Nama" },
                nomorHP = hp.trim(),
                jumlahHutang = jumlah,
                status = "Belum Lunas",
                catatan = catatan.trim()
            )
            repository.insertHutang(h)
        }
    }

    fun editHutang(hutang: Hutang) {
        viewModelScope.launch {
            repository.updateHutang(hutang)
        }
    }

    fun tandaiHutangLunas(hutang: Hutang) {
        viewModelScope.launch {
            repository.updateHutang(hutang.copy(status = "Lunas"))
        }
    }

    fun hapusHutang(hutang: Hutang) {
        viewModelScope.launch {
            repository.deleteHutang(hutang)
        }
    }

    // Reports Aggregator helper (Filters: "Harian", "Mingguan", "Bulanan")
    fun getReportData(filter: String, transactions: List<Transaksi>): ReportData {
        val now = System.currentTimeMillis()
        val limitTime = when (filter) {
            "Harian" -> getStartOfToday()
            "Mingguan" -> now - (7L * 24 * 60 * 60 * 1000)
            "Bulanan" -> now - (30L * 24 * 60 * 60 * 1000)
            else -> 0L
        }

        val filteredList = transactions.filter { it.tanggal >= limitTime }

        val omzet = filteredList.sumOf { it.hargaJual }
        val keuntungan = filteredList.sumOf { it.keuntungan }
        val totalTrx = filteredList.size

        // Kategori terlaris
        val kategoriCounts = filteredList.groupBy { it.kategori }
            .mapValues { it.value.size }
        val kategoriTerlaris = kategoriCounts.maxByOrNull { it.value }?.key ?: "Belum ada transaksi"

        // Sparkline / Historical Points (group by day/time depending on range)
        val sortedList = filteredList.sortedBy { it.tanggal }
        val chartDataPoints = mutableListOf<Float>()
        val chartLabels = mutableListOf<String>()

        val sdf = SimpleDateFormat(if (filter == "Harian") "HH:mm" else "dd MMM", Locale("id", "ID"))
        
        // Let's dynamic slice or group to build a beautiful sparkline representation
        if (filter == "Harian") {
            // Group by hour blocks of today
            val groupedByHour = sortedList.groupBy { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.tanggal }
                cal.get(Calendar.HOUR_OF_DAY)
            }
            for (hour in 0..24) {
                val hourTrx = groupedByHour[hour] ?: emptyList()
                chartDataPoints.add(hourTrx.sumOf { it.hargaJual }.toFloat())
                if (hour % 4 == 0) {
                    chartLabels.add(String.format("%02d:00", hour))
                } else {
                    chartLabels.add("")
                }
            }
        } else {
            // Group by days
            val groupedByDay = sortedList.groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.tanggal }
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            val numDays = if (filter == "Mingguan") 7 else 30
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -numDays + 1)
            
            for (i in 0 until numDays) {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val dayTime = calendar.timeInMillis
                
                val dayTrx = groupedByDay[dayTime] ?: emptyList()
                chartDataPoints.add(dayTrx.sumOf { it.hargaJual }.toFloat())
                
                // Show label every few days
                if (filter == "Mingguan" || i % 5 == 0 || i == numDays - 1) {
                    chartLabels.add(sdf.format(Date(dayTime)))
                } else {
                    chartLabels.add("")
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return ReportData(
            omzet = omzet,
            keuntungan = keuntungan,
            totalTransaksi = totalTrx,
            kategoriTerlaris = kategoriTerlaris,
            chartPoints = chartDataPoints,
            chartLabels = chartLabels
        )
    }

    // --- Dynamic Backup & Restore (JSON String style) ---
    fun backupDataToJSONString(): String {
        try {
            val root = JSONObject()
            
            // 1. Settings Setup
            val currentSettings = pengaturan.value ?: Pengaturan()
            val settingsJson = JSONObject().apply {
                put("namaKonter", currentSettings.namaKonter)
                put("batasSaldoRendah", currentSettings.batasSaldoRendah)
            }
            root.put("pengaturan", settingsJson)

            // 2. Saldo
            root.put("saldo", saldo.value?.jumlah ?: 0.0)

            // 3. Transaksi
            val trxArray = JSONArray()
            semuaTransaksi.value.forEach { trx ->
                val trxObj = JSONObject().apply {
                    put("tanggal", trx.tanggal)
                    put("kategori", trx.kategori)
                    put("namaPelanggan", trx.namaPelanggan ?: "")
                    put("nomorHP", trx.nomorHP ?: "")
                    put("nominal", trx.nominal)
                    put("modal", trx.modal)
                    put("hargaJual", trx.hargaJual)
                    put("catatan", trx.catatan)
                    put("keuntungan", trx.keuntungan)
                }
                trxArray.put(trxObj)
            }
            root.put("transaksi", trxArray)

            // 4. Hutang
            val htgArray = JSONArray()
            semuaHutang.value.forEach { htg ->
                val htgObj = JSONObject().apply {
                    put("namaPelanggan", htg.namaPelanggan)
                    put("nomorHP", htg.nomorHP)
                    put("jumlahHutang", htg.jumlahHutang)
                    put("tanggal", htg.tanggal)
                    put("catatan", htg.catatan)
                    put("status", htg.status)
                }
                htgArray.put(htgObj)
            }
            root.put("hutang", htgArray)

            // 5. Riwayat Saldo
            val rwyArray = JSONArray()
            semuaRiwayatSaldo.value.forEach { rwy ->
                val rwyObj = JSONObject().apply {
                    put("tanggal", rwy.tanggal)
                    put("tipe", rwy.tipe)
                    put("jumlah", rwy.jumlah)
                    put("saldoLama", rwy.saldoLama)
                    put("saldoBaru", rwy.saldoBaru)
                    put("catatan", rwy.catatan)
                }
                rwyArray.put(rwyObj)
            }
            root.put("riwayatSaldo", rwyArray)

            return root.toString(2) // Beautifully formatted indent
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    suspend fun restoreDataFromJSONString(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)

            // Use transactional rebuild!
            db.withTransaction {
                // Clear and write database!
                db.clearAllTables()

                // Restore settings
                if (root.has("pengaturan")) {
                    val settingsObj = root.getJSONObject("pengaturan")
                    val setup = Pengaturan(
                        id = 1,
                        namaKonter = settingsObj.optString("namaKonter", "Shinfox Store"),
                        batasSaldoRendah = settingsObj.optDouble("batasSaldoRendah", 100000.0),
                        setupComplete = true
                    )
                    db.pengaturanDao().upsertPengaturan(setup)
                }

                // Restore saldo
                val sVal = root.optDouble("saldo", 0.0)
                db.saldoDao().upsertSaldo(Saldo(id = 1, jumlah = sVal))

                // Restore transactions
                if (root.has("transaksi")) {
                    val trxArr = root.getJSONArray("transaksi")
                    for (i in 0 until trxArr.length()) {
                        val obj = trxArr.getJSONObject(i)
                        val trx = Transaksi(
                            tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                            kategori = obj.getString("kategori"),
                            namaPelanggan = obj.optString("namaPelanggan").ifBlank { null },
                            nomorHP = obj.optString("nomorHP").ifBlank { null },
                            nominal = obj.getDouble("nominal"),
                            modal = obj.getDouble("modal"),
                            hargaJual = obj.getDouble("hargaJual"),
                            catatan = obj.optString("catatan", ""),
                            keuntungan = obj.optDouble("keuntungan", obj.getDouble("hargaJual") - obj.getDouble("modal"))
                        )
                        db.transaksiDao().insertTransaksi(trx)
                    }
                }

                // Restore debts
                if (root.has("hutang")) {
                    val htgArr = root.getJSONArray("hutang")
                    for (i in 0 until htgArr.length()) {
                        val obj = htgArr.getJSONObject(i)
                        val htg = Hutang(
                            namaPelanggan = obj.getString("namaPelanggan"),
                            nomorHP = obj.optString("nomorHP", ""),
                            jumlahHutang = obj.getDouble("jumlahHutang"),
                            tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                            catatan = obj.optString("catatan", ""),
                            status = obj.optString("status", "Belum Lunas")
                        )
                        db.hutangDao().insertHutang(htg)
                    }
                }

                // Restore riwayat saldo
                if (root.has("riwayatSaldo")) {
                    val rwyArr = root.getJSONArray("riwayatSaldo")
                    for (i in 0 until rwyArr.length()) {
                        val obj = rwyArr.getJSONObject(i)
                        val rwy = RiwayatSaldo(
                            tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                            tipe = obj.getString("tipe"),
                            jumlah = obj.getDouble("jumlah"),
                            saldoLama = obj.optDouble("saldoLama", 0.0),
                            saldoBaru = obj.optDouble("saldoBaru", 0.0),
                            catatan = obj.optString("catatan", "")
                        )
                        db.riwayatSaldoDao().insertRiwayatSaldo(rwy)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Helper: start of today in stamp
    private fun getStartOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Money Formatter Helper
    fun formatRupiah(value: Double): String {
        return try {
            val formatter = java.text.DecimalFormat("#,###")
            formatter.format(value).replace(",", ".")
        } catch (e: java.lang.Exception) {
            value.toInt().toString()
        }
    }
}

// Data Classes for States
data class DashboardStats(
    val saldoKonter: Double = 0.0,
    val omzetHariIni: Double = 0.0,
    val keuntunganHariIni: Double = 0.0,
    val jumlahTransaksiHariIni: Int = 0,
    val hutangAktif: Double = 0.0
)

data class InsightMessage(
    val title: String,
    val message: String,
    val isCritical: Boolean
)

data class ReportData(
    val omzet: Double,
    val keuntungan: Double,
    val totalTransaksi: Int,
    val kategoriTerlaris: String,
    val chartPoints: List<Float>,
    val chartLabels: List<String>
)
