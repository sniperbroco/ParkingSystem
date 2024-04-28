package com.example.parkingsystem

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.parkingsystem.ui.theme.ParkingSystemTheme
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : ComponentActivity() {

    private val mapListener = object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean {
            return true
        }

        override fun onZoom(event: ZoomEvent?): Boolean {
            return false
        }
    }

    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLocationPermission() // Check and request location permission if necessary


        Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        setContent {
            ParkingSystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ParkingAreaList()
                }
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    // You can proceed with accessing the location

                    Log.e("MainActivity", "Location permission granted")
                } else {
                    // Permission was denied
                    // Handle the case where the user denied the permission
                }
            }
        }
    }


    @Composable
    fun ParkingAreaList(modifier: Modifier = Modifier.fillMaxSize()) {
        // Define the state of the search text
        var searchText by remember { mutableStateOf("Tacloban") }

        // Use a Box to layer content
        Box() {
            // OSMDroidMapView as the background covering the whole screen
            OSMDroidMapView(
                modifier = Modifier.fillMaxHeight()// This ensures the map view covers the entire screen
            )

            // Column to hold the overlays
            Column(
                modifier = Modifier
                    .fillMaxSize() // Allow the column to take the full space
                    .padding(16.dp)
            ) {
                // Top section with title and search box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Parking Areas",
                        fontSize = 20.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0XFF101921))
                    ) {
                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            textStyle = TextStyle(fontSize = 20.sp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Icon"
                                )
                            },
                            placeholder = { Text(text = "Search") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(500.dp))

                // Sort and location section
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sort by: Distance")
                    Text("UP Tacloban")
                    Text("Tacloban Place")
                }

                // Parking area items
                ParkingAreaItem(title = "RTR Plaza", price = "FREE")
                ParkingAreaItem(title = "Nique'Residence", price = "P20/HR")
            }
        }
    }


    @Composable
    fun OSMDroidMapView(modifier: Modifier = Modifier.fillMaxHeight()) {
        // Define a state to hold the MapView instance
        val mapView = remember { mutableStateOf<MapView?>(null) }

        // Use a Box to layer the MapView and FloatingActionButton
        Box() {
            // Initialize and set up the MapView
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        // Enable multi-touch controls
                        setMultiTouchControls(true)
                        // Set the tile source to OpenStreetMap Mapnik
                        setTileSource(TileSourceFactory.MAPNIK)

                        val startPoint = GeoPoint(12.8797, 121.7740)
                        controller.setCenter(startPoint)

                        controller.setZoom(6.0)
                        // Create a new MyLocationNewOverlay
                        val mLocationOverlay =
                            MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                        // Enable my location on the overlay
                        mLocationOverlay.enableMyLocation()
                        // Optionally, enable following location
                        mLocationOverlay.enableFollowLocation()
                        // Add the location overlay to the map overlays list
                        overlays.add(mLocationOverlay)
                        // Add map listener if necessary
                        addMapListener(mapListener)

                        // Store the MapView instance in the state variable
                        mapView.value = this
                    }
                },
                modifier = Modifier
                    .fillMaxHeight() // Fill the full height of the screen
            )

            // FloatingActionButton for the "location" button
            // FloatingActionButton for the "location" button
            FloatingActionButton(
                onClick = {
                    // Access the map view instance
                    val map = mapView.value

                    // Log the map instance
                    Log.e("MainActivity", "Map: $map")

                    if (map == null) {
                        // Log a message if mapView is null
                        println("MapView instance is null")
                        return@FloatingActionButton // Exit the click listener early if map is null
                    }

                    // Access the MyLocationNewOverlay
                    val locationOverlay = map.overlays.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                    if (locationOverlay == null) {
                        // Log a message if MyLocationNewOverlay is null
                        println("MyLocationNewOverlay instance is null")
                        return@FloatingActionButton // Exit the click listener early if locationOverlay is null
                    }

                    // If the overlay exists and has a valid location, center the map on the hardcoded location
                    val taclobanGeoPoint = GeoPoint(11.2443, 125.0015)
                    //map.controller.setCenter(taclobanGeoPoint)
                    //map.controller.setZoom(9.0) // Adjust the zoom level as needed

                    map.controller.animateTo(taclobanGeoPoint, 9.0, 2000)

                    // Log the current map center
                    val currentCenter = map.getMapCenter()
                    Log.e("MainActivity", "Location: ${currentCenter.latitude}, ${currentCenter.longitude}")
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd), // Align the button to the bottom-right corner
                content = {
                    // Icon for the FloatingActionButton
                    Icon(
                        imageVector = Icons.Default.LocationOn, // Choose the appropriate icon
                        contentDescription = "Show My Location"
                    )
                }
            )
        }
    }

    @Composable
    fun ParkingAreaItem(title: String, price: String) {
        Row(
            modifier = Modifier.padding(start = 16.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = title,
                modifier = Modifier.weight(0.7f)
            )

            Text(
                text = price,
                modifier = Modifier.weight(0.3f)
            )
        }
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun ParkingAreaListPreview() {
        ParkingSystemTheme {
            ParkingAreaList()
        }
    }
}