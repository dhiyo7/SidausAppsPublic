package dev7.id.sidausappspublic.Activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dev7.id.pakhendrawan.Helper.Utils
import dev7.id.sidausappspublic.Model.*
import dev7.id.sidausappspublic.R
import dev7.id.sidausappspublic.Server.ApiUtil
import kotlinx.android.synthetic.main.activity_adan2.*
import kotlinx.android.synthetic.main.content_ubah_usaha.*
import kotlinx.android.synthetic.main.content_ubah_usaha.btnSimpan
import kotlinx.android.synthetic.main.content_ubah_usaha.etAlamat
import kotlinx.android.synthetic.main.content_ubah_usaha.etNama
import kotlinx.android.synthetic.main.content_ubah_usaha.etPj
import kotlinx.android.synthetic.main.content_ubah_usaha.spinnerDesa
import kotlinx.android.synthetic.main.content_ubah_usaha.spinnerJenisIzin
import kotlinx.android.synthetic.main.content_ubah_usaha.spinnerKecamatan
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class AduanActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private var apiKecamatan = ApiUtil.getKecamatanInterface()
    private var apiDesa = ApiUtil.getDesaInterface()
    private var api= ApiUtil.getUsahaInterface()
    private var currentUsaha = Usaha()
    private var filteredDesa = mutableListOf<Desa>()
    private var desas = mutableListOf<Desa>()
    private var kecamatans = mutableListOf<Kecamatan>()
    private var jenisIzins = Utils.getJenisIzins()
    private var myLocation : Location? = null
    var mMap: GoogleMap? = null
    var mapFragment: SupportMapFragment? = null
    internal lateinit var customView: View
    internal lateinit var nestedScrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adan2)
        customView = findViewById<View>(R.id.customView)
        nestedScrollView = findViewById(R.id.parent)
        mapFragment = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment!!.getMapAsync(this)
        fetchUsaha(getIdUsaha())
        fetchKecamatan()
        postAduan()
    }



    private fun postAduan() {
        btnSimpan.setOnClickListener {
            val pj = etPj.text.toString().trim()
            val alamat = etAlamat.text.toString().trim()
            val nama = etNama.text.toString().trim()
            val isi_aduan = etIsi.text.toString().trim()
            val jawaban = etJawaban.text.toString().trim()
            val verify = "N"


            if (!pj.isEmpty() && !alamat.isEmpty() && !nama.isEmpty() && isi_aduan.isNotEmpty() && jawaban.isNotEmpty()) {
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
                    Toast.makeText(this@AduanActivity, "Desa belum dipilih", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if(myLocation == null){
                    Toast.makeText(this@AduanActivity, "Cannot get location. Please check your permission", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                btnSimpan.isEnabled=false
                val ji = spinnerJenisIzin.selectedItem as JenisIzin
                val desa = spinnerDesa.selectedItem as Desa
                val req = api.postAduan("Token ${getToken()}", nama, pj, alamat, verify, ji.id, desa.id, isi_aduan, jawaban, myLocation?.latitude, myLocation?.longitude)
                req.enqueue(object : Callback<Aduan>{
                    override fun onFailure(call: Call<Aduan>, t: Throwable) {
                        Toast.makeText(this@AduanActivity, "Failure ${t.message}", Toast.LENGTH_LONG).show()
                        btnSimpan.isEnabled = true
                    }

                    override fun onResponse(call: Call<Aduan>, response: Response<Aduan>) {
                        if(response.isSuccessful){
                            Toast.makeText(this@AduanActivity, "Success", Toast.LENGTH_LONG).show()
                            finish()
                        }else{
                            Toast.makeText(this@AduanActivity, "Something went wrong", Toast.LENGTH_LONG).show()
                            btnSimpan.isEnabled = true
                        }
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
                Toast.makeText(this@AduanActivity, "gagal update", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@AduanActivity, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@AduanActivity, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<Desa>>, response: Response<List<Desa>>) {
                if(response.isSuccessful){
                    desas = response.body() as MutableList<Desa>
                    filterDesa(currentUsaha.desa.toInt())
                    spinnerKecamatanBehavior()
                    spinnerJenisIzinBehavior()
//                    spinnerKepemilikanBehavior()
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

    override fun onMapReady(p0: GoogleMap?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this@AduanActivity, "Anda harus mengizinkan akses lokasi", Toast.LENGTH_LONG).show()
            return
        }
        val mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@AduanActivity)
        mFusedLocationProviderClient.lastLocation.apply {
            addOnCompleteListener {
                if(it.isSuccessful){
                    it.result?.let {
                        myLocation = it
                        mMap = p0
                        val point = LatLng(myLocation!!.latitude, myLocation!!.longitude)
                        mMap?.apply {
                            uiSettings?.isZoomControlsEnabled = true
                            uiSettings?.isMyLocationButtonEnabled = true
                            uiSettings?.isTiltGesturesEnabled = true
                            animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().target(point).zoom(15f).build()))
                            clear()
                            isMyLocationEnabled = true
                            mMap?.setOnMapClickListener(this@AduanActivity)
                        }
                    }
                }
            }
            addOnFailureListener {
                Toast.makeText(this@AduanActivity, "Tidak dapat mengambil lokasi", Toast.LENGTH_LONG).show()
            }
        }

    }


    override fun onMapClick(p0: LatLng?) {
        Toast.makeText(this@AduanActivity, "Location changet to lat : ${p0?.latitude} and lon : ${p0?.longitude}", Toast.LENGTH_LONG).show()
        val marker = MarkerOptions().position(LatLng(p0!!.latitude, p0.longitude))
        mMap?.clear()
        mMap?.addMarker(marker)
    }

//    override fun onMapClick(p0: LatLng?) {
//        latitude = atLng.latitude
//        longitude = latLng.longitude
//        alamat = ApiUtils.getAddressSimple(latitude, longitude, this)
//        val marker = MarkerOptions().position(LatLng(latLng.latitude, latLng.longitude))
//                .title("Lokasi saya").snippet(alamat)
//        mMap.clear()
//        mMap.addMarker(marker)
//        Log.d("catatan", "$latitude")    }

}
