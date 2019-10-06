package dev7.id.sidausappspublic.Activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import dev7.id.pakhendrawan.Helper.Utils
import dev7.id.sidausappspublic.Model.*
import dev7.id.sidausappspublic.R
import dev7.id.sidausappspublic.Server.ApiUtil
import kotlinx.android.synthetic.main.content_ubah_usaha.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class Adan2Activity : AppCompatActivity() {

    private var apiKecamatan = ApiUtil.getKecamatanInterface()
    private var apiDesa = ApiUtil.getDesaInterface()
    private var api= ApiUtil.getUsahaInterface()
    private var currentUsaha = Usaha()
    private var filteredDesa = mutableListOf<Desa>()
    private var desas = mutableListOf<Desa>()
    private var kecamatans = mutableListOf<Kecamatan>()
    private var jenisIzins = Utils.getJenisIzins()
    private var kepemilikans = Utils.getKepemilikans()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adan2)


        fetchUsaha(getIdUsaha())
        fetchKecamatan()
        postUsaha()
    }



    private fun postUsaha() {
        btnSimpan.setOnClickListener {
            val pj = etPj.text.toString().trim()
            val alamat = etAlamat.text.toString().trim()
            val nama = etNama.text.toString().trim()
            val hp = etHp.text.toString().trim()
            val verify = "N"

            if (!pj.isEmpty() && !alamat.isEmpty() && !nama.isEmpty() && !hp.isEmpty()) {
                if (pj.length < 2) {
                    etPj.error = "Masukan Penananggug Jawab dengan benar"
                    etPj.requestFocus()
                    return@setOnClickListener
                }
                if (alamat.length < 4) {
                    etAlamat.error = "Masukan Alamat dengan benar"
                    etAlamat.requestFocus()
                    return@setOnClickListener
                }
                if (nama.length < 2) {
                    etNama.error = "Masukan Nama Minimal 2 Karakter"
                    etNama.requestFocus()
                    return@setOnClickListener
                }

                if (filteredDesa[0].id == 0) {
                    Toast.makeText(this@Adan2Activity, "Desa belum dipilih", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                btnSimpan.isEnabled=false
                val ji = spinnerJenisIzin.selectedItem as JenisIzin
                val kp = spinnerKepemilikan.selectedItem as Kepemilikan
                val desa = spinnerDesa.selectedItem as Desa

                val request = api.putUsaha("Token ${getToken()}", currentUsaha.id, nama, pj, alamat, hp, verify, ji.getId(), kp.getId(), desa.getId())
                request.enqueue(object : Callback<Usaha> {
                    override fun onFailure(call: Call<Usaha>, t: Throwable) {
                        println("wek "+t.message)
                        Toast.makeText(this@Adan2Activity, "Gagal Nmen", Toast.LENGTH_SHORT).show()

                    }

                    override fun onResponse(call: Call<Usaha>, response: Response<Usaha>) {
                        if (response.isSuccessful){
                            Toast.makeText(this@Adan2Activity, "Sukses", Toast.LENGTH_SHORT).show()
                            finish()
                        }else{
                            Toast.makeText(this@Adan2Activity, "Respon Gagal", Toast.LENGTH_SHORT).show()
                            println("weeek "+response.body().toString())

                        }
                        btnSimpan.isEnabled = true
                    }

                })
            }
        }
    }

    private fun getIdUsaha()=intent.getIntExtra("USAHA", 0)

    private fun fetchUsaha(id_usaha:Int){
        api.getUsahabyID("Token ${getToken()}", id_usaha).enqueue(object : Callback<Usaha> {
            override fun onFailure(call: Call<Usaha>, t: Throwable) {
                println("wekwk ${t.message}")
                Toast.makeText(this@Adan2Activity, "gagal update", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Usaha>, response: Response<Usaha>) {
                if (response.isSuccessful){
                    currentUsaha = response.body() as Usaha
                    fillForm()
                    println(currentUsaha.namaPenanggungJawab)
                }else{
                    println("respon ${response.body()}" )
                }
            }

        })
    }

    private fun fillForm(){
        etPj.setText(currentUsaha.namaPenanggungJawab)
        etNama.setText(currentUsaha.nama)
        etAlamat.setText(currentUsaha.alamat)
    }

    private fun getToken(): String? {
        val SharedPreferences = this.getSharedPreferences("USER", Context.MODE_PRIVATE)
        return SharedPreferences.getString("TOKEN", "UNDIFINED")
    }

    private fun fetchKecamatan(){
        apiKecamatan.getKecamatan("Token ${getToken()}").enqueue(object : Callback<List<Kecamatan>> {
            override fun onFailure(call: Call<List<Kecamatan>>, t: Throwable) {
                println(t.message)
                Toast.makeText(this@Adan2Activity, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<Kecamatan>>, response: Response<List<Kecamatan>>) {
                if(response.isSuccessful){
                    kecamatans = (response.body() as List<Kecamatan>).toMutableList()
                    fetchDesa()
                }
            }
        })
    }

    private fun fetchDesa(){
        apiDesa.getDesa("Token ${getToken()}").enqueue(object : Callback<List<Desa>> {
            override fun onFailure(call: Call<List<Desa>>, t: Throwable) {
                println(t.message)
                Toast.makeText(this@Adan2Activity, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<Desa>>, response: Response<List<Desa>>) {
                if(response.isSuccessful){
                    desas = response.body() as MutableList<Desa>
                    filterDesa(currentUsaha.desa.toInt())
                    spinnerKecamatanBehavior()
                    spinnerJenisIzinBehavior()
                    spinnerKepemilikanBehavior()
                }
            }
        })
    }

    private fun spinnerKecamatanBehavior(){
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, kecamatans)
        spinnerKecamatan.adapter = adapter
        spinnerKecamatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val kecamatan = adapterView.getItemAtPosition(i) as Kecamatan
                filterDesa(kecamatan.id)
            }
            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        val desa = selectedDesa()
        for (position in 0 until adapter.count) {
            val x = adapter.getItem(position) as Kecamatan
            if (x.id == desa.kecamatan) {
                spinnerKecamatan.setSelection(position)
                return
            }
        }
    }
    private fun filterDesa(id_kecamatan : Int){
        val temp = ArrayList<Desa>()
        for (ds in desas) {
            if (ds.kecamatan == id_kecamatan) {
                temp.add(ds)
            }
        }
        if (temp.isEmpty()) {
            temp.add(Desa(0, "Tidak ada desa", 0))
            spinnerDesa.isEnabled = false
        } else {
            spinnerDesa.isEnabled = true
        }
        filteredDesa = temp
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, filteredDesa)
        spinnerDesa.adapter = adapter
    }

    private fun selectedDesa() : Desa{
        var selectedDesa = Desa()
        for(ds in desas){
            if(ds.id == currentUsaha.desa.toInt()){
                selectedDesa = ds
                break
            }
        }
        return selectedDesa
    }


    private fun spinnerJenisIzinBehavior() {
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, jenisIzins)
        spinnerJenisIzin.adapter = adapter
        for (position in 0 until adapter.count) {
            val x = adapter.getItem(position) as JenisIzin
            if (x.id.equals(currentUsaha.jenis)) {
                spinnerJenisIzin.setSelection(position)
                return
            }
        }
    }

    private fun spinnerKepemilikanBehavior() {
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, kepemilikans)
        spinnerKepemilikan.adapter = adapter
        for (position in 0 until adapter.count) {
            val x = adapter.getItem(position) as Kepemilikan
            if (x.id.equals(currentUsaha.kepemilikan)) {
                spinnerKepemilikan.setSelection(position)
                return
            }
        }
    }
}
