package fr.cpe.hello

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import fr.cpe.hello.udpSocket.UDPMessageSocket
import fr.cpe.hello.udpSocket.UDPMessageSocketListener
import fr.cpe.hello.ui.theme.MonHelloWorldTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.math.sqrt
import org.json.JSONObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import fr.cpe.hello.model.Sensor
import fr.cpe.hello.model.SensorValues
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

class MainActivity : ComponentActivity() {
    private val appId = 254

    private lateinit var udpMessageSocket: UDPMessageSocket
    private var sensorOrder: String by mutableStateOf("LTHPU")
    private var ipAddress: String by mutableStateOf("192.168.210.23")
    private var udpResponse: String by mutableStateOf("")

    private var myListItems = mutableListOf(
        SensorOrderList("Lumière"),
        SensorOrderList("Température"),
        SensorOrderList("Humidité"),
        SensorOrderList("Pression"),
        SensorOrderList("UV")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startUdpReceiver{}


        enableEdgeToEdge()
        setContent {
            MonHelloWorldTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(WindowInsets.safeDrawing.asPaddingValues()), // safe area
                            horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            value = ipAddress,
                            onValueChange = { ipAddress = it },
                            label = { Text("Entrez l'addresse du serveur") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row (
                            modifier = Modifier
                                .padding(16.dp)
                        ){
                            Button(
                                onClick = { sendMessage() },
                                modifier = Modifier.weight(0.4f)
                            ) {
                                Text("Demander une valeur")
                            }
                            Spacer(modifier = Modifier.weight(0.1f))
                            //Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { sendUdpMessage("getListOrder()") },
//                                onClick = {reorderListManually(listOf("Température", "Pression", "UV", "Humidité", "Lumière")) },
                                modifier = Modifier.weight(0.4f)
                            ) {
                                Text("Envoyer l'ordre d'affichage")
                            }
                        }

                        MyList(
                            items = myListItems,
                            onMove = { newList ->
                                for(i in 0..4){
                                    myListItems.set(i, newList.get(i))
                                }
                                setSensorOrder(myListItems)
                                sendSensorOrderMessage()
                                // Envoyer un SETSENSORORDER de la liste
                            }
                        )
                    }
                }
            }
        }
    }

    private fun structureSensorOrderMessage(sensorOrder : String): String{


        return ""
    }

    private fun setSensorOrder(myListItems: List<SensorOrderList>){
        var newOrder = ""
        for (item in myListItems) {
            when(item.name){
                "Lumière" -> newOrder += "L"
                "Température" -> newOrder += "T"
                "Humidité" -> newOrder += "H"
                "Pression" -> newOrder +=  "P"
                "UV" -> newOrder += "U"
            }
        }
        Log.d("ORDER", newOrder)

        sensorOrder = newOrder
    }

    // Fonction qui envoie le message en UDP sur un thread IO
    private fun sendUdpMessage(message: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val address = InetAddress.getByName(ipAddress)
                val port = 10000
                DatagramSocket().use { socket ->
                    val buffer = message.toByteArray()
                    val packet = DatagramPacket(buffer, buffer.size, address, port)
                    socket.send(packet)
                }
            } catch (e: Exception) {
                Log.e("UDP_SENDER", "Erreur : ${e.message}", e)
            }
        }
    }

    private fun startUdpReceiver(
        port: Int = 10000,
        onMessageReceived: (String) -> Unit
    ) {
            var socket = UDPMessageSocket(InetAddress.getByName(ipAddress), 10000)
            udpMessageSocket = socket

            socket.addListener(object: UDPMessageSocketListener {
                override fun onMessage(message: String) {
                    // MEssage qui arrive du serveur
                    Log.d("UDP_RECEIVER", message)
                    testCustomJson(message)
                }

            })
        }



    private fun testCustomJson(message: String){
//        val jsonString = Json.encodeToString(user)
        val decode = Json.decodeFromString<SensorValues>(message)
        Log.d("JSON_DECODE", decode.toString())
    }


    private fun sendSensorOrderMessage(){
        udpMessageSocket.sendMessage(sensorOrder) // Envoyer un message
    }

    private fun sendMessage(){
        udpMessageSocket.sendMessage("getValues()") // Envoyer un message
    }

    data class SensorOrderList(val name: String)

}

