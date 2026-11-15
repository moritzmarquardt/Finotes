package de.marquisproject.finotes.ui.viewmodels

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marquisproject.finotes.NoteRoute
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val loadedNote = MutableStateFlow<Note?>(null)
    private var lastSavedNote: Note? = null

    val titleState = TextFieldState()
    val bodyState = TextFieldState()

    val isPinned: StateFlow<Boolean> = loadedNote
        .map { it?.isPinned ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val noteStatus: StateFlow<NoteStatus> = loadedNote
        .map { it?.noteStatus ?: NoteStatus.ACTIVE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteStatus.ACTIVE)

    val isEditingAllowed: StateFlow<Boolean> = combine(noteStatus, loadedNote) { status, note ->
        status == NoteStatus.ACTIVE && note != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val areQuickActionsAllowed: StateFlow<Boolean> = loadedNote
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val shouldFocusBody: StateFlow<Boolean> = loadedNote
        .map { note -> note != null && note.id == null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            val noteRoute = savedStateHandle.toRoute<NoteRoute>()
            val passedNoteId = noteRoute.noteId
            val note = if (passedNoteId != null) {
                Log.d("NoteViewModel", "Fetching note with ID: $passedNoteId")
                noteRepository.fetchNoteById(passedNoteId).firstOrNull() ?: Note()
            } else {
                Log.d("NoteViewModel", "Creating new empty note.")
                Note()
            }

            titleState.setTextAndPlaceCursorAtEnd(note.title)
            bodyState.setTextAndPlaceCursorAtEnd(note.body)
            loadedNote.value = note
            lastSavedNote = note

            Log.d("NoteViewModel", "Note loaded: ${note.id}")
        }

        // Auto-save on text updates without feedback-looping on loadedNote changes
        viewModelScope.launch {
            combine(
                snapshotFlow { titleState.text.toString() },
                snapshotFlow { bodyState.text.toString() }
            ) { title, body -> title to body }
                .distinctUntilChanged()
                .debounce(500.milliseconds)
                .collect { (title, body) ->
                    val current = getCurrentNoteWithLiveText() ?: return@collect
                    if ((current.id != null || current.isUnsavedNoteWithContent()) && current != lastSavedNote) {
                        save(current)
                    }
                }
        }
    }

    /**
     * Executes the DB save operation using NonCancellable context so that the database transaction
     * will never be interrupted or lost even if the ViewModel/Screen is popped immediately.
     */
    private suspend fun save(note: Note): Note = withContext(NonCancellable) {
        val saved = if (note.id == null) {
            val newId = noteRepository.insertNote(note)
            note.copy(id = newId)
        } else {
            noteRepository.updateNotes(listOf(note))
            note
        }
        lastSavedNote = saved
        loadedNote.value = saved
        saved
    }

    /**
     * Merges current base note metadata with live text field values
     */
    private fun getCurrentNoteWithLiveText(): Note? {
        val current = loadedNote.value ?: return null
        return current.copy(
            title = titleState.text.toString(),
            body = bodyState.text.toString()
        )
    }

    /**
     * Flush un-persisted text changes on back navigation and run onComplete afterward.
     */
    fun saveIfNeeded(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val updated = getCurrentNoteWithLiveText()
            if (updated != null && updated != lastSavedNote) {
                if (updated.id != null || updated.isUnsavedNoteWithContent()) {
                    save(updated)
                }
            }
            onComplete()
        }
    }

    fun togglePinned() {
        viewModelScope.launch {
            if (!areQuickActionsAllowed.value) return@launch
            val updated = getCurrentNoteWithLiveText() ?: return@launch
            save(updated.copy(isPinned = !updated.isPinned))
        }
    }

    fun archiveNote(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (!areQuickActionsAllowed.value) return@launch
            val updated = getCurrentNoteWithLiveText() ?: return@launch
            withContext(NonCancellable) {
                noteRepository.archiveNotes(listOf(updated))
                loadedNote.update { it?.copy(title = updated.title, body = updated.body, noteStatus = NoteStatus.ARCHIVED) }
            }
            onComplete()
        }
    }

    fun unarchiveNote(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (!areQuickActionsAllowed.value) return@launch
            val updated = getCurrentNoteWithLiveText() ?: return@launch
            withContext(NonCancellable) {
                noteRepository.restoreNotes(listOf(updated))
                loadedNote.update { it?.copy(title = updated.title, body = updated.body, noteStatus = NoteStatus.ACTIVE) }
            }
            onComplete()
        }
    }

    fun binNote(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (!areQuickActionsAllowed.value) return@launch
            val updated = getCurrentNoteWithLiveText() ?: return@launch
            withContext(NonCancellable) {
                noteRepository.binNotes(listOf(updated))
                loadedNote.update { it?.copy(title = updated.title, body = updated.body, noteStatus = NoteStatus.BINNED) }
            }
            onComplete()
        }
    }

    fun restoreNote(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (!areQuickActionsAllowed.value) return@launch
            val updated = getCurrentNoteWithLiveText() ?: return@launch
            withContext(NonCancellable) {
                noteRepository.restoreNotes(listOf(updated))
                loadedNote.update { it?.copy(title = updated.title, body = updated.body, noteStatus = NoteStatus.ACTIVE) }
            }
            onComplete()
        }
    }

    fun deleteNoteFromBin(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (!areQuickActionsAllowed.value) return@launch
            val current = loadedNote.value ?: return@launch
            if (current.id != null) {
                withContext(NonCancellable) {
                    noteRepository.permanentlyDeleteNotes(listOf(current))
                }
            }
            onComplete()
        }
    }
}
