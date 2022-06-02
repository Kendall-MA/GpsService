package cr.ac.gpsservice


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import cr.ac.gpsservice.databinding.ActivityMapsBinding
import cr.ac.gpsservice.db.LocationDatabase
import cr.ac.gpsservice.entity.Location
import cr.ac.gpsservice.service.GpsService

private lateinit var mMap: GoogleMap
private lateinit var locationDatabase: LocationDatabase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var binding: ActivityMapsBinding
    private val SOLICITAR_GPS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationDatabase=LocationDatabase.getInstance(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        validaPermisos()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        iniciaServicio()
        recuperarPuntos(mMap)
    }

    /**
     * Obtener los puntos almacenados en la bd y mostarlos en el mapa
     * */
    fun recuperarPuntos(googleMap:GoogleMap){
        mMap = googleMap

        for(location in locationDatabase.locationDao.query()){
            val ubicacion = LatLng(location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions().position(ubicacion).title("Nueva Ubicación"))
        }

    }


    /**
     * Hace un filtro del broadcast/accion GPS (cr.ac.gpsservice.GPS_EVENT)
     * e inicia el servicio (startService) GpsService
     * */
    fun iniciaServicio(){
        val filter= IntentFilter()
        val progreso = ProgressReceiver()
        filter.addAction(GpsService.GPS)

        registerReceiver(progreso,filter)

        val gpsService = Intent(this,GpsService::class.java)
        startService(gpsService)
    }

    /**
     * Valida si la app tiene permisos de ACCESS_FINE_LOCATION Y ACCESS_COARSE_LOCATION
     * si no tiene permisos solicita al usuario permisos (requestPermissions)
     * */
    fun validaPermisos(){
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            // NO TENGO PERMISOS
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                SOLICITAR_GPS
            )
        }
    }

    /**
     * Valdar que se le dieron los permisos a la app, en caso contrario salir
     * */
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            SOLICITAR_GPS -> {
                if ( grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    System.exit(1)
                }

            }
        }
    }

    /**
     * Es la clase para recibir los mensajes de broadcast
     * */
    class ProgressReceiver:BroadcastReceiver(){
        /**
         * Se obtiene el parametro enviado por el servicio (Location)
         * Coloca en el mapa la localizacion
         * Mueve la camara a esa localizacion
         */
        override fun onReceive(p0: Context, p1: Intent) {
            when (p1?.action) {
                GpsService.GPS -> {
                    val localizacion:Location= p1.getSerializableExtra("location") as Location
                    val punto=LatLng(localizacion.latitude,localizacion.longitude)
                    mMap.addMarker(MarkerOptions().position(punto).title("Nueva Ubicación"))
                }
            }
        }
    }
}