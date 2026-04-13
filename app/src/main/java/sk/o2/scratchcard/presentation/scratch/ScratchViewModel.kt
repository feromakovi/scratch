package sk.o2.scratchcard.presentation.scratch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import sk.o2.scratchcard.domain.usecase.ScratchCardUseCase
import javax.inject.Inject

@HiltViewModel
class ScratchViewModel @Inject constructor(
    private val scratchCardUseCase: ScratchCardUseCase,
    repository: ScratchCardRepository
) : ViewModel() {

    val cardState: StateFlow<ScratchCardState> = repository.state

    private val _isScratching = MutableStateFlow(false)
    val isScratching: StateFlow<Boolean> = _isScratching.asStateFlow()

    fun scratch() {
        if (_isScratching.value) return

        viewModelScope.launch {
            _isScratching.value = true
            scratchCardUseCase()
            _isScratching.value = false
        }
    }
}
