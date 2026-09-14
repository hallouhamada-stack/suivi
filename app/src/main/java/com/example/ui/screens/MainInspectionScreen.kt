package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.InspectionStatus
import com.example.ui.components.AddFloorDialog
import com.example.ui.components.DefectCaptureModal
import com.example.ui.components.FloorCard
import com.example.ui.components.FloorStepper
import com.example.ui.components.InspectionSummaryTable
import com.example.ui.viewmodel.InspectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainInspectionScreen(
    viewModel: InspectionViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedBuilding by viewModel.selectedBuilding.collectAsStateWithLifecycle()
    val selectedElement by viewModel.selectedElement.collectAsStateWithLifecycle()
    val elements by viewModel.elements.collectAsStateWithLifecycle()
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val activeFloorIndex by viewModel.activeFloorIndex.collectAsStateWithLifecycle()
    val activeFloor by viewModel.activeFloor.collectAsStateWithLifecycle()
    val defectModalFloor by viewModel.defectModalFloor.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var elementDropdownExpanded by remember { mutableStateOf(false) }
    var showTableSection by remember { mutableStateOf(true) }
    var showAddFloorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF97316)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SiteInspect AI",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Contrôle par étage • Vision AI",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                actions = {
                    // Quick Add Floor button in TopAppBar
                    IconButton(
                        onClick = { showAddFloorDialog = true },
                        modifier = Modifier.testTag("topbar_add_floor_btn")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Ajouter un étage",
                            tint = Color(0xFFF97316)
                        )
                    }

                    // Summary & PDF Report button
                    IconButton(
                        onClick = onNavigateToSummary,
                        modifier = Modifier.testTag("summary_nav_btn")
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Synthèse & PDF",
                            tint = Color.White
                        )
                    }

                    // Settings button
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_nav_btn")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Hierarchy Selector Bar (Building -> Element/Room)
            Surface(
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedBuilding?.name ?: "Bâtiment A",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Room / Element Selector Dropdown
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { elementDropdownExpanded = true }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedElement?.name ?: "Cuisine T5",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.ExpandMore,
                                    contentDescription = "Changer d'élément",
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = elementDropdownExpanded,
                                onDismissRequest = { elementDropdownExpanded = false }
                            ) {
                                elements.forEach { elem ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(elem.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    elem.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectElement(elem)
                                            elementDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF97316),
                            modifier = Modifier
                                .clickable { showAddFloorDialog = true }
                                .testTag("hierarchy_add_floor_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "+ Étage",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Reset checklist action
                        IconButton(
                            onClick = { viewModel.resetFloorsForElement() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Réinitialiser les 14 étages",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Floor Stepper (Horizontal Carousel & Progress)
            FloorStepper(
                floors = floors,
                activeFloorIndex = activeFloorIndex,
                onFloorSelected = { viewModel.selectFloor(it) },
                onAddFloorClick = { showAddFloorDialog = true }
            )

            // Main Floor Card (Rapid 1-Click Verification or Defect Snap)
            Box(modifier = Modifier.padding(16.dp)) {
                if (activeFloor != null) {
                    FloorCard(
                        floor = activeFloor!!,
                        elementName = selectedElement?.name ?: "Cuisine T5",
                        totalFloors = floors.size,
                        onValidateOk = { viewModel.validateFloorConforme(it) },
                        onOpenDefectDialog = { viewModel.openDefectDialog(it) },
                        onPrevFloor = { viewModel.prevFloor() },
                        onNextFloor = { viewModel.nextFloor() },
                        hasPrev = activeFloorIndex > 1,
                        hasNext = activeFloorIndex < floors.size,
                        onDeleteFloor = { activeFloor?.let { viewModel.removeFloor(it) } }
                    )
                }
            }

            // Summary Table Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTableSection = !showTableSection },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tableau de bord par étage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${floors.count { it.status == InspectionStatus.OK.name }} Conformes • ${floors.count { it.status == InspectionStatus.DEFECT.name }} Défauts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (showTableSection) "Masquer" else "Afficher",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showTableSection) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            InspectionSummaryTable(
                                floors = floors,
                                onFloorClick = { viewModel.selectFloor(it) },
                                modifier = Modifier.height(260.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Defect Capture & AI Analysis Dialog
    if (defectModalFloor != null) {
        DefectCaptureModal(
            floor = defectModalFloor!!,
            aiService = viewModel.aiService,
            aiProvider = viewModel.settingsPrefs.aiProvider,
            apiKey = if (viewModel.settingsPrefs.aiProvider == "GEMINI") viewModel.settingsPrefs.geminiApiKey else viewModel.settingsPrefs.openaiApiKey,
            language = viewModel.settingsPrefs.language,
            onDismiss = { viewModel.closeDefectDialog() },
            onSaveDefect = { photoUrl, observation, isAiGenerated ->
                viewModel.saveDefect(photoUrl, observation, isAiGenerated)
            }
        )
    }

    // Add Floor Dialog
    if (showAddFloorDialog) {
        val nextNumber = "Étage %02d".format((floors.maxOfOrNull { it.floorIndex } ?: 0) + 1)
        AddFloorDialog(
            nextDefaultNumber = nextNumber,
            onDismiss = { showAddFloorDialog = false },
            onConfirmAdd = { floorName ->
                viewModel.addNewFloor(floorName)
                showAddFloorDialog = false
            }
        )
    }
}
