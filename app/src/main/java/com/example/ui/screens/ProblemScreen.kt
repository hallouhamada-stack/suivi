package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.InspectionStatus
import com.example.ui.components.BreadcrumbNav
import com.example.ui.components.DefectCaptureModal
import com.example.ui.components.FloorCard
import com.example.ui.viewmodel.InspectionViewModel
import com.example.ui.viewmodel.NavStep
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemScreen(
    viewModel: InspectionViewModel,
    onNavigateToSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedElement by viewModel.selectedElement.collectAsStateWithLifecycle()
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val activeFloorIndex by viewModel.activeFloorIndex.collectAsStateWithLifecycle()
    val activeFloor by viewModel.activeFloor.collectAsStateWithLifecycle()
    val activeObservations by viewModel.activeFloorObservations.collectAsStateWithLifecycle()
    val defectModalFloor by viewModel.defectModalFloor.collectAsStateWithLifecycle()
    val editingObservation by viewModel.editingObservation.collectAsStateWithLifecycle()

    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraFloor by remember { mutableStateOf<FloorInspectionEntity?>(null) }

    // Camera picture take launcher with instant compression
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
            val target = pendingCameraFloor ?: activeFloor
            if (target != null) {
                // Compress camera photo immediately
                val compressed = com.example.util.ImageCompressionUtils.compressAndSaveImage(context, tempCameraFile!!)
                capturedPhotoPath = compressed?.absolutePath ?: tempCameraFile!!.absolutePath
                viewModel.openAddObservationDialog(target)
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
            Toast.makeText(context, "Erreur ouverture caméra: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val target = pendingCameraFloor ?: activeFloor
            if (target != null) {
                launchCameraInternal(target)
            }
        } else {
            Toast.makeText(context, "Permission caméra requise pour prendre une photo", Toast.LENGTH_SHORT).show()
        }
    }

    fun openCamera(floor: FloorInspectionEntity) {
        pendingCameraFloor = floor
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCameraInternal(floor)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Contrôle : ${activeFloor?.floorNumber ?: "Étage"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.TwitterTextPrimary
                        )
                        Text(
                            text = "${selectedElement?.name ?: "Espace"} (Niveau $activeFloorIndex / ${floors.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.TwitterTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(NavStep.ETAGES) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour aux Étages",
                            tint = com.example.ui.theme.TwitterBlue
                        )
                    }
                },
                actions = {
                    if (activeFloor != null) {
                        IconButton(
                            onClick = { openCamera(activeFloor!!) },
                            modifier = Modifier.testTag("topbar_camera_button")
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Ouvrir Caméra",
                                tint = com.example.ui.theme.TwitterBlue
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSummary) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "Rapport PDF",
                            tint = com.example.ui.theme.TwitterBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.TwitterWhite
                )
            )
        },
        floatingActionButton = {
            if (activeFloor != null) {
                ExtendedFloatingActionButton(
                    onClick = { openCamera(activeFloor!!) },
                    icon = {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
                    },
                    text = {
                        Text("Ouvrir Caméra", fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    containerColor = com.example.ui.theme.TwitterBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.testTag("fab_open_camera")
                )
            }
        },
        containerColor = com.example.ui.theme.TwitterBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Breadcrumb Header: Home > Espace > Étages > Problème
            BreadcrumbNav(
                currentStep = NavStep.PROBLEME,
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
                // Quick Open Camera Callout Card
                if (activeFloor != null) {
                    Card(
                        onClick = { openCamera(activeFloor!!) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_camera_banner"),
                        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.TwitterWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.TwitterBorder),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = com.example.ui.theme.TwitterBlueLight,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = com.example.ui.theme.TwitterBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ouvrir la Caméra",
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.TwitterTextPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Prendre une photo du problème pour analyse Vision AI",
                                    color = com.example.ui.theme.TwitterTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = com.example.ui.theme.TwitterBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (activeFloor != null) {
                    FloorCard(
                        floor = activeFloor!!,
                        elementName = selectedElement?.name ?: "Espace",
                        totalFloors = floors.size,
                        onValidateOk = { viewModel.validateFloorConforme(it) },
                        onOpenDefectDialog = { viewModel.openAddObservationDialog(it) },
                        observations = activeObservations,
                        onAddObservation = { viewModel.openAddObservationDialog(it) },
                        onEditObservation = { floor, obs -> viewModel.openEditObservationDialog(floor, obs) },
                        onDeleteObservation = { floor, obs -> viewModel.deleteObservation(floor, obs) },
                        onOpenCamera = { openCamera(it) },
                        onPrevFloor = { viewModel.prevFloor() },
                        onNextFloor = { viewModel.nextFloor() },
                        hasPrev = activeFloorIndex > 1,
                        hasNext = activeFloorIndex < floors.size,
                        onDeleteFloor = if (floors.size > 1) {
                            { viewModel.removeFloor(activeFloor!!) }
                        } else null
                    )
                }

                // Return to Floors List Button
                OutlinedButton(
                    onClick = { viewModel.navigateTo(NavStep.ETAGES) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("back_to_etages_list_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = com.example.ui.theme.TwitterBlue
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.TwitterBorder),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.FormatListBulleted,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = com.example.ui.theme.TwitterBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voir la liste de tous les étages de cet espace",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    if (defectModalFloor != null) {
        DefectCaptureModal(
            floor = defectModalFloor!!,
            observationToEdit = editingObservation,
            aiService = viewModel.aiService,
            aiProvider = viewModel.settingsPrefs.aiProvider,
            apiKey = viewModel.settingsPrefs.activeApiKey,
            language = viewModel.settingsPrefs.language,
            initialPhotoPath = capturedPhotoPath,
            onDismiss = {
                capturedPhotoPath = null
                viewModel.closeDefectDialog()
            },
            onSaveDefect = { photoUrl, observation, isAi ->
                capturedPhotoPath = null
                viewModel.saveDefect(photoUrl, observation, isAi)
            }
        )
    }
}
