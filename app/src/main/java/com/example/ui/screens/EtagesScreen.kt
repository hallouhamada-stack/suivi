package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.FloorObservationEntity
import com.example.data.model.InspectionStatus
import com.example.ui.components.AddFloorDialog
import com.example.ui.components.BreadcrumbNav
import com.example.ui.components.DefectCaptureModal
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EtagesScreen(
    viewModel: InspectionViewModel,
    onNavigateToSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedElement by viewModel.selectedElement.collectAsStateWithLifecycle()
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val activeFloorIndex by viewModel.activeFloorIndex.collectAsStateWithLifecycle()
    val activeFloor by viewModel.activeFloor.collectAsStateWithLifecycle()
    val elementObservations by viewModel.elementObservations.collectAsStateWithLifecycle()
    val editingObservation by viewModel.editingObservation.collectAsStateWithLifecycle()

    val obsByFloor: Map<Long, List<FloorObservationEntity>> = remember(elementObservations) {
        elementObservations.groupBy { it.floorId }
    }

    val context = LocalContext.current
    var showAddFloorDialog by remember { mutableStateOf(false) }
    var defectFloorTarget by remember { mutableStateOf<FloorInspectionEntity?>(null) }
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraFloor by remember { mutableStateOf<FloorInspectionEntity?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            val target = pendingCameraFloor
            if (target != null) {
                val compressed = com.example.util.ImageCompressionUtils.compressAndSaveImage(context, tempCameraFile!!)
                capturedPhotoPath = compressed?.absolutePath ?: tempCameraFile!!.absolutePath
                defectFloorTarget = target
            }
        }
    }

    fun launchCameraInternal(targetFloor: FloorInspectionEntity) {
        try {
            val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            tempCameraFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur caméra: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val target = pendingCameraFloor
            if (target != null) {
                launchCameraInternal(target)
            }
        } else {
            Toast.makeText(context, "Permission caméra requise", Toast.LENGTH_SHORT).show()
        }
    }

    fun openCameraForFloor(floor: FloorInspectionEntity) {
        pendingCameraFloor = floor
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCameraInternal(floor)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val okCount = floors.count { it.status == InspectionStatus.OK.name }
    val defectCount = floors.count { it.status == InspectionStatus.DEFECT.name }
    val totalCount = floors.size
    val progress = if (totalCount > 0) (okCount + defectCount).toFloat() / totalCount else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Étages : ${selectedElement?.name ?: "Espace"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TwitterTextPrimary
                        )
                        Text(
                            text = "$totalCount niveaux configurés",
                            style = MaterialTheme.typography.bodySmall,
                            color = TwitterTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(NavStep.SPACE) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour Espaces",
                            tint = TwitterBlue
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSummary,
                        modifier = Modifier.testTag("etages_export_pdf_topbar_btn")
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "Exporter Rapport PDF Espace",
                            tint = TwitterBlue
                        )
                    }
                    IconButton(
                        onClick = { showAddFloorDialog = true },
                        modifier = Modifier.testTag("etages_add_floor_topbar_btn")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Ajouter un étage",
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
            // Interactive Breadcrumb Header
            BreadcrumbNav(
                currentStep = NavStep.ETAGES,
                selectedSpaceName = selectedElement?.name,
                selectedFloorName = activeFloor?.floorNumber,
                onStepClick = { step -> viewModel.navigateTo(step) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Space Summary Banner (Clean White & Blue Twitter Design)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = TwitterWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = TwitterBlueLight,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Layers,
                                                contentDescription = null,
                                                tint = TwitterBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = selectedElement?.name ?: "Espace",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TwitterTextPrimary
                                        )
                                        Text(
                                            text = "Progression : $okCount + $defectCount / $totalCount",
                                            fontSize = 11.sp,
                                            color = TwitterTextSecondary
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { showAddFloorDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.testTag("add_floor_in_etages_btn")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("+ Étage", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = TwitterBlue,
                                trackColor = TwitterBorder
                            )
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
                            text = "Liste des Niveaux ($totalCount)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TwitterTextPrimary
                        )
                        Text(
                            text = "Touchez un étage pour contrôler →",
                            fontSize = 11.sp,
                            color = TwitterTextSecondary
                        )
                    }
                }

                items(floors, key = { it.id }) { floor ->
                    val isSelected = floor.floorIndex == activeFloorIndex
                    FloorListItem(
                        floor = floor,
                        isSelected = isSelected,
                        observations = obsByFloor[floor.id] ?: emptyList(),
                        onInspectClick = {
                            viewModel.selectFloorAndInspect(floor)
                        },
                        onQuickOk = {
                            viewModel.markFloorOk(floor)
                        },
                        onQuickDefect = {
                            defectFloorTarget = floor
                        },
                        onQuickCamera = {
                            openCameraForFloor(floor)
                        },
                        onDelete = if (floors.size > 1) {
                            { viewModel.removeFloor(floor) }
                        } else null
                    )
                }

                item {
                    OutlinedButton(
                        onClick = { showAddFloorDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("add_more_floors_card_btn"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TwitterBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = TwitterBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Ajouter un autre étage (Étage %02d...)".format(totalCount + 1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                item {
                    Button(
                        onClick = onNavigateToSummary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .padding(vertical = 2.dp)
                            .testTag("bottom_export_espace_pdf_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue),
                        shape = RoundedCornerShape(23.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Exporter le Rapport PDF de l'Espace",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

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

    if (defectFloorTarget != null) {
        DefectCaptureModal(
            floor = defectFloorTarget!!,
            observationToEdit = editingObservation,
            aiService = viewModel.aiService,
            aiProvider = viewModel.settingsPrefs.aiProvider,
            apiKey = viewModel.settingsPrefs.activeApiKey,
            language = viewModel.settingsPrefs.language,
            initialPhotoPath = capturedPhotoPath,
            onDismiss = {
                capturedPhotoPath = null
                defectFloorTarget = null
            },
            onSaveDefect = { photoUrl, observation, isAi ->
                capturedPhotoPath = null
                viewModel.markFloorDefect(defectFloorTarget!!, photoUrl, observation, isAi)
                defectFloorTarget = null
            }
        )
    }
}

@Composable
fun FloorListItem(
    floor: FloorInspectionEntity,
    isSelected: Boolean,
    observations: List<com.example.data.model.FloorObservationEntity> = emptyList(),
    onInspectClick: () -> Unit,
    onQuickOk: () -> Unit,
    onQuickDefect: () -> Unit,
    onQuickCamera: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val status = try {
        InspectionStatus.valueOf(floor.status)
    } catch (_: Exception) {
        InspectionStatus.PENDING
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspectClick() }
            .testTag("floor_item_${floor.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                InspectionStatus.OK -> TwitterGreenLight.copy(alpha = 0.5f)
                InspectionStatus.DEFECT -> TwitterRedLight.copy(alpha = 0.5f)
                InspectionStatus.PENDING -> TwitterWhite
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = when {
                isSelected -> TwitterBlue
                status == InspectionStatus.OK -> TwitterGreen.copy(alpha = 0.4f)
                status == InspectionStatus.DEFECT -> TwitterRed.copy(alpha = 0.4f)
                else -> TwitterBorder
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon Indicator
            Surface(
                shape = CircleShape,
                color = when (status) {
                    InspectionStatus.OK -> TwitterGreen
                    InspectionStatus.DEFECT -> TwitterRed
                    InspectionStatus.PENDING -> TwitterBorderDarker
                },
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (status) {
                            InspectionStatus.OK -> Icons.Default.Check
                            InspectionStatus.DEFECT -> Icons.Default.Close
                            InspectionStatus.PENDING -> Icons.Default.HourglassEmpty
                        },
                        contentDescription = null,
                        tint = if (status == InspectionStatus.PENDING) TwitterTextSecondary else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Floor title & observation
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = floor.floorNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TwitterTextPrimary
                    )
                    if (floor.isAiGenerated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TwitterBlueLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TwitterBlue, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("AI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TwitterBlue)
                            }
                        }
                    }
                }

                if (observations.isNotEmpty()) {
                    if (observations.size > 1) {
                        val photoCount = observations.count { !it.photoUrl.isNullOrBlank() }
                        Text(
                            text = "● ${observations.size} anomalies (${photoCount} photo${if (photoCount > 1) "s" else ""})",
                            fontSize = 12.sp,
                            color = TwitterRed,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = observations.first().description,
                            fontSize = 12.sp,
                            color = TwitterRed,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                } else if (floor.observation.isNotBlank()) {
                    Text(
                        text = floor.observation,
                        fontSize = 12.sp,
                        color = if (status == InspectionStatus.DEFECT) TwitterRed else TwitterGreen,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = when (status) {
                            InspectionStatus.OK -> "Conforme / Validé"
                            InspectionStatus.DEFECT -> "Problème signalé"
                            InspectionStatus.PENDING -> "Non inspecté"
                        },
                        fontSize = 11.sp,
                        color = TwitterTextSecondary
                    )
                }
            }

            // Quick actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Green OK
                IconButton(
                    onClick = onQuickOk,
                    modifier = Modifier.size(30.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TwitterGreenLight,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = "OK", tint = TwitterGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Quick Red Defect
                IconButton(
                    onClick = onQuickDefect,
                    modifier = Modifier.size(30.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TwitterRedLight,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, contentDescription = "Défaut", tint = TwitterRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Quick Camera button (Twitter Blue Style)
                if (onQuickCamera != null) {
                    IconButton(
                        onClick = onQuickCamera,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TwitterBlueLight,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Ouvrir Caméra", tint = TwitterBlue, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // Inspect arrow
                IconButton(
                    onClick = onInspectClick,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Inspecter",
                        tint = TwitterBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Supprimer",
                            tint = TwitterTextSubdued,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
