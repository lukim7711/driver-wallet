# AD_01: PRD (Product Requirement Document) — Driver Wallet Android

> **Audience:** AI Builder & Developer
> **Platform:** Native Android (Kotlin)
> **Data Mode:** Offline-first (Room local database, no backend server)

---

## 1. Ringkasan Produk

| Item | Detail |
|------|--------|
| Nama Aplikasi | Driver Wallet |
| Tipe | Native Android Application |
| Target Platform | Android phone (Min SDK 26 / Android 8.0, Target SDK 36) |
| Pengguna | Single-user (tanpa login/register) |
| Bahasa UI | Bahasa Indonesia |
| Mata Uang | Rupiah (IDR), seluruh nominal bilangan bulat (tanpa desimal) |
| Data Storage | 100% lokal — Room Database (SQLite), DataStore |
| Design System | Material 3 Expressive + Dynamic Color (Material You) |
| Primary Color Seed | Purple `#6750A4` (M3 Purple 40) |

### Elevator Pitch

Aplikasi keuangan pribadi native Android untuk driver ojol online yang ingin:
1. **Mencatat pemasukan & pengeluaran harian** dengan cepat (< 3 detik per transaksi, maks 4 tap)
2. **Melacak & melunasi hutang cicilan** di berbagai platform pinjaman online
3. **Melihat laporan keuangan** harian, mingguan, bulanan, dan custom range
4. **Mengatur budget harian** dan pengeluaran tetap (bulanan & harian)

> **⚠️ OUT OF SCOPE:** Fitur OCR (foto struk) dan Order Import (screenshot order ojol) **tidak termasuk** dalam versi ini.

---

## 2. Target Pengguna (User Persona)

| Atribut | Detail |
|---------|--------|
| Profil | Driver ojol online di Jakarta (Grab/Gojek/Shopee/Maxim) |
| Usia | 20–40 tahun |
| Literasi Digital | Menengah — terbiasa pakai HP tapi bukan orang teknis |
| Konteks Penggunaan | Di jalan, satu tangan, layar kecil, koneksi internet tidak stabil |
| Motivasi Utama | Melunasi hutang dari beberapa platform pinjaman dalam waktu tertentu |
| Pain Point | Input data harus secepat mungkin karena sedang di jalan; butuh tahu "hari ini untung atau rugi" secara instan |

---

## 3. Fitur Utama

### F01 — Dashboard Harian (Beranda)

- **Screen:** `DashboardScreen`
- **Mockup Reference:** `stitch/dashboard_harian/`

#### Konten yang Ditampilkan

| No | Komponen UI | Deskripsi | Referensi Mockup |
|----|-------------|-----------|------------------|
| 1 | **Hero Card — Keuntungan Bersih** | Card besar dengan `rounded-[28px]`, warna `primary-container`. Menampilkan profit hari ini (income − expense − debt_payment) dalam font besar (~42sp). Badge persentase perubahan vs kemarin (contoh: "+8%") | Card ungu atas di mockup |
| 2 | **Income & Expense Cards** | Grid 2 kolom. Card Masuk (icon `arrow_downward`, background `secondary-container`) dan Card Keluar (icon `arrow_upward`, background `danger-container`) | 2 card kecil di bawah hero |
| 3 | **Target Harian** | Section dengan progress bar. Menampilkan: persentase pencapaian, nominal earned vs target, badge status "ON TRACK" / "OFF TRACK" | Section "Target Harian 52%" |
| 4 | **Sisa Budget** | Card dengan info budget terpakai hari ini dan sisa budget. Progress bar horizontal | Card "Sisa Budget Rp 55.000" |
| 5 | **Alert Cicilan Mendekat** | Card warning (background merah muda, border merah) dengan icon `notifications_active`. Muncul jika ada cicilan jatuh tempo ≤ 7 hari | Card merah "Cicilan Mendekat" |
| 6 | **Transaksi Hari Ini** | List transaksi terbaru hari ini. Setiap item: icon kategori (circle), nama, waktu, dan nominal. Link "Lihat Semua" | List di bagian bawah |

