package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssetViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val assetDao = db.assetDao()
    private val conditionHistoryDao = db.conditionHistoryDao()
    private val issueLogDao = db.issueLogDao()
    private val healthCheckSessionDao = db.healthCheckSessionDao()
    private val schoolProfileDao = db.schoolProfileDao()

    private val userPrefs = UserPreferences(application)

    val activeTeacher: StateFlow<String?> = userPrefs.activeTeacherFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userPin: StateFlow<String?> = userPrefs.userPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAssets: StateFlow<List<AssetEntity>> = assetDao.getAllAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allConditionsHistory: StateFlow<List<ConditionHistoryEntity>> = kotlinx.coroutines.flow.flowOf(emptyList<ConditionHistoryEntity>())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()) // Placeholder if needed

    val allIssues: StateFlow<List<IssueLogEntity>> = issueLogDao.getAllIssues()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openIssues: StateFlow<List<IssueLogEntity>> = issueLogDao.getOpenIssues()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastSession: StateFlow<HealthCheckSessionEntity?> = healthCheckSessionDao.getLastSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val schoolProfile: StateFlow<SchoolProfileEntity?> = schoolProfileDao.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveSchoolProfile(schoolName: String, diseCode: String?, district: String, block: String) {
        viewModelScope.launch {
            val profile = SchoolProfileEntity(
                schoolName = schoolName,
                diseCode = diseCode,
                district = district,
                block = block,
                createdAt = System.currentTimeMillis()
            )
            schoolProfileDao.insertProfile(profile)
        }
    }

    fun getAssetById(assetId: Int): kotlinx.coroutines.flow.Flow<AssetEntity?> {
        return kotlinx.coroutines.flow.flow {
            emit(assetDao.getAssetById(assetId))
        }
    }

    fun getConditionHistoryForAsset(assetId: Int): kotlinx.coroutines.flow.Flow<List<ConditionHistoryEntity>> {
        return conditionHistoryDao.getHistoryForAsset(assetId)
    }

    fun getIssueById(issueId: Int): kotlinx.coroutines.flow.Flow<IssueLogEntity?> {
        return issueLogDao.getIssueById(issueId)
    }

    fun getIssuesByAsset(assetId: Int): kotlinx.coroutines.flow.Flow<List<IssueLogEntity>> {
        return issueLogDao.getIssuesByAsset(assetId)
    }

    fun setActiveTeacher(name: String) {
        viewModelScope.launch {
            userPrefs.saveActiveTeacher(name)
        }
    }

    fun setUserPin(pin: String) {
        viewModelScope.launch {
            userPrefs.saveUserPin(pin)
        }
    }

    fun addAsset(
        name: String, category: Category, serialNumber: String?,
        acquisitionDate: String, purchaseValue: Double?, photoPath: String?,
        photoUri: String? = null,
        condition: Condition,
        recordedBy: String
    ) {
        viewModelScope.launch {
            val asset = AssetEntity(
                name = name,
                category = category,
                serialNumber = serialNumber,
                acquisitionDate = acquisitionDate,
                purchaseValue = purchaseValue,
                photoPath = photoPath,
                photoUri = photoUri,
                currentCondition = condition,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val assetId = assetDao.insertAsset(asset).toInt()

            val history = ConditionHistoryEntity(
                assetId = assetId,
                condition = condition,
                note = "Initial entry",
                photoPath = photoPath,
                recordedBy = recordedBy,
                recordedAt = System.currentTimeMillis()
            )
            conditionHistoryDao.insertHistory(history)
        }
    }

    fun editAsset(
        asset: AssetEntity,
        name: String, category: Category, serialNumber: String?,
        acquisitionDate: String, purchaseValue: Double?,
        photoUri: String? = asset.photoUri
    ) {
        viewModelScope.launch {
            val updated = asset.copy(
                name = name,
                category = category,
                serialNumber = serialNumber,
                acquisitionDate = acquisitionDate,
                purchaseValue = purchaseValue,
                photoUri = photoUri,
                updatedAt = System.currentTimeMillis()
            )
            assetDao.updateAsset(updated)
        }
    }

    fun updateAssetPhoto(asset: AssetEntity, photoUri: String) {
        viewModelScope.launch {
            val updated = asset.copy(
                photoUri = photoUri,
                updatedAt = System.currentTimeMillis()
            )
            assetDao.updateAsset(updated)
        }
    }

    fun markDisposed(asset: AssetEntity) {
        viewModelScope.launch {
            val updated = asset.copy(
                isActive = false,
                updatedAt = System.currentTimeMillis()
            )
            assetDao.updateAsset(updated)
        }
    }

    fun updateCondition(asset: AssetEntity, newCondition: Condition, note: String?, photoPath: String?, recordedBy: String) {
        viewModelScope.launch {
            val updated = asset.copy(
                currentCondition = newCondition,
                updatedAt = System.currentTimeMillis()
            )
            assetDao.updateAsset(updated)

            val history = ConditionHistoryEntity(
                assetId = asset.id,
                condition = newCondition,
                note = note,
                photoPath = photoPath,
                recordedBy = recordedBy,
                recordedAt = System.currentTimeMillis()
            )
            conditionHistoryDao.insertHistory(history)
        }
    }

    fun logIssue(assetId: Int, issueType: IssueType, description: String, photoPath: String?) {
        viewModelScope.launch {
            val issue = IssueLogEntity(
                assetId = assetId,
                issueType = issueType,
                description = description,
                photoPath = photoPath,
                isResolved = false,
                loggedAt = System.currentTimeMillis(),
                resolvedAt = null
            )
            issueLogDao.insertIssue(issue)
        }
    }

    fun resolveIssue(issue: IssueLogEntity) {
        viewModelScope.launch {
            issueLogDao.updateIssue(
                issue.copy(
                    isResolved = true,
                    resolvedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun startHealthCheckSession(totalItems: Int, conductedBy: String): Long {
        val session = HealthCheckSessionEntity(
            conductedBy = conductedBy,
            itemsReviewed = 0,
            totalItems = totalItems,
            startedAt = System.currentTimeMillis(),
            completedAt = null
        )
        return healthCheckSessionDao.insertSession(session)
    }

    fun completeHealthCheckSession(sessionId: Int, conductedBy: String, itemsReviewed: Int, totalItems: Int, startedAt: Long) {
        viewModelScope.launch {
            val session = HealthCheckSessionEntity(
                sessionId = sessionId,
                conductedBy = conductedBy,
                itemsReviewed = itemsReviewed,
                totalItems = totalItems,
                startedAt = startedAt,
                completedAt = System.currentTimeMillis()
            )
            healthCheckSessionDao.updateSession(session)
        }
    }

    fun insertSampleData() {
        val teacher = activeTeacher.value ?: "Admin"

        val mockData = listOf(
            Triple("Lenovo ThinkPad", Category.TECHNOLOGY, Condition.GREEN),
            Triple("Microscope B-150", Category.LABORATORY, Condition.YELLOW),
            Triple("Cricket Kit (Junior)", Category.SPORTS, Condition.RED),
            Triple("Student Desk - Standard", Category.FURNITURE, Condition.GREEN),
            Triple("Canon Projector 4K", Category.TECHNOLOGY, Condition.GREEN),

            Triple("Chemistry Glassware Set", Category.LABORATORY, Condition.GREEN),
            Triple("Volleyball Net", Category.SPORTS, Condition.YELLOW),
            Triple("Teacher's Office Chair", Category.FURNITURE, Condition.RED),
            Triple("HP LaserJet Printer", Category.TECHNOLOGY, Condition.GREEN),
            Triple("Anatomy Skeleton Model", Category.LABORATORY, Condition.GREEN),
            Triple("Basketball (Set of 5)", Category.SPORTS, Condition.GREEN),
            Triple("Classroom Whiteboard 6x4", Category.FURNITURE, Condition.GREEN),
            Triple("Library Desktop PC", Category.TECHNOLOGY, Condition.YELLOW),
            Triple("Geometry Compass Set", Category.LABORATORY, Condition.GREEN),
            Triple("First Aid Kit", Category.OTHER, Condition.GREEN)
        )

        mockData.forEachIndexed { index, data ->
            addAsset(
                name = data.first,
                category = data.second,
                serialNumber = "SN-2026-${1000 + index}",
                acquisitionDate = "10 Jan 2026",
                purchaseValue = 1500.0 + (index * 250),
                photoPath = null,
                photoUri = null,
                condition = data.third,
                recordedBy = teacher
            )
        }
    }
}