package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BuildingEntity
import com.example.data.model.ElementEntity
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.FloorObservationEntity
import com.example.data.model.InspectionStatus
import com.example.data.preferences.SettingsPreferences
import com.example.data.repository.InspectionRepository
import com.example.service.ai.AiVisionService
import com.example.service.pdf.PdfExportService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavStep {
    HOME,
    SPACE,
    ETAGES,
    PROBLEME
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class InspectionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = InspectionRepository(db.inspectionDao())
    val settingsPrefs = SettingsPreferences(application)
    val aiService = AiVisionService()
    val pdfService = PdfExportService()

    private val _navStep = MutableStateFlow(NavStep.HOME)
    val navStep: StateFlow<NavStep> = _navStep.asStateFlow()

    private val _selectedBuilding = MutableStateFlow<BuildingEntity?>(null)
    val selectedBuilding: StateFlow<BuildingEntity?> = _selectedBuilding.asStateFlow()

    private val _selectedElement = MutableStateFlow<ElementEntity?>(null)
    val selectedElement: StateFlow<ElementEntity?> = _selectedElement.asStateFlow()

    private val _activeFloorIndex = MutableStateFlow(1)
    val activeFloorIndex: StateFlow<Int> = _activeFloorIndex.asStateFlow()

    private val _defectModalFloor = MutableStateFlow<FloorInspectionEntity?>(null)
    val defectModalFloor: StateFlow<FloorInspectionEntity?> = _defectModalFloor.asStateFlow()

    private val _editingObservation = MutableStateFlow<FloorObservationEntity?>(null)
    val editingObservation: StateFlow<FloorObservationEntity?> = _editingObservation.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val buildings: StateFlow<List<BuildingEntity>> = repository.allBuildings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val elements: StateFlow<List<ElementEntity>> = _selectedBuilding
        .flatMapLatest { b ->
            if (b != null) repository.getElementsForBuilding(b.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val floors: StateFlow<List<FloorInspectionEntity>> = _selectedElement
        .flatMapLatest { el ->
            if (el != null) repository.getFloorsForElement(el.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFloor: StateFlow<FloorInspectionEntity?> = combine(floors, _activeFloorIndex) { list, idx ->
        list.find { it.floorIndex == idx } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeFloorObservations: StateFlow<List<FloorObservationEntity>> = activeFloor
        .flatMapLatest { f ->
            if (f != null) repository.getObservationsForFloor(f.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val elementObservations: StateFlow<List<FloorObservationEntity>> = floors
        .flatMapLatest { floorList ->
            if (floorList.isNotEmpty()) {
                val ids = floorList.map { it.id }
                repository.getObservationsForFloorsFlow(ids)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    init {
        viewModelScope.launch {
            repository.ensureDefaultDataSeeded()
            repository.allBuildings.collect { bList ->
                if (_selectedBuilding.value == null && bList.isNotEmpty()) {
                    val firstBuilding = bList.first()
                    _selectedBuilding.value = firstBuilding

                    repository.getElementsForBuilding(firstBuilding.id).collect { eList ->
                        if (_selectedElement.value == null && eList.isNotEmpty()) {
                            _selectedElement.value = eList.first()
                        }
                    }
                }
            }
        }
    }

    fun selectFloor(floor: FloorInspectionEntity) {
        _activeFloorIndex.value = floor.floorIndex
    }

    fun nextFloor() {
        val total = floors.value.size
        if (_activeFloorIndex.value < total) {
            _activeFloorIndex.value += 1
        }
    }

    fun prevFloor() {
        if (_activeFloorIndex.value > 1) {
            _activeFloorIndex.value -= 1
        }
    }

    /**
     * CORE WORKFLOW:
     * [Conforme / Complet] (Green): 1-click validates the floor and jumps to the next floor automatically!
     */
    fun validateFloorConforme(floor: FloorInspectionEntity) {
        viewModelScope.launch {
            repository.markFloorOk(floor)
            _toastMessage.value = "✓ ${floor.floorNumber} validé Conforme !"

            // Auto-advance to next floor
            val total = floors.value.size
            if (floor.floorIndex < total) {
                _activeFloorIndex.value = floor.floorIndex + 1
            }
        }
    }

    /**
     * CORE WORKFLOW:
     * [Non-conforme / Problème] (Red): Prompts user to snap/upload photo
     */
    fun openDefectDialog(floor: FloorInspectionEntity) {
        _defectModalFloor.value = floor
        _editingObservation.value = null
    }

    fun openAddObservationDialog(floor: FloorInspectionEntity) {
        _defectModalFloor.value = floor
        _editingObservation.value = null
    }

    fun openEditObservationDialog(floor: FloorInspectionEntity, observation: FloorObservationEntity) {
        _defectModalFloor.value = floor
        _editingObservation.value = observation
    }

    fun closeDefectDialog() {
        _defectModalFloor.value = null
        _editingObservation.value = null
    }

    fun markFloorOk(floor: FloorInspectionEntity) {
        validateFloorConforme(floor)
    }

    fun markFloorDefect(floor: FloorInspectionEntity, photoUrl: String?, observation: String, isAiGenerated: Boolean) {
        viewModelScope.launch {
            repository.markFloorDefect(
                floor = floor,
                photoUrl = photoUrl,
                observation = observation,
                isAiGenerated = isAiGenerated
            )
            _toastMessage.value = "Défaut enregistré pour ${floor.floorNumber}"
            _defectModalFloor.value = null
            _editingObservation.value = null

            val total = floors.value.size
            if (floor.floorIndex < total) {
                _activeFloorIndex.value = floor.floorIndex + 1
            }
        }
    }

    fun saveDefect(photoUrl: String?, observation: String, isAiGenerated: Boolean) {
        val targetFloor = _defectModalFloor.value ?: return
        val currentEditing = _editingObservation.value
        viewModelScope.launch {
            if (currentEditing != null) {
                repository.updateObservation(
                    floor = targetFloor,
                    observation = currentEditing.copy(
                        description = observation.ifBlank { "Anomalie constatée" },
                        photoUrl = photoUrl ?: currentEditing.photoUrl,
                        isAiGenerated = isAiGenerated
                    )
                )
                _toastMessage.value = "✓ Observation mise à jour"
            } else {
                repository.addFloorObservation(
                    floor = targetFloor,
                    description = observation.ifBlank { "Anomalie constatée" },
                    photoUrl = photoUrl,
                    isAiGenerated = isAiGenerated
                )
                _toastMessage.value = "✓ Observation ajoutée à ${targetFloor.floorNumber}"
            }
            _defectModalFloor.value = null
            _editingObservation.value = null
        }
    }

    fun deleteObservation(floor: FloorInspectionEntity, observation: FloorObservationEntity) {
        viewModelScope.launch {
            repository.deleteFloorObservation(floor, observation.id)
            _toastMessage.value = "Observation supprimée"
        }
    }

    fun navigateTo(step: NavStep) {
        _navStep.value = step
    }

    fun selectBuilding(building: BuildingEntity) {
        _selectedBuilding.value = building
        viewModelScope.launch {
            val elemList = repository.getElementsForBuilding(building.id).firstOrNull() ?: emptyList()
            _selectedElement.value = elemList.firstOrNull()
            _activeFloorIndex.value = 1
            _toastMessage.value = "Projet actif : ${building.name}"
        }
    }

    fun addNewBuilding(name: String, code: String, contractor: String = "BTP Construction Pro", inspector: String = "Inspecteur Travaux") {
        viewModelScope.launch {
            val created = repository.addBuilding(name, code, contractor, inspector)
            _selectedBuilding.value = created
            val elemList = repository.getElementsForBuilding(created.id).firstOrNull() ?: emptyList()
            _selectedElement.value = elemList.firstOrNull()
            _activeFloorIndex.value = 1
            _toastMessage.value = "✓ Nouveau projet \"$name\" créé avec succès !"
        }
    }

    fun deleteBuilding(building: BuildingEntity) {
        viewModelScope.launch {
            repository.deleteBuilding(building.id)
            _toastMessage.value = "Projet \"${building.name}\" supprimé."
            val remaining = buildings.value.filter { it.id != building.id }
            if (remaining.isNotEmpty()) {
                selectBuilding(remaining.first())
            }
        }
    }

    fun selectElement(element: ElementEntity) {
        _selectedElement.value = element
        _activeFloorIndex.value = 1
        _navStep.value = NavStep.ETAGES
    }

    fun selectFloorAndInspect(floor: FloorInspectionEntity) {
        _activeFloorIndex.value = floor.floorIndex
        _navStep.value = NavStep.PROBLEME
    }

    fun addNewElement(name: String, description: String = "Contrôle par étage", floorsCount: Int = 1) {
        val b = _selectedBuilding.value ?: return
        viewModelScope.launch {
            val created = repository.addElement(b.id, name, description, floorsCount)
            _selectedElement.value = created
            _activeFloorIndex.value = 1
            _navStep.value = NavStep.ETAGES
            _toastMessage.value = "✓ Nouvel espace \"$name\" créé (1 étage) !"
        }
    }

    fun deleteElement(element: ElementEntity) {
        viewModelScope.launch {
            repository.deleteElement(element.id)
            _toastMessage.value = "Espace \"${element.name}\" supprimé."
            val remaining = elements.value.filter { it.id != element.id }
            if (remaining.isNotEmpty()) {
                _selectedElement.value = remaining.first()
            }
        }
    }

    fun resetFloorsForElement() {
        val el = _selectedElement.value ?: return
        viewModelScope.launch {
            repository.resetElementFloors(el.id, el.totalFloors)
            _activeFloorIndex.value = 1
            _toastMessage.value = "Réinitialisation des 14 étages effectuée."
        }
    }

    fun addNewFloor(customName: String? = null) {
        val el = _selectedElement.value ?: return
        viewModelScope.launch {
            val added = repository.addFloor(el.id, customName)
            _activeFloorIndex.value = added.floorIndex
            _toastMessage.value = "✓ ${added.floorNumber} ajouté avec succès !"
        }
    }

    fun removeFloor(floor: FloorInspectionEntity) {
        viewModelScope.launch {
            repository.deleteFloor(floor)
            _toastMessage.value = "${floor.floorNumber} supprimé."
            val remaining = floors.value.filter { it.id != floor.id }
            if (remaining.isNotEmpty()) {
                val nextTarget = remaining.find { it.floorIndex >= floor.floorIndex } ?: remaining.last()
                _activeFloorIndex.value = nextTarget.floorIndex
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
