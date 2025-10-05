package com.praktikum.testing.repository;

import com.praktikum.testing.model.Buku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class untuk MockRepositoryBuku
 *
 * TUJUAN PEMBELAJARAN:
 * - Memahami cara menggunakan mock manual
 * - Melihat bagaimana mock bekerja secara internal
 * - Membandingkan behavior mock manual dengan real implementation
 */
@DisplayName("Test Mock Repository Buku - Manual Mock Implementation")
class MockRepositoryBukuTest {

    private MockRepositoryBuku mockRepository;
    private Buku buku1;
    private Buku buku2;
    private Buku buku3;

    @BeforeEach
    void setUp() {
        // Reset mock repository sebelum setiap test
        mockRepository = new MockRepositoryBuku();

        // Setup sample data
        buku1 = new Buku("1234567890", "Pemrograman Java", "John Doe", 5, 150000.0);
        buku2 = new Buku("0987654321", "Algoritma dan Struktur Data", "Jane Smith", 3, 120000.0);
        buku3 = new Buku("1111111111", "Java Advanced", "John Doe", 4, 180000.0);
    }

    @Test
    @DisplayName("Simpan buku baru - harus berhasil")
    void testSimpanBukuBaru() {
        // Act
        boolean hasil = mockRepository.simpan(buku1);

        // Assert
        assertTrue(hasil, "Harus berhasil menyimpan buku baru");
        assertEquals(1, mockRepository.ukuran(), "Repository harus berisi 1 buku");
        assertTrue(mockRepository.mengandung("1234567890"), "Repository harus mengandung ISBN buku");
    }

    @Test
    @DisplayName("Simpan buku null - harus gagal")
    void testSimpanBukuNull() {
        // Act
        boolean hasil = mockRepository.simpan(null);

        // Assert
        assertFalse(hasil, "Harus gagal menyimpan buku null");
        assertEquals(0, mockRepository.ukuran(), "Repository harus tetap kosong");
    }

    @Test
    @DisplayName("Simpan buku dengan ISBN null - harus gagal")
    void testSimpanBukuDenganIsbnNull() {
        // Arrange
        Buku bukuInvalid = new Buku();
        bukuInvalid.setJudul("Judul Tanpa ISBN");

        // Act
        boolean hasil = mockRepository.simpan(bukuInvalid);

        // Assert
        assertFalse(hasil, "Harus gagal menyimpan buku tanpa ISBN");
        assertEquals(0, mockRepository.ukuran(), "Repository harus tetap kosong");
    }

    @Test
    @DisplayName("Simpan buku duplikat - harus overwrite")
    void testSimpanBukuDuplikat() {
        // Arrange - Simpan buku pertama
        mockRepository.simpan(buku1);

        // Buat buku dengan ISBN sama tapi data berbeda
        Buku bukuUpdated = new Buku("1234567890", "Java Programming Updated", "John Doe Updated", 10, 200000.0);

        // Act - Simpan buku dengan ISBN yang sama
        boolean hasil = mockRepository.simpan(bukuUpdated);

        // Assert
        assertTrue(hasil, "Harus berhasil menyimpan (overwrite)");
        assertEquals(1, mockRepository.ukuran(), "Repository harus tetap berisi 1 buku");

        // Verify data terupdate
        Optional<Buku> bukuDiRepository = mockRepository.cariByIsbn("1234567890");
        assertTrue(bukuDiRepository.isPresent());
        assertEquals("Java Programming Updated", bukuDiRepository.get().getJudul());
    }

    @Test
    @DisplayName("Cari buku by ISBN - buku ditemukan")
    void testCariByIsbnBukuDitemukan() {
        // Arrange
        mockRepository.simpan(buku1);
        mockRepository.simpan(buku2);

        // Act
        Optional<Buku> hasil = mockRepository.cariByIsbn("0987654321");

        // Assert
        assertTrue(hasil.isPresent(), "Harus menemukan buku");
        assertEquals("Algoritma dan Struktur Data", hasil.get().getJudul());
        assertEquals("Jane Smith", hasil.get().getPengarang());
    }

    @Test
    @DisplayName("Cari buku by ISBN - buku tidak ditemukan")
    void testCariByIsbnBukuTidakDitemukan() {
        // Arrange
        mockRepository.simpan(buku1);

        // Act
        Optional<Buku> hasil = mockRepository.cariByIsbn("9999999999");

        // Assert
        assertFalse(hasil.isPresent(), "Harus mengembalikan Optional.empty()");
    }

    @Test
    @DisplayName("Cari buku by ISBN null - harus empty")
    void testCariByIsbnNull() {
        // Act
        Optional<Buku> hasil = mockRepository.cariByIsbn(null);

        // Assert
        assertFalse(hasil.isPresent(), "Harus mengembalikan Optional.empty() untuk ISBN null");
    }

