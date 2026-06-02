package com.example.reloj;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

public class CronometroFragment extends Fragment {

    private Button btnIniciar, btnRestablecer;
    private Handler handler;
    private boolean isRunning = false;
    private long tiempoIniciado = 0L;
    private long tiempoEnMillis = 0L;
    private long tiempoBuzon = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cronometro, container, false);

        btnIniciar = view.findViewById(R.id.btn_iniciar_cronometro);
        btnRestablecer = view.findViewById(R.id.btn_restablecer_cronometro);
        ImageView btnMenu = view.findViewById(R.id.btn_menu_cronometro);

        handler = new Handler(Looper.getMainLooper());

        btnMenu.setOnClickListener(v -> {
            DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);
            if (drawer != null) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        btnIniciar.setOnClickListener(v -> {
            if (!isRunning) {
                tiempoIniciado = SystemClock.uptimeMillis();
                handler.post(runnableCronometro);
                isRunning = true;
                btnIniciar.setText("Pausar");
            } else {
                tiempoBuzon += tiempoEnMillis;
                handler.removeCallbacks(runnableCronometro);
                isRunning = false;
                btnIniciar.setText("Iniciar");
            }
        });

        btnRestablecer.setOnClickListener(v -> {
            handler.removeCallbacks(runnableCronometro);
            isRunning = false;
            tiempoIniciado = 0L;
            tiempoEnMillis = 0L;
            tiempoBuzon = 0L;

            // Resetear todos los TextViews a 0
            int[] ids = {R.id.tv_h1, R.id.tv_h2, R.id.tv_m1, R.id.tv_m2, R.id.tv_s1, R.id.tv_s2, R.id.tv_c1, R.id.tv_c2};
            for (int id : ids) {
                TextView tv = view.findViewById(id);
                if (tv != null) tv.setText("0");
            }
            btnIniciar.setText("Iniciar");
        });

        return view;
    }

    private final Runnable runnableCronometro = new Runnable() {
        @Override
        public void run() {
            tiempoEnMillis = SystemClock.uptimeMillis() - tiempoIniciado;
            long tiempoTotal = tiempoBuzon + tiempoEnMillis;

            int segT = (int) (tiempoTotal / 1000);
            int h = segT / 3600;
            int m = (segT % 3600) / 60;
            int s = segT % 60;
            int c = (int) (tiempoTotal % 1000) / 10;

            View view = getView();
            if (view != null) {
                ((TextView) view.findViewById(R.id.tv_h1)).setText(String.valueOf(h / 10));
                ((TextView) view.findViewById(R.id.tv_h2)).setText(String.valueOf(h % 10));
                ((TextView) view.findViewById(R.id.tv_m1)).setText(String.valueOf(m / 10));
                ((TextView) view.findViewById(R.id.tv_m2)).setText(String.valueOf(m % 10));
                ((TextView) view.findViewById(R.id.tv_s1)).setText(String.valueOf(s / 10));
                ((TextView) view.findViewById(R.id.tv_s2)).setText(String.valueOf(s % 10));
                ((TextView) view.findViewById(R.id.tv_c1)).setText(String.valueOf(c / 10));
                ((TextView) view.findViewById(R.id.tv_c2)).setText(String.valueOf(c % 10));
            }

            handler.postDelayed(this, 20);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (isRunning) {
            tiempoBuzon += tiempoEnMillis;
            handler.removeCallbacks(runnableCronometro);
            isRunning = false;
            btnIniciar.setText("Iniciar");
        }
    }
}