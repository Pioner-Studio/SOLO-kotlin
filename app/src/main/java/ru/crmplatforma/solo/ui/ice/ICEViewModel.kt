package ru.crmplatforma.solo.ui.ice

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.crmplatforma.solo.data.local.entity.ICEContactEntity
import ru.crmplatforma.solo.data.repository.ICERepository
import javax.inject.Inject

/**
 * UI State для ICE.
 */
data class ICEState(
    val ice: ICEContactEntity? = null,
    val isLoading: Boolean = true,
    val hasData: Boolean = false
)

/**
 * ICEViewModel — управление экстренной карточкой.
 */
@HiltViewModel
class ICEViewModel @Inject constructor(
    private val iceRepository: ICERepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<ICEState> = iceRepository.getICE()
        .map { ice ->
            ICEState(
                ice = ice,
                isLoading = false,
                hasData = ice != null
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ICEState()
        )

    // === Actions ===

    fun callPhone(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
