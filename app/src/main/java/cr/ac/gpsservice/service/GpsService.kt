package cr.ac.gpsservice.service

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import android.os.Looper
import com.google.android.gms.location.*
import cr.ac.gpsservice.db.LocationDatabase
import cr.ac.gpsservice.entity.Location


class GpsService : IntentService("GpsService") {

    lateinit var fusedLocation: FusedLocationProviderClient // proveedor de localización google
    private lateinit var locationRequest : LocationRequest
    lateinit var locationCallback: LocationCallback

    private lateinit var locationDatabase: LocationDatabase

    // Constante de clase
    companion object{
        var GPS= "cr.ac.GpsService.GPS_EVENT"
    }

    override fun onHandleIntent(intent: Intent?) {
        locationDatabase = LocationDatabase.getInstance(this)
        getLocation()
    }


    /**
     * Inicializa los atributos locationCallback y fusedLocation
     * colaca un intervalo de actualizacion de 10000 y una prioridad de PRIORITY_HIGH_ACCURACY
     * recibe la ubicacion del gps mediante un onLocationResult
     * y envia un broadcast con el una instancia de Location y la accion GPS (cr.ac.gpsservice.GPS_EVENT)
     * ademas guarda la localizacion en la BD
     * */
    @SuppressLint("MissingPermission")
    fun getLocation(){

        fusedLocation= LocationServices.getFusedLocationProviderClient(this)

        //val locationRequest=LocationRequest.create()
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 10000 // If not here
        locationRequest.fastestInterval = 5000  // If it can it'll do it here
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            // determina que se hace cuando hay una ubicacion gps
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationRequest==null) {
                    return
                } // Dibujar en el mapa los puntos
                for (location in locationResult.locations) {
                    val ubicacion = Location(null,location.latitude,location.longitude)
                    val bcIntent=Intent()
                    //bcIntent.action=GPS
                    bcIntent.setAction(GPS)
                    bcIntent.putExtra("location", ubicacion)
                    sendBroadcast(bcIntent)
                    locationDatabase.locationDao.insert(Location(null, ubicacion.latitude, ubicacion.longitude))
                    LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                }
            }
        }
        fusedLocation.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
        Looper.loop()

    }
}