#### Behavior

- Pull-to-refresh memuat ulang semua data dari Room
- Onboarding overlay muncul saat pertama kali buka app (flag di DataStore)
- Semua kalkulasi dilakukan di lokal (Room queries + UseCase)

#### Daily Target Formula

```
dailyExpense     = SUM(daily_expenses.amount)
proratedMonthly  = ROUND(SUM(monthly_expenses.amount) / daysInMonth)
dailyDebt        = ROUND(totalDebtRemaining / workingDaysRemaining)
targetAmount     = dailyExpense + proratedMonthly + dailyDebt

// Pada hari libur (rest day): targetAmount = 0, isOnTrack = true
// workingDays = calendar days − rest days (configurable via settings)
```

#### Urgency Level untuk Alert Cicilan

| Level | Kondisi | Warna |
|-------|---------|-------|
| `overdue` | Hari sudah lewat jatuh tempo (days ≤ 0) | Error / Merah |
| `critical` | 1–3 hari sebelum jatuh tempo | Error Container |
| `warning` | 4–7 hari sebelum jatuh tempo | Warning / Orange |
| `normal` | > 7 hari | Tidak tampil di Dashboard |

---

### F02 — Quick-Tap Input (Catat Transaksi Cepat)

- **Screen:** `QuickInputScreen`
- **Mockup Reference:** `stitch/input_transaksi_quick-tap/`

#### Alur Input (Single Screen, Multi-Section)

Berbeda dari web (multi-step), mockup Android menunjukkan **single-screen layout** dengan section:

| No | Section | Deskripsi | Referensi Mockup |
|----|---------|-----------|------------------|
| 1 | **Type Toggle** | Segmented button MASUK (hijau/primary) / KELUAR (merah). Posisi di header, `rounded-full` | Toggle di atas |
| 2 | **Kategori Grid** | Baris icon kategori. Setiap item: icon `rounded-[20px]` 64dp + label. Kategori aktif highlight `primary-container` | 4 icon: Order, Tips, Bonus, Lainnya |
| 3 | **Amount Display** | Teks besar nominal (prefix "Rp"), `inputMode = numeric`, text-align right | "Rp 15.000" besar di tengah |
| 4 | **Note Input** | TextField dengan icon `edit`, placeholder "Tambah catatan...", `rounded-2xl`, maxLength 100 | Field catatan |
| 5 | **Quick Presets** | Baris tombol cepat: +10rb, +20rb, +50rb. `rounded-xl`, outline style | 3 chip tombol |
| 6 | **Number Pad** | Custom number pad 3×4 grid (1–9, ".", 0, backspace). Tombol `rounded-full`, 64dp height | Keypad di bawah |
| 7 | **Simpan Button** | Full-width, `bg-primary`, `rounded-full`, `py-4`, label "SIMPAN" | Tombol ungu bawah |

#### Kategori

**Pemasukan (Income) — 5 kategori:**

| ID | Label | Material Icon |
|----|-------|---------------|
| `order` | Order | `shopping_bag` (filled) |
| `tips` | Tips | `savings` |
| `bonus` | Bonus | `redeem` |
| `insentif` | Insentif | `emoji_events` |
| `lainnya` | Lainnya | `more_horiz` |

**Pengeluaran (Expense) — 8 kategori:**

| ID | Label | Material Icon |
|----|-------|---------------|
| `bbm` | BBM | `local_gas_station` |
| `makan` | Makan | `restaurant` |
| `rokok` | Rokok | `smoking_rooms` |
| `pulsa` | Pulsa | `phone_android` |
| `rt` | RT | `home` |
| `parkir` | Parkir | `local_parking` |
| `service` | Service | `build` |
| `lainnya` | Lainnya | `more_horiz` |

> **Catatan Mockup:** Mockup menampilkan 4 kategori income (Order, Tips, Bonus, Lainnya) dengan tombol "Lihat Semua" untuk expand. Implementasi harus support scroll/expand untuk menampilkan semua kategori.

