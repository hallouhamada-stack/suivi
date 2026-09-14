package com.example.data.repository

import com.example.data.dao.InspectionDao
import com.example.data.model.BuildingEntity
import com.example.data.model.ElementEntity
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.FloorObservationEntity
import com.example.data.model.InspectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class InspectionRepository(private val dao: InspectionDao) {

    val allBuildings: Flow<List<BuildingEntity>> = dao.getAllBuildings()

    suspend fun addBuilding(
        name: String,
        code: String,
        contractorName: String = "BTP Construction Pro",
        inspectorName: String = "Inspecteur Travaux"
    ): BuildingEntity {
        val building = BuildingEntity(
            name = name,
            code = code,
            contractorName = contractorName,
            inspectorName = inspectorName
        )
        val buildingId = dao.insertBuilding(building)
        val elementId = dao.insertElement(
            ElementEntity(
                buildingId = buildingId,
                name = "Espace Principal",
                description = "Contrôle par étage",
                totalFloors = 1
            )
        )
        dao.insertFloor(
            FloorInspectionEntity(
                elementId = elementId,
                floorNumber = "Étage 01",
                floorIndex = 1,
                status = InspectionStatus.PENDING.name,
                photoUrl = null,
                observation = "",
                isAiGenerated = false
            )
        )
        return building.copy(id = buildingId)
    }

    suspend fun deleteBuilding(buildingId: Long) {
        dao.deleteBuildingById(buildingId)
    }

    fun getElementsForBuilding(buildingId: Long): Flow<List<ElementEntity>> =
        dao.getElementsForBuilding(buildingId)

    fun getFloorsForElement(elementId: Long): Flow<List<FloorInspectionEntity>> =
        dao.getFloorsForElement(elementId)

    fun getOkCount(elementId: Long): Flow<Int> = dao.getOkCount(elementId)

    fun getDefectCount(elementId: Long): Flow<Int> = dao.getDefectCount(elementId)

    suspend fun addElement(buildingId: Long, name: String, description: String = "Contrôle par étage", floorsCount: Int = 1): ElementEntity {
        val element = ElementEntity(
            buildingId = buildingId,
            name = name,
            description = description,
            totalFloors = floorsCount
        )
        val elementId = dao.insertElement(element)
        val floors = (1..floorsCount).map { i ->
            FloorInspectionEntity(
                elementId = elementId,
                floorNumber = "Étage %02d".format(i),
                floorIndex = i,
                status = InspectionStatus.PENDING.name,
                photoUrl = null,
                observation = "",
                isAiGenerated = false
            )
        }
        dao.insertFloors(floors)
        return element.copy(id = elementId)
    }

    suspend fun deleteElement(elementId: Long) {
        dao.deleteElementById(elementId)
    }

    suspend fun updateFloor(floor: FloorInspectionEntity) {
        dao.updateFloor(floor)
    }

    suspend fun addFloor(elementId: Long, floorNumber: String? = null): FloorInspectionEntity {
        val currentFloors = dao.getFloorsForElement(elementId).firstOrNull() ?: emptyList()
        val nextIndex = (currentFloors.maxOfOrNull { it.floorIndex } ?: 0) + 1
        val label = if (!floorNumber.isNullOrBlank()) floorNumber else "Étage %02d".format(nextIndex)
        val newFloor = FloorInspectionEntity(
            elementId = elementId,
            floorNumber = label,
            floorIndex = nextIndex,
            status = InspectionStatus.PENDING.name,
            photoUrl = null,
            observation = "",
            isAiGenerated = false
        )
        val id = dao.insertFloor(newFloor)
        return newFloor.copy(id = id)
    }

    suspend fun deleteFloor(floor: FloorInspectionEntity) {
        dao.deleteFloorById(floor.id)
    }

    suspend fun markFloorOk(floor: FloorInspectionEntity) {
        dao.updateFloor(
            floor.copy(
                status = InspectionStatus.OK.name,
                inspectedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun markFloorDefect(
        floor: FloorInspectionEntity,
        photoUrl: String?,
        observation: String,
        isAiGenerated: Boolean
    ) {
        if (observation.isNotBlank() || !photoUrl.isNullOrBlank()) {
            dao.insertObservation(
                FloorObservationEntity(
                    floorId = floor.id,
                    description = observation.ifBlank { "Anomalie constatée" },
                    photoUrl = photoUrl,
                    isAiGenerated = isAiGenerated
                )
            )
        }
        dao.updateFloor(
            floor.copy(
                status = InspectionStatus.DEFECT.name,
                photoUrl = photoUrl ?: floor.photoUrl,
                observation = observation.ifBlank { floor.observation },
                isAiGenerated = isAiGenerated,
                inspectedAt = System.currentTimeMillis()
            )
        )
    }

    fun getObservationsForFloor(floorId: Long): Flow<List<FloorObservationEntity>> =
        dao.getObservationsForFloor(floorId)

    suspend fun getObservationsForFloors(floorIds: List<Long>): List<FloorObservationEntity> =
        dao.getObservationsForFloors(floorIds)

    fun getObservationsForFloorsFlow(floorIds: List<Long>): Flow<List<FloorObservationEntity>> =
        dao.getObservationsForFloorsFlow(floorIds)

    suspend fun addFloorObservation(
        floor: FloorInspectionEntity,
        description: String,
        photoUrl: String?,
        isAiGenerated: Boolean
    ): FloorObservationEntity {
        val obs = FloorObservationEntity(
            floorId = floor.id,
            description = description.ifBlank { "Anomalie constatée" },
            photoUrl = photoUrl,
            isAiGenerated = isAiGenerated
        )
        val id = dao.insertObservation(obs)
        // Ensure floor is marked DEFECT
        dao.updateFloor(
            floor.copy(
                status = InspectionStatus.DEFECT.name,
                photoUrl = photoUrl ?: floor.photoUrl,
                observation = description.ifBlank { floor.observation },
                isAiGenerated = isAiGenerated,
                inspectedAt = System.currentTimeMillis()
            )
        )
        return obs.copy(id = id)
    }

    suspend fun updateObservation(
        floor: FloorInspectionEntity,
        observation: FloorObservationEntity
    ) {
        dao.updateObservation(observation)
        dao.updateFloor(
            floor.copy(
                status = InspectionStatus.DEFECT.name,
                photoUrl = observation.photoUrl ?: floor.photoUrl,
                observation = observation.description,
                isAiGenerated = observation.isAiGenerated,
                inspectedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteFloorObservation(floor: FloorInspectionEntity, observationId: Long) {
        dao.deleteObservationById(observationId)
        val remaining = dao.getObservationsForFloor(floor.id).firstOrNull() ?: emptyList()
        val remainingWithoutDeleted = remaining.filter { it.id != observationId }
        if (remainingWithoutDeleted.isEmpty()) {
            dao.updateFloor(
                floor.copy(
                    status = InspectionStatus.PENDING.name,
                    photoUrl = null,
                    observation = "",
                    isAiGenerated = false
                )
            )
        } else {
            val first = remainingWithoutDeleted.first()
            dao.updateFloor(
                floor.copy(
                    photoUrl = first.photoUrl,
                    observation = first.description,
                    isAiGenerated = first.isAiGenerated
                )
            )
        }
    }

    suspend fun resetElementFloors(elementId: Long, totalFloors: Int = 14) {
        dao.resetFloorsForElement(elementId)
        val freshFloors = (1..totalFloors).map { i ->
            FloorInspectionEntity(
                elementId = elementId,
                floorNumber = "Étage %02d".format(i),
                floorIndex = i,
                status = InspectionStatus.PENDING.name,
                photoUrl = null,
                observation = "",
                isAiGenerated = false
            )
        }
        dao.insertFloors(freshFloors)
    }

    suspend fun ensureDefaultDataSeeded() {
        val existingBuildings = dao.getAllBuildings().firstOrNull()
        if (existingBuildings.isNullOrEmpty()) {
            val buildingId = dao.insertBuilding(
                BuildingEntity(
                    name = "Bâtiment A - Résidence Les Terrasses",
                    code = "BAT-A-2026",
                    contractorName = "BTP Construction Pro",
                    inspectorName = "J. Dupont (Qualité & Conformité)"
                )
            )

            val cuisineId = dao.insertElement(
                ElementEntity(
                    buildingId = buildingId,
                    name = "Cuisine T5",
                    description = "Contrôle par étage",
                    totalFloors = 1
                )
            )

            dao.insertElement(
                ElementEntity(
                    buildingId = buildingId,
                    name = "Salle de Bain T5",
                    description = "Étanchéité, plomberie et finitions",
                    totalFloors = 1
                )
            )

            // Seed Floors for Cuisine T5: Étage 01 only (user adds more as wanted)
            val floors = listOf(
                FloorInspectionEntity(
                    elementId = cuisineId,
                    floorNumber = "Étage 01",
                    floorIndex = 1,
                    status = InspectionStatus.PENDING.name,
                    photoUrl = null,
                    observation = "",
                    isAiGenerated = false
                )
            )
            dao.insertFloors(floors)
        }
    }
}
