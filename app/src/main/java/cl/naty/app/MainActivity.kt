package cl.naty.app

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    // Declarar objetos de la pantalla
    private lateinit var messageView: TextView
    private lateinit var humidityText: TextView
    private lateinit var turnOffButton: Button
    private lateinit var turnOnHumidifierButton: Button
    private lateinit var turnOnDeshumidifierButton: Button

    // Declarar un objeto que representa la conexión MQTT
    private lateinit var mqttClient: MqttClientHelper
    private val sensorTopic = MqttClientHelper.SENSOR_TOPIC
    private val deviceTopic = MqttClientHelper.DEVICE_TOPIC

    // Variable para humedad
    private var humedadActual = 50 //
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hacer referencia a los elementos de la pantalla
        messageView = findViewById(R.id.messageView)
        humidityText = findViewById(R.id.humidityText)
        turnOnHumidifierButton = findViewById(R.id.turnOnHumidifierButton)
        turnOnDeshumidifierButton = findViewById(R.id.turnOnDeshumidifierButton)
        turnOffButton = findViewById(R.id.turnOffButton)

        // Crear la referencia al cliente MQTT
        mqttClient = MqttClientHelper()
        messageView.append("OK!\n")

        // Suscribirse al tópico y enviar el primer mensaje
        mqttClient.subscribeToTopic(sensorTopic, messageView)
        mqttClient.publishMessage(sensorTopic, "¡El sensor está en línea!")

        mqttClient.subscribeToTopic(deviceTopic, messageView)
        mqttClient.publishMessage(deviceTopic, "¡El dispositivo está en línea!")

        // Inicializar la visualización de la humedad
        actualizarHumedad()

        turnOffButton.setOnClickListener {
            clearMessages()
            messageView.append("Apagando el dispositivo...\n")
            val turnOffMessage = "OFF: Apagar el dispositivo"
            mqttClient.publishMessage(deviceTopic, turnOffMessage)

            handler.removeCallbacksAndMessages(null)
        }
        turnOnHumidifierButton.setOnClickListener {
            clearMessages()
            messageView.append("Encendiendo el humidificador...\n")
            val turnOnHumidifierMessage = "ON_HUMIDIFIER: Encender el humidificador"
            mqttClient.publishMessage(deviceTopic, turnOnHumidifierMessage)
            incrementarHumedad()
        }

        turnOnDeshumidifierButton.setOnClickListener {
            clearMessages()
            messageView.append("Encendiendo el deshumidificador...\n")
            val turnOnDeshumidifierMessage = "ON_DESHUMIDIFIER: Encender el deshumidificador"
            mqttClient.publishMessage(deviceTopic, turnOnDeshumidifierMessage)
            disminuirHumedad()
        }

    }

    private fun clearMessages() {
        messageView.text = ""
    }

    private fun incrementarHumedad() {
        handler.removeCallbacksAndMessages(null)

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (humedadActual < 100) {
                    humedadActual += 1
                    actualizarHumedad()
                    handler.postDelayed(this, 2000)
                }
            }
        }, 1000)
    }

    private fun disminuirHumedad() {
        handler.removeCallbacksAndMessages(null)

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (humedadActual > 0) {
                    humedadActual -= 1
                    actualizarHumedad()
                    handler.postDelayed(this, 2000)
                }
            }
        }, 1000)
    }

    private fun actualizarHumedad() {
        humidityText.text = "Humedad: $humedadActual%"

        val humidityPanel = findViewById<View>(R.id.humidityPanel)

        when {
            humedadActual in 30..60 -> {
                humidityPanel.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            }
            humedadActual in 15..29 || humedadActual in 61..75 -> {
                humidityPanel.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
            }
            else -> {
                humidityPanel.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        // Cerrar la conexión con el servicio MQTT
        mqttClient.disconnect()
    }
}
