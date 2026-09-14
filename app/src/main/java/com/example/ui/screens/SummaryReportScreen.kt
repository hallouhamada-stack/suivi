package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuildingEntity
import com.example.data.model.ElementEntity
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.FloorObservationEntity
import com.example.data.model.InspectionStatus
import com.example.service.pdf.PdfExportService
import com.example.ui.components.InspectionSummaryTable
import com.example.ui.components.SignaturePad
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryReportScreen(
    siteName: String,
    building: BuildingEntity,
    element: ElementEntity,
    floors: List<FloorInspectionEntity>,
    floorObservations: Map<Long, List<FloorObservationEntity>> = emptyMap(),
    inspectorName: String,
    contractorName: String,
    pdfService: PdfExportService,
    onFloorClick: (FloorInspectionEntity) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var inspectorSig by remember { mutableStateOf<Bitmap?>(null) }
    var contractorSig by remember { mutableStateOf<Bitmap?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfStatusMessage by remember { mutableStateOf<String?>(null) }

    val okCount = floors.count { it.status == InspectionStatus.OK.name }
    val defectCount = floors.count { it.status == InspectionStatus.DEFECT.name }
    val totalCount = floors.size
    val complianceRate = if (totalCount > 0) (okCount * 100) / totalCount else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Synthèse & Rapport PDF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TwitterTextPrimary
                        )
                        Text(
                            "${building.name} • ${element.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TwitterTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = TwitterTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TwitterWhite,
                    titleContentColor = TwitterTextPrimary,
                    navigationIconContentColor = TwitterTextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(TwitterBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // KPI Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "Total",
                    value = "$totalCount",
                    sub = "Étages",
                    color = TwitterBlue,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Conformes",
                    value = "$okCount",
                    sub = "Validés",
                    color = TwitterGreen,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Défauts",
                    value = "$defectCount",
                    sub = "Réserves",
                    color = TwitterRed,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Taux",
                    value = "$complianceRate%",
                    sub = "Conformité",
                    color = TwitterBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instant Inspection Summary Table
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TwitterWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Tableau récapitulatif par niveau",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TwitterTextPrimary
                    )
                    Text(
                        text = "Touchez une ligne pour ouvrir le détail de l'étage",
                        style = MaterialTheme.typography.bodySmall,
                        color = TwitterTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InspectionSummaryTable(
                        floors = floors,
                        observationsByFloor = floorObservations,
                        onFloorClick = onFloorClick,
                        modifier = Modifier.height(280.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Joint Signatures Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TwitterWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Signatures conjointes contradictoires",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TwitterTextPrimary
                    )
                    Text(
                        text = "Les signatures électroniques seront imprimées sur le PDF exporté",
                        style = MaterialTheme.typography.bodySmall,
                        color = TwitterTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Inspector Signature Pad
                    SignaturePad(
                        title = "1. Le Contrôleur / Inspecteur",
                        subtitle = inspectorName,
                        onSignatureCaptured = { inspectorSig = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contractor Signature Pad
                    SignaturePad(
                        title = "2. L'Entreprise / Le Titulaire",
                        subtitle = contractorName,
                        onSignatureCaptured = { contractorSig = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val cleanSpaceName = remember(element.name) {
                element.name.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "Espace" }
            }
            val todayDateStr = remember {
                SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).format(Date())
            }
            val targetPdfFileName = remember(cleanSpaceName, todayDateStr) {
                "Rapport_${cleanSpaceName}_${todayDateStr}.pdf"
            }

            // Download to device Button
            Button(
                onClick = {
                    isGeneratingPdf = true
                    pdfStatusMessage = "Génération du PDF compressé avec photos..."
                    scope.launch {
                        try {
                            val pdf = pdfService.generateInspectionPdf(
                                context = context,
                                siteName = siteName,
                                building = building,
                                element = element,
                                floors = floors,
                                floorObservations = floorObservations,
                                inspectorName = inspectorName,
                                contractorName = contractorName,
                                inspectorSignature = inspectorSig,
                                contractorSignature = contractorSig
                            )
                            generatedPdfFile = pdf
                            val savedFile = pdfService.downloadPdf(context, pdf, targetPdfFileName)
                            isGeneratingPdf = false
                            pdfStatusMessage = "✓ Téléchargé dans Téléchargements : $targetPdfFileName"
                        } catch (e: Exception) {
                            isGeneratingPdf = false
                            pdfStatusMessage = "Erreur téléchargement: ${e.localizedMessage}"
                        }
                    }
                },
                enabled = !isGeneratingPdf,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("download_pdf_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue),
                shape = RoundedCornerShape(25.dp)
            ) {
                if (isGeneratingPdf) {
                    CircularProgressIndicator(
                        color = TwitterWhite,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Génération en cours...", fontWeight = FontWeight.Bold, color = TwitterWhite)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp), tint = TwitterWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Télécharger le Rapport PDF", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TwitterWhite)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary row: Partager & Ouvrir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isGeneratingPdf = true
                        pdfStatusMessage = "Préparation du partage..."
                        scope.launch {
                            try {
                                val pdf = generatedPdfFile ?: pdfService.generateInspectionPdf(
                                    context = context,
                                    siteName = siteName,
                                    building = building,
                                    element = element,
                                    floors = floors,
                                    floorObservations = floorObservations,
                                    inspectorName = inspectorName,
                                    contractorName = contractorName,
                                    inspectorSignature = inspectorSig,
                                    contractorSignature = contractorSig
                                )
                                generatedPdfFile = pdf
                                isGeneratingPdf = false
                                pdfStatusMessage = "✓ Rapport prêt au partage"
                                pdfService.sharePdf(context, pdf)
                            } catch (e: Exception) {
                                isGeneratingPdf = false
                                pdfStatusMessage = "Erreur partage: ${e.localizedMessage}"
                            }
                        }
                    },
                    enabled = !isGeneratingPdf,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("share_pdf_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TwitterBlue),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = TwitterBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Partager", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TwitterBlue)
                }

                OutlinedButton(
                    onClick = {
                        val fileToOpen = generatedPdfFile
                        if (fileToOpen != null) {
                            pdfService.openPdf(context, fileToOpen)
                        } else {
                            isGeneratingPdf = true
                            scope.launch {
                                try {
                                    val pdf = pdfService.generateInspectionPdf(
                                        context = context,
                                        siteName = siteName,
                                        building = building,
                                        element = element,
                                        floors = floors,
                                        floorObservations = floorObservations,
                                        inspectorName = inspectorName,
                                        contractorName = contractorName,
                                        inspectorSignature = inspectorSig,
                                        contractorSignature = contractorSig
                                    )
                                    generatedPdfFile = pdf
                                    isGeneratingPdf = false
                                    pdfService.openPdf(context, pdf)
                                } catch (e: Exception) {
                                    isGeneratingPdf = false
                                    pdfStatusMessage = "Erreur ouverture: ${e.localizedMessage}"
                                }
                            }
                        }
                    },
                    enabled = !isGeneratingPdf,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("open_pdf_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TwitterBlue),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp), tint = TwitterBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ouvrir", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TwitterBlue)
                }
            }

            if (pdfStatusMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = if (generatedPdfFile != null) TwitterGreenLight else TwitterRedLight,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (generatedPdfFile != null) TwitterGreen.copy(alpha = 0.3f) else TwitterRed.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pdfStatusMessage ?: "",
                            color = if (generatedPdfFile != null) TwitterGreen else TwitterRed,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = TwitterWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
        tonalElevation = 0.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TwitterTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = sub,
                fontSize = 9.sp,
                color = TwitterTextSecondary
            )
        }
    }
}
