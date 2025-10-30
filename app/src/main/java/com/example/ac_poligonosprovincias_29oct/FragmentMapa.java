package com.example.ac_poligonosprovincias_29oct;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FragmentMapa extends Fragment implements OnMapReadyCallback {

    private GoogleMap myMap;
    private Polygon poligonoActual;

    // listas compartidas
    private final List<LatLng> provinciasSeleccionadas = new ArrayList<>();
    // mapa nombre → drawable id (lo recibirás desde la Activity)
    private Map<String, Integer> provinciaIconos;

    public void setProvinciaIconos(Map<String, Integer> provinciaIconos) {
        this.provinciaIconos = provinciaIconos;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mapa, container, false);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng centroSantaCruz = new LatLng(-17.7833, -63.1821);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centroSantaCruz, 6f));
    }

    public void agregarProvincia(String nombre, LatLng coord) {
        if (myMap == null || coord == null) return;

        MarkerOptions markerOptions = new MarkerOptions()
                .position(coord)
                .title(nombre);

        Integer iconResId = (provinciaIconos != null) ? provinciaIconos.get(nombre) : null;
        if (iconResId != null) {
            Bitmap original = BitmapFactory.decodeResource(getResources(), iconResId);
            Bitmap scaled = Bitmap.createScaledBitmap(original, 100, 100, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(scaled));
        } else {
            markerOptions.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        }

        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.infoWindowAnchor(0.5f, -0.2f);
        myMap.addMarker(markerOptions);

        provinciasSeleccionadas.add(coord);
        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 6f));
    }

    /** Genera polígono SIN cruces (orden angular) – puedes cambiar color a gusto */
    public void generarPoligonoVerde() {
        if (myMap == null) return;

        if (poligonoActual != null) poligonoActual.remove();
        if (provinciasSeleccionadas.size() < 3) return;

        double latSum = 0, lngSum = 0;
        for (LatLng p : provinciasSeleccionadas) { latSum += p.latitude; lngSum += p.longitude; }
        final double latC = latSum / provinciasSeleccionadas.size();
        final double lngC = lngSum / provinciasSeleccionadas.size();

        List<LatLng> ordenadas = new ArrayList<>(provinciasSeleccionadas);
        Collections.sort(ordenadas, new Comparator<LatLng>() {
            @Override public int compare(LatLng a, LatLng b) {
                double angA = Math.atan2(a.latitude - latC, a.longitude - lngC);
                double angB = Math.atan2(b.latitude - latC, b.longitude - lngC);
                return Double.compare(angA, angB);
            }
        });

        PolygonOptions opts = new PolygonOptions()
                .addAll(ordenadas)
                .strokeColor(0xFF008000)    // borde verde oscuro
                .fillColor(0x4400FF00)      // verde translúcido
                .strokeWidth(5);

        poligonoActual = myMap.addPolygon(opts);
    }

    /** Limpia mapa y selección */
    public void limpiar() {
        if (myMap != null) myMap.clear();
        provinciasSeleccionadas.clear();
        poligonoActual = null;
    }
}
