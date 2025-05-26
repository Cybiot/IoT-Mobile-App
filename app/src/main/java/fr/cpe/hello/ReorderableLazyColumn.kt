package fr.cpe.hello

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun MyList(
    items: List<MainActivity.SensorOrderList>,
    onMove: (List<MainActivity.SensorOrderList>) -> Unit
) {
    val stateList = rememberLazyListState()

    val dragDropState =
        rememberDragDropState(
            lazyListState = stateList,
            draggableItemsNum = items.size,
            onMove = { fromIndex, toIndex ->
                Log.d("ITEMS", items.joinToString { it.name })
                var newList = items.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
                onMove(newList)
            })

    LazyColumn(
        userScrollEnabled = false,
        modifier = Modifier.dragContainer(dragDropState),
        state = stateList,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Ordre d'affichage", fontSize = 30.sp)
        }

        draggableItems(items = items, dragDropState = dragDropState) { modifier, item ->
            Item(modifier = modifier, name = item.name)
        }

    }
}


@Composable
private fun Item(modifier: Modifier = Modifier, name: String) {
    Card(modifier = modifier) {
        Text(
            name,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
    }
}