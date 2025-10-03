package com.praktikum.testing.service;

import com.praktikum.testing.model.Buku;
import com.praktikum.testing.model.Anggota;
import com.praktikum.testing.repository.RepositoriBuku;
import com.praktikum.testing.util.ValidationUtils;
import java.util.List;
import java.util.Optional;

public class ServicePerpustakaan {

    private final RepositoriBuku repositoriBuku;
    private final KalkulatorDenda kalkulatorDenda;

    public ServicePerpustakaan(RepositoriBuku repositoryBuku, KalkulatorDenda kalkulatorDenda) {
        this.repositoriBuku = repositoryBuku;
        this.kalkulatorDenda = kalkulatorDenda;
    }

    public boolean tambahBuku(Buku buku) {
        if (buku == null || !ValidationUtils.isValidBuku(buku)) {
            return false;
        }

        // Cek apakah buku dengan ISBN yang sama sudah ada
        Optional<Buku> bukuExisting = repositoriBuku.cariByIsbn(buku.getIsbn());
        if (bukuExisting.isPresent()) {
            return false; // Buku sudah ada
        }

        return repositoriBuku.simpan(buku);
    }

    public boolean hapusBuku(String isbn) {
        if (!ValidationUtils.isValidISBN(isbn)) {
            return false;
        }

        Optional<Buku> buku = repositoriBuku.cariByIsbn(isbn);
        if (buku.isEmpty()) {
            return false; // Buku tidak ditemukan
        }

        // Cek apakah ada salinan yang sedang dipinjam
        if (buku.get().getJumlahTersedia() < buku.get().getJumlahTotal()) {
            return false; // Tidak bisa hapus karena ada yang dipinjam
        }

        return repositoriBuku.hapus(isbn);
    }

    public Optional<Buku> cariBukuByIsbn(String isbn) {
        if (!ValidationUtils.isValidISBN(isbn)) {
            return Optional.empty();
        }
        return repositoriBuku.cariByIsbn(isbn);
    }

    public List<Buku> cariBukuByJudul(String judul) {
        return repositoriBuku.cariByJudul(judul);
    }

    public List<Buku> cariBukuByPengarang(String pengarang) {
        return repositoriBuku.cariByPengarang(pengarang);
    }

    public boolean bukuTersedia(String isbn) {
        Optional<Buku> buku = repositoriBuku.cariByIsbn(isbn);
        return buku.isPresent() && buku.get().isTersedia();
    }

    public int getJumlahTersedia(String isbn) {
        Optional<Buku> buku = repositoriBuku.cariByIsbn(isbn);
        return buku.map(Buku::getJumlahTersedia).orElse(0);
    }

    /**
     * Proses peminjaman buku.
     * Urutan penting:
     *  1. validasi anggota
     *  2. cek apakah anggota boleh pinjam lagi
     *  3. cek ketersediaan buku (tanpa melakukan side-effect)
     *  4. jika lolos, lakukan updateJumlahTersedia (side-effect) dan ubah state anggota
     */
    public boolean pinjamBuku(String isbn, Anggota anggota) {
        // Validasi input dasar
        if (isbn == null || isbn.isBlank() || anggota == null) {
            return false;
        }

        // Validasi anggota
        if (!ValidationUtils.isValidAnggota(anggota) || !anggota.isAktif()) {
            return false;
        }

        // Cek apakah anggota masih bisa pinjam (harus dilakukan sebelum side-effect)
        if (!anggota.bolehPinjamLagi()) {
            return false;
        }

        // Cek ketersediaan buku (ambil data, tapi jangan ubah apapun dulu)
        Optional<Buku> bukuOpt = repositoriBuku.cariByIsbn(isbn);
        if (bukuOpt.isEmpty()) {
            return false;
        }

        Buku buku = bukuOpt.get();
        if (!buku.isTersedia() || buku.getJumlahTersedia() <= 0) {
            return false;
        }

        // Sekarang aman melakukan side-effect (update jumlah tersedia)
        boolean updateBerhasil = repositoriBuku.updateJumlahTersedia(isbn, buku.getJumlahTersedia() - 1);

        if (updateBerhasil) {
            anggota.tambahBukuDipinjam(isbn);
            return true;
        }

        return false;
    }

    public boolean kembalikanBuku(String isbn, Anggota anggota) {
        // Validasi
        if (!ValidationUtils.isValidISBN(isbn) || anggota == null) {
            return false;
        }

        // Cek apakah anggota meminjam buku ini
        if (!anggota.getIdBukuDipinjam().contains(isbn)) {
            return false;
        }

        Optional<Buku> bukuOpt = repositoriBuku.cariByIsbn(isbn);
        if (bukuOpt.isEmpty()) {
            return false;
        }

        Buku buku = bukuOpt.get();

        // Update jumlah tersedia
        boolean updateBerhasil = repositoriBuku.updateJumlahTersedia(isbn, buku.getJumlahTersedia() + 1);

        if (updateBerhasil) {
            anggota.hapusBukuDipinjam(isbn);
            return true;
        }

        return false;
    }
}
