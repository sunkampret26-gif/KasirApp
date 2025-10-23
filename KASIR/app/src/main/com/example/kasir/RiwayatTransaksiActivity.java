package com.example.kasir;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RiwayatTransaksiActivity extends Activity {
    SQLiteDatabase db;
    TextView textRiwayat;
    Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout utama
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        btnReset = new Button(this);
        btnReset.setText(" Reset Semua Transaksi Hari Ini");
        layout.addView(btnReset);

        textRiwayat = new TextView(this);
        textRiwayat.setPadding(20, 20, 20, 20);
        textRiwayat.setTypeface(android.graphics.Typeface.MONOSPACE); // biar rapi kayak nota
        textRiwayat.setTextSize(14);
        layout.addView(new ScrollView(this) {{
            addView(textRiwayat);
        }});

        setContentView(layout);

        db = openOrCreateDatabase("kasir.db", MODE_PRIVATE, null);

        tampilkanRiwayat();

        // Tombol Reset Semua Transaksi Hari Ini
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(RiwayatTransaksiActivity.this)
                        .setTitle("Konfirmasi")
                        .setMessage("Yakin ingin menghapus semua transaksi hari ini?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                hapusTransaksiHariIni();
                            }
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }
        });
    }

    // ✅ Fungsi hapus transaksi hari ini (versi fix)
    private void hapusTransaksiHariIni() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Hapus semua data transaksi dengan tanggal hari ini
        db.execSQL("DELETE FROM transaksi_detail WHERE tanggal LIKE ?", new String[]{today + "%"});

        // Setelah dihapus, perbarui tampilan
        tampilkanRiwayat();

        // Tampilkan notifikasi berhasil
        new AlertDialog.Builder(this)
                .setTitle("Berhasil")
                .setMessage("Semua transaksi hari ini telah dihapus.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void tampilkanRiwayat() {
        StringBuilder sb = new StringBuilder();

        // Ambil tanggal hari ini
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Ambil semua transaksi hari ini, dikelompokkan per id_transaksi
        Cursor cursorTransaksi = db.rawQuery(
                "SELECT DISTINCT id_transaksi, tanggal FROM transaksi_detail WHERE tanggal LIKE ? ORDER BY id DESC",
                new String[]{today + "%"}
        );

        if (cursorTransaksi.getCount() == 0) {
            textRiwayat.setText("Belum ada transaksi hari ini.");
            cursorTransaksi.close();
            return;
        }

        int totalHarian = 0;

        while (cursorTransaksi.moveToNext()) {
            String idTransaksi = cursorTransaksi.getString(cursorTransaksi.getColumnIndex("id_transaksi"));
            String tanggal = cursorTransaksi.getString(cursorTransaksi.getColumnIndex("tanggal"));

            sb.append("ID: ").append(idTransaksi).append("\n");
            sb.append(formatTanggal(tanggal)).append("\n");
            sb.append("-----------------------------\n");

            // Ambil detail item dari transaksi ini
            Cursor c = db.rawQuery(
                    "SELECT nama_menu, jumlah, harga, total FROM transaksi_detail WHERE id_transaksi = ?",
                    new String[]{idTransaksi}
            );

            int totalTransaksi = 0;
            while (c.moveToNext()) {
                String nama = c.getString(c.getColumnIndex("nama_menu"));
                int jumlah = c.getInt(c.getColumnIndex("jumlah"));
                int harga = c.getInt(c.getColumnIndex("harga"));
                int total = c.getInt(c.getColumnIndex("total"));

                sb.append(String.format(Locale.getDefault(), "%-15s %2d x %,6d = %,8d\n",
                        nama, jumlah, harga, total));
                totalTransaksi += total;
            }
            c.close();

            sb.append("\nTotal transaksi: Rp ").append(String.format(Locale.getDefault(), "%,d", totalTransaksi));
            sb.append("\n=============================\n\n");

            totalHarian += totalTransaksi;
        }

        cursorTransaksi.close();

        sb.append("-----------------------------\n");
        sb.append("Total penjualan hari ini: Rp ").append(String.format(Locale.getDefault(), "%,d", totalHarian));

        textRiwayat.setText(sb.toString());
    }

    private Object formatTanggal(String tanggal) {
        // Belum dipakai, bisa dikembangkan nanti kalau mau ubah format tampilan tanggal
        return tanggal;
    }
}
