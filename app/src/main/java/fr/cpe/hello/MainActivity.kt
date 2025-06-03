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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.cpe.hello.draggableList.DraggableList
import fr.cpe.hello.model.Sensor
import fr.cpe.hello.ui.theme.Yellow500
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val devMode: Boolean = true

    private lateinit var udpMessageSocket: UDPMessageSocket
    private var sensorOrder: String by mutableStateOf("LTHPU")
    private var ipAddress: String by mutableStateOf("192.168.55.23")
    private var udpResponse: String by mutableStateOf("")

    private var currentHistoryValue: String by mutableStateOf("T")
    private var currentSensor: String by mutableStateOf("FACB2D3E3BDDA177")
//    MicroBit 1 : FACB2D3E3BDDA177
//    MicroBit 2 : A9AB1BD844F3FC8C

    private var sensorValues: SensorValues by mutableStateOf(
        SensorValues(
            reading_id = 1,
            sensor_crouscam_id = 0,
            timestamp = "05/06/25-09:12:17",
            temperature = 27.3f,
            humidity = 18.7f,
            uv = 2.5f,
            luminosity = 2.5f,
            pressure = 16.1f
        )
    )

    private var sensorValuesHistory = mutableStateListOf(
        SensorValues(
            reading_id = 1,
            sensor_crouscam_id = 0,
            timestamp = "05/06/25-09:12:17",
            temperature = 27.3f,
            humidity = 18.7f,
            uv = 2.5f,
            luminosity = 2.5f,
            pressure = 16.1f
        ),
        SensorValues(
            reading_id = 0,
            sensor_crouscam_id = 0,
            timestamp = "05/06/25-09:10:51",
            temperature = 20.0f,
            humidity = 18.0f,
            uv = 1.0f,
            luminosity = 2.0f,
            pressure = 12.0f
        ),
    )

    private var sensorList = mutableStateListOf(
        Sensor(
            id = 0,
            crouscam_id = 0,
            sensor_configuration = "",
            sensor_name = "FACB2D3E3BDDA177",
            last_seen = 0.0
        ),
        Sensor(
            id = 1,
            crouscam_id = 1,
            sensor_configuration = "",
            sensor_name = "A9AB1BD844F3FC8C",
            last_seen = 0.0
        ),
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
        sendGetSensorList()
        sendGetSensorValues()

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
                                sensorList = sensorList,
                                onSensorSelected = { crouscam_id ->
                                    currentSensor = crouscam_id
                                    Log.d("SENSOR_SELECTED", "Capteur sélectionné: $crouscam_id")
                                }
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

                        composable(
                            SensorAppScreen.HISTORY.name,
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
                            HistoryScreen(
                                navController = navController
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
                        "DATA_SENSOR_VALUES" -> updateSensorValues(payload.jsonObject)
                        "DATA_SENSOR_LIST" -> updateSensorList(payload.jsonObject)
                    }
                }else{
                    Log.e("NO_PAYLOAD", "l'objet ne contient pas de payload : \n$message")
                }
            }
        })
    }

    private fun updateSensorList(payload: JsonObject) {
        try {
            val values = Json.decodeFromJsonElement<List<Sensor>>(payload)

            for(i in 0..values.size - 1){
                sensorList.set(i, values.get(i))
            }
            setSensorOrder(myListItems)
            sendSensorOrderMessage()

        } catch (e: Exception) {
            Log.e("JSON_CONVERT", "Erreur lors de la validation: ${e.message}")
        }
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

            sensorValuesHistory.add(sensorValues)
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
        val s = """{"event":"DATA_SENSOR_ORDER", "payload": {"sensor_order": $sensorOrder}}"""
        udpMessageSocket.sendMessage(s)
    }

    private fun sendGetSensorList() {
        val s = """{"event":"DATA_SENSOR_LIST", "payload": {}}"""
        udpMessageSocket.sendMessage(s)
        Log.d("UDP_SEND", "sendGetSensorList $ipAddress")
    }

    private fun sendGetSensorValues() {
        val s = """{"event":"DATA_SENSOR_VALUES", "payload": {"crouscam_id": $currentSensor}}"""
        udpMessageSocket.sendMessage(s)
        Log.d("UDP_SEND", "sendGetSensorValues")
    }

    data class SensorOrderList(val name: String)

    enum class SensorAppScreen() {
        HOME,
        DETAIL,
        ORDER,
        HISTORY
    }

    @Composable
    fun HomeScreen(
        navController: NavController,
        ipAddress: String,
        onIpAddressChange: (String) -> Unit,
        sensorList: List<Sensor>, // Ajoutez ce paramètre
        onSensorSelected: (String) -> Unit // Ajoutez ce paramètre pour la sélection
    ) {
        // On demande les valeurs toutes les 10 secondes
        LaunchedEffect(Unit) {
            while (true) {
                delay(10000)
                sendGetSensorList()

            }
        }
        Column(
            modifier = Modifier
                .padding(WindowInsets.safeDrawing.asPaddingValues()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = ipAddress,
                onValueChange = onIpAddressChange,
                label = { Text("Entrez l'addresse du serveur") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.padding(50.dp))
            // Boutons dynamiques basés sur sensorList
            if (sensorList.isNotEmpty()) {
                Text(
                    "Capteurs disponibles",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                sensorList.forEachIndexed { index, sensor ->
                    Button(
                        onClick = {
                            onSensorSelected(sensor.sensor_name)
                            navController.navigate(SensorAppScreen.DETAIL.name) {
                                popUpTo(SensorAppScreen.DETAIL.name) {
                                    inclusive = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sensor.last_seen > 0) Color.Green else Color.Gray
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Capteur ${sensor.sensor_name}")
                            Text(
                                text = if (sensor.last_seen > 0) "En ligne" else "Hors ligne",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                Text(
                    "Aucun capteur détecté",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }


    @Composable
    fun DetailScreen(
        navController: NavController
    ) {
        // On demande les valeurs toutes les 10 secondes
        LaunchedEffect(Unit) {
            while (true) {
                delay(10000)
                sendGetSensorValues()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Bouton pour tester le changement de valeurs dans le serveur
//            Button(
//                onClick = {
//                    // Mise à jour des valeurs du capteur avec de nouvelles données
//                    sensorValues = sensorValues.copy(
//                        temperature = 30.5f,
//                        humidity = 25.3f,
//                        uv = 4.2f,
//                        luminosity = 2.8f,
//                        pressure = 15.6f
//                    )
//                },
//                modifier = Modifier.padding(bottom = 16.dp)
//            ) {
//                Text("Changer les valeurs")
//            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LevelScreen(
                        LevelState(
                            unitName = "Température",
                            unit = "°C",
                            value = sensorValues.temperature,
                            maxValue = 40f,
                            arcValue = sensorValues.temperature / 40f,
                        )
                    ) { }
                }
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LevelScreen(
                        LevelState(
                            unitName = "Luminosité",
                            unit = "lux",
                            value = sensorValues.luminosity,
                            maxValue = 5f,
                            arcValue = sensorValues.luminosity / 5f,
                        )
                    ) { }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LevelScreen(
                        LevelState(
                            unitName = "Humidité",
                            unit = "g/m³",
                            value = sensorValues.humidity,
                            maxValue = 40f,
                            arcValue = sensorValues.humidity / 40f,
                        )
                    ) { }
                }
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    LevelScreen(
                        LevelState(
                            unitName = "Pression",
                            unit = "Pa",
                            value = sensorValues.pressure,
                            maxValue = 20f,
                            arcValue = sensorValues.pressure / 20f,
                        )
                    ) { }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    LevelScreen(
                        LevelState(
                            unitName = "UV",
                            unit = "mW/cm²",
                            value = sensorValues.uv,
                            maxValue = 6f,
                            arcValue = sensorValues.uv / 6f,
                        )
                    ) { }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.HOME.name) {
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
                        navController.navigate(SensorAppScreen.HISTORY.name) {
                            popUpTo(SensorAppScreen.HISTORY.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Historique",
                        modifier = Modifier.size(25.dp)
                    )
                }

                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.ORDER.name) {
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

    @Composable
    fun HistoryScreen(
        navController: NavController,
    ) {
        Column() {
            // Liste déroulante des valeurs historiques
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sensorValuesHistory) { sensorValue ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Lecture #${sensorValue.reading_id}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Capteur: ${sensorValue.sensor_crouscam_id}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                text = "Timestamp: ${sensorValue.timestamp}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Affichage des valeurs des capteurs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    SensorValueItem("Température", "${sensorValue.temperature}°C")
                                    SensorValueItem("Humidité", "${sensorValue.humidity} g/m³")
                                    SensorValueItem("UV", "${sensorValue.uv} mW/cm²")
                                }
                                Column {
                                    SensorValueItem("Luminosité", "${sensorValue.luminosity} lux")
                                    SensorValueItem("Pression", "${sensorValue.pressure} Pa")
                                }
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                    //.align(Alignment.End),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.HOME.name) {
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
                        navController.navigate(SensorAppScreen.DETAIL.name) {
                            popUpTo(SensorAppScreen.DETAIL.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Retour detail",
                        modifier = Modifier.size(25.dp)
                    )
                }

                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.ORDER.name) {
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
//            }

        }


    }

    // élément utilisé dans l'affichage de l'historique des valeurs
    @Composable
    fun SensorValueItem(label: String, value: String) {
        Row(
            modifier = Modifier
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)

        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(80.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
