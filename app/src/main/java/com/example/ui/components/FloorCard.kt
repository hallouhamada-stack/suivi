package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.FloorObservationEntity
import com.example.data.model.InspectionStatus
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
import java.io.File

@Composable
fun FloorCard(
    floor: FloorInspectionEntity,
    elementName: String,
    totalFloors: Int,
    onValidateOk: (FloorInspectionEntity) -> Unit,
    onOpenDefectDialog: (FloorInspectionEntity) -> Unit,
    onPrevFloor: () -> Unit,
    onNextFloor: () -> Unit,
    hasPrev: Boolean,
    hasNext: Boolean,
    observations: List<FloorObservationEntity> = emptyList(),
    onAddObservation: (FloorInspectionEntity) -> Unit = onOpenDefectDialog,
    onEditObservation: (FloorInspectionEntity, FloorObservationEntity) -> Unit = { _, _ -> },
    onDeleteObservation: (FloorInspectionEntity, FloorObservationEntity) -> Unit = { _, _ -> },
    onOpenCamera: ((FloorInspectionEntity) -> Unit)? = null,
    onDeleteFloor: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var previewPhotoUrl by remember { mutableStateOf<String?>(null) }
    val statusColor by animateColorAsState(
        when (floor.status) {
            InspectionStatus.OK.name -> TwitterGreen
            InspectionStatus.DEFECT.name -> TwitterRed
            else -> TwitterTextSubdued
        },
        label = "status_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_floor_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TwitterWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Navigation & Floor title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevFloor,
                    enabled = hasPrev,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Étage précédent",
                        tint = if (hasPrev) TwitterBlue else TwitterBorderDarker
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$elementName • Étage ${floor.floorIndex} / $totalFloors",
                        style = MaterialTheme.typography.labelMedium,
                        color = TwitterBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = floor.floorNumber,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TwitterTextPrimary
                        )
                        if (onDeleteFloor != null && totalFloors > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onDeleteFloor,
                                modifier = Modifier.size(28.dp).testTag("delete_floor_btn")
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Supprimer cet étage",
                                    tint = TwitterTextSubdued,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onNextFloor,
                    enabled = hasNext,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Étage suivant",
                        tint = if (hasNext) TwitterBlue else TwitterBorderDarker
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when (floor.status) {
                    InspectionStatus.OK.name -> TwitterGreenLight
                    InspectionStatus.DEFECT.name -> TwitterRedLight
                    else -> TwitterBackground
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when (floor.status) {
                        InspectionStatus.OK.name -> TwitterGreen.copy(alpha = 0.3f)
                        InspectionStatus.DEFECT.name -> TwitterRed.copy(alpha = 0.3f)
                        else -> TwitterBorder
                    }
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (floor.status) {
                            InspectionStatus.OK.name -> "CONFORME & VALIDÉ"
                            InspectionStatus.DEFECT.name -> "NON-CONFORME / DÉFAUT SIGNALÉ"
                            else -> "EN ATTENTE D'INSPECTION"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (floor.status) {
                            InspectionStatus.OK.name -> TwitterGreen
                            InspectionStatus.DEFECT.name -> TwitterRed
                            else -> TwitterTextSecondary
                        }
                    )
                }
            }

            // If DEFECT or has observations: Show list of observations, each with its photo and actions
            AnimatedVisibility(visible = floor.status == InspectionStatus.DEFECT.name || observations.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TwitterRedLight.copy(alpha = 0.4f))
                        .border(1.dp, TwitterRed.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    // Header of defect section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (observations.size > 1) "Anomalies constatées (${observations.size} observations)" else "Constat d'anomalie",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TwitterRed
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onOpenCamera != null) {
                                IconButton(
                                    onClick = { onOpenCamera(floor) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = "Ouvrir Caméra",
                                        tint = TwitterBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onAddObservation(floor) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Ajouter observation",
                                    tint = TwitterRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (observations.isNotEmpty()) {
                        // Display each observation with its image and controls
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            observations.forEachIndexed { obsIndex, obs ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = TwitterWhite,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                                    shadowElevation = 0.5.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = TwitterRedLight
                                                ) {
                                                    Text(
                                                        text = "Obs #${obsIndex + 1}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = TwitterRed,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }

                                                if (obs.isAiGenerated) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = TwitterBlueLight,
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                Icons.Default.AutoAwesome,
                                                                contentDescription = null,
                                                                tint = TwitterBlue,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = "Vision AI",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = TwitterBlue
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { onEditObservation(floor, obs) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Modifier",
                                                        tint = TwitterBlue,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onDeleteObservation(floor, obs) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DeleteOutline,
                                                        contentDescription = "Supprimer",
                                                        tint = TwitterRed,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = obs.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TwitterTextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )

                                        if (!obs.photoUrl.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, TwitterBorder, RoundedCornerShape(8.dp))
                                                    .clickable { previewPhotoUrl = obs.photoUrl }
                                            ) {
                                                val imageModel: Any = if (obs.photoUrl.startsWith("drawable://")) {
                                                    obs.photoUrl.removePrefix("drawable://").toIntOrNull() ?: obs.photoUrl
                                                } else {
                                                    File(obs.photoUrl)
                                                }

                                                AsyncImage(
                                                    model = ImageRequest.Builder(context)
                                                        .data(imageModel)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "Photo observation ${obsIndex + 1}",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )

                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.65f),
                                                    shape = RoundedCornerShape(bottomStart = 8.dp),
                                                    modifier = Modifier.align(Alignment.TopEnd)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Photo,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Photo #${obsIndex + 1} (Agrandir)", color = Color.White, fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Fallback to legacy single floor observation
                        Text(
                            text = if (floor.observation.isNotBlank()) floor.observation else "Aucune observation rédigée",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TwitterTextPrimary
                        )

                        if (!floor.photoUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, TwitterBorder, RoundedCornerShape(8.dp))
                            ) {
                                val imageModel: Any = if (floor.photoUrl.startsWith("drawable://")) {
                                    floor.photoUrl.removePrefix("drawable://").toIntOrNull() ?: floor.photoUrl
                                } else {
                                    File(floor.photoUrl)
                                }

                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                                        .data(imageModel)
                                                        .crossfade(true)
                                                        .build(),
                                    contentDescription = "Photo du défaut",
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Button to add another observation to this floor
                    OutlinedButton(
                        onClick = { onAddObservation(floor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TwitterRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TwitterRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (observations.isEmpty()) "+ Ajouter une observation détaillée" else "+ Ajouter une observation supplémentaire",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RAPID ACTION BUTTONS (MANDATORY WORKFLOW)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // RED BUTTON: Non-conforme / Problème (prompts user to snap/upload photo)
                Button(
                    onClick = { onOpenDefectDialog(floor) },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("defect_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TwitterRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PROBLÈME",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = "Saisie défaut",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 10.sp
                        )
                    }
                }

                // DIRECT OPEN CAMERA BUTTON (Twitter Blue Theme)
                if (onOpenCamera != null) {
                    Button(
                        onClick = { onOpenCamera(floor) },
                        modifier = Modifier
                            .weight(1.15f)
                            .height(54.dp)
                            .testTag("open_camera_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TwitterBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CAMÉRA",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                lineHeight = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Ouvrir & Photo",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 10.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // GREEN BUTTON: Conforme / Complet (1-click validates floor and jumps to next)
                Button(
                    onClick = { onValidateOk(floor) },
                    modifier = Modifier
                        .weight(1.25f)
                        .height(54.dp)
                        .testTag("conforme_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TwitterGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "CONFORME",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = "Valider & Suivant →",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 10.sp
                        )
                    }
                }
            }
        }
    }

    // Photo Preview Dialog
    if (previewPhotoUrl != null) {
        Dialog(onDismissRequest = { previewPhotoUrl = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TwitterWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preuve photo - ${floor.floorNumber}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = TwitterTextPrimary
                        )
                        IconButton(onClick = { previewPhotoUrl = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TwitterTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val imageModel: Any = if (previewPhotoUrl!!.startsWith("drawable://")) {
                        previewPhotoUrl!!.removePrefix("drawable://").toIntOrNull() ?: previewPhotoUrl!!
                    } else {
                        File(previewPhotoUrl!!)
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Photo agrandie",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { previewPhotoUrl = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
                    ) {
                        Text("Fermer", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