#### Target UX

- Selesai dalam **< 3 detik**, maksimal **4 tap**, **0 ketik** (jika pakai preset amount)
- Setelah simpan: Snackbar "Transaksi tersimpan", navigate back ke Dashboard
- Tombol SIMPAN disabled jika nominal = 0
- Saat saving: label berubah "⏳ Menyimpan..." dan disabled (prevent double tap)

#### Data yang Disimpan

```kotlin
Transaction(
    id = UUID.randomUUID().toString(),
    createdAt = nowJakarta().toIsoString(),  // ISO 8601 + WIB
    type = "income" | "expense",
    amount = 15000,        // Integer Rupiah
    category = "order",    // dari kategori terpilih
    note = "",             // opsional, max 100 char
    source = "manual",     // selalu "manual" (tidak ada OCR)
    debtId = null,
    isDeleted = false,
)
```

---

### F05 — Kelola Hutang (Debt Management)

- **Screen:** `DebtListScreen`, `DebtFormScreen`
- **Mockup Reference:** `stitch/kelola_hutang/`

#### Layout Utama

| No | Komponen | Deskripsi | Referensi Mockup |
|----|----------|-----------|------------------|
| 1 | **Header** | Title "Kelola Hutang" + tombol Add (`+`) di kanan atas, `rounded-full bg-secondary-container` | Header mockup |
| 2 | **Hero Card Total** | Card `bg-primary rounded-[2rem]`. Total Sisa Hutang dalam font besar. Warning badge jika ada jatuh tempo minggu ini. Background: decorative icon `account_balance_wallet` semi-transparent | Card ungu besar |
| 3 | **Daftar Pinjaman** | Section header "Daftar Pinjaman" + tombol "Lihat Riwayat" | Section header |
| 4 | **Debt Card** | Per-hutang card `rounded-[1.5rem]`. Berisi: platform icon + nama + badge status (Aktif/Lunas%), sisa hutang, gradient progress bar, info cicilan "Cicilan X dari Y · Z% Lunas", info jatuh tempo + bunga, tombol Detail & Bayar | Card per pinjaman |

#### Debt Card Detail

Setiap card hutang menampilkan:
- **Header:** Platform icon (rounded-2xl, warna berbeda per platform) + nama platform + badge status
- **Nominal:** "Sisa Hutang" label + nominal besar
- **Progress:** Gradient progress bar (linear-gradient blue → purple) + "Cicilan X dari Y" + "Z% Lunas"
- **Info Due:** Icon `calendar_clock` + tanggal jatuh tempo + info bunga
- **Actions:** 2 tombol — "Detail" (text button) + "Bayar" (filled button `bg-primary rounded-full`)

#### Fitur CRUD

| Action | Deskripsi |
|--------|-----------|
| **Tambah** | Tombol `+` di header → navigate ke `DebtFormScreen`. Form: platform, nominal total, cicilan/bulan, jumlah cicilan, tanggal jatuh tempo (due_day 1–31), tipe denda, rate denda, tipe hutang, catatan |
| **Edit** | Dari tombol "Detail" → `DebtFormScreen` pre-filled |
| **Hapus** | Dialog konfirmasi → soft delete (`isDeleted = true`) |
| **Bayar Cicilan** | Dialog pembayaran → pilih jadwal unpaid → konfirmasi nominal → **atomic Room @Transaction**: update debts remaining, update schedule status, insert debt_payment transaction |

#### Platform Icons (dari Mockup)

| Platform | Icon | Background Color |
|----------|------|-----------------|
| Shopee Pinjam | `shopping_bag` | Orange-100 |
| GoPay Later | `account_balance_wallet` | Blue-100 |
| Kredivo | `credit_card` | Orange-100 |
| SeaBank | `account_balance` | Green-100 |
| Lainnya | `payments` | Grey-100 |

#### Dark Mode Support