    @Test
    @DisplayName("Cari buku by judul - case insensitive")
    void testCariByJudulCaseInsensitive() {
        // Arrange
        mockRepository.simpan(buku1); // "Pemrograman Java"
        mockRepository.simpan(buku2); // "Algoritma dan Struktur Data"
        mockRepository.simpan(buku3); // "Java Advanced"

        // Act - Test berbagai case
        List<Buku> hasil1 = mockRepository.cariByJudul("Java");
        List<Buku> hasil2 = mockRepository.cariByJudul("java");
        List<Buku> hasil3 = mockRepository.cariByJudul("JAVA");

        // Assert
        assertEquals(2, hasil1.size(), "Harus menemukan 2 buku dengan kata 'Java'");
        assertEquals(2, hasil2.size(), "Harus case insensitive");
        assertEquals(2, hasil3.size(), "Harus case insensitive");

        // Verify judul yang ditemukan
        assertTrue(hasil1.stream().anyMatch(b -> b.getJudul().equals("Pemrograman Java")));
        assertTrue(hasil1.stream().anyMatch(b -> b.getJudul().equals("Java Advanced")));
    }

    @Test
    @DisplayName("Cari buku by judul partial match")
    void testCariByJudulPartialMatch() {
        // Arrange
        mockRepository.simpan(buku1);
        mockRepository.simpan(buku2); // "Algoritma dan Struktur Data"
        mockRepository.simpan(buku3);

        // Act
        List<Buku> hasil = mockRepository.cariByJudul("Algoritma");

        // Assert
        assertEquals(1, hasil.size());
        assertEquals("Algoritma dan Struktur Data", hasil.get(0).getJudul());
    }

    @Test
    @DisplayName("Cari buku by judul kosong - harus empty list")
    void testCariByJudulKosong() {
        // Arrange
        mockRepository.simpan(buku1);

        // Act
        List<Buku> hasil1 = mockRepository.cariByJudul("");
        List<Buku> hasil2 = mockRepository.cariByJudul(" ");
        List<Buku> hasil3 = mockRepository.cariByJudul(null);

        // Assert
        assertTrue(hasil1.isEmpty(), "Harus empty untuk string kosong");
        assertTrue(hasil2.isEmpty(), "Harus empty untuk whitespace");
        assertTrue(hasil3.isEmpty(), "Harus empty untuk null");
    }

    @Test
    @DisplayName("Cari buku by pengarang")
    void testCariByPengarang() {
        // Arrange
        mockRepository.simpan(buku1); // John Doe
        mockRepository.simpan(buku2); // Jane Smith
        mockRepository.simpan(buku3); // John Doe

        // Act
        List<Buku> hasil = mockRepository.cariByPengarang("John Doe");

        // Assert
        assertEquals(2, hasil.size(), "Harus menemukan 2 buku oleh John Doe");
        assertTrue(hasil.stream().allMatch(b -> b.getPengarang().equals("John Doe")));
    }

    @Test
    @DisplayName("Hapus buku yang ada - harus berhasil")
    void testHapusBukuYangAda() {
        // Arrange
        mockRepository.simpan(buku1);
        mockRepository.simpan(buku2);
        assertEquals(2, mockRepository.ukuran());

        // Act
        boolean hasil = mockRepository.hapus("1234567890");

        // Assert
        assertTrue(hasil, "Harus berhasil menghapus");
        assertEquals(1, mockRepository.ukuran(), "Harus tersisa 1 buku");
        assertFalse(mockRepository.mengandung("1234567890"), "Buku harus sudah dihapus");
        assertTrue(mockRepository.mengandung("0987654321"), "Buku2 masih harus ada");
    }

    @Test
    @DisplayName("Hapus buku yang tidak ada - harus gagal")
    void testHapusBukuYangTidakAda() {
        // Arrange
        mockRepository.simpan(buku1);

        // Act
        boolean hasil = mockRepository.hapus("9999999999");

        // Assert
        assertFalse(hasil, "Harus gagal menghapus buku yang tidak ada");
        assertEquals(1, mockRepository.ukuran(), "Repository harus tetap berisi 1 buku");
    }

    @Test
    @DisplayName("Hapus dengan ISBN null - harus gagal")
    void testHapusDenganIsbnNull() {
        // Act
        boolean hasil = mockRepository.hapus(null);

        // Assert
        assertFalse(hasil, "Harus gagal untuk ISBN null");
    }

    @Test
    @DisplayName("Update jumlah tersedia - valid update")
    void testUpdateJumlahTersediaValid() {
        // Arrange
        mockRepository.simpan(buku1); // jumlahTotal = 5, jumlahTersedia = 5

        // Act
        boolean hasil = mockRepository.updateJumlahTersedia("1234567890", 3);

        // Assert
        assertTrue(hasil, "Harus berhasil update");
        Optional<Buku> bukuUpdated = mockRepository.cariByIsbn("1234567890");
        assertTrue(bukuUpdated.isPresent());
        assertEquals(3, bukuUpdated.get().getJumlahTersedia(), "Jumlah tersedia harus terupdate");
        assertEquals(5, bukuUpdated.get().getJumlahTotal(), "Jumlah total harus tetap sama");
    }

