package com.simats.appartmentliving.ui.viewmodels

import com.simats.appartmentliving.data.NoticeDto
import com.simats.appartmentliving.data.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

sealed class NoticeOperationResult {
    object Idle : NoticeOperationResult()
    object Loading : NoticeOperationResult()
    object Success : NoticeOperationResult()
    data class Error(val message: String) : NoticeOperationResult()
}

class NoticeViewModel {
    private val _notices = MutableStateFlow<List<NoticeDto>>(emptyList())
    val notices: StateFlow<List<NoticeDto>> = _notices

    private val _latestNotices = MutableStateFlow<List<NoticeDto>>(emptyList())
    val latestNotices: StateFlow<List<NoticeDto>> = _latestNotices

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _createStatus = MutableStateFlow<NoticeOperationResult>(NoticeOperationResult.Idle)
    val createStatus: StateFlow<NoticeOperationResult> = _createStatus

    private val _deleteStatus = MutableStateFlow<NoticeOperationResult>(NoticeOperationResult.Idle)
    val deleteStatus: StateFlow<NoticeOperationResult> = _deleteStatus

    fun fetchNotices() {
        _isLoading.value = true
        _error.value = null
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
            try {
                val list = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getNotices()
                }
                _notices.value = list
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Failed to load notices"
            }
        }
    }

    fun fetchLatestNotices() {
        _isLoading.value = true
        _error.value = null
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
            try {
                val list = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getLatestNotices()
                }
                _latestNotices.value = list
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Failed to load latest notices"
            }
        }
    }

    fun createNotice(title: String, description: String, category: String, priority: String) {
        _createStatus.value = NoticeOperationResult.Loading
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
            try {
                val noticeDto = NoticeDto(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    createdBy = "Admin"
                )
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.createNotice(noticeDto)
                }
                _createStatus.value = NoticeOperationResult.Success
                fetchNotices()
            } catch (e: HttpException) {
                val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
                _createStatus.value = NoticeOperationResult.Error(errorMsg)
            } catch (e: Exception) {
                _createStatus.value = NoticeOperationResult.Error(e.message ?: "Failed to create notice")
            }
        }
    }

    fun deleteNotice(id: String) {
        _deleteStatus.value = NoticeOperationResult.Loading
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.deleteNotice(id)
                }
                _deleteStatus.value = NoticeOperationResult.Success
                fetchNotices()
            } catch (e: Exception) {
                _deleteStatus.value = NoticeOperationResult.Error(e.message ?: "Failed to delete notice")
            }
        }
    }

    fun resetCreateStatus() {
        _createStatus.value = NoticeOperationResult.Idle
    }

    fun resetDeleteStatus() {
        _deleteStatus.value = NoticeOperationResult.Idle
    }
}