Mockup `kelola_hutang` menunjukkan **dark mode support eksplisit**:
- Background: `#1C1B1F` (dark surface)
- Card: `bg-surface-dark` (`#2a2830`)
- Text: white / gray-400
- Badge: dark variant backgrounds
- Progress bar: tetap gradient

---

### F06 — Laporan Keuangan (Report)

- **Screen:** `ReportScreen`
- **Mockup Reference:** `stitch/laporan_mingguan/`

#### Tab Navigation

3 tab menggunakan segmented button `rounded-full`:

| Tab | Label | Endpoint Data (Room Query) |
|-----|-------|---------------------------|
| 1 | **Mingguan** (default) | Transaksi per hari dalam minggu (Sen–Min) |
| 2 | **Bulanan** | Ringkasan per bulan, breakdown per kategori |
| 3 | **Custom** | Date range picker → ringkasan periode |

#### Layout Mingguan (dari Mockup)

| No | Komponen | Deskripsi |
|----|----------|-----------|
| 1 | **Week Navigator** | Chevron left/right + "Minggu Ini" label + date range badge (contoh: "14 Okt – 20 Okt") |
| 2 | **Bar Chart** | Dual-bar per hari (7 hari). Bar income = `primary` (purple), bar expense = `primary-container` (light purple). Hari aktif/terpilih: bar lebih tebal + shadow + label bold. Height ~192dp |
| 3 | **Summary Cards** | 2-col grid: Pemasukan (icon `arrow_downward`, green) + Pengeluaran (icon `arrow_upward`, red) |
| 4 | **Total Keuntungan** | Hero card full-width, `bg-primary-container rounded-[28px]`, nominal besar. Decorative circles di background |
| 5 | **Detail per Hari** | List item per hari: date circle (rounded-2xl), nama hari, jumlah transaksi badge, profit nominal, breakdown "In: Xrb • Out: Yrb" |

#### Fitur Export CSV

- Tombol export di toolbar (icon `more_vert` menu)
- Generate CSV file berisi seluruh transaksi dalam range
- Share via Android `ShareSheet` + `FileProvider`
- Format CSV: `id, tanggal, tipe, nominal, kategori, catatan`

---

### F07 — Pengaturan (Settings)

- **Screen:** `SettingsScreen`
- **Mockup Reference:** `stitch/pengaturan_aplikasi/`

#### Section Layout

| No | Section | Komponen | Referensi Mockup |
|----|---------|----------|------------------|
| 1 | **Preferensi Umum** | Toggle Mode Gelap (Switch M3) | Toggle dark mode |
| 2 | **Budget Harian** | 4 FilledTextField (M3 style): BBM, Makan, Rokok, Pulsa/Data. Masing-masing input angka dengan suffix "Rp" | Input fields budget |
| 3 | **Target & Perencanaan** | Item "Target tanggal lunas" — tap untuk edit, tampilkan DatePicker. Menampilkan tanggal saat ini (contoh: "31 Des 2024") | Row dengan icon `event` |
| 4 | **Pengeluaran Tetap Bulanan** | List item (icon circle + nama + deskripsi + nominal). Tombol "Tambah" di header section | List Rumah Tangga, SPP Anak |
| 5 | **Pengeluaran Tetap Harian** | Sama seperti bulanan, untuk expense harian | List Parkir |
| 6 | **Simpan Button** | `ExtendedFloatingActionButton` atau button `bg-primary-container rounded-[16px]` dengan icon `save` + label "Simpan Perubahan" | Tombol di bawah |

#### M3 FilledTextField Style (dari Mockup)

```
┌──────────────────────────────────┐
│ BBM                          Rp  │  ← Label atas (saat focus/filled)
│ 30000                            │  ← Value
└──────────────────────────────────┘
  ↑ bottom border: 1px outline (default), 2px primary (focus)
  ↑ background: surfaceVariant (F7F2FA)
  ↑ border-radius: top-only (4dp top, 0dp bottom) — M3 Filled style
```

#### Settings Data (Key-Value)

