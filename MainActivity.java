package com.example.kasir;

import android.app.*;
import android.content.*;
import android.database.sqlite.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    ListView listView;
    TextView totalText;
    Button tambahBtn, bayarBtn, btnRiwayat;

    ArrayList<MenuItem> daftarMenu = new ArrayList<>();
    SharedPreferences pref;
    SQLiteDatabase db; // Tambahan: database untuk transaksi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        totalText = findViewById(R.id.totalText);
        tambahBtn = findViewById(R.id.tambahBtn);
        bayarBtn = findViewById(R.id.bayarBtn);
        btnRiwayat = findViewById(R.id.btnRiwayat); // Tambahan tombol lihat transaksi

        pref = getSharedPreferences("menuData", MODE_PRIVATE);

        //  Buat / buka database transaksi
        db = openOrCreateDatabase("kasir.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS transaksi_detail(" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "id_transaksi TEXT," + // tambah kolom ini
        "nama_menu TEXT," +
        "jumlah INTEGER," +
        "harga INTEGER," +
        "total INTEGER," +
        "tanggal TEXT)");
        
        //  Muat dan tampilkan data menu
        muatDataMenu();
        tampilkanMenu();

        //Tombol tambah menu
        tambahBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TambahMenuActivity.class));
            }
        });

        //Tombol bayar
        bayarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tampilkanStruk();
            }
        });

        //Tombol lihat transaksi
        btnRiwayat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RiwayatTransaksiActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        muatDataMenu();
        tampilkanMenu();
    }

    void muatDataMenu() {
       daftarMenu.clear() ;
        String json = pref.getString("menu_list", "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                daftarMenu.add(new MenuItem(o.getString("nama"), o.getInt("harga")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

	void tampilkanMenu() {
		final ArrayAdapter<MenuItem> adapter = new ArrayAdapter<MenuItem>(this, android.R.layout.simple_list_item_1, daftarMenu) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// Buat baris layout horizontal
				LinearLayout row = new LinearLayout(getContext());
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.setPadding(16, 8, 16, 8);

				MenuItem item = getItem(position);

				// Nama menu
				TextView nama = new TextView(getContext());
				nama.setText(item.nama);
				nama.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

				// Harga
				TextView harga = new TextView(getContext());
				harga.setText("Rp" + item.harga);
				harga.setGravity(Gravity.END);
				harga.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

				// Jumlah klik
				TextView jumlah = new TextView(getContext());
				jumlah.setText("x" + item.jumlah);
				jumlah.setGravity(Gravity.END);
				jumlah.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

				// Masukkan ke layout
				row.addView(nama);
				row.addView(harga);
				row.addView(jumlah);

				return row;
			}
		};

		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					daftarMenu.get(position).jumlah++;
					adapter.notifyDataSetChanged(); // update tampilan jumlah di ListView
					hitungTotal();
				}
			});
			
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					final int pos = position;

					new AlertDialog.Builder(MainActivity.this)
						.setTitle("Hapus Menu")
						.setMessage("Yakin mau hapus menu \"" + daftarMenu.get(pos).nama + "\" ?")
						.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// Hapus dari daftar
								daftarMenu.remove(pos);

								// Simpan ulang ke SharedPreferences
								try {
									JSONArray jsonArray = new JSONArray();
									for (MenuItem item : daftarMenu) {
										JSONObject obj = new JSONObject();
										obj.put("nama", item.nama);
										obj.put("harga", item.harga);
										jsonArray.put(obj);
									}
									pref.edit().putString("menu_list", jsonArray.toString()).apply();
								} catch (Exception e) {
									e.printStackTrace();
								}

								// Refresh tampilan
								adapter.notifyDataSetChanged();
								hitungTotal();

								Toast.makeText(MainActivity.this, "Menu dihapus", Toast.LENGTH_SHORT).show();
							}
						})
						.setNegativeButton("Batal", null)
						.show();

					return true; // penting supaya klik lama gak dianggap klik biasa
				}
			});
		
	}
	
			

    void hitungTotal() {
        int total = 0;
        for (MenuItem item : daftarMenu) {
            total += item.getSubtotal();
        }
        totalText.setText("Total: Rp" + total);
    }
	void tampilkanStruk() {
		StringBuilder sb = new StringBuilder();
		int total = 0;

		sb.append("=============================\n");
		sb.append("       STRUK PEMBELIAN\n");
		sb.append(" MI AYAM BAKSO MAREM\n");
		sb.append("=============================\n");

		for (MenuItem item : daftarMenu) {
			if (item.jumlah > 0) {
				sb.append(String.format(Locale.getDefault(),
										"%-18s %2d x %,6d = %,8d\n",
										item.nama, item.jumlah, item.harga, item.getSubtotal()));
				total += item.getSubtotal();
			}
		}

		sb.append("-----------------------------\n");
		sb.append(String.format(Locale.getDefault(), "Total Bayar: Rp %,d\n", total));

		String tanggal = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new java.util.Date());
		sb.append("Tanggal: ").append(tanggal).append("\n");
		sb.append("=============================\n");
		sb.append("Terima kasih telah berbelanja!\n");

		new AlertDialog.Builder(this)
            .setTitle("Struk Pembelian")
            .setMessage(sb.toString())
            .setPositiveButton("OK", null)
            .show();

		try {
			db.execSQL("ALTER TABLE transaksi_detail ADD COLUMN id_transaksi TEXT");
		} catch (Exception e) {
			// abaikan kalau kolom sudah ada
		}

		try {
			String idTransaksi = "TRX-" + System.currentTimeMillis();

			for (MenuItem item : daftarMenu) {
				if (item.jumlah > 0) {
					db.execSQL(
                        "INSERT INTO transaksi_detail (id_transaksi, nama_menu, jumlah, harga, total, tanggal) VALUES (?, ?, ?, ?, ?, datetime('now','localtime'))",
                        new Object[]{idTransaksi, item.nama, item.jumlah, item.harga, item.getSubtotal()}
					);
				}
			}
		} catch (Exception e) {
			Toast.makeText(this, "Gagal menyimpan ke database: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

		for (MenuItem item : daftarMenu) {
			item.jumlah = 0;
		}
		hitungTotal();
	}}