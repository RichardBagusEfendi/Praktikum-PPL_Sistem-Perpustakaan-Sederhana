package com.praktikum.testing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Model Buku")
class BukuTest {

    @Test
    @DisplayName("Test Constructor Penuh dan Status Awal")
    void testConstructorPenuh() {
        Buku buku = new Buku("1234567890", "Pemrograman", "Penulis A", 10, 50000.0);

        assertEquals("1234567890", buku.getIsbn());
        assertEquals("Pemrograman", buku.getJudul());
        assertEquals("Penulis A", buku.getPengarang());
        assertEquals(10, buku.getJumlahTotal());
        assertEquals(10, buku.getJumlahTersedia(), "Jumlah Tersedia harus sama dengan Jumlah Total saat inisialisasi");
        assertEquals(50000.0, buku.getHarga());
        assertTrue(buku.isTersedia());
    }

    @Test
    @DisplayName("Test Constructor Kosong dan Setters/Getters")
    void testConstructorKosongDanAksesors() {
        Buku buku = new Buku();

        // Test Setters
        buku.setIsbn("000");
        buku.setJudul("Judul Baru");
        buku.setPengarang("Pengarang Baru");
        buku.setJumlahTotal(20);
        buku.setJumlahTersedia(5);
        buku.setHarga(100.0);

        // Test Getters
        assertEquals("000", buku.getIsbn());
        assertEquals("Judul Baru", buku.getJudul());
        assertEquals("Pengarang Baru", buku.getPengarang());
        assertEquals(20, buku.getJumlahTotal());
        assertEquals(5, buku.getJumlahTersedia());
        assertEquals(100.0, buku.getHarga());

        // Uji isTersedia() - true
        assertTrue(buku.isTersedia());

        // Uji isTersedia() - false
        buku.setJumlahTersedia(0);
        assertFalse(buku.isTersedia());
    }

    @Test
    @DisplayName("Test equals, hashCode, dan toString")
    void testEqualsHashCodeToString() {
        Buku bukuA = new Buku("ISBN1", "Judul A", "Penulis", 1, 10);
        Buku bukuB = new Buku("ISBN1", "Judul B", "Penulis", 5, 50); // ISBN sama, data lain berbeda

        // equals dan hashCode (berdasarkan ISBN)
        assertTrue(bukuA.equals(bukuB));
        assertEquals(bukuA.hashCode(), bukuB.hashCode());

        // equals null dan class berbeda
        assertFalse(bukuA.equals(null));
        assertFalse(bukuA.equals(new Object()));

        // toString
        String expected = "Buku{isbn='ISBN1', judul='Judul A', pengarang='Penulis', jumlahTotal=1, jumlahTersedia=1, harga=10.0}";
        assertEquals(expected, bukuA.toString());
    }
}