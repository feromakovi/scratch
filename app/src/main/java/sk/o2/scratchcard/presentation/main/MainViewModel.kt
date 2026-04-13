package sk.o2.scratchcard.presentation.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.repository.ScratchCardRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: ScratchCardRepository
) : ViewModel() {

    val cardState: StateFlow<ScratchCardState> = repository.state
}
