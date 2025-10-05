package com.praktikum.testing.repository;

import com.praktikum.testing.model.Buku;
import java.util.List;
import java.util.Optional;

public interface RepositoriBuku {
    boolean simpan(Buku buku);
    Optional<Buku> cariByIsbn(String isbn);
    List<Buku> cariByJudul(String judul);
    List<Buku> cariByPengarang(String pengarang);
    boolean hapus(String isbn);
    boolean updateJumlahTersedia(String isbn, int jumlahTersediaBaru);
    List<Buku> cariSemua();
}