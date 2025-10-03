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
    private Buku bukuTest;
    private Anggota anggotaTest;

    @BeforeEach
    void setUp() {
        servicePerpustakaan = new ServicePerpustakaan(mockRepositoriBuku, mockKalkulatorDenda);

        // PERBAIKAN: Selalu buat objek baru di setiap test untuk menghindari state bocor (leak)
        bukuTest = new Buku("1234567890", "Pemrograman Java", "John Doe", 5, 150000.0);
        anggotaTest = new Anggota("A001", "John Student", "john@student.ac.id",
                "081234567890", Anggota.TipeAnggota.MAHASISWA);
    }

    @Test
    @DisplayName("Tambah buku berhasil ketika data valid dan buku belum ada")
    void testTambahBukuBerhasil() {
        // Arrange - Mock behavior
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.empty());
        when(mockRepositoriBuku.simpan(bukuTest)).thenReturn(true);

        // Act
        boolean hasil = servicePerpustakaan.tambahBuku(bukuTest);

        // Assert
        assertTrue(hasil, "Harus berhasil menambah buku");
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku).simpan(bukuTest);
    }

    @Test
    @DisplayName("Tambah buku gagal ketika buku sudah ada")
    void testTambahBukuGagalBukuSudahAda() {
        // Arrange
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        // Act
        boolean hasil = servicePerpustakaan.tambahBuku(bukuTest);

        // Assert
        assertFalse(hasil, "Tidak boleh menambah buku yang sudah ada");
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku, never()).simpan(any(Buku.class));
    }

    @Test
    @DisplayName("Tambah buku gagal ketika data tidak valid")
    void testTambahBukuGagalDataTidakValid() {
        // Arrange
        Buku bukuTidakValid = new Buku("123", "", "", 0, -100.0);

        // Act
        boolean hasil = servicePerpustakaan.tambahBuku(bukuTidakValid);

        // Assert
        assertFalse(hasil, "Tidak boleh menambah buku dengan data tidak valid");
        verifyNoInteractions(mockRepositoriBuku);
    }

    @Test
    @DisplayName("Hapus buku berhasil ketika tidak ada yang dipinjam")
    void testHapusBukuBerhasil() {
        // Arrange
        bukuTest.setJumlahTersedia(5); // Semua salinan tersedia
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));
        when(mockRepositoriBuku.hapus("1234567890")).thenReturn(true);

        // Act
        boolean hasil = servicePerpustakaan.hapusBuku("1234567890");

        // Assert
        assertTrue(hasil, "Harus berhasil menghapus buku");
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku).hapus("1234567890");
    }

    @Test
    @DisplayName("Hapus buku gagal ketika ada yang dipinjam")
    void testHapusBukuGagalAdaYangDipinjam() {
        // Arrange
        bukuTest.setJumlahTersedia(2); // Ada yang dipinjam (5 total - 2 tersedia = 3 dipinjam)
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        // Act
        boolean hasil = servicePerpustakaan.hapusBuku("1234567890");

        // Assert
        assertFalse(hasil, "Tidak boleh menghapus buku yang sedang dipinjam");
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku, never()).hapus(anyString());
    }

    @Test
    @DisplayName("Cari buku by ISBN berhasil")
    void testCariBukuByIsbnBerhasil() {
        // Arrange
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        // Act
        Optional<Buku> hasil = servicePerpustakaan.cariBukuByIsbn("1234567890");

        // Assert
        assertTrue(hasil.isPresent(), "Harus menemukan buku");
        assertEquals("Pemrograman Java", hasil.get().getJudul());
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
    }

    @Test
    @DisplayName("Cari buku by judul berhasil")
    void testCariBukuByJudul() {
        // Arrange
        List<Buku> daftarBuku = Arrays.asList(bukuTest);
        when(mockRepositoriBuku.cariByJudul("Java")).thenReturn(daftarBuku);

        // Act
        List<Buku> hasil = servicePerpustakaan.cariBukuByJudul("Java");

        // Assert
        assertEquals(1, hasil.size());
        assertEquals("Pemrograman Java", hasil.get(0).getJudul());
        verify(mockRepositoriBuku).cariByJudul("Java");
    }

    @Test
    @DisplayName("Pinjam buku berhasil ketika semua kondisi terpenuhi")
    void testPinjamBukuBerhasil() {
        // Arrange
        bukuTest.setJumlahTersedia(3);
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));
        when(mockRepositoriBuku.updateJumlahTersedia("1234567890", 2)).thenReturn(true);

        // Act
        boolean hasil = servicePerpustakaan.pinjamBuku("1234567890", anggotaTest);

        // Assert
        assertTrue(hasil, "Harus berhasil meminjam buku");
        assertTrue(anggotaTest.getIdBukuDipinjam().contains("1234567890"));
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku).updateJumlahTersedia("1234567890", 2);
    }

    @Test
    @DisplayName("Pinjam buku gagal ketika buku tidak tersedia")
    void testPinjamBukuGagalTidakTersedia() {
        // Arrange
        bukuTest.setJumlahTersedia(0); // Tidak ada yang tersedia
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        // Act
        boolean hasil = servicePerpustakaan.pinjamBuku("1234567890", anggotaTest);

        // Assert
        assertFalse(hasil, "Tidak boleh meminjam buku yang tidak tersedia");
        assertFalse(anggotaTest.getIdBukuDipinjam().contains("1234567890"));
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku, never()).updateJumlahTersedia(anyString(), anyInt());
    }

    @Test
    @DisplayName("Pinjam buku gagal ketika anggota tidak aktif")
    void testPinjamBukuGagalAnggotaTidakAktif() {
        // Arrange
        anggotaTest.setAktif(false);

        // Act
        boolean hasil = servicePerpustakaan.pinjamBuku("1234567890", anggotaTest);

        // Assert
        assertFalse(hasil, "Anggota tidak aktif tidak boleh meminjam buku");
        verifyNoInteractions(mockRepositoriBuku);
    }

    @Test
    @DisplayName("Pinjam buku gagal ketika batas pinjam tercapai")
    void testPinjamBukuGagalBatasPinjamTercapai() {
        // Arrange - Mahasiswa sudah pinjam 3 buku (batas maksimal)
        anggotaTest.tambahBukuDipinjam("1111111111");
        anggotaTest.tambahBukuDipinjam("2222222222");
        anggotaTest.tambahBukuDipinjam("3333333333");

        // PERBAIKAN ARRANGE: Tambahkan mocking karena ServicePerpustakaan memanggil cariByIsbn
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        // Act
        boolean hasil = servicePerpustakaan.pinjamBuku("1234567890", anggotaTest);

        // Assert
        assertFalse(hasil, "Tidak boleh meminjam ketika batas pinjam tercapai");

        // PERBAIKAN ASSERT: Verifikasi bahwa pencarian buku terjadi, tetapi update tidak terjadi.
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku, never()).updateJumlahTersedia(anyString(), anyInt());
    }

    @Test
    @DisplayName("Kembalikan buku berhasil")
    void testKembalikanBukuBerhasil() {
        // Arrange
        anggotaTest.tambahBukuDipinjam("1234567890");
        bukuTest.setJumlahTersedia(2);
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));
        when(mockRepositoriBuku.updateJumlahTersedia("1234567890", 3)).thenReturn(true);

        // Act
        boolean hasil = servicePerpustakaan.kembalikanBuku("1234567890", anggotaTest);

        // Assert
        assertTrue(hasil, "Harus berhasil mengembalikan buku");
        assertFalse(anggotaTest.getIdBukuDipinjam().contains("1234567890"));
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
        verify(mockRepositoriBuku).updateJumlahTersedia("1234567890", 3);
    }

    @Test
    @DisplayName("Kembalikan buku gagal ketika anggota tidak meminjam buku tersebut")
    void testKembalikanBukuGagalTidakMeminjam() {
        // Act
        boolean hasil = servicePerpustakaan.kembalikanBuku("1234567890", anggotaTest);

        // Assert
        assertFalse(hasil, "Tidak boleh mengembalikan buku yang tidak dipinjam");
        verifyNoInteractions(mockRepositoriBuku);
    }

    @Test
    @DisplayName("Cek ketersediaan buku")
    void testBukuTersedia() {
        // Arrange - Buku tersedia
        bukuTest.setJumlahTersedia(1);
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        // Act & Assert
        assertTrue(servicePerpustakaan.bukuTersedia("1234567890"));

        // Arrange - Buku tidak tersedia
        bukuTest.setJumlahTersedia(0);
        // Mock repositori dipanggil lagi, tidak perlu when baru jika return sama (Optional.of(bukuTest))

        // Act & Assert
        assertFalse(servicePerpustakaan.bukuTersedia("1234567890"));
    }

    @Test
    @DisplayName("Get jumlah tersedia")
    void testGetJumlahTersedia() {
        // Arrange
        bukuTest.setJumlahTersedia(3);
        when(mockRepositoriBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(bukuTest));

        // Act
        int jumlah = servicePerpustakaan.getJumlahTersedia("1234567890");

        // Assert
        assertEquals(3, jumlah);
        verify(mockRepositoriBuku).cariByIsbn("1234567890");
    }

    @Test
    @DisplayName("Get jumlah tersedia untuk buku yang tidak ada")
    void testGetJumlahTersediaBukuTidakAda() {
        // Arrange
        when(mockRepositoriBuku.cariByIsbn("9999999999")).thenReturn(Optional.empty());

        // Act
        int jumlah = servicePerpustakaan.getJumlahTersedia("9999999999");

        // Assert
        assertEquals(0, jumlah);
        verify(mockRepositoriBuku).cariByIsbn("9999999999");
    }
}