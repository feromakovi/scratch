package sk.o2.scratchcard.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import sk.o2.scratchcard.domain.model.ScratchCardState

class ScratchCardRepositoryImplTest {

    private val repository = ScratchCardRepositoryImpl()

    @Test
    fun `initial state is Unscratched`() {
        assertEquals(ScratchCardState.Unscratched, repository.state.value)
    }

    @Test
    fun `initial isActivating is false`() {
        assertFalse(repository.isActivating.value)
    }

    @Test
    fun `updateState allows Unscratched to Scratched`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        assertEquals(ScratchCardState.Scratched("code-123"), repository.state.value)
    }

    @Test
    fun `updateState allows Scratched to Activated`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        repository.updateState(ScratchCardState.Activated("code-123"))
        assertEquals(ScratchCardState.Activated("code-123"), repository.state.value)
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState rejects Unscratched to Activated`() {
        repository.updateState(ScratchCardState.Activated("code-123"))
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState rejects Scratched to Unscratched`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        repository.updateState(ScratchCardState.Unscratched)
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState rejects Activated to Scratched`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        repository.updateState(ScratchCardState.Activated("code-123"))
        repository.updateState(ScratchCardState.Scratched("another-code"))
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState rejects Activated to Unscratched`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        repository.updateState(ScratchCardState.Activated("code-123"))
        repository.updateState(ScratchCardState.Unscratched)
    }

    @Test(expected = IllegalStateException::class)
    fun `updateState rejects double scratch`() {
        repository.updateState(ScratchCardState.Scratched("code-123"))
        repository.updateState(ScratchCardState.Scratched("code-456"))
    }

    @Test
    fun `setActivating updates isActivating flow`() {
        repository.setActivating(true)
        assertTrue(repository.isActivating.value)

        repository.setActivating(false)
        assertFalse(repository.isActivating.value)
    }
}
