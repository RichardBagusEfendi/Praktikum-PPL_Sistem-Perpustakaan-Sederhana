package com.praktikum.testing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Model Anggota")
class AnggotaTest {

    private final String ISBN_1 = "1111111111";
    private final String ISBN_2 = "2222222222";

    @Test
    @DisplayName("Test Constructor dan Getters/Setters")
    void testConstructorDanAksesors() {
        // Test Constructor Penuh
        Anggota anggota = new Anggota("A001", "Nama Test", "test@mail.com", "081", Anggota.TipeAnggota.DOSEN);

        assertEquals("A001", anggota.getIdAnggota());
        assertEquals("Nama Test", anggota.getNama());
        assertEquals("test@mail.com", anggota.getEmail());
        assertEquals("081", anggota.getTelepon());
        assertEquals(Anggota.TipeAnggota.DOSEN, anggota.getTipeAnggota());
        assertTrue(anggota.isAktif());
        assertTrue(anggota.getIdBukuDipinjam().isEmpty());
        assertEquals(0, anggota.getJumlahBukuDipinjam());

        // Test Constructor Kosong dan Setters
        Anggota anggotaKosong = new Anggota();
        assertNotNull(anggotaKosong.getIdBukuDipinjam());
        assertTrue(anggotaKosong.isAktif());

        anggotaKosong.setAktif(false);
        assertFalse(anggotaKosong.isAktif());

        anggotaKosong.setIdAnggota("A002");
        assertEquals("A002", anggotaKosong.getIdAnggota());

        anggotaKosong.setTipeAnggota(Anggota.TipeAnggota.MAHASISWA);
        assertEquals(Anggota.TipeAnggota.MAHASISWA, anggotaKosong.getTipeAnggota());

        // Test Setters List
        List<String> pinjamanBaru = Arrays.asList(ISBN_1);
        anggotaKosong.setIdBukuDipinjam(pinjamanBaru);
        assertEquals(1, anggotaKosong.getJumlahBukuDipinjam());
        // Verifikasi bahwa getIdBukuDipinjam mengembalikan salinan
        List<String> listPinjaman = anggotaKosong.getIdBukuDipinjam();
        assertNotSame(listPinjaman, anggotaKosong.getIdBukuDipinjam(), "Harus mengembalikan salinan list");
    }

    @Test
    @DisplayName("Test Batas Pinjam (Switch Case Coverage)")
    void testGetBatasPinjam() {
        Anggota mhs = new Anggota("A", "N", "E", "T", Anggota.TipeAnggota.MAHASISWA);
        Anggota dosen = new Anggota("A", "N", "E", "T", Anggota.TipeAnggota.DOSEN);
        Anggota umum = new Anggota("A", "N", "E", "T", Anggota.TipeAnggota.UMUM);

        assertEquals(5, mhs.getBatasPinjam(), "Mahasiswa harus 5");
        assertEquals(10, dosen.getBatasPinjam(), "Dosen harus 10");
        assertEquals(3, umum.getBatasPinjam(), "Umum harus 3");

        // Test Default (set TipeAnggota ke null untuk memicu default)
        Anggota anggotaNull = new Anggota();
        anggotaNull.setTipeAnggota(null);
        assertEquals(3, anggotaNull.getBatasPinjam(), "Default harus 3");
    }

    @Test
    @DisplayName("Test bolehPinjamLagi() (Branch Coverage)")
    void testBolehPinjamLagi() {
        Anggota anggota = new Anggota("A", "N", "E", "T", Anggota.TipeAnggota.UMUM); // Batas 3

        // 1. Aktif dan belum penuh (TRUE && TRUE)
        assertTrue(anggota.bolehPinjamLagi(), "Harus true (aktif, pinjaman 0/3)");

        // 2. Aktif dan penuh (TRUE && FALSE)
        anggota.tambahBukuDipinjam(ISBN_1);
        anggota.tambahBukuDipinjam(ISBN_2);
        anggota.tambahBukuDipinjam("3333333333");
        assertFalse(anggota.bolehPinjamLagi(), "Harus false (aktif, pinjaman 3/3)");

        // 3. Tidak aktif dan belum penuh (FALSE && TRUE)
        anggota = new Anggota("A", "N", "E", "T", Anggota.TipeAnggota.UMUM);
        anggota.setAktif(false);
        assertFalse(anggota.bolehPinjamLagi(), "Harus false (tidak aktif)");

        // 4. Tidak aktif dan penuh (FALSE && FALSE)
        anggota.tambahBukuDipinjam(ISBN_1);
        anggota.tambahBukuDipinjam(ISBN_2);
        anggota.tambahBukuDipinjam("3333333333");
        assertFalse(anggota.bolehPinjamLagi(), "Harus false (tidak aktif dan penuh)");
    }

    @Test
    @DisplayName("Test tambah dan hapus buku (Branch Coverage)")
    void testTambahDanHapusBuku() {
        Anggota anggota = buatAnggotaTesting();

        // 1. Tambah buku pertama (memenuhi if)
        anggota.tambahBukuDipinjam(ISBN_1);
        assertEquals(1, anggota.getJumlahBukuDipinjam());

        // 2. Coba tambah buku yang sama (Gagal masuk if)
        anggota.tambahBukuDipinjam(ISBN_1);
        assertEquals(1, anggota.getJumlahBukuDipinjam(), "Pinjaman duplikat harus diabaikan");

        // Hapus buku pertama
        anggota.hapusBukuDipinjam(ISBN_1);
        assertEquals(0, anggota.getJumlahBukuDipinjam());
    }

    @Test
    @DisplayName("Test equals, hashCode, dan toString")
    void testEqualsHashCodeToString() {
        Anggota anggota1 = new Anggota("A001", "John", "E", "T", Anggota.TipeAnggota.UMUM);
        Anggota anggota2 = new Anggota("A001", "Beda", "Beda", "Beda", Anggota.TipeAnggota.DOSEN);

        // equals dan hashCode berdasarkan idAnggota
        assertTrue(anggota1.equals(anggota2));
        assertEquals(anggota1.hashCode(), anggota2.hashCode());

        // equals null dan class berbeda
        assertFalse(anggota1.equals(null));
        assertFalse(anggota1.equals(new Object()));

        // Test toString
        anggota1.tambahBukuDipinjam(ISBN_1);
        String expected = "Anggota{idAnggota='A001', nama='John', email='E', telepon='T', tipeAnggota=UMUM, jumlahBukuDipinjam=1, aktif=true}";
        assertTrue(anggota1.toString().contains("idAnggota='A001'")); // Cek partial
    }

    private Anggota buatAnggotaTesting() {
        return new Anggota("A001", "John Student", "john@student.ac.id",
                "081234567890", Anggota.TipeAnggota.MAHASISWA);
    }
}