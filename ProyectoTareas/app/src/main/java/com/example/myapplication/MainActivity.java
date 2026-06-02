package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.ContextThemeWrapper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private EditText campoTarea;
    private Button btnAgregar;
    private LinearLayout contenedorTareas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs=getSharedPreferences("Tareas",MODE_PRIVATE);
        campoTarea=findViewById(R.id.campoTarea);
        btnAgregar=findViewById(R.id.btnAgregar);
        contenedorTareas=findViewById(R.id.contenedorTareas);
        btnAgregar.setOnClickListener(view -> {
            agregarNuevaTarea();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        cargarTareasGuardadas();
    }

    private CheckBox crearCheckBox(String texto, boolean checked){
        CheckBox cb=new CheckBox(
                new ContextThemeWrapper(this,R.style.CheckBoxTarea)
        );
        cb.setButtonDrawable(R.drawable.checkbox_personalizado);
        cb.setPadding(20,10,10,10);
        cb.setText(texto);
        cb.setTextSize(20);
        cb.setChecked(checked);
        cb.setOnCheckedChangeListener((buttonView,isChecked)->{
            cb.animate()
                    .alpha(0f)
                    .translationY(isChecked ? 40 : -40)
                    .setDuration(150)
                    .withEndAction(() -> {

                        contenedorTareas.removeView(cb);

                        if (isChecked) {
                            // Marcado → al final
                            contenedorTareas.addView(cb);
                        } else {
                            // Desmarcado → antes del primer checkbox marcado
                            int posicion = obtenerPosicionPrimerChecked();
                            contenedorTareas.addView(cb, posicion);
                        }

                        cb.setTranslationY(0);
                        cb.animate().alpha(1f).setDuration(150).start();
                        guardarTareas();
                    })
                    .start();
        });
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 13, 0, 13);// margen: izquierda, arriba, derecha, abajo
        cb.setLayoutParams(params);
        return cb;
    }

    private int obtenerPosicionPrimerChecked() {
        for (int i = 0; i < contenedorTareas.getChildCount(); i++) {
            CheckBox cb = (CheckBox) contenedorTareas.getChildAt(i);
            if (cb.isChecked()) {
                return i;
            }
        }
        // Si no hay ninguno marcado, va al final
        return contenedorTareas.getChildCount();
    }

    private void agregarNuevaTarea(){
        String txtTarea=campoTarea.getText().toString().trim(); //.trim elimina los espacios basura
        if(!txtTarea.isEmpty()){
           CheckBox nuevaCheck=crearCheckBox(txtTarea,false);
           contenedorTareas.addView(nuevaCheck);
           campoTarea.setText("");
           campoTarea.clearFocus();
           guardarTareas();
        }
    }
    //Metodos para persistencia de datos (Guardado de tareas)
    private void guardarTareas(){
        StringBuilder sb= new StringBuilder();
        for (int i=0;i<contenedorTareas.getChildCount();i++){
            CheckBox cb=(CheckBox) contenedorTareas.getChildAt(i);
            sb.append(cb.getText())
                    .append("|")
                    .append(cb.isChecked())
                    .append(";;;");
        }
        prefs.edit().putString("lista_tareas",sb.toString()).apply();
    }

    private void cargarTareasGuardadas(){
        String listaRaw=prefs.getString("lista_tareas","");
        if(!listaRaw.isEmpty()){
            String[]tareas=listaRaw.split(";;;");
            for(String t:tareas){
                if(!t.isEmpty()){
                    String[] partes=t.split("\\|");
                    String texto=partes[0];
                    boolean checked=Boolean.parseBoolean(partes[1]);
                    CheckBox cb=crearCheckBox(texto,checked);
                    contenedorTareas.addView(cb);
                }
            }
        }
    }
}