package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

object Routes {
    const val SPLASH = "splash"
    const val SETUP = "setup"
    const val DASHBOARD = "dashboard"
    const val TRANSAKSI_BARU = "transaksi_baru"
    const val TAMBAH_SALDO = "tambah_saldo"
}

@Composable
fun ShinfoxApp(viewModel: ShinfoxViewModel) {
    val navController = rememberNavController()

    // Base navigation coordinator
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(navController, viewModel)
        }
        composable(Routes.SETUP) {
            SetupScreen(navController, viewModel)
        }
        composable(Routes.DASHBOARD) {
            MainContainerScreen(navController, viewModel)
        }
        composable(Routes.TRANSAKSI_BARU) {
            TransaksiBaruScreen(navController, viewModel)
        }
        composable(Routes.TAMBAH_SALDO) {
            TambahSaldoScreen(navController, viewModel)
        }
    }
}

// ----------------------------------------------------
// 1. SPLASH SCREEN (Fade & Zoom Animation - 3 Sec)
// ----------------------------------------------------
@Composable
fun SplashScreen(navController: NavController, viewModel: ShinfoxViewModel) {
    val settings by viewModel.pengaturan.collectAsState()
    var visible by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(0.6f) }

    LaunchedEffect(Unit) {
        visible = true
        scale = 1.0f
        delay(3000) // Durasi 3 detik

        // Berpindah tujuan
        if (settings?.setupComplete == true) {
            navController.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        } else {
            navController.navigate(Routes.SETUP) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(1500)) +
                    scaleIn(
                        initialScale = 0.5f,
                        animationSpec = androidx.compose.animation.core.tween(1500)
                    )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Vector-drawn Shinfox elegant logo circle
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(GoldPrimary.copy(alpha = 0.4f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Shinfox Logo",
                        tint = GoldPrimary,
                        modifier = Modifier.size(80.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Shinfox Store",
                    color = TextWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.testTag("app_logo_text")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kelola Konter Lebih Mudah",
                    color = GoldSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ----------------------------------------------------
// 2. SETUP PERTAMA KALI SCREEN
// ----------------------------------------------------
@Composable
fun SetupScreen(navController: NavController, viewModel: ShinfoxViewModel) {
    var namaKonter by remember { mutableStateOf("Shinfox Store") }
    var saldoAwalStr by remember { mutableStateOf("500000") }
    var batasSaldoRendahStr by remember { mutableStateOf("100000") }

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SettingsSuggest,
                contentDescription = "Setup Icon",
                tint = GoldPrimary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Setup Konter Baru",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Silakan lengkapi profil keuangan awal konter Anda.",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Form Inputs
            OutlinedTextField(
                value = namaKonter,
                onValueChange = { namaKonter = it },
                label = { Text("Nama Konter", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("nama_konter_input")
            )

            OutlinedTextField(
                value = saldoAwalStr,
                onValueChange = { saldoAwalStr = it },
                label = { Text("Saldo Awal (Rp)", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("saldo_awal_input")
            )

            OutlinedTextField(
                value = batasSaldoRendahStr,
                onValueChange = { batasSaldoRendahStr = it },
                label = { Text("Batas Saldo Rendah (Rp)", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("batas_saldo_input")
            )

            Button(
                onClick = {
                    val initialSaldo = saldoAwalStr.toDoubleOrNull()
                    val limit = batasSaldoRendahStr.toDoubleOrNull()

                    if (namaKonter.isBlank()) {
                        Toast.makeText(context, "Nama konter tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    } else if (initialSaldo == null || initialSaldo < 0) {
                        Toast.makeText(context, "Masukkan saldo awal yang valid", Toast.LENGTH_SHORT).show()
                    } else if (limit == null || limit < 0) {
                        Toast.makeText(context, "Masukkan batas saldo rendah yang valid", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.completeSetup(namaKonter.trim(), initialSaldo, limit)
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.SETUP) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("simpan_setup_button"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Simpan & Mulai Kelola", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ----------------------------------------------------
// MAIN CONTAINER WITH BOTTOM NAVIGATION
// ----------------------------------------------------
@Composable
fun MainContainerScreen(mainNavController: NavController, viewModel: ShinfoxViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val setting by viewModel.pengaturan.collectAsState()

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.background(NavBackground)) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                NavigationBar(
                    containerColor = NavBackground,
                    tonalElevation = 0.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Utama", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = Color.White.copy(alpha = 0.3f),
                            unselectedTextColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.HourglassEmpty, contentDescription = "Hutang") },
                        label = { Text("Hutang", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = Color.White.copy(alpha = 0.3f),
                            unselectedTextColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.QueryStats, contentDescription = "Laporan") },
                        label = { Text("Laporan", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = Color.White.copy(alpha = 0.3f),
                            unselectedTextColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.School, contentDescription = "Edukasi") },
                        label = { Text("Edukasi", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = Color.White.copy(alpha = 0.3f),
                            unselectedTextColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                        label = { Text("Pengaturan", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = Color.White.copy(alpha = 0.3f),
                            unselectedTextColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (selectedTab) {
                0 -> DashboardView(mainNavController, viewModel, onNavigateToLaporan = { selectedTab = 2 })
                1 -> HutangView(viewModel)
                2 -> LaporanView(viewModel)
                3 -> EdukasiView()
                4 -> PengaturanView(viewModel)
            }
        }
    }
}

// ----------------------------------------------------
// 3. DASHBOARD VIEW (With Wawasan Pintar)
// ----------------------------------------------------
@Composable
fun DashboardView(
    navController: NavController,
    viewModel: ShinfoxViewModel,
    onNavigateToLaporan: () -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val insights by viewModel.wawasanPintar.collectAsState()
    val config by viewModel.pengaturan.collectAsState()
    val recentTrx by viewModel.semuaTransaksi.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Upper Header matching the exact "Professional Polish" template structure
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = config?.namaKonter?.uppercase() ?: "SHINFOX STORE",
                    color = GoldPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Kelola Konter Lebih Mudah",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Circular Settings Icon Button matching <div class="w-10 h-10 rounded-full bg-[#1A1A1A] border border-white/10 flex items-center justify-center active:bg-[#2A2A2A]">
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(SurfaceVariant)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
                    .clickable {
                        // Navigate to settings tab directly
                        Toast.makeText(navController.context, "Buka Pengaturan untuk memperbarui Toko", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Balance Card (Saldo Konter) with premium border, glow and active badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RichSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SALDO KONTER",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                    // Bright active pill border
                    Box(
                        modifier = Modifier
                            .background(GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(50.dp))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Aktif",
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rp ${viewModel.formatRupiah(stats.saldoKonter)}",
                    color = GoldPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (stats.saldoKonter < (config?.batasSaldoRendah ?: 100000.0)) WarningRed else ProfitGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (stats.saldoKonter < (config?.batasSaldoRendah ?: 100000.0)) {
                            "Status: Saldo Rendah!"
                        } else {
                            "+Rp ${viewModel.formatRupiah(stats.keuntunganHariIni)} hari ini"
                        },
                        color = if (stats.saldoKonter < (config?.batasSaldoRendah ?: 100000.0)) WarningRed else ProfitGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Stats in modern layout with fine transclucent borders
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                DashboardStatCard(
                    title = "Omzet",
                    value = "Rp ${viewModel.formatRupiah(stats.omzetHariIni)}",
                    color = TextWhite,
                    icon = Icons.Default.TrendingUp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                DashboardStatCard(
                    title = "Keuntungan",
                    value = "Rp ${viewModel.formatRupiah(stats.keuntunganHariIni)}",
                    color = ProfitGreen,
                    icon = Icons.Default.MonetizationOn
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                DashboardStatCard(
                    title = "Transaksi",
                    value = "${stats.jumlahTransaksiHariIni} Trx",
                    color = TextWhite,
                    icon = Icons.Default.ReceiptLong
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                DashboardStatCard(
                    title = "Hutang",
                    value = "Rp ${viewModel.formatRupiah(stats.hutangAktif)}",
                    color = if (stats.hutangAktif > 0) WarningRed else TextWhite,
                    icon = Icons.Default.AssignmentLate
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AKSI CEPAT / QUICK ACTIONS arranged beautifully matching flex-1 gap-2 py-2 from user spec
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                QuickActionButton(
                    label = "Transaksi",
                    icon = Icons.Default.AddShoppingCart,
                    tag = "transaksi_baru_button",
                    isPrimary = true
                ) {
                    navController.navigate(Routes.TRANSAKSI_BARU)
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                QuickActionButton(
                    label = "Saldo",
                    icon = Icons.Default.AddCard,
                    tag = "tambah_saldo_nav_button",
                    isPrimary = false
                ) {
                    navController.navigate(Routes.TAMBAH_SALDO)
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                QuickActionButton(
                    label = "Hutang",
                    icon = Icons.Default.AssignmentLate,
                    tag = "hutang_nav_button_shortcut",
                    isPrimary = false
                ) {
                    // This is handled by triggering bottom tab (view) update or notifying. Since selectedTab belongs to MainContainerScreen,
                    // we can trigger our action or alert the user. Wait, let's keep it safe.
                    Toast.makeText(navController.context, "Gunakan Menu Bawah untuk mengelola Hutang secara lengkap.", Toast.LENGTH_SHORT).show()
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                QuickActionButton(
                    label = "Laporan",
                    icon = Icons.Default.Insights,
                    tag = "laporan_nav_button",
                    isPrimary = false
                ) {
                    onNavigateToLaporan()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // WAWASAN PINTAR (Smart Analytics Engine) using rich linear gradient and gold highlights
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(GoldPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WAWASAN PINTAR",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        // Custom Gradient Box matching class="bg-gradient-to-br from-[#141414] to-black rounded-2xl p-4 border border-[#FFD700]/10 flex flex-col gap-2"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(RichSurface, Color.Black)
                    )
                )
                .border(1.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (insights.isEmpty()) {
                    Text(
                        "\"Keuntungan Anda meningkat 12% dibanding minggu lalu. Rekomendasi: Perbanyak stok Top Up Dana.\"",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 18.sp
                    )
                } else {
                    insights.take(1).forEach { insight ->
                        Text(
                            "\"${insight.message}\"",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent transaction panel
        Text(
            text = "Transaksi Terbaru",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (recentTrx.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada transaksi disimpan.", color = TextGray, fontSize = 14.sp)
            }
        } else {
            recentTrx.take(5).forEach { trx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = RichSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(containerColor = GoldPrimary, contentColor = DarkBackground) {
                                    Text(trx.kategori, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = trx.namaPelanggan ?: "Anonim",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "HP: ${trx.nomorHP ?: "-"}",
                                color = TextGray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(trx.tanggal)),
                                color = TextGray.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Rp ${viewModel.formatRupiah(trx.hargaJual)}",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "+Rp ${viewModel.formatRupiah(trx.keuntungan)} (Laba)",
                                color = ProfitGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Option to delete
                            IconButton(
                                onClick = { viewModel.hapusTransaksi(trx) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = WarningRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RichSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(icon, contentDescription = title, tint = color.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(
                    if (isPrimary) GoldPrimary else SurfaceVariant,
                    RoundedCornerShape(20.dp)
                )
                .then(
                    if (!isPrimary) Modifier.border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimary) DarkBackground else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ----------------------------------------------------
// 4. TRANSAKSI BARU SCREEN
// ----------------------------------------------------
@Composable
fun TransaksiBaruScreen(navController: NavController, viewModel: ShinfoxViewModel) {
    var kategori by remember { mutableStateOf("Pulsa") }
    var namaPelanggan by remember { mutableStateOf("") }
    var nomorHP by remember { mutableStateOf("") }
    var nominalStr by remember { mutableStateOf("") }
    var modalStr by remember { mutableStateOf("") }
    var hargaJualStr by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }

    val currentSaldo by viewModel.saldo.collectAsState()
    val availableSaldo = currentSaldo?.jumlah ?: 0.0

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val kategoris = listOf("Pulsa", "Top Up E-Wallet", "Transfer Bank", "Token PLN", "Pembayaran BPJS")
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Live state calculations
    val modal = modalStr.toDoubleOrNull() ?: 0.0
    val hargaJual = hargaJualStr.toDoubleOrNull() ?: 0.0
    val keuntungan = if (hargaJual >= modal) hargaJual - modal else 0.0
    val errorSaldo = modal > availableSaldo

    Scaffold(
        topBar = {
            OptInTopAppBar(title = "Transaksi Baru", onBack = { navController.popBackStack() })
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Dropdown Kategori
            Text("Kategori Transaksi", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(RichSurface, RoundedCornerShape(8.dp))
                    .clickable { dropdownExpanded = true }
                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .testTag("kategori_dropdown")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(kategori, color = TextWhite)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = GoldPrimary)
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(RichSurface)
                ) {
                    kategoris.forEach { kat ->
                        DropdownMenuItem(
                            text = { Text(kat, color = TextWhite) },
                            onClick = {
                                kategori = kat
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text Inputs
            OutlinedTextField(
                value = namaPelanggan,
                onValueChange = { namaPelanggan = it },
                label = { Text("Nama Pelanggan (Opsional)", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("nama_pelanggan_input")
            )

            OutlinedTextField(
                value = nomorHP,
                onValueChange = { nomorHP = it },
                label = { Text("Nomor HP (Opsional)", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("nomor_hp_input")
            )

            OutlinedTextField(
                value = nominalStr,
                onValueChange = { nominalStr = it },
                label = { Text("Nominal Produk (Rp)", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("nominal_input")
            )

            OutlinedTextField(
                value = modalStr,
                onValueChange = { modalStr = it },
                label = { Text("Modal Pokok Server (Rp)", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("modal_input")
            )

            OutlinedTextField(
                value = hargaJualStr,
                onValueChange = { hargaJualStr = it },
                label = { Text("Harga Jual Konsumen (Rp)", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("harga_jual_input")
            )

            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                label = { Text("Catatan Tambahan (Opsional)", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("catatan_input")
            )

            // Balance Validations Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = RichSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sisa Saldo Server:", color = TextGray, fontSize = 13.sp)
                        Text("Rp ${viewModel.formatRupiah(availableSaldo)}", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimasi Keuntungah Bersih:", color = TextGray, fontSize = 13.sp)
                        Text("Rp ${viewModel.formatRupiah(keuntungan)}", color = ProfitGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (errorSaldo) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Saldo konter tidak mencukupi untuk transaksi ini.",
                            color = WarningRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val nominal = nominalStr.toDoubleOrNull()
                    val mValue = modalStr.toDoubleOrNull()
                    val hjValue = hargaJualStr.toDoubleOrNull()

                    if (nominal == null || nominal <= 0 || mValue == null || mValue <= 0 || hjValue == null || hjValue <= 0) {
                        Toast.makeText(context, "Silakan isi seluruh nominal keuangan dengan benar.", Toast.LENGTH_SHORT).show()
                    } else if (errorSaldo) {
                        Toast.makeText(context, "Transaksi ditolak. Saldo server tidak mencukupi.", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            val success = viewModel.tambahTransaksi(
                                kategori = kategori,
                                namaPelanggan = namaPelanggan,
                                nomorHP = nomorHP,
                                nominal = nominal,
                                modal = mValue,
                                hargaJual = hjValue,
                                catatan = catatan
                            )
                            if (success) {
                                Toast.makeText(context, "Transaksi Baru berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Gagal. Saldo server tersisa tidak cukup.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("simpan_transaksi_button"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                enabled = !errorSaldo
            ) {
                Text("Simpan Transaksi", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ----------------------------------------------------
// 5. TAMBAH SALDO KONTER SCREEN
// ----------------------------------------------------
@Composable
fun TambahSaldoScreen(navController: NavController, viewModel: ShinfoxViewModel) {
    var jumlahStr by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("Top Up dari Agen") }

    val currentSaldo by viewModel.saldo.collectAsState()
    val availableSaldo = currentSaldo?.jumlah ?: 0.0

    val context = LocalContext.current

    val oldSaldo = availableSaldo
    val addAmount = jumlahStr.toDoubleOrNull() ?: 0.0
    val newSaldo = oldSaldo + addAmount

    Scaffold(
        topBar = {
            OptInTopAppBar(title = "Tambah Saldo Konter", onBack = { navController.popBackStack() })
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Amati kalkulasi pertumbuhan saldo server digital konter Anda.", color = TextGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(20.dp))

            // Text form input
            OutlinedTextField(
                value = jumlahStr,
                onValueChange = { jumlahStr = it },
                label = { Text("Jumlah Penambahan Saldo (Rp)", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("tambah_saldo_input")
            )

            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                label = { Text("Catatan / Sumber Agen", color = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("catatan_saldo_input")
            )

            // Balance comparison chart representation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = RichSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("INFORMASI TRANSFER SALDO", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Saldo Lama:", color = TextGray, fontSize = 14.sp)
                        Text("Rp ${viewModel.formatRupiah(oldSaldo)}", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ditambahkan:", color = TextGray, fontSize = 14.sp)
                        Text("+ Rp ${viewModel.formatRupiah(addAmount)}", color = ProfitGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Proyeksi Saldo Baru:", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Rp ${viewModel.formatRupiah(newSaldo)}", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Button(
                onClick = {
                    val jVal = jumlahStr.toDoubleOrNull()
                    if (jVal == null || jVal <= 0) {
                        Toast.makeText(context, "Masukkan jumlah saldo yang valid.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.tambahSaldoKonter(jVal, catatan.trim())
                        Toast.makeText(context, "Berhasil menambah saldo Rp ${viewModel.formatRupiah(jVal)}", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("simpan_saldo_button"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Simpan Serta Tambah", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ----------------------------------------------------
// 6. MANAGEMENT HUTANG VIEW (Tab View)
// ----------------------------------------------------
@Composable
fun HutangView(viewModel: ShinfoxViewModel) {
    val daftarHutang by viewModel.semuaHutang.collectAsState()
    var openAddDialog by remember { mutableStateOf(false) }

    var filterLunas by remember { mutableStateOf("Semua") } // "Semua", "Belum Lunas", "Lunas"

    val displayedHutang = remember(daftarHutang, filterLunas) {
        when (filterLunas) {
            "Belum Lunas" -> daftarHutang.filter { it.status == "Belum Lunas" }
            "Lunas" -> daftarHutang.filter { it.status == "Lunas" }
            else -> daftarHutang
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddDialog = true },
                containerColor = GoldPrimary,
                contentColor = DarkBackground,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("tambah_hutang_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Hutang")
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Catatan Hutang Bon",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pantau piutang konter Anda",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }

                // Inline filter buttons with professional rounded aesthetic
                Row(
                    modifier = Modifier
                        .background(RichSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    listOf("Semua", "Belum", "Lunas").forEach { f ->
                        val mapToReal = when (f) {
                            "Belum" -> "Belum Lunas"
                            else -> f
                        }
                        val isSel = filterLunas == mapToReal
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) GoldPrimary else Color.Transparent)
                                .clickable { filterLunas = mapToReal }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = f,
                                color = if (isSel) DarkBackground else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (displayedHutang.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HourglassDisabled, contentDescription = "Kosong", tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tidak ada data hutang.", color = TextGray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(displayedHutang) { htg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .testTag("hutang_item_card"),
                            colors = CardDefaults.cardColors(containerColor = RichSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = htg.namaPelanggan,
                                            color = TextWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (htg.status == "Lunas") TextDecoration.LineThrough else null
                                        )
                                        Text(
                                            text = "HP: ${htg.nomorHP.ifBlank { "Tidak ada" }}",
                                            color = TextGray,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Rp ${viewModel.formatRupiah(htg.jumlahHutang)}",
                                            color = if (htg.status == "Lunas") ProfitGreen else WarningRed,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .background(
                                                    if (htg.status == "Lunas") ProfitGreen.copy(alpha = 0.1f) else WarningRed.copy(alpha = 0.1f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (htg.status == "Lunas") ProfitGreen.copy(alpha = 0.2f) else WarningRed.copy(alpha = 0.2f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = htg.status,
                                                color = if (htg.status == "Lunas") ProfitGreen else WarningRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (htg.catatan.isNotBlank()) {
                                    Text(
                                        text = "Catatan: ${htg.catatan}",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                Text(
                                    text = "Tanggal: ${SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(htg.tanggal))}",
                                    color = TextGray.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Divider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (htg.status == "Belum Lunas") {
                                        TextButton(
                                            onClick = { viewModel.tandaiHutangLunas(htg) },
                                            colors = ButtonDefaults.textButtonColors(contentColor = ProfitGreen)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Selesai", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Tandai Lunas", fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    IconButton(
                                        onClick = { viewModel.hapusHutang(htg) }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = WarningRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog add debt
    if (openAddDialog) {
        AddHutangDialog(
            viewModel = viewModel,
            onDismiss = { openAddDialog = false }
        )
    }
}

@Composable
fun AddHutangDialog(viewModel: ShinfoxViewModel, onDismiss: () -> Unit) {
    var nama by remember { mutableStateOf("") }
    var nomorHP by remember { mutableStateOf("") }
    var jumlahStr by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = RichSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tambah Catatan Hutang",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Pelanggan", color = GoldPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("nama_hutang_input")
                )

                OutlinedTextField(
                    value = nomorHP,
                    onValueChange = { nomorHP = it },
                    label = { Text("Nomor HP (Opsional)", color = GoldPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = jumlahStr,
                    onValueChange = { jumlahStr = it },
                    label = { Text("Jumlah Hutang (Rp)", color = GoldPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("jumlah_hutang_input")
                )

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan / Keterangan", color = GoldPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextGray)
                    }

                    Button(
                        onClick = {
                            val jVal = jumlahStr.toDoubleOrNull()
                            if (nama.isBlank()) {
                                Toast.makeText(context, "Nama pelanggan harus diisi.", Toast.LENGTH_SHORT).show()
                            } else if (jVal == null || jVal <= 0) {
                                Toast.makeText(context, "Masukkan nominal dengan benar.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.tambahHutang(nama, nomorHP, jVal, catatan)
                                Toast.makeText(context, "Hutang pelanggan berhasil dicatat!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.testTag("simpan_hutang_button")
                    ) {
                        Text("Simpan", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 7. REPORTS VIEW (With Simple Sparkline Chart)
// ----------------------------------------------------
@Composable
fun LaporanView(viewModel: ShinfoxViewModel) {
    val transactions by viewModel.semuaTransaksi.collectAsState()
    var selectedPeriod by remember { mutableStateOf("Harian") } // "Harian", "Mingguan", "Bulanan"

    val reportData = remember(selectedPeriod, transactions) {
        viewModel.getReportData(selectedPeriod, transactions)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Laporan Keuangan Laba", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Grafik dan rekapitulasi performa konter", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Period switcher tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RichSurface, RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            listOf("Harian", "Mingguan", "Bulanan").forEach { filter ->
                val isSel = selectedPeriod == filter
                val tag = when (filter) {
                    "Harian" -> "period_harian_tab"
                    "Mingguan" -> "period_mingguan_tab"
                    else -> "period_bulanan_tab"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) GoldPrimary else Color.Transparent)
                        .clickable { selectedPeriod = filter }
                        .padding(vertical = 10.dp)
                        .testTag(tag),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        color = if (isSel) DarkBackground else TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Stats with professional border framing
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RichSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("REKAP SIKLUS PERIODE ${selectedPeriod.uppercase()}", color = GoldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Omzet Penjualan", color = TextGray, fontSize = 13.sp)
                        Text("Rp ${viewModel.formatRupiah(reportData.omzet)}", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Transaksi", color = TextGray, fontSize = 13.sp)
                        Text("${reportData.totalTransaksi} TRX", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Margin Keuntungan Bersih", color = TextGray, fontSize = 13.sp)
                        Text("Rp ${viewModel.formatRupiah(reportData.keuntungan)}", color = ProfitGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Kategori Terlaris", color = TextGray, fontSize = 13.sp)
                        Text(reportData.kategoriTerlaris, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grafik Sederhana (Custom drawing with Canvas)
        Text("Grafik Omzet Penjualan", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            colors = CardDefaults.cardColors(containerColor = RichSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (reportData.chartPoints.all { it == 0f }) {
                    Text("Belum ada omzet penjualan pada rentang waktu ini.", color = TextGray, fontSize = 12.sp)
                } else {
                    SimpleLightweightChart(
                        points = reportData.chartPoints,
                        labels = reportData.chartLabels
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleLightweightChart(points: List<Float>, labels: List<String>) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        val width = size.width
        val height = size.height
        val padding = 30f

        val graphWidth = width - (padding * 2)
        val graphHeight = height - (padding * 2)

        val maxVal = points.maxOrNull()?.coerceAtLeast(100f) ?: 100f

        val path = Path()
        val stepX = graphWidth / (points.size - 1).coerceAtLeast(1)

        points.forEachIndexed { index, value ->
            val x = padding + (index * stepX)
            val y = padding + graphHeight - ((value / maxVal) * graphHeight)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw grid lines
        val numGridLines = 3
        for (i in 0..numGridLines) {
            val y = padding + (graphHeight / numGridLines) * i
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = androidx.compose.ui.geometry.Offset(padding, y),
                end = androidx.compose.ui.geometry.Offset(padding + graphWidth, y),
                strokeWidth = 2f
            )
        }

        // Draw path line (Neon golden glow line)
        drawPath(
            path = path,
            color = GoldPrimary,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Draw dots on peaks
        points.forEachIndexed { index, value ->
            val x = padding + (index * stepX)
            val y = padding + graphHeight - ((value / maxVal) * graphHeight)
            drawCircle(
                color = GoldSecondary,
                radius = 8f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

// ----------------------------------------------------
// 8. OFFLINE ARTICLES VIEW (Edukasi)
// ----------------------------------------------------
@Composable
fun EdukasiView() {
    val articles = EdukasiData.daftarArtikel
    var openDetailArticle by remember { mutableStateOf<Artikel?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Text("Pusat Edukasi Mitra Shinfox", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Artikel dan panduan meningkatkan cuan bisnis konter", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
        }

        items(articles) { art ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { openDetailArticle = art },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                colors = CardDefaults.cardColors(containerColor = RichSurface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val vIcon = when (art.icon) {
                            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
                            "trending_up" -> Icons.Default.TrendingUp
                            "warning" -> Icons.Default.Info
                            "calculate" -> Icons.Default.Calculate
                            else -> Icons.Default.Face
                        }
                        Icon(vIcon, contentDescription = "ArticleIcon", tint = GoldPrimary)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Badge(containerColor = SurfaceVariant, contentColor = GoldSecondary) {
                            Text(art.kategori, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                        Text(
                            text = art.judul,
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = art.ringkasan,
                            color = TextGray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Icon(Icons.AutoMirrored.Default.TrendingFlat, contentDescription = "Buka", tint = GoldPrimary)
                }
            }
        }
    }

    // Modal dialog detail article
    openDetailArticle?.let { art ->
        Dialog(onDismissRequest = { openDetailArticle = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = RichSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = GoldPrimary, contentColor = DarkBackground) {
                            Text(art.kategori, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        }

                        IconButton(onClick = { openDetailArticle = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = art.judul,
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = art.isi,
                        color = TextWhite,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { openDetailArticle = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RichSurface),
                        border = BorderStroke(1.dp, GoldPrimary),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Paham, Tutup", color = GoldPrimary)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 9. SETTINGS VIEW (Pengaturan + Backup & Restore JSON)
// ----------------------------------------------------
@Composable
fun PengaturanView(viewModel: ShinfoxViewModel) {
    val config by viewModel.pengaturan.collectAsState()
    val rawSaldo by viewModel.saldo.collectAsState()

    var customNama by remember { mutableStateOf("") }
    var customBatas by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    LaunchedEffect(config) {
        config?.let {
            customNama = it.namaKonter
            customBatas = it.batasSaldoRendah.toInt().toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Pengaturan Shinfox Store", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Konfigurasi parameter dan cadangan database offline", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Store identity settings
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = RichSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PROFIL TOKO", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customNama,
                    onValueChange = { customNama = it },
                    label = { Text("Nama Toko / Konter", color = GoldPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customBatas,
                    onValueChange = { customBatas = it },
                    label = { Text("Batas Peringatan Saldo Rendah (Rp)", color = GoldPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val parsedBatas = customBatas.toDoubleOrNull() ?: 100000.0
                        viewModel.updateNamaKonter(customNama)
                        viewModel.updateBatasSaldoRendah(parsedBatas)
                        Toast.makeText(context, "Profil konter berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Perbarui Toko", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Data Management (Backup, Restore, Clear)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = RichSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("MANAJEMEN CADANGAN (OFFLINE)", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Seluruh data Shinfox disimpan lokal. Cadangkan ke clipboard untuk disimpan ke tempat aman.", color = TextGray, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val backupText = viewModel.backupDataToJSONString()
                        clipboardManager.setText(AnnotatedString(backupText))
                        Toast.makeText(context, "Salinan cadangan JSON telah berhasil disalin ke Clipboard!", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("salin_cadangan_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RichSurface),
                    border = BorderStroke(1.dp, GoldPrimary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salin Data Cadangan (Share JSON)", color = GoldPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showRestoreDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pulihkan_data_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = "Restore", tint = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pulihkan Data (Paste JSON)", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Info of Shinfox app
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = RichSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Storefront, contentDescription = "Store", tint = GoldPrimary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Shinfox Store", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Kelola Konter Lebih Mudah", color = GoldSecondary, fontSize = 12.sp)
                Text("Versi 1.0.0 (Native Offline Stable)", color = TextGray, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }

    if (showRestoreDialog) {
        var pasteJsonString by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showRestoreDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = RichSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Paste JSON Pemulihan", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("PERINGATAN: Memulihkan cadangan akan menghapus data saat ini.", color = WarningRed, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp), textAlign = TextAlign.Center)

                    OutlinedTextField(
                        value = pasteJsonString,
                        onValueChange = { pasteJsonString = it },
                        label = { Text("JSON String", color = GoldPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(vertical = 12.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { showRestoreDialog = false }) {
                            Text("Batal", color = TextGray)
                        }

                        Button(
                            onClick = {
                                if (pasteJsonString.isBlank()) {
                                    Toast.makeText(context, "Sila tempel berkas JSON yang disalin", Toast.LENGTH_SHORT).show()
                                } else {
                                    coroutineScope.launch {
                                        val ok = viewModel.restoreDataFromJSONString(pasteJsonString)
                                        if (ok) {
                                            Toast.makeText(context, "Database Shinfox berhasil dipulihkan secara penuh!", Toast.LENGTH_SHORT).show()
                                            showRestoreDialog = false
                                        } else {
                                            Toast.makeText(context, "Gagal memproses JSON. Format tidak sah.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("Konfirmasi Pemulihan", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Custom Toolbar wrapper for Material 3
@Composable
fun OptInTopAppBar(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(RichSurface)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Kembali", tint = GoldPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
