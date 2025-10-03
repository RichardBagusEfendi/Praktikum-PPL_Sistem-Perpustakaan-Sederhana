package com.praktikum.testing.service;

import com.praktikum.testing.model.Buku;
import com.praktikum.testing.model.Anggota;
import com.praktikum.testing.repository.RepositoriBuku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServicePerpustakaanTest {

    @Mock
    private RepositoriBuku mockRepoBuku;

    @Mock
    private KalkulatorDenda mockKalkulatorDenda;

    @InjectMocks
    private ServicePerpustakaan servicePerpustakaan;

    @BeforeEach
    void setUp() {
        // @InjectMocks akan membuat instance ServicePerpustakaan dengan mock yang di-inject.
        // tidak perlu inisialisasi manual kecuali ingin override behavior.
    }

    @Test
    void testPinjamBukuGagalBatasPinjamTercapai() {
        // Arrange
        Anggota mockAnggota = mock(Anggota.class);
        when(mockAnggota.isAktif()).thenReturn(true);
        when(mockAnggota.bolehPinjamLagi()).thenReturn(false); // sudah mencapai batas

        // Agar alur sampai pengecekan anggota saja, kita bisa juga stub cariByIsbn
        Buku buku = new Buku("1234567890", "Judul", "Pengarang", 5, 5);
        when(mockRepoBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(buku));

        // Act
        boolean result = servicePerpustakaan.pinjamBuku("1234567890", mockAnggota);

        // Assert
        assertFalse(result, "Seharusnya gagal karena anggota tidak boleh pinjam lagi");
        // Pastikan tidak ada update jumlah tersedia yg dipanggil karena gagal pada cek anggota
        verify(mockRepoBuku, never()).updateJumlahTersedia(anyString(), anyInt());
    }

    @Test
    void testPinjamBukuBerhasil() {
        // Arrange
        Anggota mockAnggota = mock(Anggota.class);
        when(mockAnggota.isAktif()).thenReturn(true);
        when(mockAnggota.bolehPinjamLagi()).thenReturn(true);

        Buku buku = new Buku("1234567890", "Judul", "Pengarang", 5, 5);
        when(mockRepoBuku.cariByIsbn("1234567890")).thenReturn(Optional.of(buku));
        when(mockRepoBuku.updateJumlahTersedia("1234567890", 4)).thenReturn(true);

        // Act
        boolean result = servicePerpustakaan.pinjamBuku("1234567890", mockAnggota);

        // Assert
        assertTrue(result, "Seharusnya peminjaman berhasil");
        verify(mockRepoBuku, times(1)).updateJumlahTersedia("1234567890", 4);
        verify(mockAnggota, times(1)).tambahBukuDipinjam("1234567890");
    }

    // Tambahkan test-case lain bila perlu (mis. buku tidak tersedia, invalid anggota, dll.)
}
