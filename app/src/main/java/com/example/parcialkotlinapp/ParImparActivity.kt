package com.example.parcialkotlinapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class ParImparActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_par_impar)

        // Conectamos Kotlin con los componentes del XML
        val etNumero = findViewById<EditText>(R.id.etNumero)
        val btnValidar = findViewById<Button>(R.id.btnValidar)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)


        btnValidar.setOnClickListener {

            val texto = etNumero.text.toString()

            // Validar que el campo no esté vacío
            if (texto.isEmpty()) {
                etNumero.error = "Ingrese un número"
                return@setOnClickListener
            }

            // Conversión segura
            val numero = texto.toIntOrNull()

            if (numero == null) {
                etNumero.error = "Ingrese un número entero válido"
                return@setOnClickListener
            }

            // Validación par o impar
            if (numero % 2 == 0) {
                tvResultado.text = "$numero es PAR"
            } else {
                tvResultado.text = "$numero es IMPAR"
            }
        }

        // =====================================
        // FLECHA SUPERIOR (volver a la pantalla principal)
        // =====================================

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarParImpar)

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}