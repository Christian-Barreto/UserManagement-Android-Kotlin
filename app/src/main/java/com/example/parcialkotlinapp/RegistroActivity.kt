package com.example.parcialkotlinapp

import android.R.attr.password
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

import com.example.parcialkotlinapp.api.ApiClient
import com.example.parcialkotlinapp.models.CrearUsuarioRequest
import com.example.parcialkotlinapp.models.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.DatePickerDialog
import java.util.Calendar
import androidx.appcompat.app.AlertDialog
import com.example.parcialkotlinapp.models.ErrorResponse
import com.google.gson.Gson


class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_registro)

        // COMPONENTES
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etApellido = findViewById<EditText>(R.id.etApellido)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRegistro)
        val etPasswordRegistro = findViewById<EditText>(R.id.etPasswordRegistro)

        val etFechaNacimiento = findViewById<EditText>(R.id.etFechaNacimiento)

        val tvEdad = findViewById<TextView>(R.id.tvEdad)

        val etUniversidad = findViewById<EditText>(R.id.etUniversidad)

        val spSemestre = findViewById<Spinner>(R.id.spSemestre)

        // FLECHA SUPERIOR (volver a la pantalla anterior)
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(
            R.id.toolbarRegistro
        )

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        val tvMensaje =
            findViewById<TextView>(R.id.tvMensajeRegistro)

        // FECHA DE NACIMIENTO (DatePicker)
        var fechaElegida: Calendar? = null

        etFechaNacimiento.setOnClickListener {

            val hoy = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                this,
                { _, anio, mes, dia ->

                    val elegida = Calendar.getInstance()
                    elegida.set(anio, mes, dia)
                    fechaElegida = elegida

                    tvEdad.text = "Edad: ${calcularEdad(elegida)} años"

                    // El mes empieza en 0 (enero = 0), por eso se suma 1
                    etFechaNacimiento.setText(
                        String.format("%02d/%02d/%04d", dia, mes + 1, anio)
                    )
                },
                hoy.get(Calendar.YEAR) - 18,
                hoy.get(Calendar.MONTH),
                hoy.get(Calendar.DAY_OF_MONTH)
            )

            // No permite elegir fechas futuras
            datePicker.datePicker.maxDate = System.currentTimeMillis()

            datePicker.show()
        }

        // SEMESTRE (de 1 a 10)
        val semestres = listOf("Seleccione semestre") +
                (1..10).map { it.toString() }

        val adaptadorSemestre = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            semestres
        )

        adaptadorSemestre.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spSemestre.adapter = adaptadorSemestre

        // BOTÓN REGISTRAR
        btnRegistrar.setOnClickListener {

            val nombre =
                etNombre.text.toString().trim()

            val apellido =
                etApellido.text.toString().trim()

            val correo =
                etCorreo.text.toString().trim()

            val password = etPasswordRegistro.text.toString().trim()

            val universidad = etUniversidad.text.toString().trim()

            val nacimiento = fechaElegida

            if (nacimiento == null) {
                Toast.makeText(
                    this,
                    "Seleccione la fecha de nacimiento",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // VALIDACIONES
            if (nombre.isBlank()) {
                etNombre.error = "Ingrese el nombre"
                return@setOnClickListener
            }

            if (apellido.isBlank()) {
                etApellido.error = "Ingrese el apellido"
                return@setOnClickListener
            }

            if (correo.isBlank()) {
                etCorreo.error = "Ingrese el correo"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(correo)
                    .matches()) {

                etCorreo.error = "Correo no válido"
                return@setOnClickListener
            }

            if (password.isBlank()) {
                etPasswordRegistro.error = "Ingrese la contraseña"
                return@setOnClickListener
            }

            if (universidad.isBlank()) {
                etUniversidad.error = "Ingrese la universidad"
                return@setOnClickListener
            }

            if (spSemestre.selectedItemPosition == 0) {
                Toast.makeText(
                    this,
                    "Seleccione un semestre",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // La posición coincide con el número (posición 1 = semestre 1)
            val semestre = spSemestre.selectedItemPosition

            // La API espera la fecha como yyyy-MM-dd
            val fechaNacimiento = String.format(
                "%04d-%02d-%02d",
                nacimiento.get(Calendar.YEAR),
                nacimiento.get(Calendar.MONTH) + 1,
                nacimiento.get(Calendar.DAY_OF_MONTH)
            )

            // OBJETO QUE ENVIAREMOS AL BACKEND

            val nuevoUsuario = CrearUsuarioRequest(
                nombre = nombre,
                apellido = apellido,
                correo = correo,
                password = password,
                fechaNacimiento = fechaNacimiento,
                universidad = universidad,
                semestre = semestre
            )

            tvMensaje.text = "Registrando usuario..."

            // LLAMADA AL BACKEND
            ApiClient.usuarioApi
                .crearUsuario(nuevoUsuario)
                .enqueue(object : Callback<Usuario> {

                    override fun onResponse(
                        call: Call<Usuario>,
                        response: Response<Usuario>
                    ) {

                        if (response.isSuccessful) {

                            tvMensaje.text = ""

                            AlertDialog.Builder(this@RegistroActivity)
                                .setTitle("Registro exitoso")
                                .setMessage(
                                    "Tu cuenta fue creada correctamente. " +
                                            "Ya puedes iniciar sesión."
                                )
                                .setCancelable(false)
                                .setPositiveButton("ACEPTAR") { _, _ ->
                                    // Cierra el registro y vuelve al Login
                                    finish()
                                }
                                .show()

                        } else {

                            val mensaje = leerMensajeError(response)

                            tvMensaje.text = mensaje

                            if (mensaje.contains("correo", ignoreCase = true)) {
                                etCorreo.error = mensaje
                                etCorreo.requestFocus()
                            }

                            Toast.makeText(
                                this@RegistroActivity,
                                mensaje,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<Usuario>,
                        t: Throwable
                    ) {

                        tvMensaje.text =
                            "Error de conexión: ${t.message}"
                    }
                })
        }

    }

    // Calcula la edad considerando día, mes y año
    private fun calcularEdad(nacimiento: Calendar): Int {

        val hoy = Calendar.getInstance()

        var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)

        val mesHoy = hoy.get(Calendar.MONTH)
        val mesNacimiento = nacimiento.get(Calendar.MONTH)

        // Si todavía no llegó su cumpleaños este año, se resta 1
        if (mesHoy < mesNacimiento ||
            (mesHoy == mesNacimiento &&
                    hoy.get(Calendar.DAY_OF_MONTH) < nacimiento.get(Calendar.DAY_OF_MONTH))) {
            edad--
        }

        return edad
    }

    // Lee el mensaje que envía la API cuando responde con error
    private fun leerMensajeError(response: Response<*>): String {

        val cuerpo = response.errorBody()?.string()

        val mensajeApi = try {
            Gson().fromJson(cuerpo, ErrorResponse::class.java)?.mensaje
        } catch (e: Exception) {
            null
        }

        // Si la API envió un mensaje, se muestra tal cual
        if (!mensajeApi.isNullOrBlank()) {
            return mensajeApi
        }

        // Si no envió mensaje, se explica según el código
        return when (response.code()) {
            400 -> "Datos inválidos. Revise el formulario."
            500 -> "Error interno del servidor. Intente más tarde."
            else -> "Error inesperado (código ${response.code()})."
        }
    }
}