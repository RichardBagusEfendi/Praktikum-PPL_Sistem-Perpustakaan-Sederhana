package com.praktikum.testing.service;

import com.praktikum.testing.model.Anggota;
import com.praktikum.testing.model.Buku;
import com.praktikum.testing.repository.RepositoriBuku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Service Perpustakaan")
public class ServicePerpustakaanTest {

    @Mock
    private RepositoriBuku mockRepositoriBuku;

    @Mock
    private KalkulatorDenda mockKalkulatorDenda;

    private ServicePerpustakaan servicePerpustakaan;
    private Anggota anggotaTest;

    @BeforeEach
    void setUp() {
        servicePerpustakaan = new ServicePerpustakaan(mockRepositoriBuku, mockKalkulatorDenda);

        // Buat Anggota baru di setiap test
        anggotaTest = new Anggota("A001", "John Student", "john@student.ac.id",
                "081234567890", Anggota.TipeAnggota.MAHASISWA);
    }

    // --- Utility Method untuk membuat Buku baru agar tidak ada state bocor ---
    private Buku buatBukuTesting(String isbn, int jumlahTotal, int jumlahTersedia) {
        Buku buku = new Buku(isbn, "Pemrograman Java", "John Doe", jumlahTotal, 150000.0);
        buku.setJumlahTersedia(jumlahTersedia);
        return buku;
    }

    // --- Test Method Perbaikan/Penambahan ---

    @Test
    @DisplayName("Tambah buku berhasil ketika data valid dan buku belum ada")
    void testTambahBukuBerhasil() {
        Buku bukuTest = buatBukuTesting("1234567890", 5, 5);
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.empty());
        when(mockRepositoriBuku.simpan(bukuTest)).thenReturn(true);

        boolean hasil = servicePerpustakaan.tambahBuku(bukuTest);

