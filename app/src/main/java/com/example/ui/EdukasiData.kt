package com.example.ui

data class Artikel(
    val id: Int,
    val judul: String,
    val kategori: String,
    val ringkasan: String,
    val isi: String,
    val icon: String
)

object EdukasiData {
    val daftarArtikel = listOf(
        Artikel(
            id = 1,
            judul = "Cara Mengelola Saldo Konter",
            kategori = "Manajemen Saldo",
            ringkasan = "Pisahkan uang pribadi dan kas konter untuk menjaga kestabilan modal digital Anda.",
            isi = "Salah satu kesalahan fatal pemilik konter pemula adalah mencampur aduk uang hasil penjualan pulsa dengan dompet pribadi.\n\n" +
                  "Berikut tips mengelola saldo secara profesional:\n" +
                  "• Catat setiap penambahan saldo modal secara rinci di riwayat keuangan.\n" +
                  "• Selalu sisihkan keuntungan harian dan masukkan kembali sebagai modal tambahan agar kapasitas transaksi konter Anda semakin meningkat.\n" +
                  "• Hindari membelanjakan modal 'aktif' (saldo server) untuk keperluan di luar toko agar tidak terjadi kekosongan stok saat pelanggan datang.",
            icon = "account_balance_wallet"
        ),
        Artikel(
            id = 2,
            judul = "Tips Sukses Bisnis Konter Pemula",
            kategori = "Strategi Pemula",
            ringkasan = "Sediakan produk lengkap harian dan berikan pelayanan tercepat untuk menarik pelanggan loyal.",
            isi = "Pengembangan konter pulsa membutuhkan konsistensi pelayanan yang luar biasa.\n\n" +
                  "Bagi pemula, terapkan strategi taktis berikut:\n" +
                  "• Sediakan variasi produk lengkap mulai dari pulsa reguler, paket data internet, token PLN listrik, hingga top-up e-wallet (DANA, OVO, ShopeePay).\n" +
                  "• Proses transaksi secepat mungkin. Kecepatan pengiriman pulsa adalah nilai jual utama Anda.\n" +
                  "• Desain konter yang rapi, bersih, dan informatif sehingga memicu rasa percaya konsumen.",
            icon = "trending_up"
        ),
        Artikel(
            id = 3,
            judul = "Strategi Jitu Menghindari Bon Macet",
            kategori = "Hutang",
            ringkasan = "Terapkan aturan tegas pengutangan dan kelola pembukuan piutang pelanggan secara disiplin.",
            isi = "Piutang tidak sehat atau 'bon' macet adalah penyebab utama konter pulsa gulung tikar.\n\n" +
                  "Cara terbaik menghindarinya:\n" +
                  "• Buat batas maksimal berhutang untuk setiap pelanggan (contoh: maksimal Rp 50.000).\n" +
                  "• Gunakan pencatatan menu 'Hutang' di aplikasi Shinfox ini untuk memantau status jatuh tempo.\n" +
                  "• Ingatkan pelanggan secara sopan melalui WhatsApp ketika melampaui tenggat pembayaran.\n" +
                  "• Berani berkata 'Maaf, saldo sedang limit' secara halus kepada konsumen yang memiliki riwayat pembayaran buruk.",
            icon = "warning"
        ),
        Artikel(
            id = 4,
            judul = "Rumus Praktis Menghitung Keuntungan",
            kategori = "Keuangan",
            ringkasan = "Pahami perbedaan modal asli, keuntungan bersih, dan biaya operasional harian konter.",
            isi = "Memperoleh omzet tinggi bukan berarti untung besar. Anda harus jeli melacak rumus keuntungan bersih modal Anda.\n\n" +
                  "Rumus matematika konter:\n" +
                  "Harga Jual - Harga Modal = Keuntungan\n\n" +
                  "Contoh: Pulsa 10.000 dengan modal ditarik server Rp 10.200 dilepas dengan harga jual Rp 12.500. Keuntungan bersih murni Anda adalah Rp 2.300.\n" +
                  "Pantau statistik laporan laba mingguan dan bulanan pada sistem Shinfox secara periodik untuk menganalisis tren perkembangan konter Anda.",
            icon = "calculate"
        ),
        Artikel(
            id = 5,
            judul = "Standardisasi Pelayanan Pelanggan Premium",
            kategori = "Pelayanan",
            ringkasan = "Tingkatkan loyalitas pelanggan Anda dengan keramahan optimal dan kepatuhan transaksi.",
            isi = "Senyum ramah dan komparasi harga yang transparan membuat pembeli merasa dihargai.\n\n" +
                  "Pondasi sukses pelayanan konter:\n" +
                  "• Selalu tanyakan kembali kenyamanan pembeli (misal: 'Apakah pulsanya sudah masuk, Kak?').\n" +
                  "• Tampilkan harga jual produk secara jelas agar pembeli tidak merasa tertipu.\n" +
                  "• Tanggapi komplain kegagalan jaringan operator dengan bersikap tenang dan bantu melaporkannya ke agen pusat layanan.",
            icon = "face"
        )
    )
}
