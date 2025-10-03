package com.praktikum.testing.util;

import com.praktikum.testing.model.Anggota;
import com.praktikum.testing.model.Buku;

public class ValidationUtils {

    // Validasi email
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Validasi email sederhana: pola dasar [a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return email.matches(emailRegex);
    }

    // Validasi nomor telepon Format (minimal 10-13 digit, dimulai 08 atau +628)
    public static boolean isValidNomorTelepon(String telepon) {
        if (telepon == null || telepon.trim().isEmpty()) {
            return false;
        }

        // Hapus spasi dan tanda hubung
        String teleponBersih = telepon.replaceAll("[\\s\\-]+", "");

        // Nomor telepon Indonesia harus dimulai dengan 08 atau +628 dan memiliki 10-13 digit
        // Regex sederhana: memastikan panjang 10-13 dan dimulai dengan 08 atau +628
        return teleponBersih.matches("^(08|\\+628)\\d{8,11}$");
    }

    // Validasi ISBN sederhana (10 atau 13 digit)
    public static boolean isValidISBN(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }

        // Hapus tanda hubung dan spasi
        String isbnBersih = isbn.replaceAll("[\\s\\-]+", "");

        // Harus berupa 10 atau 13 digit
        return isbnBersih.matches("^\\d{10}$") || isbnBersih.matches("^\\d{13}$");
    }

    // Validasi Buku
    public static boolean isValidBuku(Buku buku) {
        if (buku == null) {
            return false;
        }

        return isValidISBN(buku.getIsbn()) &&
                isValidString(buku.getJudul()) &&
                isValidString(buku.getPengarang()) &&
                buku.getJumlahTotal() > 0 &&
                buku.getJumlahTersedia() >= 0 &&
                buku.getJumlahTersedia() <= buku.getJumlahTotal() &&
                isAngkaNonNegatif(buku.getHarga());
    }

    // Validasi Anggota
    public static boolean isValidAnggota(Anggota anggota) {
        if (anggota == null) {
            return false;
        }

        return isValidString(anggota.getIdAnggota()) &&
                isValidString(anggota.getNama()) &&
                isValidEmail(anggota.getEmail()) &&
                isValidNomorTelepon(anggota.getTelepon()) &&
                anggota.getTipeAnggota() != null;
    }

    // Validasi String (tidak null dan tidak kosong setelah trim)
    public static boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    // Validasi angka positif
    public static boolean isAngkaPositif(double angka) {
        return angka > 0;
    }

    // Validasi angka non-negatif
    public static boolean isAngkaNonNegatif(double angka) {
        return angka >= 0;
    }
}