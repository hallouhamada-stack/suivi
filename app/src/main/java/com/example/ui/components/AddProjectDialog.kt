package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Pin
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.ui.theme.TwitterBlue
import com.example.ui.theme.TwitterBlueLight
import com.example.ui.theme.TwitterTextPrimary
import com.example.ui.theme.TwitterTextSecondary
import com.example.ui.theme.TwitterWhite

@Composable
fun AddProjectDialog(
    defaultContractor: String = "BTP Construction Pro",
    defaultInspector: String = "Inspecteur Travaux",
    onDismiss: () -> Unit,
    onConfirmAdd: (name: String, code: String, contractor: String, inspector: String) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var projectCode by remember { mutableStateOf("") }
    var contractor by remember { mutableStateOf(defaultContractor) }
    var inspector by remember { mutableStateOf(defaultInspector) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TwitterWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TwitterBlueLight,
                    modifier = Modifier.size(36.dp)
                ) {
                    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Apartment,
                            contentDescription = null,
                            tint = TwitterBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Nouveau Projet / Chantier",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TwitterTextPrimary
                    )
                    Text(
                        text = "Créer un nouveau site à inspecter",
                        style = MaterialTheme.typography.bodySmall,
                        color = TwitterTextSecondary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Nom du Projet / Bâtiment *") },
                    placeholder = { Text("Ex: Bâtiment C - Tour Étoile") },
                    leadingIcon = {
                        Icon(Icons.Default.Business, contentDescription = null, tint = TwitterTextSecondary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = projectCode,
                    onValueChange = { projectCode = it },
                    label = { Text("Code / Réf. Chantier *") },
                    placeholder = { Text("Ex: BAT-C-2026") },
                    leadingIcon = {
                        Icon(Icons.Default.Pin, contentDescription = null, tint = TwitterTextSecondary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_code_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contractor,
                    onValueChange = { contractor = it },
                    label = { Text("Entreprise Générale") },
                    leadingIcon = {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = TwitterTextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inspector,
                    onValueChange = { inspector = it },
                    label = { Text("Inspecteur Référent") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (projectName.isNotBlank()) {
                        val finalCode = if (projectCode.isNotBlank()) projectCode.trim() else "PRJ-${System.currentTimeMillis() % 10000}"
                        onConfirmAdd(projectName.trim(), finalCode, contractor.trim(), inspector.trim())
                    }
                },
                enabled = projectName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("confirm_create_project_btn")
            ) {
                Text("Créer le projet", fontWeight = FontWeight.Bold, color = TwitterWhite)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_create_project_btn")
            ) {
                Text("Annuler", color = TwitterTextSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
