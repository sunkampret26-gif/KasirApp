package com.example.kasir;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;

public class TambahMenuActivity extends Activity {

    EditText namaInput, hargaInput;
    Button simpanBtn;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_menu);

        namaInput = findViewById(R.id.namaInput);
        hargaInput = findViewById(R.id.hargaInput);
        simpanBtn = findViewById(R.id.simpanBtn);

        pref = getSharedPreferences("menuData", MODE_PRIVATE);

        simpanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanMenu();
            }
        });
    }

    void simpanMenu() {
        String nama = namaInput.getText().toString().trim();
        String hargaStr = hargaInput.getText().toString().trim();
        if (nama.isEmpty() || hargaStr.isEmpty()) {
            Toast.makeText(this, "Isi nama dan harga!", Toast.LENGTH_SHORT).show();
            return;
        }

        int harga = Integer.parseInt(hargaStr);

        try {
            String json = pref.getString("menu_list", "[]");
            JSONArray arr = new JSONArray(json);

            JSONObject o = new JSONObject();
            o.put("nama", nama);
            o.put("harga", harga);
            arr.put(o);

            pref.edit().putString("menu_list", arr.toString()).apply();
            Toast.makeText(this, "Menu tersimpan!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}