    @Test
    @DisplayName("Update jumlah tersedia - melebihi jumlah total harus gagal")
    void testUpdateJumlahTersediaMelebihiTotal() {
        // Arrange
        mockRepository.simpan(buku1); // jumlahTotal = 5

        // Act - coba set jumlah tersedia > jumlah total
        boolean hasil = mockRepository.updateJumlahTersedia("1234567890", 10);

        // Assert
        assertFalse(hasil, "Harus gagal update karena melebihi jumlah total");
        // Verify data tidak berubah
        Optional<Buku> buku = mockRepository.cariByIsbn("1234567890");
        assertTrue(buku.isPresent());
        assertEquals(5, buku.get().getJumlahTersedia(), "Jumlah tersedia harus tetap sama");
    }

    @Test
    @DisplayName("Update jumlah tersedia - jumlah negatif harus gagal")
    void testUpdateJumlahTersediaNegatif() {
        // Arrange
        mockRepository.simpan(buku1);

        // Act
        boolean hasil = mockRepository.updateJumlahTersedia("1234567890", -1);

        // Assert
        assertFalse(hasil, "Harus gagal untuk jumlah negatif");
    }

    @Test
    @DisplayName("Update jumlah tersedia - buku tidak ditemukan harus gagal")
    void testUpdateJumlahTersediaBukuTidakDitemukan() {
        // Act
        boolean hasil = mockRepository.updateJumlahTersedia("9999999999", 3);

        // Assert
        assertFalse(hasil, "Harus gagal untuk buku yang tidak ada");
    }

    @Test
    @DisplayName("Cari semua buku - repository kosong")
    void testCariSemuaRepositoryKosong() {
        // Act
        List<Buku> hasil = mockRepository.cariSemua();

        // Assert
        assertTrue(hasil.isEmpty(), "Harus empty list untuk repository kosong");
    }

    @Test
    @DisplayName("Cari semua buku - repository berisi beberapa buku")
    void testCariSemuaRepositoryBerisi() {
        // Arrange
        mockRepository.simpan(buku1);
        mockRepository.simpan(buku2);
        mockRepository.simpan(buku3);

        // Act
        List<Buku> hasil = mockRepository.cariSemua();

        // Assert
        assertEquals(3, hasil.size(), "Harus mengembalikan semua buku");
        assertTrue(hasil.contains(buku1));
        assertTrue(hasil.contains(buku2));
        assertTrue(hasil.contains(buku3));
    }

    @Test
    @DisplayName("Bersihkan repository - harus kosong")
    void testBersihkanRepository() {
        // Arrange
        mockRepository.simpan(buku1);
        mockRepository.simpan(buku2);
        assertEquals(2, mockRepository.ukuran());

        // Act
        mockRepository.bersihkan();

        // Assert
        assertEquals(0, mockRepository.ukuran(), "Repository harus kosong setelah dibersihkan");
    }

    @Test
    @DisplayName("Integration test - simulasi flow peminjaman buku")
    void testIntegrationFlowPeminjaman() {
        // Simulasi flow lengkap peminjaman buku

        // 1. Tambah buku ke repository
        mockRepository.simpan(buku1);
        assertEquals(1, mockRepository.ukuran());

        // 2. Cek ketersediaan awal
        Optional<Buku> bukuAwal = mockRepository.cariByIsbn("1234567890");
        assertTrue(bukuAwal.isPresent());
        assertEquals(5, bukuAwal.get().getJumlahTersedia());

        // 3. Pinjam buku (kurangi jumlah tersedia)
        boolean updateBerhasil = mockRepository.updateJumlahTersedia("1234567890", 3);
        assertTrue(updateBerhasil);

        // 4. Verifikasi perubahan
        Optional<Buku> bukuSetelahPinjam = mockRepository.cariByIsbn("1234567890");
        assertTrue(bukuSetelahPinjam.isPresent());
        assertEquals(3, bukuSetelahPinjam.get().getJumlahTersedia());

        // 5. Kembalikan buku (tambah jumlah tersedia)
        updateBerhasil = mockRepository.updateJumlahTersedia("1234567890", 4);
        assertTrue(updateBerhasil);

        // 6. Verifikasi kembali ke semula
        Optional<Buku> bukuSetelahKembali = mockRepository.cariByIsbn("1234567890");
        assertTrue(bukuSetelahKembali.isPresent());
        assertEquals(4, bukuSetelahKembali.get().getJumlahTersedia());
    }

    @Test
    @DisplayName("Test utility methods - ukuran dan mengandung")
    void testUtilityMethods() {
        // 1. Test awal repository kosong
        assertEquals(0, mockRepository.ukuran());
        assertFalse(mockRepository.mengandung("1234567890"));

        // 2. Tambah buku
        mockRepository.simpan(buku1);
        assertEquals(1, mockRepository.ukuran());
        assertTrue(mockRepository.mengandung("1234567890"));
        assertFalse(mockRepository.mengandung("9999999999"));

        // 3. Tambah buku kedua
        mockRepository.simpan(buku2);
        assertEquals(2, mockRepository.ukuran());
        assertTrue(mockRepository.mengandung("0987654321"));
    }
}