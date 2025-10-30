package com.example.ac_poligonosprovincias_29oct;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    private Spinner spinnerProvincias;
    private Button btnGenerar;
    private Button btnLimpiar;

    private HashMap<String, LatLng> provinciasCoords = new HashMap<>();
    private ArrayAdapter<String> adapter;
    private List<String> provinciasDisponibles = new ArrayList<>();
    private List<LatLng> provinciasSeleccionadas = new ArrayList<>();
    private Polygon poligonoActual; // referencia al polígono actual
    private HashMap<String, Integer> provinciaIconos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        spinnerProvincias = findViewById(R.id.spinnerProvincias);
        btnGenerar = findViewById(R.id.btnGenerar);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        // Cargar el fragmento del mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;

        inicializarProvincias();
        provinciasDisponibles.addAll(provinciasCoords.keySet());


        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                provinciasDisponibles);
        spinnerProvincias.setAdapter(adapter);

        spinnerProvincias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String provincia = parent.getItemAtPosition(position).toString();
                LatLng coord = provinciasCoords.get(provincia);

                if (coord != null) {
                    Integer iconResId = provinciaIconos.get(provincia);
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(coord)
                            .title(provincia);
                    if (iconResId != null) {
                        Bitmap original = BitmapFactory.decodeResource(getResources(), iconResId);
                        Bitmap scaled = Bitmap.createScaledBitmap(original, 100, 100, false);
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(scaled));
                    } else {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    }
                    markerOptions.anchor(0.5f, 0.5f);
                    markerOptions.infoWindowAnchor(0.5f, -0.2f);
                    myMap.addMarker(markerOptions);


                    provinciasSeleccionadas.add(coord);
                    provinciasDisponibles.remove(provincia);
                    adapter.notifyDataSetChanged();
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 6f));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnGenerar.setOnClickListener(v -> generarPoligonoCircular());
        btnLimpiar.setOnClickListener(v -> limpiarMapa());
    }

    private void generarPoligonoCircular() {
        // Eliminar polígono anterior
        if (poligonoActual != null) {
            poligonoActual.remove();
        }

        if (provinciasSeleccionadas.size() >= 3) {
            // Calcular punto central (promedio)
            double latSum = 0, lngSum = 0;
            for (LatLng p : provinciasSeleccionadas) {
                latSum += p.latitude;
                lngSum += p.longitude;
            }
            final double latCentro = latSum / provinciasSeleccionadas.size();
            final double lngCentro = lngSum / provinciasSeleccionadas.size();

            // Ordenar los puntos por ángulo polar (sentido antihorario)
            List<LatLng> ordenadas = new ArrayList<>(provinciasSeleccionadas);
            Collections.sort(ordenadas, new Comparator<LatLng>() {
                @Override
                public int compare(LatLng a, LatLng b) {
                    double angA = Math.atan2(a.latitude - latCentro, a.longitude - lngCentro);
                    double angB = Math.atan2(b.latitude - latCentro, b.longitude - lngCentro);
                    return Double.compare(angA, angB);
                }
            });

            // Dibujar el polígono ordenado (no se cruzan líneas)
            PolygonOptions polygonOptions = new PolygonOptions()
                    .addAll(ordenadas)
                    .strokeColor(0xFFFF0000)
                    .fillColor(0x44FF0000)
                    .strokeWidth(5);
            poligonoActual = myMap.addPolygon(polygonOptions);
        }
    }
    private void limpiarMapa() {
        myMap.clear();
        provinciasSeleccionadas.clear();
        provinciasDisponibles.clear();
        provinciasDisponibles.addAll(provinciasCoords.keySet());
        adapter.notifyDataSetChanged();
        poligonoActual = null;
    }

    private void inicializarProvincias() {
        provinciasCoords.put("Andrés Ibáñez", new LatLng(-17.846997548435276, -63.20462197764984));
        provinciasCoords.put("Ángel Sandóval", new LatLng(-17.0795129197428, -58.81962337497668));
        provinciasCoords.put("Chiquitos", new LatLng(-17.736072470114358, -60.80540622408801));
        provinciasCoords.put("Cordillera", new LatLng(-18.841272650062606, -61.935224213320424));
        provinciasCoords.put("Florida", new LatLng(-18.103959361249462, -63.912939494547665));
        provinciasCoords.put("Germán Busch", new LatLng(-18.691081199556532, -58.37129670217149));
        provinciasCoords.put("Guarayos", new LatLng(-15.252360763472685, -63.25302387779408));
        provinciasCoords.put("Ichilo", new LatLng(-16.90911867729617, -64.1930341670344));
        provinciasCoords.put("Ignacio Warnes", new LatLng(-17.338742817530438, -62.987116856944034));
        provinciasCoords.put("José Miguel de Velasco", new LatLng(-15.439158537769787, -60.936655727945265));
        provinciasCoords.put("Manuel María Caballero", new LatLng(-17.804604176947272, -64.45950233040124));
        provinciasCoords.put("Ñuflo de Chávez", new LatLng(-15.664731102087076, -62.22561568562163));
        provinciasCoords.put("Obispo Santistevan", new LatLng(-16.65662662910246, -63.45500671943528));
        provinciasCoords.put("Sara", new LatLng(-16.800764935940023, -63.74234759493154));
        provinciasCoords.put("Vallegrande", new LatLng(-18.6161258094168, -64.00644214245281));

        provinciaIconos.put("Andrés Ibáñez", R.drawable.andresibanez);
        provinciaIconos.put("Ángel Sandóval", R.drawable.angelsandoval);
        provinciaIconos.put("Chiquitos", R.drawable.chiquitos);
        provinciaIconos.put("Cordillera", R.drawable.cordillera);
        provinciaIconos.put("Florida", R.drawable.florida);
        provinciaIconos.put("Germán Busch", R.drawable.germanbusch);
        provinciaIconos.put("Guarayos", R.drawable.guarayos);
        provinciaIconos.put("Ichilo", R.drawable.ichilo);
        provinciaIconos.put("Ignacio Warnes", R.drawable.warnes);
        provinciaIconos.put("José Miguel de Velasco", R.drawable.velasco);
        provinciaIconos.put("Manuel María Caballero", R.drawable.manuelmariaca);
        provinciaIconos.put("Ñuflo de Chávez", R.drawable.nuflochavez);
        provinciaIconos.put("Obispo Santistevan", R.drawable.ovisposanti);
        provinciaIconos.put("Sara", R.drawable.sara);
        provinciaIconos.put("Vallegrande", R.drawable.vallegrande);
    }
}