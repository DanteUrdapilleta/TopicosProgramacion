package com.example.reloj;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat; // Importante: Asegúrate de tener esta importación
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RelojFragment extends Fragment {

    private TextView tvHoraDigital;
    private Handler handler;
    private Runnable runnableHora;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reloj, container, false);

        tvHoraDigital = view.findViewById(R.id.tv_hora_digital);

        // Control del menú desplegable superior
        ImageView btnMenu = view.findViewById(R.id.btn_menu_reloj);
        btnMenu.setOnClickListener(v -> {
            DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);
            if (drawer != null) {
                // Cambiado a GravityCompat.START que es el estándar compatible
                drawer.openDrawer(GravityCompat.START);
            }
        });

        // Hilo encargado de refrescar el reloj digital inferior en tiempo real
        handler = new Handler(Looper.getMainLooper());
        runnableHora = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                String horaActual = sdf.format(new Date());
                tvHoraDigital.setText(horaActual);

                // Re-ejecutar cada segundo
                handler.postDelayed(this, 1000);
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(runnableHora);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnableHora);
    }
}