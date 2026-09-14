package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.data.model.InspectionStatus
import com.example.ui.components.AddProjectDialog
import com.example.ui.components.BreadcrumbNav
import com.example.ui.components.ProjectSelectorDialog
import com.example.ui.theme.TwitterBackground
import com.example.ui.theme.TwitterBlue
import com.example.ui.theme.TwitterBlueDark
import com.example.ui.theme.TwitterBlueLight
import com.example.ui.theme.TwitterBorder
import com.example.ui.theme.TwitterBorderDarker
import com.example.ui.theme.TwitterGreen
import com.example.ui.theme.TwitterGreenLight
import com.example.ui.theme.TwitterRed
import com.example.ui.theme.TwitterRedLight
import com.example.ui.theme.TwitterTextPrimary
import com.example.ui.theme.TwitterTextSecondary
import com.example.ui.theme.TwitterTextSubdued
import com.example.ui.theme.TwitterWhite
import com.example.ui.viewmodel.InspectionViewModel
import com.example.ui.viewmodel.NavStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: InspectionViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buildings by viewModel.buildings.collectAsStateWithLifecycle()
    val building by viewModel.selectedBuilding.collectAsStateWithLifecycle()
    val elements by viewModel.elements.collectAsStateWithLifecycle()
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val selectedElement by viewModel.selectedElement.collectAsStateWithLifecycle()
    val activeFloor by viewModel.activeFloor.collectAsStateWithLifecycle()

    var showProjectSelectorDialog by remember { mutableStateOf(false) }
    var showAddProjectDialog by remember { mutableStateOf(false) }

    val totalFloorsCount = floors.size
    val okCount = floors.count { it.status == InspectionStatus.OK.name }
    val defectCount = floors.count { it.status == InspectionStatus.DEFECT.name }
    val pendingCount = totalFloorsCount - okCount - defectCount
    val complianceRate = if (okCount + defectCount > 0) {
        (okCount.toFloat() / (okCount + defectCount) * 100).toInt()
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TwitterBlue,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SiteInspect AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TwitterTextPrimary
                            )
                            Text(
                                text = "Contrôle Qualité Chantier",
                                style = MaterialTheme.typography.bodySmall,
                                color = TwitterTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showProjectSelectorDialog = true },
                        modifier = Modifier.testTag("switch_project_topbar_btn")
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Changer de projet",
                            tint = TwitterBlue
                        )
                    }
                    IconButton(
                        onClick = { showAddProjectDialog = true },
                        modifier = Modifier.testTag("add_project_topbar_btn")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Nouveau projet",
                            tint = TwitterBlue
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSummary,
                        modifier = Modifier.testTag("home_summary_btn")
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Rapport PDF",
                            tint = TwitterBlue
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("home_settings_btn")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = TwitterTextSecondary
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
            // Interactive Breadcrumb Header
            BreadcrumbNav(
                currentStep = NavStep.HOME,
                selectedSpaceName = selectedElement?.name,
                selectedFloorName = activeFloor?.floorNumber,
                onStepClick = { step -> viewModel.navigateTo(step) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Project & Site Card (Clean White & Blue Twitter Design)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TwitterWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = TwitterBlueLight
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = TwitterBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = building?.code ?: "BAT-A",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TwitterBlue
                                    )
                                }
                            }
                            Text(
                                text = "En cours d'inspection",
                                fontSize = 12.sp,
                                color = TwitterTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = viewModel.settingsPrefs.siteName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TwitterTextPrimary
                        )
                        Text(
                            text = "${building?.name ?: "Bâtiment A"} • Entreprise : ${building?.contractorName ?: "BTP"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TwitterTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch project & Add another project buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showProjectSelectorDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("switch_project_card_btn"),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TwitterBlue),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorderDarker)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp), tint = TwitterBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Projets (${buildings.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = { showAddProjectDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_project_card_btn"),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TwitterBlue),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBlueLight)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = TwitterBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Nouveau", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TwitterBlue, maxLines = 1)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Twitter Blue Call To Action Pill Button
                        Button(
                            onClick = { viewModel.navigateTo(NavStep.SPACE) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_inspection_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Accéder aux Espaces (Pièces / Lots) →",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Global KPI Stats Title
                Text(
                    text = "Aperçu de l'Inspection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TwitterTextPrimary
                )

                // Simple Clean KPI Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiStatCard(
                        title = "Espaces",
                        value = elements.size.toString(),
                        subtitle = "Lots",
                        icon = Icons.Default.Apartment,
                        color = TwitterBlue,
                        modifier = Modifier.weight(1f)
                    )

                    KpiStatCard(
                        title = "Étages",
                        value = totalFloorsCount.toString(),
                        subtitle = "Niveaux",
                        icon = Icons.Default.Layers,
                        color = TwitterBlueDark,
                        modifier = Modifier.weight(1f)
                    )

                    KpiStatCard(
                        title = "Défauts",
                        value = defectCount.toString(),
                        subtitle = "Anomalies",
                        icon = Icons.Default.Error,
                        color = TwitterRed,
                        modifier = Modifier.weight(1f)
                    )

                    KpiStatCard(
                        title = "Conformes",
                        value = okCount.toString(),
                        subtitle = "Validés",
                        icon = Icons.Default.CheckCircle,
                        color = TwitterGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Compliance Rate Card (Clean Twitter White card with subtle border)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TwitterWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Taux de Conformité actuel",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = TwitterTextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (complianceRate >= 80) TwitterGreenLight else TwitterRedLight
                            ) {
                                Text(
                                    text = "$complianceRate%",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = if (complianceRate >= 80) TwitterGreen else TwitterRed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { if (totalFloorsCount > 0) okCount.toFloat() / totalFloorsCount else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = TwitterBlue,
                            trackColor = TwitterBorder,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = TwitterGreen, modifier = Modifier.size(8.dp)) {}
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$okCount validés", fontSize = 12.sp, color = TwitterTextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = TwitterRed, modifier = Modifier.size(8.dp)) {}
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$defectCount défauts", fontSize = 12.sp, color = TwitterTextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = TwitterTextSubdued, modifier = Modifier.size(8.dp)) {}
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$pendingCount en attente", fontSize = 12.sp, color = TwitterTextSecondary)
                            }
                        }
                    }
                }

                // Workflow Navigation Shortcut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Workflow d'Inspection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TwitterTextPrimary
                    )
                    Text(
                        text = "Étape par étape",
                        style = MaterialTheme.typography.bodySmall,
                        color = TwitterTextSecondary
                    )
                }

                WorkflowStepCard(
                    stepNumber = "1",
                    title = "Espaces (Pièces / Lots)",
                    description = "Sélectionnez ou créez un espace (ex: Cuisine T5, Salle d'eau...)",
                    onClick = { viewModel.navigateTo(NavStep.SPACE) }
                )

                WorkflowStepCard(
                    stepNumber = "2",
                    title = "Étages (Niveaux systématiques)",
                    description = "Visualisez la liste des étages et ajoutez des niveaux",
                    onClick = { viewModel.navigateTo(NavStep.ETAGES) }
                )

                WorkflowStepCard(
                    stepNumber = "3",
                    title = "Contrôle & Analyse Photo",
                    description = "Validez en 1-clic ou capturez les défauts avec la caméra",
                    onClick = { viewModel.navigateTo(NavStep.PROBLEME) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showProjectSelectorDialog) {
            ProjectSelectorDialog(
                buildings = buildings,
                selectedBuilding = building,
                onSelectBuilding = { b -> viewModel.selectBuilding(b) },
                onDeleteBuilding = { b -> viewModel.deleteBuilding(b) },
                onOpenAddProject = { showAddProjectDialog = true },
                onDismiss = { showProjectSelectorDialog = false }
            )
        }

        if (showAddProjectDialog) {
            AddProjectDialog(
                defaultContractor = viewModel.settingsPrefs.contractorName,
                defaultInspector = viewModel.settingsPrefs.inspectorName,
                onDismiss = { showAddProjectDialog = false },
                onConfirmAdd = { name, code, contractor, inspector ->
                    viewModel.addNewBuilding(name, code, contractor, inspector)
                    showAddProjectDialog = false
                }
            )
        }
    }
}

@Composable
fun KpiStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TwitterWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TwitterTextPrimary
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TwitterTextSecondary
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = TwitterTextSubdued
            )
        }
    }
}

@Composable
fun WorkflowStepCard(
    stepNumber: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TwitterWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = TwitterBlueLight,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stepNumber,
                        fontWeight = FontWeight.Bold,
                        color = TwitterBlue
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TwitterTextPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TwitterTextSecondary
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TwitterBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
