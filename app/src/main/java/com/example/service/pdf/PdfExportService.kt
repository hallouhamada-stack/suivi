package com.example.service.pdf

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.BuildingEntity
import com.example.data.model.ElementEntity
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.FloorObservationEntity
import com.example.data.model.InspectionStatus
import com.example.util.ImageCompressionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExportService {

    suspend fun generateInspectionPdf(
        context: Context,
        siteName: String,
        building: BuildingEntity,
        element: ElementEntity,
        floors: List<FloorInspectionEntity>,
        floorObservations: Map<Long, List<FloorObservationEntity>> = emptyMap(),
        inspectorName: String,
        contractorName: String,
        inspectorSignature: Bitmap? = null,
        contractorSignature: Bitmap? = null
    ): File = withContext(Dispatchers.IO) {
        val pdfDoc = PdfDocument()
        val pageWidth = 595 // A4 standard width (points)
        val pageHeight = 842 // A4 standard height (points)
        var pageNumber = 1

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.rgb(226, 232, 240)
        }

        val margin = 32f
        var currentY = margin

        // --- 1. HEADER BANNER ---
        paint.color = Color.rgb(29, 155, 240) // Twitter Blue
        canvas.drawRoundRect(RectF(margin, currentY, pageWidth - margin, currentY + 64f), 6f, 6f, paint)

        // Accent strip on the left
        paint.color = Color.rgb(15, 20, 25) // Twitter Dark text accent
        canvas.drawRoundRect(RectF(margin, currentY, margin + 6f, currentY + 64f), 4f, 4f, paint)

        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 15f
        canvas.drawText("RAPPORT DE CONTRÔLE QUALITÉ CHANTIER", margin + 18f, currentY + 26f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 9.5f
        textPaint.color = Color.rgb(230, 243, 255)
        canvas.drawText("Tableau récapitulatif systématique par niveau • Vision AI & Inspection", margin + 18f, currentY + 42f, textPaint)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
        val dateStr = dateFormat.format(Date())
        textPaint.textSize = 8.5f
        textPaint.color = Color.rgb(210, 235, 255)
        canvas.drawText("Édition officielle du : $dateStr", margin + 18f, currentY + 56f, textPaint)

        currentY += 74f

        // --- 2. TABLEAU 1 : INFORMATIONS DU PROJET ET INTERVENANTS ---
        drawSectionTitle(canvas, margin, currentY, "1. FICHE D'IDENTIFICATION DU CHANTIER")
        currentY += 16f

        val table1Width = pageWidth - margin * 2
        val colLabelW = 90f
        val colValW = (table1Width - colLabelW * 2) / 2f
        val t1RowH = 19f

        val t1Data = listOf(
            Pair(Pair("Chantier / Site :", siteName), Pair("Date du Contrôle :", dateStr)),
            Pair(Pair("Bâtiment :", building.name), Pair("Espace Contrôlé :", element.name)),
            Pair(Pair("Contrôleur :", inspectorName), Pair("Entreprise :", contractorName))
        )

        for (row in t1Data) {
            val rTop = currentY
            val rBottom = currentY + t1RowH

            // Col 1 Label
            drawTableCell(canvas, margin, rTop, margin + colLabelW, rBottom, row.first.first, isHeader = true)
            // Col 1 Value
            drawTableCell(canvas, margin + colLabelW, rTop, margin + colLabelW + colValW, rBottom, row.first.second, isHeader = false)
            // Col 2 Label
            drawTableCell(canvas, margin + colLabelW + colValW, rTop, margin + colLabelW * 2 + colValW, rBottom, row.second.first, isHeader = true)
            // Col 2 Value
            drawTableCell(canvas, margin + colLabelW * 2 + colValW, rTop, pageWidth - margin, rBottom, row.second.second, isHeader = false)

            currentY += t1RowH
        }

        currentY += 12f

        // --- 3. TABLEAU 2 : SYNTHÈSE CHIFFRÉE DE CONFORMITÉ ---
        drawSectionTitle(canvas, margin, currentY, "2. SYNTHÈSE STATISTIQUE DE CONFORMITÉ")
        currentY += 16f

        val okCount = floors.count { it.status == InspectionStatus.OK.name }
        val defectCount = floors.count { it.status == InspectionStatus.DEFECT.name }
        val pendingCount = floors.count { it.status == InspectionStatus.PENDING.name }
        val totalCount = floors.size
        val complianceRate = if (totalCount > 0) (okCount * 100) / totalCount else 0

        val colKpiW = table1Width / 4f

        // Header Row of KPI Table
        val kpiHeaderH = 18f
        val kpiHeaders = listOf("TOTAL ÉTAGES", "CONFORMES (OK)", "RÉSERVES / DÉFAUTS", "TAUX CONFORMITÉ")
        for ((i, title) in kpiHeaders.withIndex()) {
            val left = margin + i * colKpiW
            val right = left + colKpiW
            drawTableKpiHeader(canvas, left, currentY, right, currentY + kpiHeaderH, title)
        }
        currentY += kpiHeaderH

        // Value Row of KPI Table
        val kpiValueH = 26f
        val kpiValues = listOf(
            Pair("$totalCount", Color.rgb(15, 23, 42)),
            Pair("✓ $okCount", Color.rgb(22, 163, 74)),
            Pair("✕ $defectCount", Color.rgb(220, 38, 38)),
            Pair("$complianceRate%", Color.rgb(37, 99, 235))
        )
        for ((i, v) in kpiValues.withIndex()) {
            val left = margin + i * colKpiW
            val right = left + colKpiW
            drawTableKpiValue(canvas, left, currentY, right, currentY + kpiValueH, v.first, v.second)
        }
        currentY += kpiValueH + 14f

        // --- 4. TABLEAU 3 : TABLEAU RÉCAPITULATIF DÉTAILLÉ PAR NIVEAU ---
        drawSectionTitle(canvas, margin, currentY, "3. TABLEAU DÉTAILLÉ DE CONTRÔLE PAR ÉTAGE")
        currentY += 16f

        // Column X coordinates for Table 3
        val x0 = margin
        val x1 = margin + 35f       // N° (35f)
        val x2 = margin + 110f      // Étage (75f)
        val x3 = margin + 215f      // Statut (105f)
        val x4 = pageWidth - margin - 80f // Observation (236f)
        val x5 = pageWidth - margin // Photo (80f)

        fun drawFloorTableHeader(c: Canvas, topY: Float): Float {
            val hH = 22f
            paint.color = Color.rgb(29, 155, 240) // Twitter Blue
            c.drawRect(x0, topY, x5, topY + hH, paint)

            // Outer border and vertical grid lines for header
            val hBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.rgb(29, 155, 240)
            }
            c.drawRect(x0, topY, x5, topY + hH, hBorder)
            c.drawLine(x1, topY, x1, topY + hH, hBorder)
            c.drawLine(x2, topY, x2, topY + hH, hBorder)
            c.drawLine(x3, topY, x3, topY + hH, hBorder)
            c.drawLine(x4, topY, x4, topY + hH, hBorder)

            textPaint.color = Color.WHITE
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 8.5f
            textPaint.textAlign = Paint.Align.CENTER
            c.drawText("N°", (x0 + x1) / 2f, topY + 15f, textPaint)
            c.drawText("ÉTAGE / NIVEAU", (x1 + x2) / 2f, topY + 15f, textPaint)
            c.drawText("STATUT QUALITÉ", (x2 + x3) / 2f, topY + 15f, textPaint)

            textPaint.textAlign = Paint.Align.LEFT
            c.drawText("CONSTAT & OBSERVATIONS DÉTAILLÉES", x3 + 8f, topY + 15f, textPaint)

            textPaint.textAlign = Paint.Align.CENTER
            c.drawText("PHOTO", (x4 + x5) / 2f, topY + 15f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            return topY + hH
        }

        currentY = drawFloorTableHeader(canvas, currentY)

        val gridBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.rgb(203, 213, 225)
        }

        for ((index, floor) in floors.withIndex()) {
            val obsList = floorObservations[floor.id] ?: emptyList()
            val floorRowH = when {
                obsList.size >= 3 -> 56f
                obsList.size == 2 -> 44f
                else -> 38f
            }

            // Check if we need to paginate (leave room for signature block if last page)
            val isLastFew = (index >= floors.size - 2)
            val neededSpace = if (isLastFew) 180f else 60f

            if (currentY + floorRowH > pageHeight - neededSpace) {
                // Page footer
                drawPageFooter(canvas, margin, pageWidth, pageHeight, pageNumber)
                pdfDoc.finishPage(page)

                pageNumber++
                val nextInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDoc.startPage(nextInfo)
                canvas = page.canvas
                currentY = margin

                // Header on new page
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawRect(margin, currentY, pageWidth - margin, currentY + 24f, paint)
                textPaint.color = Color.WHITE
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.textSize = 10f
                textPaint.textAlign = Paint.Align.LEFT
                canvas.drawText("TABLEAU DES CONTRÔLES (SUITE) - ${element.name}", margin + 10f, currentY + 16f, textPaint)
                currentY += 30f

                currentY = drawFloorTableHeader(canvas, currentY)
            }

            val rTop = currentY
            val rBottom = currentY + floorRowH

            // Alternating row background
            paint.color = if (index % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
            canvas.drawRect(x0, rTop, x5, rBottom, paint)

            // Full Table Grid: outer rectangle and vertical column separators
            canvas.drawRect(x0, rTop, x5, rBottom, gridBorderPaint)
            canvas.drawLine(x1, rTop, x1, rBottom, gridBorderPaint)
            canvas.drawLine(x2, rTop, x2, rBottom, gridBorderPaint)
            canvas.drawLine(x3, rTop, x3, rBottom, gridBorderPaint)
            canvas.drawLine(x4, rTop, x4, rBottom, gridBorderPaint)

            // Col 1: Index / N°
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 9f
            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(String.format(Locale.US, "%02d", index + 1), (x0 + x1) / 2f, rTop + (floorRowH / 2f) + 4f, textPaint)

            // Col 2: Floor Name
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = Color.rgb(15, 23, 42)
            textPaint.textSize = 9f
            canvas.drawText(floor.floorNumber, (x1 + x2) / 2f, rTop + (floorRowH / 2f) + 4f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            // Col 3: Status Badge
            val badgeH = 20f
            val badgeTop = rTop + (floorRowH - badgeH) / 2f
            val badgeBox = RectF(x2 + 8f, badgeTop, x3 - 8f, badgeTop + badgeH)
            when (floor.status) {
                InspectionStatus.OK.name -> {
                    paint.color = Color.rgb(220, 252, 231) // Green fill
                    canvas.drawRoundRect(badgeBox, 4f, 4f, paint)
                    val bBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                        color = Color.rgb(134, 239, 172)
                    }
                    canvas.drawRoundRect(badgeBox, 4f, 4f, bBorder)
                    textPaint.color = Color.rgb(21, 128, 61)
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textPaint.textSize = 8f
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText("✓ CONFORME", badgeBox.centerX(), badgeBox.centerY() + 3f, textPaint)
                }
                InspectionStatus.DEFECT.name -> {
                    paint.color = Color.rgb(254, 226, 226) // Red fill
                    canvas.drawRoundRect(badgeBox, 4f, 4f, paint)
                    val bBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                        color = Color.rgb(252, 165, 165)
                    }
                    canvas.drawRoundRect(badgeBox, 4f, 4f, bBorder)
                    textPaint.color = Color.rgb(185, 28, 28)
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textPaint.textSize = 8f
                    textPaint.textAlign = Paint.Align.CENTER
                    val badgeLabel = if (obsList.size > 1) "✕ ${obsList.size} DÉFAUTS" else "✕ DÉFAUT"
                    canvas.drawText(badgeLabel, badgeBox.centerX(), badgeBox.centerY() + 3f, textPaint)
                }
                else -> {
                    paint.color = Color.rgb(241, 245, 249) // Gray fill
                    canvas.drawRoundRect(badgeBox, 4f, 4f, paint)
                    val bBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                        color = Color.rgb(226, 232, 240)
                    }
                    canvas.drawRoundRect(badgeBox, 4f, 4f, bBorder)
                    textPaint.color = Color.rgb(100, 116, 139)
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    textPaint.textSize = 7.5f
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText("EN ATTENTE", badgeBox.centerX(), badgeBox.centerY() + 3f, textPaint)
                }
            }
            textPaint.textAlign = Paint.Align.LEFT

            // Col 4: Observation Details with multiple observations support
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 7.5f
            val obsColWidth = (x4 - x3) - 16f

            if (obsList.isNotEmpty()) {
                if (obsList.size == 1) {
                    val singleObs = obsList[0]
                    textPaint.color = Color.rgb(185, 28, 28)
                    val prefix = if (singleObs.isAiGenerated) "[Vision AI] " else ""
                    drawWrappedTableText(canvas, "$prefix${singleObs.description}", x3 + 8f, rTop + 14f, obsColWidth, textPaint, maxLines = 2, lineHeight = 10f)
                } else {
                    var obsY = rTop + 11f
                    for ((oIdx, obs) in obsList.take(3).withIndex()) {
                        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textPaint.textSize = 7.5f
                        textPaint.color = Color.rgb(185, 28, 28)
                        val bullet = "• Obs ${oIdx + 1}: "
                        canvas.drawText(bullet, x3 + 8f, obsY, textPaint)

                        val bulletW = textPaint.measureText(bullet)
                        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        textPaint.color = Color.rgb(30, 41, 59)
                        val prefix = if (obs.isAiGenerated) "[AI] " else ""
                        val maxChars = 34
                        val descShort = if (obs.description.length > maxChars) obs.description.take(maxChars - 3) + "..." else obs.description
                        canvas.drawText("$prefix$descShort", x3 + 8f + bulletW, obsY, textPaint)
                        obsY += 13f
                    }
                    if (obsList.size > 3) {
                        textPaint.textSize = 7f
                        textPaint.color = Color.rgb(100, 116, 139)
                        canvas.drawText("+ ${obsList.size - 3} autre(s) observation(s)", x3 + 8f, obsY, textPaint)
                    }
                }
            } else {
                textPaint.color = if (floor.status == InspectionStatus.DEFECT.name) Color.rgb(185, 28, 28) else Color.rgb(51, 65, 85)
                val obsText = if (floor.observation.isNotBlank()) {
                    val prefix = if (floor.isAiGenerated) "[Vision AI] " else ""
                    "$prefix${floor.observation}"
                } else if (floor.status == InspectionStatus.OK.name) {
                    "Vérification effectuée - Aucune anomalie constatée"
                } else {
                    "En attente de vérification sur site"
                }
                drawWrappedTableText(canvas, obsText, x3 + 8f, rTop + (floorRowH / 2f) - 2f, obsColWidth, textPaint, maxLines = 2, lineHeight = 10f)
            }

            // Col 5: Photo proof thumbnail (compressed decoding)
            val photoBox = RectF(x4 + 6f, rTop + 4f, x5 - 6f, rBottom - 4f)
            val obsPhotos = obsList.mapNotNull { it.photoUrl }.filter { it.isNotBlank() }
            if (obsPhotos.isNotEmpty()) {
                drawCompressedPhotosThumbnails(context, canvas, obsPhotos, photoBox)
            } else if (!floor.photoUrl.isNullOrBlank()) {
                drawSingleCompressedPhoto(context, canvas, floor.photoUrl, photoBox)
            } else {
                drawEmptyPhotoBox(canvas, photoBox)
            }

            currentY += floorRowH
        }

        currentY += 16f

        // --- 5. TABLEAU 4 : PROCÈS-VERBAL ET SIGNATURES CONJOINTES ---
        if (currentY + 120f > pageHeight - margin) {
            drawPageFooter(canvas, margin, pageWidth, pageHeight, pageNumber)
            pdfDoc.finishPage(page)

            pageNumber++
            val sigInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDoc.startPage(sigInfo)
            canvas = page.canvas
            currentY = margin
        }

        drawSectionTitle(canvas, margin, currentY, "4. PROCÈS-VERBAL ET SIGNATURES CONTRADICTOIRES")
        currentY += 16f

        val sigTableW = pageWidth - margin * 2
        val colSigW = sigTableW / 2f
        val sigHeaderH = 18f
        val sigContentH = 88f

        // Signature Table Header
        drawTableCell(canvas, margin, currentY, margin + colSigW, currentY + sigHeaderH, "VISA DU CONTRÔLEUR / INSPECTEUR QUALITÉ", isHeader = true)
        drawTableCell(canvas, margin + colSigW, currentY, pageWidth - margin, currentY + sigHeaderH, "VISA DE L'ENTREPRISE / TITULAIRE DES TRAVAUX", isHeader = true)
        currentY += sigHeaderH

        // Signature Left Cell (Inspector)
        drawSignatureTableCell(
            canvas = canvas,
            left = margin,
            top = currentY,
            right = margin + colSigW,
            bottom = currentY + sigContentH,
            name = inspectorName,
            legalNotice = "Certifie la conformité des vérifications sur l'espace.",
            signature = inspectorSignature
        )

        // Signature Right Cell (Contractor)
        drawSignatureTableCell(
            canvas = canvas,
            left = margin + colSigW,
            top = currentY,
            right = pageWidth - margin,
            bottom = currentY + sigContentH,
            name = contractorName,
            legalNotice = "Prise de connaissance et engagement de levée des réserves.",
            signature = contractorSignature
        )

        currentY += sigContentH + 10f

        // Page footer on the last page
        drawPageFooter(canvas, margin, pageWidth, pageHeight, pageNumber)

        pdfDoc.finishPage(page)

        // Save PDF with clean naming: Rapport_[SpaceName]_[Date].pdf
        val cleanSpaceName = element.name.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "Espace" }
        val dateStrForFile = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).format(Date())
        val outputFileName = "Rapport_${cleanSpaceName}_${dateStrForFile}.pdf"

        val outputDir = File(context.filesDir, "inspection_reports")
        if (!outputDir.exists()) outputDir.mkdirs()
        val pdfFile = File(outputDir, outputFileName)

        FileOutputStream(pdfFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        pdfFile
    }

    private fun drawSectionTitle(canvas: Canvas, x: Float, y: Float, title: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 20, 25)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 9.5f
        }
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(29, 155, 240) // Twitter Blue marker
        }
        canvas.drawRect(x, y + 2f, x + 3f, y + 12f, barPaint)
        canvas.drawText(title, x + 8f, y + 10f, paint)
    }

    private fun drawTableCell(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        text: String,
        isHeader: Boolean
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isHeader) Color.rgb(247, 249, 250) else Color.WHITE
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(239, 243, 244)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isHeader) Color.rgb(83, 100, 113) else Color.rgb(15, 20, 25)
            typeface = Typeface.create(Typeface.DEFAULT, if (isHeader) Typeface.BOLD else Typeface.NORMAL)
            textSize = 8f
        }

        canvas.drawRect(left, top, right, bottom, bgPaint)
        canvas.drawRect(left, top, right, bottom, borderPaint)
        canvas.drawText(text, left + 6f, top + (bottom - top) / 2f + 3f, textPaint)
    }

    private fun drawTableKpiHeader(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, text: String) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(29, 155, 240) // Twitter Blue
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(29, 155, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawRect(left, top, right, bottom, bgPaint)
        canvas.drawRect(left, top, right, bottom, borderPaint)
        canvas.drawText(text, (left + right) / 2f, top + 12f, textPaint)
    }

    private fun drawTableKpiValue(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, text: String, textColor: Int) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawRect(left, top, right, bottom, bgPaint)
        canvas.drawRect(left, top, right, bottom, borderPaint)
        canvas.drawText(text, (left + right) / 2f, top + 17f, textPaint)
    }

    private fun drawWrappedTableText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint,
        maxLines: Int = 2,
        lineHeight: Float = 11f
    ) {
        if (text.isBlank()) {
            canvas.drawText("—", x, y, paint)
            return
        }
        val words = text.split(" ")
        var currentLine = ""
        var lineCount = 0
        var currentY = y

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (lineCount < maxLines - 1) {
                    canvas.drawText(currentLine, x, currentY, paint)
                    currentY += lineHeight
                    lineCount++
                    currentLine = word
                } else {
                    var truncated = testLine
                    while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
                        truncated = truncated.dropLast(1)
                    }
                    canvas.drawText("$truncated...", x, currentY, paint)
                    currentLine = ""
                    break
                }
            }
        }
        if (currentLine.isNotEmpty() && lineCount < maxLines) {
            canvas.drawText(currentLine, x, currentY, paint)
        }
    }

    private fun drawSignatureTableCell(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        name: String,
        legalNotice: String,
        signature: Bitmap?
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(250, 250, 250)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawRect(left, top, right, bottom, bgPaint)
        canvas.drawRect(left, top, right, bottom, borderPaint)

        // Signatory Name
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 8.5f
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Signataire : $name", left + 8f, top + 14f, textPaint)

        // Legal mention
        textPaint.color = Color.rgb(100, 116, 139)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 7f
        canvas.drawText(legalNotice, left + 8f, top + 26f, textPaint)

        // Signature box inside cell
        val sigBox = RectF(left + 8f, top + 32f, right - 8f, bottom - 6f)
        val sigBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(sigBox, 3f, 3f, sigBg)
        canvas.drawRoundRect(sigBox, 3f, 3f, borderPaint)

        if (signature != null) {
            canvas.drawBitmap(signature, null, sigBox, null)
        } else {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(148, 163, 184)
                textSize = 7.5f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Signature électronique validée", sigBox.centerX(), sigBox.centerY() + 2.5f, p)
        }
    }

    private fun drawPageFooter(canvas: Canvas, margin: Float, pageWidth: Int, pageHeight: Int, pageNum: Int) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7.5f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.8f
        }
        val y = pageHeight - 18f
        canvas.drawLine(margin, y - 6f, pageWidth - margin, y - 6f, linePaint)
        canvas.drawText("SiteInspect AI • Rapport Officiel de Contrôle par Étage", margin, y + 4f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $pageNum", pageWidth - margin, y + 4f, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    /**
     * Downloads/copies the PDF to the device's public Downloads directory with the name and date.
     */
    fun downloadPdf(context: Context, sourcePdf: File, targetFileName: String): File {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, targetFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        sourcePdf.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                }
            }

            val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (publicDownloads.exists() || publicDownloads.mkdirs()) {
                val destFile = File(publicDownloads, targetFileName)
                sourcePdf.copyTo(destFile, overwrite = true)
                return destFile
            }
        } catch (_: Exception) {
            // Fallback handled below
        }

        val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        val destFile = File(fallbackDir, targetFileName)
        sourcePdf.copyTo(destFile, overwrite = true)
        return destFile
    }

    /**
     * Opens the PDF in the default external PDF viewer app.
     */
    fun openPdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(openIntent, "Ouvrir le rapport PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun drawEmptyPhotoBox(canvas: Canvas, box: RectF) {
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(box, 3f, 3f, bgPaint)
        canvas.drawRoundRect(box, 3f, 3f, borderPaint)

        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Sans photo", box.centerX(), box.centerY() + 2.5f, p)
    }

    private fun drawSingleCompressedPhoto(context: Context, canvas: Canvas, photoUrl: String?, box: RectF) {
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(box, 3f, 3f, bgPaint)
        canvas.drawRoundRect(box, 3f, 3f, borderPaint)

        if (photoUrl.isNullOrBlank()) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(148, 163, 184)
                textSize = 7f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Sans photo", box.centerX(), box.centerY() + 2.5f, p)
            return
        }

        try {
            // Use compressed decoding at 160x160 with RGB_565 for optimal PDF file size
            val bitmap = ImageCompressionUtils.decodeThumbnail(photoUrl, 160, 160)
                ?: decodeFallbackBitmap(context, photoUrl)

            if (bitmap != null) {
                canvas.drawBitmap(bitmap, null, box, null)
            } else {
                val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(148, 163, 184)
                    textSize = 7f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Photo", box.centerX(), box.centerY() + 2.5f, p)
            }
        } catch (_: Exception) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(148, 163, 184)
                textSize = 7f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Photo", box.centerX(), box.centerY() + 2.5f, p)
        }
    }

    private fun drawCompressedPhotosThumbnails(context: Context, canvas: Canvas, photos: List<String>, box: RectF) {
        if (photos.isEmpty()) {
            drawEmptyPhotoBox(canvas, box)
            return
        }
        if (photos.size == 1) {
            drawSingleCompressedPhoto(context, canvas, photos[0], box)
            return
        }

        // Multiple photos: draw 2 side-by-side thumbnails with badge if more
        val gap = 2f
        val halfW = (box.width() - gap) / 2f
        val box1 = RectF(box.left, box.top, box.left + halfW, box.bottom)
        val box2 = RectF(box.left + halfW + gap, box.top, box.right, box.bottom)

        drawSingleCompressedPhoto(context, canvas, photos[0], box1)
        drawSingleCompressedPhoto(context, canvas, photos[1], box2)

        if (photos.size > 2) {
            val overlay = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(170, 15, 23, 42)
            }
            canvas.drawRoundRect(box2, 3f, 3f, overlay)

            val badgeText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 7f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("+${photos.size - 1}", box2.centerX(), box2.centerY() + 2.5f, badgeText)
        }
    }

    private fun decodeFallbackBitmap(context: Context, photoUrl: String): Bitmap? {
        return try {
            if (photoUrl.startsWith("drawable://")) {
                val resId = photoUrl.removePrefix("drawable://").toIntOrNull()
                if (resId != null) BitmapFactory.decodeResource(context.resources, resId) else null
            } else {
                val file = File(photoUrl)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Rapport de Contrôle Chantier - ${pdfFile.name}")
            putExtra(Intent.EXTRA_TEXT, "Veuillez trouver ci-joint le rapport de contrôle par étage validé conjointement.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Partager le rapport d'inspection PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