| Key | Type | Contoh | Deskripsi |
|-----|------|--------|-----------|
| `debt_target_date` | String (ISO date) | "2024-12-31" | Target tanggal lunas semua hutang |
| `rest_days` | String (comma-separated) | "0,6" | Hari libur: 0=Minggu, 6=Sabtu |

---

### F08 — Onboarding

- **Screen:** `OnboardingOverlay` (full-screen dialog/overlay composable)
- Muncul **sekali** saat pertama kali buka aplikasi
- Flag `has_seen_onboarding: Boolean` disimpan di DataStore
- Konten: 3–4 halaman pengenalan fitur utama (Dashboard, Input, Hutang, Laporan)
- Swipe horizontal (HorizontalPager) + dot indicator + tombol "Mulai"

---

### F09 — Bottom Navigation

- **Component:** Material 3 `NavigationBar`
- **5 Menu Item** (konsisten di semua mockup):

| No | Label | Icon (Outlined) | Icon (Active/Filled) | Route |
|----|-------|-----------------|---------------------|-------|
| 1 | Beranda | `home` | `home` (filled) | `DashboardRoute` |
| 2 | Input | `add_circle` | `add_circle` (filled) | `QuickInputRoute` |
| 3 | Hutang | `account_balance_wallet` | `account_balance_wallet` (filled) | `DebtListRoute` |
| 4 | Laporan | `bar_chart` / `analytics` | filled variant | `ReportRoute` |
| 5 | Pengaturan | `settings` | `settings` (filled) | `SettingsRoute` |

#### Badge Notifikasi

- Item "Hutang" menunjukkan **red badge dot** jika ada cicilan overdue/critical (≤ 3 hari)
- Badge: `w-10dp h-10dp rounded-full bg-error` diposisikan top-right icon

---

### F10 — Snackbar Notification

- Material 3 `SnackbarHost` di dalam `Scaffold`
- `SnackbarHostState` di-hoist di level App/Navigation scaffold
- Digunakan untuk: "Transaksi tersimpan", "Hutang berhasil dibayar", error messages
- Duration: `SnackbarDuration.Short` (default)

---

## 4. Non-Functional Requirements

| Aspek | Requirement |
|-------|-------------|
| **Kecepatan Input** | Transaksi bisa dicatat dalam < 3 detik |
| **Offline** | 100% offline — semua data di Room Database lokal |
| **Dark Mode** | Wajib, ikuti system theme via `isSystemInDarkTheme()` + manual toggle di Settings |
| **Dynamic Color** | Material You via `dynamicColorScheme()` pada Android 12+ (fallback ke purple theme) |
| **Adaptive Layout** | `WindowSizeClass` untuk phone/tablet (phone sebagai target utama) |
| **Accessibility** | `contentDescription` pada semua icon/image, minimum touch target 48dp, TalkBack compatible |
| **Single User** | Tidak ada fitur login/register/multi-user |
| **Bahasa** | UI dalam Bahasa Indonesia |
| **Mata Uang** | Semua nominal `Int` (Rupiah), tanpa desimal |
| **Tanggal/Waktu** | `java.time.*` API (LocalDate, ZonedDateTime), timezone selalu `Asia/Jakarta` |
| **Soft Delete** | Data tidak pernah benar-benar dihapus, hanya ditandai `isDeleted = true` |
| **Security** | Tidak ada hardcoded secret, gunakan `EncryptedDataStore` untuk data sensitif |
| **Min SDK** | 26 (Android 8.0 Oreo) |
| **Target SDK** | 36 |

---

## 5. Out of Scope (TIDAK Termasuk)

| Fitur | Alasan |
|-------|--------|
| OCR Foto Struk | Dihilangkan dari versi ini |
| Order Import via Screenshot | Dihilangkan dari versi ini |
| Multi-user / Authentication | Single-user app |
| Push Notification | Hanya alert visual di dalam app |
| Cross-device Sync / Cloud Backup | Offline-first, data hanya di device |
| Multi-bahasa | Hanya Bahasa Indonesia |
| Multi-currency | Hanya Rupiah (IDR) |
| Integrasi langsung API Grab/Gojek | Tidak ada integrasi pihak ketiga |

