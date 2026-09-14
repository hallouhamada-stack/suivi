package com.example.ui.components

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.InspectionStatus
import java.io.File

import com.example.ui.theme.TwitterBackground
import com.example.ui.theme.TwitterBlue
import com.example.ui.theme.TwitterBlueLight
import com.example.ui.theme.TwitterBorder
import com.example.ui.theme.TwitterGreen
import com.example.ui.theme.TwitterGreenLight
import com.example.ui.theme.TwitterRed
import com.example.ui.theme.TwitterRedLight
import com.example.ui.theme.TwitterTextPrimary
import com.example.ui.theme.TwitterTextSecondary
import com.example.ui.theme.TwitterWhite

@Composable
fun InspectionSummaryTable(
    floors: List<FloorInspectionEntity>,
    observationsByFloor: Map<Long, List<com.example.data.model.FloorObservationEntity>> = emptyMap(),
    onFloorClick: (FloorInspectionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredFloors = remember(floors, selectedFilter) {
        when (selectedFilter) {
            "OK" -> floors.filter { it.status == InspectionStatus.OK.name }
            "DEFECT" -> floors.filter { it.status == InspectionStatus.DEFECT.name }
            "PENDING" -> floors.filter { it.status == InspectionStatus.PENDING.name }
            else -> floors
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("inspection_summary_table")
    ) {
        // Filter row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("Tous (${floors.size})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedFilter == "OK",
                onClick = { selectedFilter = "OK" },
                label = {
                    Text(
                        "Conformes (${floors.count { it.status == InspectionStatus.OK.name }})",
                        color = TwitterGreen,
                        fontSize = 11.sp
                    )
                }
            )
            FilterChip(
                selected = selectedFilter == "DEFECT",
                onClick = { selectedFilter = "DEFECT" },
                label = {
                    Text(
                        "Défauts (${floors.count { it.status == InspectionStatus.DEFECT.name }})",
                        color = TwitterRed,
                        fontSize = 11.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Table Header
        Surface(
            color = TwitterBackground,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ÉTAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TwitterTextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(70.dp)
                )
                Text(
                    text = "STATUT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TwitterTextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(90.dp)
                )
                Text(
                    text = "OBSERVATION (IA)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TwitterTextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "PHOTO",
                    style = MaterialTheme.typography.labelSmall,
                    color = TwitterTextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(50.dp)
                )
            }
        }

        // Table Rows
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TwitterBorder, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
        ) {
            items(filteredFloors, key = { it.id }) { floor ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (floor.floorIndex % 2 == 0) TwitterBackground else TwitterWhite)
                        .clickable { onFloorClick(floor) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // [Floor]
                    Text(
                        text = floor.floorNumber,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TwitterTextPrimary,
                        modifier = Modifier.width(70.dp)
                    )

                    // [Status]
                    Box(modifier = Modifier.width(90.dp)) {
                        when (floor.status) {
                            InspectionStatus.OK.name -> {
                                Surface(
                                    color = TwitterGreenLight,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterGreen.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = TwitterGreen,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "OK",
                                            color = TwitterGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            InspectionStatus.DEFECT.name -> {
                                Surface(
                                    color = TwitterRedLight,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterRed.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            tint = TwitterRed,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "DÉFAUT",
                                            color = TwitterRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            else -> {
                                Surface(
                                    color = TwitterBackground,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.HourglassEmpty,
                                            contentDescription = null,
                                            tint = TwitterTextSecondary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "ATTENTE",
                                            color = TwitterTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // [Observation (AI Written or Multiple)]
                    val obsList = observationsByFloor[floor.id] ?: emptyList()
                    val hasAi = obsList.any { it.isAiGenerated } || floor.isAiGenerated
                    val obsText = when {
                        obsList.size > 1 -> "${obsList.size} observations jointes"
                        obsList.size == 1 -> obsList[0].description
                        floor.observation.isNotBlank() -> floor.observation
                        floor.status == InspectionStatus.OK.name -> "Conforme"
                        else -> "—"
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasAi) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "IA",
                                tint = TwitterBlue,
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(end = 2.dp)
                            )
                        }
                        Text(
                            text = obsText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (floor.status == InspectionStatus.DEFECT.name) TwitterRed else TwitterTextPrimary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    // [Photo Thumbnail]
                    val allPhotos = if (obsList.isNotEmpty()) {
                        obsList.mapNotNull { it.photoUrl }.filter { it.isNotBlank() }
                    } else if (!floor.photoUrl.isNullOrBlank()) {
                        listOf(floor.photoUrl)
                    } else {
                        emptyList()
                    }

                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TwitterBackground)
                            .border(1.dp, TwitterBorder, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (allPhotos.isNotEmpty()) {
                            val firstPhoto = allPhotos.first()
                            val imgModel: Any = if (firstPhoto.startsWith("drawable://")) {
                                firstPhoto.removePrefix("drawable://").toIntOrNull() ?: firstPhoto
                            } else {
                                File(firstPhoto)
                            }
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imgModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Vignette",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (allPhotos.size > 1) {
                                Surface(
                                    color = TwitterBlue.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(topStart = 4.dp),
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                ) {
                                    Text(
                                        text = "+${allPhotos.size - 1}",
                                        color = TwitterWhite,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        } else {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = TwitterTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
