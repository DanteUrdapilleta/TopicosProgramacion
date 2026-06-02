package com.example.reloj;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import java.util.Locale;

public class TemporizadorFragment extends Fragment {

    private TextView tvHoras, tvMinutos, tvSegundos;
    private ImageButton btnPlay, btnBorrar;
    private String inputNum = ""; // Guarda los dígitos presionados

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long tiempoRestanteEnMillis = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temporizador, container, false);

        // Vincular vistas de la interfaz
        tvHoras = view.findViewById(R.id.tv_horas);
        tvMinutos = view.findViewById(R.id.tv_minutos);
        tvSegundos = view.findViewById(R.id.tv_segundos);
        btnPlay = view.findViewById(R.id.btn_play);
        btnBorrar = view.findViewById(R.id.btn_borrar);

        // Configurar botón del menú desplegable superior
        ImageView btnMenu = view.findViewById(R.id.btn_menu_temporizador);
        btnMenu.setOnClickListener(v -> {
            DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);
            drawer.openDrawer(GravityCompat.START);
        });

        // Configurar los botones numéricos del 0 al 9
        int[] botonesIds = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};
        for (int id : botonesIds) {
            view.findViewById(id).setOnClickListener(this::alPresionarNumero);
        }

        // Configurar botón de borrar
        btnBorrar.setOnClickListener(v -> {
            if (isTimerRunning) {
                detenerTemporizador();
            } else if (inputNum.length() > 0) {
                inputNum = inputNum.substring(0, inputNum.length() - 1);
                actualizarPantallaPorEntrada();
            }
        });

        // Configurar botón de Play/Pause
        btnPlay.setOnClickListener(v -> {
            if (isTimerRunning) {
                pausarTemporizador();
            } else {
                iniciarTemporizador();
            }
        });

        return view;
    }

    private void alPresionarNumero(View view) {
        if (isTimerRunning) return; // No permite escribir si ya está corriendo

        Button b = (Button) view;
        // Límite de 5 dígitos para evitar desbordar horas (Max: 9h 59m 59s aprox)
        if (inputNum.length() < 5) {
            inputNum += b.getText().toString();
            actualizarPantallaPorEntrada();
        }
    }

    private void actualizarPantallaPorEntrada() {
        // Rellenar con ceros a la izquierda para simular el recorrido
        String formateado = String.format("%05d", inputNum.isEmpty() ? 0 : Long.parseLong(inputNum));

        // Ejemplo "01234" -> Horas: 0, Minutos: 12, Segundos: 34
        String hrs = formateado.substring(0, 1);
        String mins = formateado.substring(1, 3);
        String segs = formateado.substring(3, 5);

        tvHoras.setText(hrs);
        tvMinutos.setText(mins);
        tvSegundos.setText(segs);
    }

    private void iniciarTemporizador() {
        if (tiempoRestanteEnMillis == 0) {
            int hrs = Integer.parseInt(tvHoras.getText().toString());
            int mins = Integer.parseInt(tvMinutos.getText().toString());
            int segs = Integer.parseInt(tvSegundos.getText().toString());

            long totalSegundos = (hrs * 3600) + (mins * 60) + segs;
            if (totalSegundos == 0) {
                Toast.makeText(getContext(), "Asigna un tiempo primero", Toast.LENGTH_SHORT).show();
                return;
            }
            tiempoRestanteEnMillis = totalSegundos * 1000;
        }

        countDownTimer = new CountDownTimer(tiempoRestanteEnMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestanteEnMillis = millisUntilFinished;
                actualizarPantallaConContador();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                tiempoRestanteEnMillis = 0;
                inputNum = "";
                actualizarPantallaPorEntrada();
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
                Toast.makeText(getContext(), "¡Tiempo terminado!", Toast.LENGTH_LONG).show();
            }
        }.start();

        isTimerRunning = true;
        btnPlay.setImageResource(android.R.drawable.ic_media_pause); // Cambia icono a pausa
    }

    private void pausarTemporizador() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
    }

    private void detenerTemporizador() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        tiempoRestanteEnMillis = 0;
        inputNum = "";
        actualizarPantallaPorEntrada();
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
    }

    private void actualizarPantallaConContador() {
        int horas = (int) (tiempoRestanteEnMillis / 1000) / 3600;
        int minutos = (int) ((tiempoRestanteEnMillis / 1000) % 3600) / 60;
        int segundos = (int) (tiempoRestanteEnMillis / 1000) % 60;

        tvHoras.setText(String.format(Locale.getDefault(), "%02d", horas));
        tvMinutos.setText(String.format(Locale.getDefault(), "%02d", minutos));
        tvSegundos.setText(String.valueOf(segundos));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Evita fugas de memoria al cambiar de fragmento
        }
    }
}