package com.example.reprodcutor;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnSelect;
    private ImageView ivCover;
    private TextView tvFileName, tvCurrentTime, tvTotalTime;
    private SeekBar seekBar;
    private ImageButton btnPrevious, btnPlayPause, btnNext;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    // Lista para manejar múltiples canciones
    private List<Uri> playlist = new ArrayList<>();
    private int currentIndex = -1;

    // Lanzador para seleccionar múltiples archivos
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    playlist.clear();

                    // Revisar si el usuario seleccionó varios archivos o solo uno
                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            playlist.add(clipData.getItemAt(i).getUri());
                        }
                    } else if (data.getData() != null) {
                        playlist.add(data.getData());
                    }

                    if (!playlist.isEmpty()) {
                        currentIndex = 0;

                        // Mensaje para confirmar cuántas pistas se cargaron
                        Toast.makeText(MainActivity.this, "Pistas cargadas: " + playlist.size(), Toast.LENGTH_SHORT).show();

                        reproducirAudio(playlist.get(currentIndex));
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelect = findViewById(R.id.btnSelect);
        ivCover = findViewById(R.id.ivCover);
        tvFileName = findViewById(R.id.tvFileName);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        seekBar = findViewById(R.id.seekBar);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);

        btnSelect.setOnClickListener(v -> abrirSelector());
        btnPlayPause.setOnClickListener(v -> alternarReproduccion());
        btnNext.setOnClickListener(v -> siguientePista());
        btnPrevious.setOnClickListener(v -> pistaAnterior());

        // Permitir al usuario adelantar o atrasar la canción con la barra
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatoTiempo(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void abrirSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Permite seleccionar varios archivos
        filePickerLauncher.launch(intent);
    }


    private void alternarReproduccion() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setBackgroundResource(R.drawable.ic_play);
        } else {
            mediaPlayer.start();
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            actualizarBarraProgreso();
        }
    }

    private void siguientePista() {
        if (playlist.isEmpty() || currentIndex == -1) return;

        // Validar si solo hay una canción cargada
        if (playlist.size() == 1) {
            Toast.makeText(this, "Solo hay 1 archivo en la lista", Toast.LENGTH_SHORT).show();
            return;
        }

        currentIndex++;
        if (currentIndex >= playlist.size()) {
            currentIndex = 0; // Vuelve al inicio si llega al final
        }
        reproducirAudio(playlist.get(currentIndex));
    }

    private void pistaAnterior() {
        if (playlist.isEmpty() || currentIndex == -1) return;

        // Validar si solo hay una canción cargada
        if (playlist.size() == 1) {
            Toast.makeText(this, "Solo hay 1 archivo en la lista", Toast.LENGTH_SHORT).show();
            return;
        }

        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = playlist.size() - 1; // Va al final si está en la primera
        }
        reproducirAudio(playlist.get(currentIndex));
    }

    // Hilo en segundo plano para actualizar la barra visualmente
    private Runnable actualizarSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int pos = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(pos);
                tvCurrentTime.setText(formatoTiempo(pos));
                handler.postDelayed(this, 1000); // Se actualiza cada segundo
            }
        }
    };

    private void actualizarBarraProgreso() {
        handler.removeCallbacks(actualizarSeekBar);
        handler.post(actualizarSeekBar);
    }

    private String formatoTiempo(int milisegundos) {
        int minutos = (milisegundos / 1000) / 60;
        int segundos = (milisegundos / 1000) % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    // Método para obtener el nombre sin la ruta completa
    private String obtenerNombreArchivo(Context context, Uri uri) {
        String resultado = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    resultado = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (resultado == null) {
            resultado = uri.getPath();
            int corte = resultado.lastIndexOf('/');
            if (corte != -1) {
                resultado = resultado.substring(corte + 1);
            }
        }
        return resultado;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(actualizarSeekBar);
    }
    private void cargarPortada(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            byte[] art = retriever.getEmbeddedPicture();

            if (art != null) {
                // Si el archivo tiene portada, la convierte y la muestra
                Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                ivCover.setImageBitmap(bitmap);
            } else {
                // Si no tiene portada, limpia el ImageView
                ivCover.setImageDrawable(null);
                // Aquí podrías poner una imagen por defecto usando:
                // ivCover.setImageResource(R.drawable.tu_imagen_por_defecto);
            }
        } catch (Exception e) {
            ivCover.setImageDrawable(null);
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void reproducirAudio(Uri uri) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();

            // Extraer nombre real del archivo
            tvFileName.setText(obtenerNombreArchivo(this, uri));

            int duracion = mediaPlayer.getDuration();
            tvTotalTime.setText(formatoTiempo(duracion));
            seekBar.setMax(duracion);
            seekBar.setProgress(0);

            // ---> AQUÍ CARGAMOS LA PORTADA <---
            cargarPortada(uri);

            mediaPlayer.start();
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            actualizarBarraProgreso();

            // Cuando termine la canción, pasar automáticamente a la siguiente
            mediaPlayer.setOnCompletionListener(mp -> siguientePista());

        } catch (IOException e) {
            Toast.makeText(this, "Error al cargar el archivo", Toast.LENGTH_SHORT).show();
        }
    }
}