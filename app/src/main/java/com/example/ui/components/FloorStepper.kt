package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.InspectionStatus

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
fun FloorStepper(
    floors: List<FloorInspectionEntity>,
    activeFloorIndex: Int,
    onFloorSelected: (FloorInspectionEntity) -> Unit,
    onAddFloorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = floors.size
    val inspectedCount = floors.count { it.status != InspectionStatus.PENDING.name }
    val progress = if (total > 0) inspectedCount.toFloat() / total.toFloat() else 0f
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TwitterWhite,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progression par étage",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TwitterTextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$inspectedCount / $total contrôlés (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TwitterBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TwitterBlueLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBlue),
                        modifier = Modifier
                            .clickable { onAddFloorClick() }
                            .testTag("add_floor_header_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Ajouter",
                                tint = TwitterBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "+ Étage",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TwitterBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = TwitterBlue,
                trackColor = TwitterBorder
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Floor Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                floors.forEach { floor ->
                    val isSelected = floor.floorIndex == activeFloorIndex
                    val (bgColor, textColor, borderColor) = when (floor.status) {
                        InspectionStatus.OK.name -> Triple(
                            TwitterGreenLight,
                            TwitterGreen,
                            TwitterGreen.copy(alpha = 0.3f)
                        )
                        InspectionStatus.DEFECT.name -> Triple(
                            TwitterRedLight,
                            TwitterRed,
                            TwitterRed.copy(alpha = 0.3f)
                        )
                        else -> Triple(
                            TwitterBackground,
                            TwitterTextSecondary,
                            TwitterBorder
                        )
                    }

                    Box(
                        modifier = Modifier
                            .testTag("floor_chip_${floor.floorIndex}")
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) TwitterBlueLight else bgColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) TwitterBlue else borderColor,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onFloorSelected(floor) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Status icon indicator
                            when (floor.status) {
                                InspectionStatus.OK.name -> {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(TwitterGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                InspectionStatus.DEFECT.name -> {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(TwitterRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }

                            Text(
                                text = "R+${floor.floorIndex}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TwitterBlue else textColor
                            )
                        }
                    }
                }

                // Dedicated "+ Ajouter" button chip
                Box(
                    modifier = Modifier
                        .testTag("add_floor_chip_btn")
                        .clip(RoundedCornerShape(20.dp))
                        .background(TwitterBlueLight)
                        .border(
                            width = 1.dp,
                            color = TwitterBlue,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onAddFloorClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Ajouter un étage",
                            tint = TwitterBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ Étage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TwitterBlue
                        )
                    }
                }
            }
        }
    }
}
