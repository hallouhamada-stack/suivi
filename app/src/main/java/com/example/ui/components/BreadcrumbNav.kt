package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TwitterBlue
import com.example.ui.theme.TwitterBlueLight
import com.example.ui.theme.TwitterBorder
import com.example.ui.theme.TwitterTextPrimary
import com.example.ui.theme.TwitterTextSecondary
import com.example.ui.theme.TwitterWhite
import com.example.ui.viewmodel.NavStep

data class BreadcrumbItem(
    val step: NavStep,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector
)

@Composable
fun BreadcrumbNav(
    currentStep: NavStep,
    selectedSpaceName: String? = null,
    selectedFloorName: String? = null,
    onStepClick: (NavStep) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BreadcrumbItem(
            step = NavStep.HOME,
            title = "Accueil",
            icon = Icons.Default.Home
        ),
        BreadcrumbItem(
            step = NavStep.SPACE,
            title = "Espaces",
            subtitle = selectedSpaceName,
            icon = Icons.Default.Apartment
        ),
        BreadcrumbItem(
            step = NavStep.ETAGES,
            title = "Étages",
            subtitle = selectedFloorName?.takeIf { currentStep == NavStep.PROBLEME },
            icon = Icons.Default.Layers
        ),
        BreadcrumbItem(
            step = NavStep.PROBLEME,
            title = "Contrôle",
            subtitle = if (currentStep == NavStep.PROBLEME) selectedFloorName else null,
            icon = Icons.Default.WarningAmber
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("breadcrumb_nav_bar"),
        color = TwitterWhite,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            items.forEachIndexed { index, item ->
                val isActive = currentStep == item.step
                val isPast = currentStep.ordinal > item.step.ordinal
                val isClickable = isPast || isActive

                val bgAnimation by animateColorAsState(
                    targetValue = when {
                        isActive -> TwitterBlue
                        isPast -> TwitterBlueLight
                        else -> Color.Transparent
                    },
                    label = "crumb_bg"
                )

                val textColor = when {
                    isActive -> Color.White
                    isPast -> TwitterBlue
                    else -> TwitterTextSecondary
                }

                val iconTint = when {
                    isActive -> Color.White
                    isPast -> TwitterBlue
                    else -> TwitterTextSecondary
                }

                // Sleek Pill Breadcrumb chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(bgAnimation)
                        .then(
                            if (!isActive && !isPast) Modifier.border(1.dp, TwitterBorder, RoundedCornerShape(20.dp))
                            else Modifier
                        )
                        .clickable(enabled = isClickable) {
                            onStepClick(item.step)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("crumb_${item.step.name.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = iconTint,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!item.subtitle.isNullOrBlank() && (isActive || isPast)) {
                                "${item.title}: ${item.subtitle}"
                            } else {
                                item.title
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }

                if (index < items.size - 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Suivant",
                        tint = if (isPast) TwitterBlue else TwitterBorder,
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

