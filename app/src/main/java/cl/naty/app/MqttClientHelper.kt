package cl.naty.app

import android.widget.TextView
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttConnectionException(message: String, cause: Throwable) : Exception(message, cause)
class MqttSubscriptionException(message: String, cause: Throwable) : Exception(message, cause)
class MqttPublishException(message: String, cause: Throwable) : Exception(message, cause)

class MqttClientHelper {
    private val SERVER_URI = "tcp://broker.emqx.io:1883"
    private val CLIENT_ID = "Naty"

    companion object {
        const val SENSOR_TOPIC = "sensorTopic"
        const val DEVICE_TOPIC = "deviceTopic"
    }

    private lateinit var mqttClient: MqttClient

    init {
        try {
            connectToMqttBroker()
        } catch (e: MqttException) {
            e.printStackTrace()
            throw MqttConnectionException("Error al conectar al servidor MQTT", e)
        }
    }

    private fun connectToMqttBroker() {
        try {
            val persistence = MemoryPersistence()
            mqttClient = MqttClient(SERVER_URI, CLIENT_ID, persistence)
            val options = MqttConnectOptions()
            options.isCleanSession = true
            mqttClient.connect(options)
        } catch (e: MqttException) {
            e.printStackTrace()
            throw MqttConnectionException("Error al conectar al servidor MQTT", e)
        }
    }

    fun subscribeToTopic(topic: String, messageView: TextView) {
        try {
            mqttClient.subscribe(topic) { _, message ->
                val payload = String(message.payload)
                messageView.append("[$topic] $payload\n")
                println("[$topic] Mensaje recibido: $payload")

                // Agregar lógica para verificar el mensaje de encendido/apagado de dispositivos
                when (payload.toUpperCase()) {
                    "OFF: APAGAR EL DISPOSITIVO" -> {
                        // shutdownDevice()
                    }
                    "HUMIDIFICAR: ENCENDER EL HUMIDIFICADOR" -> {
                        // turnOnHumidifier()
                    }
                    "DESHUMIDIFICAR: ENCENDER EL DESHUMIDIFICADOR" -> {
                        // turnOnDeshumidifier()
                    }
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
            throw MqttSubscriptionException("Error al suscribirse al tópico $topic", e)
        }
    }

    fun publishMessage(topic: String, message: String) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttClient.publish(topic, mqttMessage)
        } catch (e: MqttException) {
            e.printStackTrace()
            throw MqttPublishException("Error al publicar mensaje en el tópico $topic", e)
        }
    }

    fun disconnect() {
        mqttClient.disconnect()
    }
}
