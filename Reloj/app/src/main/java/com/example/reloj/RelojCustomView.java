package com.example.reloj;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.Calendar;

public class RelojCustomView extends View {

    private Paint paintCirculo;
    private Paint paintManecillas;
    private int radio = 0;
    private int centroX = 0;
    private int centroY = 0;

    public RelojCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inicializar();
    }

    private void inicializar() {
        // Configuración para el círculo exterior blanco
        paintCirculo = new Paint();
        paintCirculo.setColor(Color.WHITE);
        paintCirculo.setStyle(Paint.Style.STROKE);
        paintCirculo.setStrokeWidth(8f); // Grosor de la línea
        paintCirculo.setAntiAlias(true);

        // Configuración para las manecillas del reloj
        paintManecillas = new Paint();
        paintManecillas.setColor(Color.WHITE);
        paintManecillas.setStyle(Paint.Style.STROKE);
        paintManecillas.setStrokeWidth(10f); // Un poco más gruesas
        paintManecillas.setStrokeCap(Paint.Cap.ROUND); // Puntas redondeadas
        paintManecillas.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Calcular el centro y el radio dinámicamente según el tamaño asignado
        centroX = w / 2;
        centroY = h / 2;
        int menorMedida = Math.min(w, h);
        radio = (menorMedida / 2) - 20; // Margen de seguridad para que no se corte
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (radio <= 0) return;

        // 1. Dibujar la circunferencia exterior limpia
        canvas.drawCircle(centroX, centroY, radio, paintCirculo);

        // 2. Obtener la hora actual del sistema
        Calendar calendario = Calendar.getInstance();
        float hora = calendario.get(Calendar.HOUR);
        float minuto = calendario.get(Calendar.MINUTE);
        float segundo = calendario.get(Calendar.SECOND);

        // Ajustar la hora incluyendo la fracción de los minutos pasados
        hora = hora + minuto / 60f;

        // 3. Calcular ángulos en radianes (restando 90 grados para empezar en el 12)
        double anguloHora = Math.toRadians((hora * 30) - 90);
        double anguloMinuto = Math.toRadians((minuto * 6) - 90);

        // 4. Dibujar manecilla de las Horas (más corta: 50% del radio)
        float largoHora = radio * 0.5f;
        float hx = (float) (centroX + Math.cos(anguloHora) * largoHora);
        float hy = (float) (centroY + Math.sin(anguloHora) * largoHora);
        paintManecillas.setStrokeWidth(12f); // Más gruesa
        canvas.drawLine(centroX, centroY, hx, hy, paintManecillas);

        // 5. Dibujar manecilla de los Minutos (más larga: 75% del radio)
        float largoMinuto = radio * 0.75f;
        float mx = (float) (centroX + Math.cos(anguloMinuto) * largoMinuto);
        float my = (float) (centroY + Math.sin(anguloMinuto) * largoMinuto);
        paintManecillas.setStrokeWidth(8f); // Más delgada
        canvas.drawLine(centroX, centroY, mx, my, paintManecillas);

        // 6. Actualizar automáticamente el lienzo cada segundo de forma fluida
        postInvalidateDelayed(1000);
    }
}