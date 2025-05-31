package fr.cpe.hello

import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.foundation.Canvas
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import fr.cpe.hello.udpSocket.UDPMessageSocket
import fr.cpe.hello.udpSocket.UDPMessageSocketListener
import fr.cpe.hello.ui.theme.MonHelloWorldTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import androidx.navigation.compose.rememberNavController
import fr.cpe.hello.model.SensorValues
import kotlinx.serialization.json.*
import fr.cpe.hello.model.LevelState
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.cpe.hello.draggableList.DraggableList
import fr.cpe.hello.ui.theme.Yellow500

class MainActivity : ComponentActivity() {
    private val devMode: Boolean = true

    private lateinit var udpMessageSocket: UDPMessageSocket
    private var sensorOrder: String by mutableStateOf("LTHPU")
    private var ipAddress: String by mutableStateOf("192.168.210.23")
    private var udpResponse: String by mutableStateOf("")

    private var currentSensor: String by mutableStateOf("FACB2D3E3BDDA177")
//    MicroBit 1 : FACB2D3E3BDDA177
//    MicroBit 2 : A9AB1BD844F3FC8C

    private var sensorValues: SensorValues by mutableStateOf(
        SensorValues(
            reading_id = 0,
            sensor_crouscam_id = 0,
            timestamp = "",
            temperature = 0.0,
            humidity = 0.0,
            uv = 0.0,
            luminosity = 0.0,
            pressure = 0.0
        )
    )
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
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = SensorAppScreen.HOME.name,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Page Home
                        composable(
                            SensorAppScreen.HOME.name,
                            enterTransition = {
                                slideInVertically(
                                    initialOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(400, easing = EaseOutQuart)
                                ) + fadeIn(tween(400))
                            },
                            exitTransition = {
                                slideOutVertically(
                                    targetOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(400, easing = EaseInQuart)
                                ) + fadeOut(tween(400))
                            },
                        ) {
                            HomeScreen(
                                navController = navController,
                                ipAddress = ipAddress,
                                onIpAddressChange = { ipAddress = it },
                                onSendMessage = { sendMessage() },
                                onSendOrder = { sendUdpMessage("getListOrder()") }
                            )
                        }

                        // Page de detail
                        composable(
                            SensorAppScreen.DETAIL.name,
                            enterTransition = {
                                slideInVertically(
                                    initialOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(400, easing = EaseOutQuart)
                                ) + fadeIn(tween(400))
                            },
                            exitTransition = {
                                slideOutVertically(
                                    targetOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(400, easing = EaseInQuart)
                                ) + fadeOut(tween(400))
                            },

                        ) {
                            DetailScreen(
                                navController = navController
                            )
                        }

