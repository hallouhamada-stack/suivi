package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BuildingEntity
import com.example.data.model.ElementEntity
import com.example.data.model.FloorInspectionEntity
import com.example.data.model.FloorObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    // Buildings
    @Query("SELECT * FROM buildings ORDER BY id ASC")
    fun getAllBuildings(): Flow<List<BuildingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: BuildingEntity): Long

    @Query("DELETE FROM buildings WHERE id = :buildingId")
    suspend fun deleteBuildingById(buildingId: Long)

    // Elements
    @Query("SELECT * FROM elements WHERE buildingId = :buildingId ORDER BY id ASC")
    fun getElementsForBuilding(buildingId: Long): Flow<List<ElementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElement(element: ElementEntity): Long

    @Query("DELETE FROM elements WHERE id = :elementId")
    suspend fun deleteElementById(elementId: Long)

    // Floor Inspections
    @Query("SELECT * FROM floor_inspections WHERE elementId = :elementId ORDER BY floorIndex ASC")
    fun getFloorsForElement(elementId: Long): Flow<List<FloorInspectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFloors(floors: List<FloorInspectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFloor(floor: FloorInspectionEntity): Long

    @Query("DELETE FROM floor_inspections WHERE id = :floorId")
    suspend fun deleteFloorById(floorId: Long)

    @Update
    suspend fun updateFloor(floor: FloorInspectionEntity)

    @Query("SELECT COUNT(*) FROM floor_inspections WHERE elementId = :elementId AND status = 'OK'")
    fun getOkCount(elementId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM floor_inspections WHERE elementId = :elementId AND status = 'DEFECT'")
    fun getDefectCount(elementId: Long): Flow<Int>

    @Query("DELETE FROM floor_inspections WHERE elementId = :elementId")
    suspend fun resetFloorsForElement(elementId: Long)

    // Floor Observations (Multiple observations with images per floor)
    @Query("SELECT * FROM floor_observations WHERE floorId = :floorId ORDER BY id ASC")
    fun getObservationsForFloor(floorId: Long): Flow<List<FloorObservationEntity>>

    @Query("SELECT * FROM floor_observations WHERE floorId IN (:floorIds) ORDER BY id ASC")
    suspend fun getObservationsForFloors(floorIds: List<Long>): List<FloorObservationEntity>

    @Query("SELECT * FROM floor_observations WHERE floorId IN (:floorIds) ORDER BY id ASC")
    fun getObservationsForFloorsFlow(floorIds: List<Long>): Flow<List<FloorObservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: FloorObservationEntity): Long

    @Update
    suspend fun updateObservation(observation: FloorObservationEntity)

    @Query("DELETE FROM floor_observations WHERE id = :id")
    suspend fun deleteObservationById(id: Long)

    @Query("DELETE FROM floor_observations WHERE floorId = :floorId")
    suspend fun deleteObservationsForFloor(floorId: Long)

    @Query("SELECT COUNT(*) FROM floor_observations WHERE floorId = :floorId")
    fun getObservationCountForFloor(floorId: Long): Flow<Int>
}

