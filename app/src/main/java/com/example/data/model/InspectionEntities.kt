package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val contractorName: String = "Entreprise Générale BTP",
    val inspectorName: String = "Inspecteur Travaux",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "elements",
    foreignKeys = [
        ForeignKey(
            entity = BuildingEntity::class,
            parentColumns = ["id"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("buildingId")]
)
data class ElementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildingId: Long,
    val name: String, // e.g., "Cuisine T5", "Salle de bain", "Séjour"
    val description: String = "Contrôle systématique par étage",
    val totalFloors: Int = 1
)

enum class InspectionStatus {
    PENDING,
    OK,      // Conforme / Complet (Green)
    DEFECT   // Non-conforme / Problème (Red)
}

@Entity(
    tableName = "floor_inspections",
    foreignKeys = [
        ForeignKey(
            entity = ElementEntity::class,
            parentColumns = ["id"],
            childColumns = ["elementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("elementId")]
)
data class FloorInspectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val elementId: Long,
    val floorNumber: String, // e.g. "Étage 01", "Étage 02", ... "Étage 14"
    val floorIndex: Int,     // 1 to 14
    val status: String = InspectionStatus.PENDING.name,
    val photoUrl: String? = null, // Path to photo or drawable identifier
    val observation: String = "", // Auto-filled by AI, editable by user
    val isAiGenerated: Boolean = false,
    val inspectedAt: Long? = null
)

@Entity(
    tableName = "floor_observations",
    foreignKeys = [
        ForeignKey(
            entity = FloorInspectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["floorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("floorId")]
)
data class FloorObservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val floorId: Long,
    val description: String,
    val photoUrl: String? = null,
    val isAiGenerated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

