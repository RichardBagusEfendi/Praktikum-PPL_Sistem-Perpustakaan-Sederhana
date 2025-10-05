package com.praktikum.testing.util;

import com.praktikum.testing.model.Buku;
import com.praktikum.testing.model.Anggota;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Validasi Utils")
public class ValidationUtilsTest {

    @Test
    @DisplayName("Email valid harus mengembalikan true")
    void testEmailValid() {
        assertTrue(ValidationUtils.isValidEmail("mahasiswa@univ.ac.id"));
        assertTrue(ValidationUtils.isValidEmail("test@gmail.com"));
        assertTrue(ValidationUtils.isValidEmail("user123@domain.org"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "email-tanpa-at.com", "email@", "@domain.com", "email@domain.com,"})
    @DisplayName("Email tidak valid harus mengembalikan false")
    void testEmailTidakValid(String emailTidakValid) {
        assertFalse(ValidationUtils.isValidEmail(emailTidakValid));
    }

    @Test
    @DisplayName("Email null harus mengembalikan false")
    void testEmailNull() {
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    @DisplayName("Nomor telepon valid harus mengembalikan true")
    void testNomorTeleponValid() {
        assertTrue(ValidationUtils.isValidNomorTelepon("081234567890"));
        assertTrue(ValidationUtils.isValidNomorTelepon("+628123456789"));
        assertTrue(ValidationUtils.isValidNomorTelepon("+62812-3456-7890"));
        // Tambahan untuk branch coverage: format dengan spasi
        assertTrue(ValidationUtils.isValidNomorTelepon("0812 3456 7890"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "123456789", "07123456789", "081234", "-627123456789", "0812345678901234"})
    @DisplayName("Nomor telepon tidak valid harus mengembalikan false")
    void testNomorTeleponTidakValid(String teleponTidakValid) {
        assertFalse(ValidationUtils.isValidNomorTelepon(teleponTidakValid));
    }

    @Test
    @DisplayName("ISBN valid harus mengembalikan true")
    void testISBNValid() {
        assertTrue(ValidationUtils.isValidISBN("1234567890")); // 10 digit
        assertTrue(ValidationUtils.isValidISBN("1234567890123")); // 13 digit
        assertTrue(ValidationUtils.isValidISBN("123-456-789-0")); // 10 digit dengan strip
        // Tambahan untuk branch coverage: 13 digit dengan spasi/strip
        assertTrue(ValidationUtils.isValidISBN("978 1234567890"));
    }

    // Tambahan untuk ISBN invalid
    @Test
    @DisplayName("ISBN tidak valid harus mengembalikan false")
    void testISBNTidakValid() {
        assertFalse(ValidationUtils.isValidISBN(null));
        assertFalse(ValidationUtils.isValidISBN(""));
        assertFalse(ValidationUtils.isValidISBN("123456789")); // Kurang dari 10
        assertFalse(ValidationUtils.isValidISBN("ABCDEFGHIJ")); // Bukan angka
    }


    @Test
    @DisplayName("Buku valid harus mengembalikan true")
    void testBukuValid() {
        Buku buku = new Buku("1234567890", "Pemrograman Java", "John Doe", 5, 150000.0);
        assertTrue(ValidationUtils.isValidBuku(buku));
    }

    @Test
    @DisplayName("Buku dengan data tidak valid harus mengembalikan false")
    void testBukuTidakValid() {
        // Buku null
        assertFalse(ValidationUtils.isValidBuku(null));

        // ISBN tidak valid
        Buku bukuIsbnTidakValid = new Buku("123", "Judul", "Pengarang", 5, 100000.0);
        assertFalse(ValidationUtils.isValidBuku(bukuIsbnTidakValid), "ISBN tidak valid");

        // Judul tidak valid
        Buku bukuJudulInvalid = new Buku("1234567890", " ", "Pengarang", 5, 100000.0);
        assertFalse(ValidationUtils.isValidBuku(bukuJudulInvalid), "Judul kosong/whitespace");

        // Pengarang tidak valid
        Buku bukuPengarangInvalid = new Buku("1234567890", "Judul", "", 5, 100000.0);
        assertFalse(ValidationUtils.isValidBuku(bukuPengarangInvalid), "Pengarang kosong");

        // Jumlah total nol/negatif
        Buku bukuJumlahNegatif = new Buku("1234567890", "Judul", "Pengarang", 0, 100000.0);
        assertFalse(ValidationUtils.isValidBuku(bukuJumlahNegatif), "Jumlah total <= 0");

        // Jumlah tersedia > jumlah total
        Buku bukuJmlTersediaLebih = new Buku("1234567890", "Judul", "Pengarang", 5, 100000.0);
        bukuJmlTersediaLebih.setJumlahTersedia(6);
        assertFalse(ValidationUtils.isValidBuku(bukuJmlTersediaLebih), "Jumlah tersedia > Jumlah total");

        // Harga negatif
        Buku bukuHargaNegatif = new Buku("1234567890", "Judul", "Pengarang", 5, -10000.0);
        assertFalse(ValidationUtils.isValidBuku(bukuHargaNegatif), "Harga negatif");
    }

    @Test
    @DisplayName("Anggota valid harus mengembalikan true")
    void testAnggotaValid() {
        Anggota anggota = new Anggota("A001", "John Doe", "john@univ.ac.id",
                "081234567890", Anggota.TipeAnggota.MAHASISWA);
        assertTrue(ValidationUtils.isValidAnggota(anggota));
    }

    // Tambahan untuk Anggota invalid (menargetkan TipeAnggota null)
    @Test
    @DisplayName("Anggota dengan TipeAnggota null harus mengembalikan false")
    void testAnggotaTipeNull() {
        Anggota anggota = new Anggota("A001", "John Doe", "john@univ.ac.id",
                "081234567890", null);
        assertFalse(ValidationUtils.isValidAnggota(anggota));
    }


    @Test
    @DisplayName("String valid harus mengembalikan true")
    void testStringValid() {
        assertTrue(ValidationUtils.isValidString("teks"));
        assertTrue(ValidationUtils.isValidString("teks dengan spasi"));
        assertFalse(ValidationUtils.isValidString(""));
        assertFalse(ValidationUtils.isValidString(" "));
        assertFalse(ValidationUtils.isValidString(null));
    }

    @Test
    @DisplayName("Angka positif dan non-negatif harus valid")
    void testValidasiAngka() {
        assertTrue(ValidationUtils.isAngkaPositif(1.0));
        assertTrue(ValidationUtils.isAngkaPositif(100.0));
        assertFalse(ValidationUtils.isAngkaPositif(0.0));
        assertFalse(ValidationUtils.isAngkaPositif(-1.0));

        assertTrue(ValidationUtils.isAngkaNonNegatif(0.0));
        assertTrue(ValidationUtils.isAngkaNonNegatif(10.0));
        assertFalse(ValidationUtils.isAngkaNonNegatif(-0.1));
    }
}