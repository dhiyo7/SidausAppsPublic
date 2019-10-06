package dev7.id.sidausappspublic.Activity

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import dev7.id.pakhendrawan.Helper.Utils
import dev7.id.sidausappspublic.Model.Desa
import dev7.id.sidausappspublic.Model.Kecamatan
import dev7.id.sidausappspublic.Model.Usaha
import dev7.id.sidausappspublic.R
import dev7.id.sidausappspublic.Server.ApiUtil

import kotlinx.android.synthetic.main.activity_aduan.*
import com.google.android.gms.maps.SupportMapFragment




class AduanActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {


    private var apiKecamatan = ApiUtil.getKecamatanInterface()
    private var apiDesa = ApiUtil.getDesaInterface()
    private var api= ApiUtil.getUsahaInterface()
    private var currentUsaha = Usaha()
    private var filteredDesa = mutableListOf<Desa>()
    private var desas = mutableListOf<Desa>()
    private var kecamatans = mutableListOf<Kecamatan>()
    private var jenisIzins = Utils.getJenisIzins()
    private var kepemilikans = Utils.getKepemilikans()

//    var latitude: Double = 0.toDouble() , var longitude:Double = 0.toDouble()
    var mMap: GoogleMap? = null
    var mapFragment: SupportMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aduan)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMapClick(p0: LatLng?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