                        // Page pour changer l'ordre
                        composable(
                            SensorAppScreen.ORDER.name,
                            enterTransition = {
                                slideInVertically(
                                    initialOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(400, easing = EaseOutQuart)
                                ) + fadeIn(tween(400))
                            },
                            exitTransition = {
                                slideOutVertically(
                                    targetOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(400, easing = EaseInQuart)
                                ) + fadeOut(tween(400))
                            },
                            )
                        {
                            OrderScreen(
                                navController = navController,
                                myListItems = myListItems,
                                onMove = { newList ->
                                    for(i in 0..4){
                                        myListItems.set(i, newList.get(i))
                                    }
                                    setSensorOrder(myListItems)
                                    sendSensorOrderMessage()
                                }

                            )
                        }
                    }
                }
            }
        }
    }





    // Vos fonctions existantes restent inchangées
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
                Log.d("UDP_RECEIVER", message)
                var json = decodeJsonAsObject(message)
                var event = json.get("event")
                var payload = json.get("payload")

                if (payload != null) {
                    when (event.toString()) {
                        "DATA_VALUES" -> updateSensorValues(payload.jsonObject)
                        "DATA_SENSORLIST" -> updateSensorList(payload.jsonObject)
                    }
                }else{
                    Log.e("NO_PAYLOAD", "l'objet ne contient pas de payload : \n$message")
                }
            }
        })
    }

    private fun updateSensorList(payload: JsonObject) {

    }



    private fun updateSensorValues(payload: JsonObject) {
        try {
            // Vérification que ce soit bien un SensorValues
            val requiredFields = listOf("reading_id", "sensor_crouscam_id", "timestamp",
                "temperature", "humidity", "uv", "luminosity", "pressure")
            val missingFields = requiredFields.filter { !payload.containsKey(it) }
            if (missingFields.isNotEmpty()) {
                Log.w("JSON_CONVERT", "Champs manquants: ${missingFields.joinToString(", ")}")
            }

            val values = Json.decodeFromJsonElement<SensorValues>(payload)
            sensorValues.reading_id = values.reading_id
            sensorValues.sensor_crouscam_id = values.sensor_crouscam_id
            sensorValues.timestamp = values.timestamp
            sensorValues.temperature = values.temperature
            sensorValues.humidity = values.humidity
            sensorValues.uv = values.uv
            sensorValues.luminosity = values.luminosity
            sensorValues.pressure = values.pressure

        } catch (e: Exception) {
            Log.e("JSON_CONVERT", "Erreur lors de la validation: ${e.message}")
        }
    }

    private fun decodeJsonAsObject(message: String): JsonObject {
        val jsonObject = Json.parseToJsonElement(message).jsonObject
        Log.d("JSON_DECODE", jsonObject.toString())
        return jsonObject
    }

    private fun sendSensorOrderMessage(){
        udpMessageSocket.sendMessage(sensorOrder)
    }

    private fun sendMessage(){
        udpMessageSocket.sendMessage("getValues()")
    }

    private fun getSensorList(): String {

        return ""
    }

    data class SensorOrderList(val name: String)

    enum class SensorAppScreen() {
        HOME,
        DETAIL,
        ORDER
    }

    @Composable
    fun HomeScreen(
        navController: NavController,
        ipAddress: String,
        onIpAddressChange: (String) -> Unit,
        onSendMessage: () -> Unit,
        onSendOrder: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .padding(WindowInsets.safeDrawing.asPaddingValues()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bouton de navigation vers Detail
            Button(
                onClick = {
                    navController.navigate(SensorAppScreen.DETAIL.name)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Aller aux détails")
            }

            TextField(
                value = ipAddress,
                onValueChange = onIpAddressChange,
                label = { Text("Entrez l'addresse du serveur") },
                modifier = Modifier.fillMaxWidth()
            )

            Row (
                modifier = Modifier.padding(16.dp)
            ){
                Button(
                    onClick = onSendMessage,
                    modifier = Modifier.weight(0.4f)
                ) {
                    Text("Demander une valeur")
                }
                Spacer(modifier = Modifier.weight(0.1f))
                Button(
                    onClick = onSendOrder,
                    modifier = Modifier.weight(0.4f)
                ) {
                    Text("Envoyer l'ordre d'affichage")
                }
            }

            if(devMode){
                for(i in 0..1){
                    var name = if(i==0) "FACB2D3E3BDDA177" else "A9AB1BD844F3FC8C"
                    Button(
                        onClick = {
                            currentSensor = name
                            navController.navigate(SensorAppScreen.DETAIL.name) {
                                // vider la pile de navigation pour éviter l'accumulation
                                popUpTo(SensorAppScreen.DETAIL.name) {
                                    inclusive = true
                                }
                            }
                        },
                        modifier = Modifier
                    ) {
                        Text("Capteur $name")
                    }
                }
            }else{
                var sensorList = getSensorList()
            }

//            DraggableList(
//                items = myListItems,
//                onMove = onMove
//            )
        }
    }

    @Composable
    fun DetailScreen(
        navController: NavController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ){
                LevelScreen( LevelState(
                    unitName = "Température",
                    unit = "°C",
                    value = 25.7f,
                    maxValue = 40f,
                    arcValue = 0.57f,
                )
                ) { }
                LevelScreen( LevelState(
                    unitName = "Luminosité",
                    unit = "lux",
                    value = 3f,
                    maxValue = 5f,
                    arcValue = 0.38f,
                )
                ) { }
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                LevelScreen( LevelState(
                    unitName = "Humidité",
                    unit = "g/m3",
                    value = 11.7f,
                    maxValue = 40f,
                    arcValue = 0.42f,
                )
                ) { }
                LevelScreen( LevelState(
                    unitName = "Pression",
                    unit = "Pa",
                    value = 10f,
                    maxValue = 20f,
                    arcValue = 0.76f,
                )
                ) { }
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                LevelScreen( LevelState(
                    unitName = "UV",
                    unit = "mW/cm2",
                    value = 1.7f,
                    maxValue = 6f,
                    arcValue = 0.82f,
                )
                ) { }
            }


            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ){
                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.HOME.name) {
                            // vider la pile de navigation pour éviter l'accumulation
                            popUpTo(SensorAppScreen.HOME.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier
                ) {
                    Text("< Accueil")
                }
                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.ORDER.name) {
                            // vider la pile de navigation pour éviter l'accumulation
                            popUpTo(SensorAppScreen.ORDER.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier
                ) {
                    Text("Ordre >")
                }
            }
        }
    }

    @Composable
    fun OrderScreen(
        navController: NavController,
        myListItems: List<SensorOrderList>,
        onMove: (List<SensorOrderList>) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DraggableList(
                items = myListItems,
                onMove = onMove
            )

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ){
                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.DETAIL.name) {
                            // vider la pile de navigation pour éviter l'accumulation
                            popUpTo(SensorAppScreen.DETAIL.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Yellow500)
                ) {
                    Text("< Détails")
                }
                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.HOME.name) {
                            // vider la pile de navigation pour éviter l'accumulation
                            popUpTo(SensorAppScreen.HOME.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier
                ) {
                    Text("Accueil >")
                }
            }
        }
    }
}