package com.praktikum.testing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Model Peminjaman")
class PeminjamanTest {

    private final LocalDate HARI_INI = LocalDate.now();
    private final LocalDate JATUH_TEMPO_KEMARIN = HARI_INI.minusDays(1);
    private final LocalDate JATUH_TEMPO_BESOK = HARI_INI.plusDays(1);
    private final LocalDate TANGGAL_PINJAM = HARI_INI.minusDays(7);

    @Test
    @DisplayName("Test Constructor dan Getters/Setters")
    void testConstructorDanAksesors() {
        // Test Constructor Penuh
        Peminjaman pinjam = new Peminjaman("P001", "A001", "123", TANGGAL_PINJAM, JATUH_TEMPO_BESOK);

        assertEquals("P001", pinjam.getIdPeminjaman());
        assertEquals("A001", pinjam.getIdAnggota());
        assertEquals("123", pinjam.getIsbnBuku());
        assertEquals(TANGGAL_PINJAM, pinjam.getTanggalPinjam());
        assertEquals(JATUH_TEMPO_BESOK, pinjam.getTanggalJatuhTempo());
        assertFalse(pinjam.isSudahDikembalikan());

        // Test Constructor Kosong dan Setters (Menutup method yang hilang)
        Peminjaman pinjamKosong = new Peminjaman();

        pinjamKosong.setIdPeminjaman("P002");
        assertEquals("P002", pinjamKosong.getIdPeminjaman());

        pinjamKosong.setIdAnggota("A002");
        assertEquals("A002", pinjamKosong.getIdAnggota());

        pinjamKosong.setIsbnBuku("456");
        assertEquals("456", pinjamKosong.getIsbnBuku());

        pinjamKosong.setTanggalPinjam(HARI_INI.minusDays(10));
        assertEquals(HARI_INI.minusDays(10), pinjamKosong.getTanggalPinjam());

        pinjamKosong.setTanggalJatuhTempo(JATUH_TEMPO_KEMARIN);
        pinjamKosong.setTanggalKembali(HARI_INI);
        pinjamKosong.setSudahDikembalikan(true);

        assertTrue(pinjamKosong.isSudahDikembalikan());
    }

    @Test
    @DisplayName("Test isTerlambat() (Branch Coverage)")
    void testIsTerlambat() {
        // Kasus 1: Belum dikembalikan & Terlambat (isAfter = true)
        Peminjaman belumKembaliTerlambat = new Peminjaman("P1", "A1", "1", TANGGAL_PINJAM, JATUH_TEMPO_KEMARIN);
        assertTrue(belumKembaliTerlambat.isTerlambat(), "Belum kembali & J.Tempo kemarin harus terlambat.");

        // Kasus 2: Belum dikembalikan & Tepat Waktu (isAfter = false)
        Peminjaman belumKembaliTepatWaktu = new Peminjaman("P2", "A2", "2", TANGGAL_PINJAM, JATUH_TEMPO_BESOK);
        assertFalse(belumKembaliTepatWaktu.isTerlambat(), "Belum kembali & J.Tempo besok tidak terlambat.");

        // Kasus 3: Sudah dikembalikan & Terlambat (isAfter = true)
        Peminjaman sudahKembaliTerlambat = new Peminjaman("P3", "A3", "3", TANGGAL_PINJAM, HARI_INI.minusDays(3));
        sudahKembaliTerlambat.setSudahDikembalikan(true);
        sudahKembaliTerlambat.setTanggalKembali(HARI_INI); // Kembali hari ini
        assertTrue(sudahKembaliTerlambat.isTerlambat(), "Sudah kembali & Tgl. Kembali > J.Tempo harus terlambat.");

        // Kasus 4: Sudah dikembalikan & Tepat Waktu (isAfter = false)
        Peminjaman sudahKembaliTepatWaktu = new Peminjaman("P4", "A4", "4", TANGGAL_PINJAM, HARI_INI);
        sudahKembaliTepatWaktu.setSudahDikembalikan(true);
        sudahKembaliTepatWaktu.setTanggalKembali(HARI_INI); // Kembali tepat waktu
        assertFalse(sudahKembaliTepatWaktu.isTerlambat(), "Sudah kembali & Tgl. Kembali = J.Tempo tidak terlambat.");
    }

    @Test
    @DisplayName("Test getHariTerlambat() (Full Branch Coverage)")
    void testGetHariTerlambat() {
        // Kasus 1: Sudah dikembalikan dan Terlambat (3 hari)
        Peminjaman p1 = new Peminjaman("P1", "A1", "1", TANGGAL_PINJAM, HARI_INI.minusDays(3));
        p1.setSudahDikembalikan(true);
        p1.setTanggalKembali(HARI_INI); // Kembali 3 hari setelah J. Tempo
        assertEquals(3, p1.getHariTerlambat(), "Sudah kembali, harus 3 hari");

        // Kasus 2: Sudah dikembalikan dan Tepat Waktu (atau lebih cepat)
        Peminjaman p2 = new Peminjaman("P2", "A2", "2", TANGGAL_PINJAM, HARI_INI);
        p2.setSudahDikembalikan(true);
        p2.setTanggalKembali(HARI_INI.minusDays(1)); // Kembali lebih cepat
        assertEquals(0, p2.getHariTerlambat(), "Sudah kembali, tepat waktu/lebih cepat harus 0");

        // Kasus 3: Belum dikembalikan dan Terlambat (1 hari)
        Peminjaman p3 = new Peminjaman("P3", "A3", "3", TANGGAL_PINJAM, JATUH_TEMPO_KEMARIN);
        assertEquals(1, p3.getHariTerlambat(), "Belum kembali, harus 1 hari");

        // Kasus 4: Belum dikembalikan dan Tepat Waktu (atau lebih cepat)
        Peminjaman p4 = new Peminjaman("P4", "A4", "4", TANGGAL_PINJAM, JATUH_TEMPO_BESOK);
        assertEquals(0, p4.getHariTerlambat(), "Belum kembali, tepat waktu/lebih cepat harus 0");
    }

    @Test
    @DisplayName("Test getDurasiPeminjaman()")
    void testGetDurasiPeminjaman() {
        // Kasus 1: Belum dikembalikan (Durasi = TanggalPinjam hingga Hari Ini)
        Peminjaman p1 = new Peminjaman("P1", "A1", "1", HARI_INI.minusDays(7), JATUH_TEMPO_KEMARIN);
        assertEquals(7, p1.getDurasiPeminjaman(), "Durasi pinjam (belum kembali) harus 7 hari");

        // Kasus 2: Sudah dikembalikan (Durasi = TanggalPinjam hingga Tanggal Kembali)
        Peminjaman p2 = new Peminjaman("P2", "A2", "2", HARI_INI.minusDays(10), JATUH_TEMPO_KEMARIN);
        p2.setSudahDikembalikan(true);
        p2.setTanggalKembali(HARI_INI.minusDays(2)); // Kembali 8 hari setelah pinjam
        assertEquals(8, p2.getDurasiPeminjaman(), "Durasi pinjam (sudah kembali) harus 8 hari");
    }
}