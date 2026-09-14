package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ElementEntity
import com.example.ui.components.AddSpaceDialog
import com.example.ui.components.BreadcrumbNav
import com.example.ui.theme.TwitterBackground
import com.example.ui.theme.TwitterBlue
import com.example.ui.theme.TwitterBlueDark
import com.example.ui.theme.TwitterBlueLight
import com.example.ui.theme.TwitterBorder
import com.example.ui.theme.TwitterBorderDarker
import com.example.ui.theme.TwitterGreen
import com.example.ui.theme.TwitterRed
import com.example.ui.theme.TwitterTextPrimary
import com.example.ui.theme.TwitterTextSecondary
import com.example.ui.theme.TwitterTextSubdued
import com.example.ui.theme.TwitterWhite
import com.example.ui.viewmodel.InspectionViewModel
import com.example.ui.viewmodel.NavStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceScreen(
    viewModel: InspectionViewModel,
    onNavigateToSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elements by viewModel.elements.collectAsStateWithLifecycle()
    val selectedElement by viewModel.selectedElement.collectAsStateWithLifecycle()
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val activeFloor by viewModel.activeFloor.collectAsStateWithLifecycle()

    var showAddSpaceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Espaces de Contrôle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TwitterTextPrimary
                        )
                        Text(
                            text = "Sélectionnez le lot ou la pièce",
                            style = MaterialTheme.typography.bodySmall,
                            color = TwitterTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(NavStep.HOME) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour Home",
                            tint = TwitterBlue
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSummary,
                        modifier = Modifier.testTag("space_export_pdf_topbar_btn")
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "Exporter Rapport PDF Espace",
                            tint = TwitterBlue
                        )
                    }
                    IconButton(
                        onClick = { showAddSpaceDialog = true },
                        modifier = Modifier.testTag("add_space_topbar_btn")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Ajouter un espace",
                            tint = TwitterBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TwitterWhite,
                    titleContentColor = TwitterTextPrimary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TwitterBackground)
        ) {
            // Interactive Breadcrumbs
            BreadcrumbNav(
                currentStep = NavStep.SPACE,
                selectedSpaceName = selectedElement?.name,
                selectedFloorName = activeFloor?.floorNumber,
                onStepClick = { step -> viewModel.navigateTo(step) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Header Banner with Quick Add (Twitter Blue Style)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = TwitterBlueLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorderDarker)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gestion des Espaces",
                                    fontWeight = FontWeight.Bold,
                                    color = TwitterBlueDark,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Ajoutez ou sélectionnez un lot pour commencer l'inspection par étage.",
                                    fontSize = 12.sp,
                                    color = TwitterTextSecondary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { showAddSpaceDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("add_space_banner_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Espace", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Liste des Espaces (${elements.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TwitterTextPrimary
                        )
                        Text(
                            text = "Touchez pour inspecter →",
                            fontSize = 12.sp,
                            color = TwitterTextSecondary
                        )
                    }
                }

                items(elements, key = { it.id }) { element ->
                    val isSelected = selectedElement?.id == element.id
                    SpaceCardItem(
                        element = element,
                        isSelected = isSelected,
                        onSelect = {
                            viewModel.selectElement(element)
                        },
                        onExportPdf = {
                            viewModel.selectElement(element)
                            onNavigateToSummary()
                        },
                        onDelete = if (elements.size > 1) {
                            { viewModel.deleteElement(element) }
                        } else null
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    if (showAddSpaceDialog) {
        AddSpaceDialog(
            onDismiss = { showAddSpaceDialog = false },
            onConfirmAdd = { name, desc, floorsCount ->
                viewModel.addNewElement(name, desc, floorsCount)
                showAddSpaceDialog = false
            }
        )
    }
}

@Composable
fun SpaceCardItem(
    element: ElementEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onExportPdf: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("space_card_${element.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TwitterBlueLight else TwitterWhite
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) TwitterBlue else TwitterBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) TwitterBlue else TwitterBlueLight,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Apartment,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else TwitterBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = element.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TwitterTextPrimary
                    )
                    Text(
                        text = element.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TwitterTextSecondary
                    )
                }

                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Supprimer cet espace",
                            tint = TwitterTextSubdued,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(onClick = onSelect, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Voir les étages",
                        tint = TwitterBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) TwitterWhite else TwitterBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(12.dp), tint = TwitterTextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${element.totalFloors} Étages",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TwitterTextSecondary
                        )
                    }
                }

                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TwitterBlue
                    ) {
                        Text(
                            text = "Actif",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onExportPdf,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("export_space_pdf_${element.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TwitterBlue),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorderDarker)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = TwitterBlue, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rapport PDF", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
