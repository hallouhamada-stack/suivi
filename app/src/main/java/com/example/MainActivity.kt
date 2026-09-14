package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.FloorObservationEntity
import com.example.ui.screens.EtagesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProblemScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpaceScreen
import com.example.ui.screens.SummaryReportScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InspectionViewModel
import com.example.ui.viewmodel.NavStep

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: InspectionViewModel = viewModel()
                    SiteInspectionApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SiteInspectionApp(viewModel: InspectionViewModel) {
    val navStep by viewModel.navStep.collectAsStateWithLifecycle()
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isSummaryOpen by remember { mutableStateOf(false) }

    val building by viewModel.selectedBuilding.collectAsStateWithLifecycle()
    val element by viewModel.selectedElement.collectAsStateWithLifecycle()
    val elements by viewModel.elements.collectAsStateWithLifecycle()
    val floors by viewModel.floors.collectAsStateWithLifecycle()
    val elementObservations by viewModel.elementObservations.collectAsStateWithLifecycle()
    val observationsByFloor: Map<Long, List<FloorObservationEntity>> = remember(elementObservations) {
        elementObservations.groupBy { it.floorId }
    }

    // Hierarchical back handling: Problème -> Étages -> Space -> Home
    BackHandler(enabled = isSettingsOpen || isSummaryOpen || navStep != NavStep.HOME) {
        when {
            isSettingsOpen -> isSettingsOpen = false
            isSummaryOpen -> isSummaryOpen = false
            navStep == NavStep.PROBLEME -> viewModel.navigateTo(NavStep.ETAGES)
            navStep == NavStep.ETAGES -> viewModel.navigateTo(NavStep.SPACE)
            navStep == NavStep.SPACE -> viewModel.navigateTo(NavStep.HOME)
        }
    }

    when {
        isSettingsOpen -> {
            SettingsScreen(
                prefs = viewModel.settingsPrefs,
                onBack = { isSettingsOpen = false }
            )
        }
        isSummaryOpen -> {
            val activeBuilding = building
            val activeElement = element ?: elements.firstOrNull()
            if (activeBuilding != null && activeElement != null) {
                if (element == null) {
                    viewModel.selectElement(activeElement)
                }
                SummaryReportScreen(
                    siteName = viewModel.settingsPrefs.siteName,
                    building = activeBuilding,
                    element = activeElement,
                    floors = floors,
                    floorObservations = observationsByFloor,
                    inspectorName = viewModel.settingsPrefs.inspectorName,
                    contractorName = viewModel.settingsPrefs.contractorName,
                    pdfService = viewModel.pdfService,
                    onFloorClick = { floor ->
                        viewModel.selectFloorAndInspect(floor)
                        isSummaryOpen = false
                    },
                    onBack = { isSummaryOpen = false }
                )
            } else {
                isSummaryOpen = false
            }
        }
        else -> {
            // Home > Space > Étages > Problème hierarchy
            when (navStep) {
                NavStep.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { isSettingsOpen = true },
                        onNavigateToSummary = { isSummaryOpen = true }
                    )
                }
                NavStep.SPACE -> {
                    SpaceScreen(
                        viewModel = viewModel,
                        onNavigateToSummary = { isSummaryOpen = true }
                    )
                }
                NavStep.ETAGES -> {
                    EtagesScreen(
                        viewModel = viewModel,
                        onNavigateToSummary = { isSummaryOpen = true }
                    )
                }
                NavStep.PROBLEME -> {
                    ProblemScreen(
                        viewModel = viewModel,
                        onNavigateToSummary = { isSummaryOpen = true }
                    )
                }
            }
        }
    }
}

