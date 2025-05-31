package fr.cpe.hello

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.cpe.hello.MainActivity.SensorAppScreen
import fr.cpe.hello.model.LevelState

// Composable pour la page Detail
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
                arcValue = 0.20f,
            )
            ) { }
            LevelScreen( LevelState(
                unitName = "Luminosité",
                unit = "lux",
                value = 3f,
                maxValue = 5f,
                arcValue = 0.20f,
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
                arcValue = 0.37f,
            )
            ) { }
            LevelScreen( LevelState(
                unitName = "Pression",
                unit = "Pa",
                value = 10f,
                maxValue = 20f,
                arcValue = 0.42f,
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


        // Bouton de retour à Home
        Button(
            onClick = {
                navController.navigate(SensorAppScreen.HOME.name) {
                    // Optionnel : vider la pile de navigation pour éviter l'accumulation
                    popUpTo(SensorAppScreen.HOME.name) {
                        inclusive = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retour à l'accueil")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton de retour avec popBackStack (alternative)
        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retour (pop)")
        }
    }
}