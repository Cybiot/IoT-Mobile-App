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
import fr.cpe.hello.ui.theme.Cyan500
import fr.cpe.hello.ui.theme.Green200
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    // Mode développement pour le debug
    private val devMode: Boolean = true

    // Socket UDP pour la communication avec les capteurs
    private lateinit var udpMessageSocket: UDPMessageSocket

    // Variables d'état pour la configuration et les données
    private var sensorOrder: String by mutableStateOf("LTHPU") // Ordre d'affichage des capteurs (Luminosité, Température, Humidité, Pression, UV)
    private var ipAddress: String by mutableStateOf("192.168.141.23") // Adresse IP du serveur de capteurs
    private var udpResponse: String by mutableStateOf("") // Dernière réponse UDP reçue

    // Variables pour le capteur actuellement sélectionné
    private var currentHistoryValue: String by mutableStateOf("T") // Type de valeur pour l'historique
    private var currentDbId: Int by mutableStateOf(0) // ID base de données du capteur sélectionné
    private var currentSensor: String by mutableStateOf("5CAEE82D") // ID du capteur actuellement sélectionné

    // Variables d'état pour les valeurs des capteurs individuelles
    private var readingDbId: Int by mutableStateOf(1)
    private var sensorCrouscamId: String by mutableStateOf("5CAEE82D")
    private var timestamp: String by mutableStateOf("05/06/25-09:12:17")
    private var temperature: Float by mutableStateOf(27.3f) // Température en °C
    private var humidity: Float by mutableStateOf(32.7f) // Humidité en g/m³
    private var uv: Float by mutableStateOf(2.5f) // UV en mW/cm²
    private var luminosity: Float by mutableStateOf(500.0f) // Luminosité en lux
    private var pressure: Float by mutableStateOf(950.1f) // Pression en Pa

    // Map pour stocker les valeurs des capteurs (alternative à l'objet SensorValues)
    private var sensorValues = mutableMapOf(
        "reading_db_id" to 1,
        "sensor_crouscam_id" to "5CAEE82D",
        "timestamp" to "05/06/25-09:12:17",
        "temperature" to 27.3f,
        "humidity" to 32.7f,
        "uv" to 2.5f,
        "luminosity" to 500.0f,
        "pressure" to 950.1f
    )

    // Liste observable de l'historique des valeurs des capteurs
    private var sensorValuesHistory = mutableStateListOf(
        SensorValues(
            reading_db_id = 1,
            sensor_crouscam_id = "5CAEE82D",
            timestamp = "05/06/25-09:12:17",
            temperature = 27.3f,
            humidity = 32.7f,
            uv = 2f,
            luminosity = 500.0f,
            pressure = 950.1f
        ),
        SensorValues(
            reading_db_id = 1,
            sensor_crouscam_id = "5CAEE82D",
            timestamp = "05/06/25-09:12:17",
            temperature = 26.9f,
            humidity = 31.1f,
            uv = 2f,
            luminosity = 513.0f,
            pressure = 951.1f
        ),
    )

    // Liste observable des capteurs disponibles
    private var sensorList = mutableStateListOf(
        Sensor(
            db_id = 0,
            crouscam_id = "5CAEE82D",
            configuration = "LTHPU",
            name = "",
            location = "",
            status = "disconnected",
            last_seen = ""
        ),
        Sensor(
            db_id = 1,
            crouscam_id = "5CAEE82D",
            configuration = "LTHPU",
            name = "",
            location = "",
            status = "disconnected",
            last_seen = ""
        ),
    )

    // Liste des éléments pour l'ordre d'affichage des capteurs
    private var myListItems = mutableListOf(
        SensorOrderList("Lumière"),
        SensorOrderList("Température"),
        SensorOrderList("Humidité"),
        SensorOrderList("Pression"),
        SensorOrderList("UV")
    )

    /**
     * Méthode appelée à la création de l'activité
     * Initialise la communication UDP et configure l'interface utilisateur
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Démarrage du récepteur UDP pour écouter les messages des capteurs
        startUdpReceiver{}

        // Envoi des requêtes initiales pour récupérer la liste des capteurs et leurs valeurs
        sendGetSensorList()
        sendGetSensorValues()

        // Configuration de l'affichage en plein écran
        enableEdgeToEdge()

        // Configuration de l'interface utilisateur avec Jetpack Compose
        setContent {
            MonHelloWorldTheme {
                val navController = rememberNavController()

                // Structure principale de l'application avec navigation
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    // Configuration de la navigation entre les écrans
                    NavHost(
                        navController = navController,
                        startDestination = SensorAppScreen.HOME.name,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Écran d'accueil - Liste des capteurs disponibles
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
                                onSensorSelected = { crouscam_id, db_id ->
                                    currentSensor = crouscam_id
                                    currentDbId = db_id
                                    Log.d("SENSOR_SELECTED", "Capteur sélectionné: $crouscam_id")
                                }
                            )
                        }

                        // Écran de détail - Affichage des valeurs du capteur sélectionné
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

                        // Écran de configuration de l'ordre d'affichage
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
                                    // Mise à jour de l'ordre des capteurs
                                    for(i in 0..4){
                                        myListItems.set(i, newList.get(i))
                                    }
                                    setSensorOrder(myListItems)
                                    sendSensorOrderMessage()
                                }
                            )
                        }

                        // Écran d'historique - Affichage de l'historique des valeurs
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

    /**
     * Configure l'ordre d'affichage des capteurs basé sur la liste d'éléments
     * @param myListItems Liste des éléments de capteurs à ordonner
     */
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

    /**
     * Démarre le récepteur UDP pour écouter les messages des capteurs
     * @param port Port d'écoute (par défaut 10000)
     * @param onMessageReceived Callback appelé lors de la réception d'un message
     */
    private fun startUdpReceiver(
        port: Int = 10000,
        onMessageReceived: (String) -> Unit
    ) {
        // Création du socket UDP
        var socket = UDPMessageSocket(InetAddress.getByName(ipAddress), 10000)
        udpMessageSocket = socket

        // Ajout d'un listener pour traiter les messages reçus
        socket.addListener(object: UDPMessageSocketListener {
            override fun onMessage(message: String) {
                Log.d("UDP_RECEIVER", message)

                // Décodage du message JSON
                var json = decodeJsonAsObject(message)
                var event = json.get("event")
                var payload = json.get("payload")

                Log.d("EVENT", event.toString())

                // Vérification que le payload est valide
                if (payload != null && payload !is JsonNull) {
                    Log.d("TEST", event.toString())

                    // Traitement selon le type d'événement
                    when (event.toString()) {
                        "\"DATA_SENSOR_VALUES\"" -> {
                            // Mise à jour des valeurs des capteurs
                            if (payload is JsonObject) {
                                updateSensorValues(payload)
                            } else {
                                Log.e("PAYLOAD_TYPE_ERROR", "Le payload n'est pas un JsonArray pour DATA_SENSOR_VALUES: ${payload::class.simpleName}")
                            }
                        }
                        "\"DATA_SENSOR_LIST\"" -> {
                            // Mise à jour de la liste des capteurs
                            if (payload is JsonArray) {
                                updateSensorList(payload)
                            } else {
                                Log.e("PAYLOAD_TYPE_ERROR", "Le payload n'est pas un JsonArray pour DATA_SENSOR_LIST: ${payload::class.simpleName}")
                            }
                        }
                        else -> {
                            Log.e("WRONG_EVENT", "Le type d'event n'est pas connu ${event.toString()}")
                        }
                    }
                } else {
                    Log.e("NO_PAYLOAD", "l'objet ne contient pas de payload valide : \n$message")
                }
            }
        })
    }

    /**
     * Met à jour la liste des capteurs disponibles
     * @param payload Données JSON contenant la liste des capteurs
     */
    private fun updateSensorList(payload: JsonArray) {
        try {
            // Conversion du JSON vers une liste d'objets Sensor
            val values = Json.decodeFromJsonElement<List<Sensor>>(payload)
            Log.d("VALUE", "size : ${values.size}")

            sensorList.clear()
            for(sensor in values){
                sensorList.add(sensor)
            }
            setSensorOrder(myListItems)
        } catch (e: Exception) {
            Log.e("JSON_CONVERT1", "Erreur lors de la validation: ${e.message}")
        }
    }

    /**
     * Met à jour les valeurs du capteur sélectionné
     * @param payload Données JSON contenant les valeurs du capteur
     */
    private fun updateSensorValues(payload: JsonObject) {
        try {
            // Conversion du JSON vers un objet SensorValues
            val values = Json.decodeFromJsonElement<SensorValues>(payload)

            // Mise à jour des variables d'état individuelles
            readingDbId = values.reading_db_id
            sensorCrouscamId = values.sensor_crouscam_id
            timestamp = values.timestamp
            temperature = values.temperature
            humidity = values.humidity
            uv = values.uv
            luminosity = values.luminosity
            pressure = values.pressure

            // Création d'un nouvel objet pour l'historique
            val sensorValueForHistory = SensorValues(
                reading_db_id = values.reading_db_id,
                sensor_crouscam_id = values.sensor_crouscam_id,
                timestamp = values.timestamp,
                temperature = values.temperature,
                humidity = values.humidity,
                uv = values.uv,
                luminosity = values.luminosity,
                pressure = values.pressure
            )
            // Ajout à l'historique
            sensorValuesHistory.add(sensorValueForHistory)

        } catch (e: Exception) {
            Log.e("JSON_CONVERT2", "Erreur lors de la validation: ${e.message}")
        }
    }

    /**
     * Décode un message JSON en objet JsonObject
     * @param message Message JSON sous forme de chaîne
     * @return JsonObject décodé
     */
    private fun decodeJsonAsObject(message: String): JsonObject {
        val jsonObject = Json.parseToJsonElement(message).jsonObject
        Log.d("JSON_DECODE", jsonObject.toString())
        return jsonObject
    }

    /**
     * Envoie un message de configuration de l'ordre des capteurs
     */
    private fun sendSensorOrderMessage(){
        val s = """{"event":"DATA_SENSOR_ORDER", "payload": {"sensor_order": "$sensorOrder", "db_id": $currentDbId}}"""
        udpMessageSocket.sendMessage(s)
    }

    /**
     * Demande la liste des capteurs disponibles
     */
    private fun sendGetSensorList() {
        val s = """{"event":"DATA_SENSOR_LIST", "payload": {}}"""
        udpMessageSocket.sendMessage(s)
        Log.d("UDP_SEND", "sendGetSensorList $ipAddress")
    }

    /**
     * Demande les valeurs du capteur actuellement sélectionné
     */
    private fun sendGetSensorValues() {
        val s = """{"event":"DATA_SENSOR_VALUES", "payload": {"crouscam_id":  "$currentSensor"}}"""
        udpMessageSocket.sendMessage(s)
        Log.d("UDP_SEND", "sendGetSensorValues")
    }

    /**
     * Classe de données pour représenter un élément de la liste d'ordre des capteurs
     */
    data class SensorOrderList(val name: String)

    /**
     * Énumération des différents écrans de l'application
     */
    enum class SensorAppScreen() {
        HOME,    // Écran d'accueil
        DETAIL,  // Écran de détail
        ORDER,   // Écran de configuration de l'ordre
        HISTORY  // Écran d'historique
    }

    /**
     * Composable pour l'écran d'accueil
     * Affiche la liste des capteurs disponibles et permet la sélection
     */
    @Composable
    fun HomeScreen(
        navController: NavController,
        ipAddress: String,
        onIpAddressChange: (String) -> Unit,
        sensorList: List<Sensor>,
        onSensorSelected: (String, Int) -> Unit
    ) {
        // Actualisation automatique de la liste des capteurs toutes les 5 secondes
        LaunchedEffect(Unit) {
            while (true) {
                delay(5000)
                sendGetSensorList()
            }
        }

        Column(
            modifier = Modifier
                .padding(WindowInsets.safeDrawing.asPaddingValues()) ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column( modifier = Modifier.padding(horizontal = 12.dp))
            {
                // Champ de saisie pour l'adresse IP du serveur
                TextField(
                    value = ipAddress,
                    onValueChange = onIpAddressChange,
                    label = { Text("Entrez l'addresse du serveur") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.padding(50.dp))

                // Affichage de la liste des capteurs ou message si aucun capteur
                if (sensorList.isNotEmpty()) {
                    Text(
                        "Capteurs disponibles",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Création d'un bouton pour chaque capteur
                    sensorList.forEachIndexed { index, sensor ->
                        Button(
                            onClick = {
                                // Sélection du capteur et navigation vers l'écran de détail
                                onSensorSelected(sensor.crouscam_id, sensor.db_id)
                                navController.navigate(SensorAppScreen.DETAIL.name) {
                                    popUpTo(SensorAppScreen.DETAIL.name) {
                                        inclusive = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            // Couleur du bouton selon le statut du capteur
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sensor.status == "connected") Green200 else Color.Gray
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Capteur ${sensor.crouscam_id}")
                                var text = ""
                                if (sensor.status == "disconnected"){
                                    text ="Hors ligne"
                                } else if (sensor.status == "unknown"){
                                    text = "Inconnu"
                                }
                                else {
                                    text = "En ligne"
                                }
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                } else {
                    // Message affiché quand aucun capteur n'est détecté
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
    }

    /**
     * Composable pour l'écran de détail
     * Affiche les valeurs actuelles du capteur sélectionné sous forme de jauges
     */
    @Composable
    fun DetailScreen(
        navController: NavController
    ) {
        // Actualisation automatique des valeurs toutes les 5 secondes
        LaunchedEffect(Unit) {
            while (true) {
                delay(5000)
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

            // Première ligne : Température et Luminosité
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
                            value = temperature,
                            maxValue = 40f,
                            arcValue = temperature / 40f,
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
                            value = luminosity,
                            maxValue = 70000f,
                            arcValue = luminosity / 70000f,
                        )
                    ) { }
                }
            }

            // Deuxième ligne : Humidité et Pression
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
                            unit = "%",
                            value = humidity,
                            maxValue = 100f,
                            arcValue = humidity / 100,
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
                            unit = "hPa",
                            value = pressure,
                            maxValue = 1500f,
                            arcValue = pressure / 1500f,
                        )
                    ) { }
                }
            }

            // Troisième ligne : UV (centré)
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
                            unit = "",
                            value = uv,
                            maxValue = 70f,
                            arcValue = uv / 70f,
                        )
                    ) { }
                }
            }

            // Boutons de navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Bouton retour vers l'accueil
                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.HOME.name) {
                            popUpTo(SensorAppScreen.HOME.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
                ) {
                    Text("< Accueil")
                }

                // Bouton vers l'historique
                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.HISTORY.name) {
                            popUpTo(SensorAppScreen.HISTORY.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Historique",
                        modifier = Modifier.size(25.dp)
                    )
                }

                // Bouton vers la configuration de l'ordre
                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.ORDER.name) {
                            popUpTo(SensorAppScreen.ORDER.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
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
        // Mise en page verticale avec un centrage vertical et horizontal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Liste d'éléments pouvant être déplacés (drag and drop)
            DraggableList(
                items = myListItems,
                onMove = onMove
            )

            // Ligne contenant deux boutons : "Détails" et "Accueil"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.DETAIL.name) {
                            // Vide la pile de navigation jusqu’à l’écran DETAIL
                            popUpTo(SensorAppScreen.DETAIL.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
                    // Exemple de personnalisation des couleurs (commenté)
                    // colors = ButtonDefaults.buttonColors(containerColor = Yellow500)
                ) {
                    Text("< Détails")
                }

                Button(
                    onClick = {
                        navController.navigate(SensorAppScreen.HOME.name) {
                            // Vide la pile de navigation jusqu’à l’écran HOME
                            popUpTo(SensorAppScreen.HOME.name) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
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
        // Effet déclenché au lancement du composable, répété toutes les 5 secondes
        LaunchedEffect(Unit) {
            while (true) {
                delay(5000)
                sendGetSensorValues() // Appel de récupération des valeurs des capteurs
            }
        }

        // Colonne principale contenant la liste historique et les boutons
        Column() {
            // Liste verticale des lectures de capteurs
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sensorValuesHistory) { sensorValue ->
                    // Carte contenant une lecture de capteur
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Informations générales sur la lecture
                            Text(
                                text = "Lecture #${sensorValue.reading_db_id}",
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

                            // Séparateur horizontal
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Affichage des valeurs dans deux colonnes
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

            // Ligne des boutons de navigation (Accueil, Détail, Ordre)
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
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
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
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
                ) {
                    // Icône de retour vers les détails
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Retour detail",
                        modifier = Modifier.size(25.dp),
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
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
                ) {
                    Text("Ordre >")
                }
            }
        }
    }

    // Élément d'affichage individuel d'une valeur de capteur avec son étiquette
    @Composable
    fun SensorValueItem(label: String, value: String) {
        Row(
            modifier = Modifier
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Étiquette de la valeur
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(80.dp)
            )
            // Valeur correspondante
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
