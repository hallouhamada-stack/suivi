package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.SettingsPreferences

import com.example.ui.theme.TwitterBackground
import com.example.ui.theme.TwitterBlue
import com.example.ui.theme.TwitterBlueLight
import com.example.ui.theme.TwitterBorder
import com.example.ui.theme.TwitterGreen
import com.example.ui.theme.TwitterGreenLight
import com.example.ui.theme.TwitterTextPrimary
import com.example.ui.theme.TwitterTextSecondary
import com.example.ui.theme.TwitterWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: SettingsPreferences,
    onBack: () -> Unit
) {
    var provider by remember { mutableStateOf(prefs.aiProvider) }
    var geminiKey by remember { mutableStateOf(prefs.geminiApiKey) }
    var openaiKey by remember { mutableStateOf(prefs.openaiApiKey) }
    var language by remember { mutableStateOf(prefs.language) }
    var inspectorName by remember { mutableStateOf(prefs.inspectorName) }
    var contractorName by remember { mutableStateOf(prefs.contractorName) }
    var siteName by remember { mutableStateOf(prefs.siteName) }

    var keyVisible by remember { mutableStateOf(false) }
    var showSavedToast by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Paramètres & Vision AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TwitterTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = TwitterTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TwitterWhite,
                    titleContentColor = TwitterTextPrimary,
                    navigationIconContentColor = TwitterTextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(TwitterBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section 1: AI Provider & API Key
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TwitterWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = TwitterBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Configuration Vision AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TwitterTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Fournisseur d'IA Vision :",
                        style = MaterialTheme.typography.labelMedium,
                        color = TwitterTextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = provider == "GEMINI",
                            onClick = { provider = "GEMINI" },
                            label = { Text("Google Gemini (3.5 Flash)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = provider == "OPENAI",
                            onClick = { provider = "OPENAI" },
                            label = { Text("OpenAI (GPT-4o mini)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // API Key Field
                    Text(
                        text = if (provider == "GEMINI") "Clé API Google Gemini :" else "Clé API OpenAI :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TwitterTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = if (provider == "GEMINI") geminiKey else openaiKey,
                        onValueChange = {
                            if (provider == "GEMINI") geminiKey = it else openaiKey = it
                        },
                        placeholder = {
                            Text(if (provider == "GEMINI") "AIzaSy..." else "sk-...")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = TwitterTextSecondary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (keyVisible) "Masquer" else "Afficher",
                                    tint = TwitterTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "La clé est stockée localement de façon sécurisée sur votre appareil.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TwitterTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Default Language
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TwitterWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = TwitterBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Langue par défaut des observations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TwitterTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = language == "fr",
                            onClick = { language = "fr" },
                            label = { Text("🇫🇷 Français") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = language == "ar",
                            onClick = { language = "ar" },
                            label = { Text("🇸🇦 العربية") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = language == "en",
                            onClick = { language = "en" },
                            label = { Text("🇬🇧 English") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Project & Parties (for PDF Joint Report)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TwitterWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Informations Chantier & Signatures",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TwitterTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = siteName,
                        onValueChange = { siteName = it },
                        label = { Text("Nom du Chantier / Opération") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inspectorName,
                        onValueChange = { inspectorName = it },
                        label = { Text("Nom de l'Inspecteur / Contrôleur") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = contractorName,
                        onValueChange = { contractorName = it },
                        label = { Text("Nom de l'Entreprise / Titulaire") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    prefs.aiProvider = provider
                    if (provider == "GEMINI") prefs.geminiApiKey = geminiKey else prefs.openaiApiKey = openaiKey
                    prefs.language = language
                    prefs.inspectorName = inspectorName
                    prefs.contractorName = contractorName
                    prefs.siteName = siteName
                    showSavedToast = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_settings_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TwitterWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enregistrer les paramètres", fontWeight = FontWeight.Bold, color = TwitterWhite)
            }

            if (showSavedToast) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = TwitterGreenLight,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Paramètres enregistrés avec succès !",
                        color = TwitterGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
