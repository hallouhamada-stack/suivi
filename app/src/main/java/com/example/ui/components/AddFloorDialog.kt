package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.ui.theme.TwitterBackground
import com.example.ui.theme.TwitterBlue
import com.example.ui.theme.TwitterBlueLight
import com.example.ui.theme.TwitterBorder
import com.example.ui.theme.TwitterTextPrimary
import com.example.ui.theme.TwitterTextSecondary
import com.example.ui.theme.TwitterWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFloorDialog(
    nextDefaultNumber: String,
    onDismiss: () -> Unit,
    onConfirmAdd: (floorName: String) -> Unit
) {
    var floorName by remember { mutableStateOf(nextDefaultNumber) }

    val presets = listOf(
        nextDefaultNumber,
        "RDC",
        "Sous-Sol -1",
        "Sous-Sol -2",
        "Combles",
        "Toiture terrasse",
        "Attique"
    ).distinct()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TwitterWhite,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_floor_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = TwitterBlueLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = null,
                            tint = TwitterBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Ajouter un étage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TwitterTextPrimary
                    )
                    Text(
                        text = "Ajout d'un niveau au contrôle systématique",
                        style = MaterialTheme.typography.bodySmall,
                        color = TwitterTextSecondary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Dénomination du niveau / étage :",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TwitterTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = floorName,
                    onValueChange = { floorName = it },
                    placeholder = { Text("Ex: Étage 15, RDC, Sous-Sol -1...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("floor_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Suggestions rapides :",
                    style = MaterialTheme.typography.labelSmall,
                    color = TwitterTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { preset ->
                        val isSelected = floorName == preset
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) TwitterBlue else TwitterBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .background(if (isSelected) TwitterBlueLight else TwitterBackground)
                                .clickable { floorName = preset }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TwitterBlue else TwitterTextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = floorName.trim().ifEmpty { nextDefaultNumber }
                    onConfirmAdd(finalName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("confirm_add_floor_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = TwitterWhite)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ajouter", fontWeight = FontWeight.Bold, color = TwitterWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TwitterTextSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