        assertTrue(hasil, "Harus berhasil menambah buku");
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku).simpan(bukuTest);
    }

    @Test
    @DisplayName("Tambah buku gagal ketika buku sudah ada")
    void testTambahBukuGagalBukuSudahAda() {
        Buku bukuTest = buatBukuTesting("1234567890", 5, 5);
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        boolean hasil = servicePerpustakaan.tambahBuku(bukuTest);

        assertFalse(hasil, "Tidak boleh menambah buku yang sudah ada");
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku, never()).simpan(any(Buku.class));
    }

    @Test
    @DisplayName("Tambah buku gagal ketika data tidak valid")
    void testTambahBukuGagalDataTidakValid() {
        Buku bukuTidakValid = new Buku("123", "", "", 0, -100.0);

        boolean hasil = servicePerpustakaan.tambahBuku(bukuTidakValid);

        assertFalse(hasil, "Tidak boleh menambah buku dengan data tidak valid");
        verifyNoInteractions(mockRepositoriBuku);
    }

    @Test
    @DisplayName("Hapus buku berhasil ketika tidak ada yang dipinjam")
    void testHapusBukuBerhasil() {
        Buku bukuTest = buatBukuTesting("1234567890", 5, 5); // Semua salinan tersedia
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));
        when(mockRepositoriBuku.hapus("1234567890")).thenReturn(true);

        boolean hasil = servicePerpustakaan.hapusBuku("1234567890");

        assertTrue(hasil, "Harus berhasil menghapus buku");
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku).hapus("1234567890");
    }

    @Test
    @DisplayName("Hapus buku gagal jika ISBN tidak valid")
    void testHapusBukuGagalInvalidISBN() {
        boolean hasil = servicePerpustakaan.hapusBuku("123");
        assertFalse(hasil);
        verifyNoInteractions(mockRepositoriBuku);
    }

    @Test
    @DisplayName("Hapus buku gagal ketika ada yang dipinjam")
    void testHapusBukuGagalAdaYangDipinjam() {
        Buku bukuTest = buatBukuTesting("1234567890", 5, 2); // Ada yang dipinjam (5 total - 2 tersedia = 3 dipinjam)
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        boolean hasil = servicePerpustakaan.hapusBuku("1234567890");

        assertFalse(hasil, "Tidak boleh menghapus buku yang sedang dipinjam");
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku, never()).hapus(anyString());
    }

    @Test
    @DisplayName("Cari buku by ISBN berhasil")
    void testCariBukuByIsbnBerhasil() {
        Buku bukuTest = buatBukuTesting("1234567890", 5, 5);
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        Optional<Buku> hasil = servicePerpustakaan.cariBukuByIsbn("1234567890");

        assertTrue(hasil.isPresent(), "Harus menemukan buku");
        assertEquals("Pemrograman Java", hasil.get().getJudul());
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
    }

    @Test
    @DisplayName("Cari buku by ISBN gagal jika ISBN tidak valid")
    void testCariBukuByIsbnGagalInvalid() {
        // Test ISBN yang tidak valid (kurang dari 10 digit)
        Optional<Buku> hasil = servicePerpustakaan.cariBukuByIsbn("123");
        assertFalse(hasil.isPresent());
        verifyNoInteractions(mockRepositoriBuku);
    }

    @Test
    @DisplayName("Cari buku by judul berhasil")
    void testCariBukuByJudul() {
        Buku bukuTest = buatBukuTesting("1234567890", 5, 5);
        List<Buku> daftarBuku = Arrays.asList(bukuTest);
        when(mockRepositoriBuku.cariByJudul("Java")).thenReturn(daftarBuku);

        List<Buku> hasil = servicePerpustakaan.cariBukuByJudul("Java");

        assertEquals(1, hasil.size());
        assertEquals("Pemrograman Java", hasil.get(0).getJudul());
        verify(mockRepositoriBuku).cariByJudul("Java");
    }

    @Test
    @DisplayName("Cari buku by judul dengan query kosong/null harus mengembalikan empty list")
    void testCariBukuByJudulInvalid() {
        // Menutupi cabang query tidak valid di ValidationUtils (Line Coverage hilang)
        when(mockRepositoriBuku.cariByJudul("")).thenReturn(Collections.emptyList());

        List<Buku> hasilKosong = servicePerpustakaan.cariBukuByJudul("");
        List<Buku> hasilNull = servicePerpustakaan.cariBukuByJudul(null); // Validasi seharusnya dilakukan di repo/util

        assertTrue(hasilKosong.isEmpty());
        assertTrue(hasilNull.isEmpty());
        verify(mockRepositoriBuku).cariByJudul(null); // Memverifikasi panggilan untuk argumen null (jika repo Anda menanganinya)
    }

    @Test
    @DisplayName("Cari buku by pengarang dengan query kosong/null harus mengembalikan empty list")
    void testCariBukuByPengarangInvalid() {
        // Menutupi cabang query tidak valid (Method Coverage hilang)
        when(mockRepositoriBuku.cariByPengarang("")).thenReturn(Collections.emptyList());

        List<Buku> hasilKosong = servicePerpustakaan.cariBukuByPengarang("");
        List<Buku> hasilNull = servicePerpustakaan.cariBukuByPengarang(null);

        assertTrue(hasilKosong.isEmpty());
        assertTrue(hasilNull.isEmpty());
        verify(mockRepositoriBuku).cariByPengarang(null);
    }

    @Test
    @DisplayName("Pinjam buku berhasil ketika semua kondisi terpenuhi")
    void testPinjamBukuBerhasil() {
        Anggota anggota = anggotaTest;
        Buku bukuTest = buatBukuTesting("1234567890", 5, 3); // Tersedia 3

        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));
        when(mockRepositoriBuku.updateJumlahTersedia("1234567890", 2)).thenReturn(true); // 3-1=2

        boolean hasil = servicePerpustakaan.pinjamBuku("1234567890", anggota);

        assertTrue(hasil, "Harus berhasil meminjam buku");
        assertTrue(anggota.getIdBukuDipinjam().contains("1234567890"));
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku).updateJumlahTersedia("1234567890", 2);
    }

    @Test
    @DisplayName("Pinjam buku gagal ketika buku tidak tersedia")
    void testPinjamBukuGagalTidakTersedia() {
        Anggota anggota = anggotaTest;
        Buku bukuTest = buatBukuTesting("1234567890", 5, 0); // Tersedia 0

        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        boolean hasil = servicePerpustakaan.pinjamBuku("1234567890", anggota);

        assertFalse(hasil, "Tidak boleh meminjam buku yang tidak tersedia");
        assertFalse(anggota.getIdBukuDipinjam().contains("1234567890"));
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku, never()).updateJumlahTersedia(anyString(), anyInt());
    }

    @Test
    @DisplayName("Pinjam buku gagal ketika anggota tidak aktif")
    void testPinjamBukuGagalAnggotaTidakAktif() {
        Anggota anggota = anggotaTest;
        anggota.setAktif(false);

        boolean hasil = servicePerpustakaan.pinjamBuku("1234567890", anggota);

        assertFalse(hasil, "Anggota tidak aktif tidak boleh meminjam buku");
        verifyNoInteractions(mockRepositoriBuku);
    }

    @Test
    @DisplayName("Pinjam buku gagal ketika batas pinjam tercapai")
    void testPinjamBukuGagalBatasPinjamTercapai() {
        Anggota anggota = anggotaTest;
        // Arrange - Mahasiswa sudah pinjam 3 buku (batas maksimal)
        anggota.tambahBukuDipinjam("1111111111");
        anggota.tambahBukuDipinjam("2222222222");
        anggota.tambahBukuDipinjam("3333333333");

        // Act
        boolean hasil = servicePerpustakaan.pinjamBuku("1234567890", anggota);

        assertFalse(hasil, "Tidak boleh meminjam ketika batas pinjam tercapai");
        verifyNoInteractions(mockRepositoriBuku);
    }

    @Test
    @DisplayName("Kembalikan buku berhasil")
    void testKembalikanBukuBerhasil() {
        Anggota anggota = anggotaTest;
        Buku bukuTest = buatBukuTesting("1234567890", 5, 2); // Tersedia 2 sebelum kembali

        anggota.tambahBukuDipinjam("1234567890");
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));
        when(mockRepositoriBuku.updateJumlahTersedia("1234567890", 3)).thenReturn(true); // 2+1=3

        boolean hasil = servicePerpustakaan.kembalikanBuku("1234567890", anggota);

        assertTrue(hasil, "Harus berhasil mengembalikan buku");
        assertFalse(anggota.getIdBukuDipinjam().contains("1234567890"));
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku).updateJumlahTersedia("1234567890", 3);
    }

    @Test
    @DisplayName("Kembalikan buku gagal ketika anggota tidak meminjam buku tersebut")
    void testKembalikanBukuGagalTidakMeminjam() {
        Anggota anggota = anggotaTest;

        boolean hasil = servicePerpustakaan.kembalikanBuku("1234567890", anggota);

        assertFalse(hasil, "Tidak boleh mengembalikan buku yang tidak dipinjam");
        verifyNoInteractions(mockRepositoriBuku);
    }

    @Test
    @DisplayName("Cek ketersediaan buku")
    void testBukuTersedia() {
        Buku bukuTestTersedia = buatBukuTesting("1234567890", 5, 1);
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTestTersedia));
        assertTrue(servicePerpustakaan.bukuTersedia("1234567890"));

        Buku bukuTestTidakTersedia = buatBukuTesting("1234567890", 5, 0);
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTestTidakTersedia));
        assertFalse(servicePerpustakaan.bukuTersedia("1234567890"));
    }

    @Test
    @DisplayName("Get jumlah tersedia")
    void testGetJumlahTersedia() {
        Buku bukuTest = buatBukuTesting("1234567890", 5, 3);
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        int jumlah = servicePerpustakaan.getJumlahTersedia("1234567890");

        assertEquals(3, jumlah);
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
    }

    @Test
    @DisplayName("Get jumlah tersedia untuk buku yang tidak ada")
    void testGetJumlahTersediaBukuTidakAda() {
        when(mockRepositoriBuku.cariByIsbn("9999999999")).thenReturn(Optional.empty());

        int jumlah = servicePerpustakaan.getJumlahTersedia("9999999999");

        assertEquals(0, jumlah);
        verify(mockRepositoriBuku).cariByIsbn("9999999999");
    }
}