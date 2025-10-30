package com.example.ac_poligonosprovincias_29oct;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;

public class FragmentControles extends Fragment {

    public interface OnFormularioListener {
        void onProvinciaSeleccionada(String provincia);
        void onGenerar();
        void onLimpiar();
    }

    private OnFormularioListener listener;
    private Spinner spinner;
    private Button btnGenerar, btnLimpiar;

    // la Activity te inyectará la lista actual (con "Provincia" + claves del mapa)
    private List<String> provinciasDisponibles;
    public void setProvinciasDisponibles(List<String> lista) { this.provinciasDisponibles = lista; }

    @Override public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFormularioListener) listener = (OnFormularioListener) context;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_controles, container, false);
        spinner = v.findViewById(R.id.spinnerProvincias);
        btnGenerar = v.findViewById(R.id.btnGenerar);
        btnLimpiar = v.findViewById(R.id.btnLimpiar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                provinciasDisponibles
        );
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String p = parent.getItemAtPosition(pos).toString();
                if ("Provincia".equals(p)) return; // placeholder
                if (listener != null) listener.onProvinciaSeleccionada(p);
                // remover del spinner para que no se repita
                provinciasDisponibles.remove(p);
                adapter.notifyDataSetChanged();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnGenerar.setOnClickListener(v1 -> { if (listener != null) listener.onGenerar(); });
        btnLimpiar.setOnClickListener(v12 -> { if (listener != null) listener.onLimpiar(); });

        return v;
    }
}