---

## 6. Screen Map & Navigation

```
┌──────────────────────────────────────────────────────────────┐
│                     BOTTOM NAVIGATION BAR                     │
├────────────┬────────────┬────────────┬───────────┬───────────┤
│  🏠 Beranda │  ✏️ Input   │  💳 Hutang  │ 📊 Laporan │ ⚙️ Setting │
│  Dashboard  │ QuickInput │  DebtList  │  Report   │ Settings  │
└────────────┴────────────┴────────────┴───────────┴───────────┘

Sub-navigation:
  DebtList → DebtForm (tambah/edit hutang)
  DebtList → PayDialog (bayar cicilan, modal dialog)
  Settings → DatePicker (target tanggal lunas)
  Settings → ExpenseForm (tambah/edit expense tetap)
  Report → Export CSV (ShareSheet, system UI)

First launch:
  App Start → OnboardingOverlay → Dashboard
```

---

## 7. Design System Reference

Berdasarkan analisis mockup di folder `stitch/`, berikut design token yang digunakan:

### Color Palette (M3 Purple Theme)

| Token | Light Value | Usage |
|-------|-------------|-------|
| `primary` | `#6750A4` | Tombol utama, accent, progress bar, active nav |
| `primary-container` | `#EADDFF` | Hero card background, badge, selected category |
| `on-primary-container` | `#21005D` | Text di atas primary-container |
| `secondary` | `#625B71` | Label secondary |
| `secondary-container` | `#E8DEF8` | Nav indicator, category default bg |
| `surface` | `#FFFBFE` | Background utama |
| `surface-variant` | `#E7E0EC` | Card border, input background, divider |
| `error` | `#B3261E` | Expense color, overdue alert, badge dot |
| `error-container` | `#F9DEDC` | Alert background cicilan mendekat |

> **Catatan untuk AI Builder:** Gunakan `MaterialTheme.colorScheme.*` tokens — **JANGAN** hardcode warna. Dynamic Color akan otomatis menyesuaikan jika device support Material You (Android 12+). Warna di atas adalah fallback theme.

### Typography Scale

| Usage | Style |
|-------|-------|
| Hero nominal (profit, total hutang) | `displaySmall` atau custom ~42sp |
| Amount display (input) | Custom ~80sp, `fontWeight = Normal` |
| Card title | `titleMedium` (16sp, medium) |
| Section header | `titleSmall` (14sp, medium, tracking wide) |
| Body text | `bodyLarge` (16sp) |
| Label / caption | `labelSmall` (11sp) |
| Button | `labelLarge` (14sp, medium) |

### Shape Scale

| Component | Radius |
|-----------|--------|
| Hero card | `28dp` (extra-large) |
| Standard card | `16dp` (large) |
| Debt card | `24dp` |
| Button (filled) | Full (`50%` / `CircleShape`) |
| Category icon | `20dp` (medium) |
| Input field (filled) | `4dp top, 0dp bottom` (M3 filled style) |
| Bottom sheet | `28dp` top corners |

### Elevation & Shadow

| Level | Usage |
|-------|-------|
| Elevation 0 | Flat cards with border |
| Elevation 1 | `0px 1px 2px rgba(0,0,0,0.3), 0px 1px 3px 1px rgba(0,0,0,0.15)` — Hero cards, buttons |
| Elevation 2 | `0px 1px 2px rgba(0,0,0,0.3), 0px 2px 6px 2px rgba(0,0,0,0.15)` — Hover/pressed state |

### Material Icons

Semua icon menggunakan **Material Symbols Outlined** (weight 400, grade 0, optical size 24).
Active/selected state menggunakan **filled variant** (FILL = 1).

---

*Dokumen ini adalah bagian 1 dari 5. Lanjut ke [AD_02_User_Stories.md](./AD_02_User_Stories.md) untuk user stories & flow detail.*
