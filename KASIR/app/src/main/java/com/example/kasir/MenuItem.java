package com.example.kasir;

public class MenuItem {
    public String nama;
    public int harga;
    public int jumlah;

    public MenuItem(String nama, int harga) {
        this.nama = nama;
        this.harga = harga;
        this.jumlah = 0;
    }

    public int getSubtotal() {
        return harga * jumlah;
    }
}

