package fr.cpe.hello

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.cpe.hello.MainActivity.SensorAppScreen
import fr.cpe.hello.MainActivity.SensorOrderList
import fr.cpe.hello.draggableList.DraggableList
import fr.cpe.hello.model.LevelState

// Composable pour la page Home
@Composable
fun HomeScreen(
    navController: NavController,
    ipAddress: String,
    onIpAddressChange: (String) -> Unit,
    myListItems: List<SensorOrderList>,
    onSendMessage: () -> Unit,
    onSendOrder: () -> Unit,
    onMove: (List<SensorOrderList>) -> Unit
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

        DraggableList(
            items = myListItems,
            onMove = onMove
        )
    }
}