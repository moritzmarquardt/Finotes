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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _noteIsLoaded = MutableStateFlow(false)
    private val _currentNote = MutableStateFlow(Note())

    /**
     * This variable stores the information whether the user has typed in content into the title or body
     * text fields but the auto-update has not run yet due to the debounce mechanism
     */
    private val _hasPendingContentChanges = MutableStateFlow(false)
    val titleState = TextFieldState()
    val bodyState = TextFieldState()

    val currentNote: StateFlow<Note> = _currentNote.asStateFlow()
    /**Editing a note should only be allowed after it has loaded from the database and if it has status active
    * This is okay because notes are small and room is quick so waiting time is short.
    * And in doing so consistent room flows are guaranteed.
    */
    val isEditingAllowed: StateFlow<Boolean> = combine(_currentNote, _noteIsLoaded) { note, isLoaded ->
        note.noteStatus == NoteStatus.ACTIVE && isLoaded
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    /**
     * Quick actions directly from the note screen such as pinning, archiving, deleting, restoring and unarchiving
     * a note should only be possible when the note is loaded
     */
    val areQuickActionsAllowed: StateFlow<Boolean> = _noteIsLoaded.asStateFlow()
    // Focus body automatically when a new unsaved note finishes loading
    val shouldFocusBody: StateFlow<Boolean> = combine(_currentNote, _noteIsLoaded) { note, isLoaded ->
        isLoaded && note.id == null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * When the NoteScreen is launched with an id passed in the route, we want to load the note from room
     * if the id is not null so if the note exists in the database. If the id is null, then the
     * note does not exist yet in the database. But we also do not want to insert a new note into
     * the database until it is certain that the note has content. This is to avoid saving and deleting
     * empty notes when the user decides to close the screen without writing a note.
     */
    init {
        /**
         * Loading the note from the database. Runs once.
         */
        viewModelScope.launch {
            val noteRoute = savedStateHandle.toRoute<NoteRoute>()
            val passedNoteId = noteRoute.noteId
            val note = if (passedNoteId != null) {
                Log.d("NoteViewModel", "Fetching note with ID: $passedNoteId")
                noteRepository.fetchNoteById(passedNoteId).firstOrNull() ?: Note()
                //TODO make this timeout safe
            } else {
                Log.d("NoteViewModel", "Creating new empty note.")
                Note()
            }

            _currentNote.update { note }
            titleState.setTextAndPlaceCursorAtEnd(note.title)
            bodyState.setTextAndPlaceCursorAtEnd(note.body)

            _noteIsLoaded.value = true
            Log.d("NoteViewModel", "Note loaded: ${note.id}")
        }

        // Update _currentNote when states change efficiently
        viewModelScope.launch {
            snapshotFlow { titleState.text }
                .filter { _noteIsLoaded.value }  // only continue if the note has been loaded since otherwise, no user changes have been made anyway
                .map { it.toString() }  // only count as change if the text changes in the state and not counting cursor changes as well
                .distinctUntilChanged()  // only continue if the value has changed
                .collect { newText ->
                    // Only update the internal current Note so that we can use debounce for saving notes to room
                    _currentNote.update { it.copy(title = newText) }
                    _hasPendingContentChanges.value = true
                }
        }
        // Analogous for the bodyState
        viewModelScope.launch {
            snapshotFlow { bodyState.text }
                .filter { _noteIsLoaded.value }
                .map { it.toString() }
                .distinctUntilChanged()
                .collect { newText ->
                    _currentNote.update { it.copy(body = newText) }
                    _hasPendingContentChanges.value = true
                }
        }

        // Debounce mechanism for saving the _currentNote
        viewModelScope.launch {
            _currentNote
                .debounce(500L.milliseconds) // Wait 500ms after the last change
                .distinctUntilChanged() // Only emit if the value has changed (uses structural equality of the data class
                .filter { noteToSave ->
                    noteToSave.isExistingNote() || noteToSave.isUnsavedNoteWithContent()
                }  // Only emit if statement inside filter is true
                .collect { noteToSave ->
                    try {
                        when {
                            noteToSave.id == null -> insertNote(noteToSave)
                            else -> updateNote(noteToSave)
                        }
                        _hasPendingContentChanges.value = false
                    } catch (e: Exception) {
                        Log.e("NoteViewModel", "Failed auto-saving note", e)
                    }
                }
        }
    }

    /**
     * Insert a note to the database and update the [_currentNote] with the new id to keep
     * it in sync with the database.
     *
     *  @param note The note to insert.
     *  @return The id of the inserted note.
     */
    private suspend fun insertNote(note: Note): Long {
        val newId = noteRepository.insertNote(note)
        _currentNote.update { note.copy(id = newId) }
        return newId
    }

    private suspend fun updateNote(note: Note) {
        noteRepository.updateNotes(listOf(note))
    }

    /**
     * Called on navigate-back to flush any un-persisted edits.
     * Inserts new notes only if they contain text (prevents blank notes), and updates existing notes
     * only if user changes are pending.
     */
    suspend fun saveIfNeeded() {
        val note = _currentNote.value
        val isExistingWithChanges = note.id != null && _hasPendingContentChanges.value

        if (note.isUnsavedNoteWithContent()) {
            insertNote(note)
            _hasPendingContentChanges.value = false
        } else if (isExistingWithChanges) {
            updateNote(note)
            _hasPendingContentChanges.value = false
        }
    }

    suspend fun updateCurrentNoteIsPinned(isPinned: Boolean) {
        if (!areQuickActionsAllowed.value) return
        val current = _currentNote.value
        if (current.id == null) {
            insertNote(current.copy(isPinned = isPinned))
        } else {
            updateNote(current.copy(isPinned = isPinned))
        }
        _currentNote.update { it.copy(isPinned = isPinned) }
    }

    suspend fun archiveNote(note: Note) {
        if (!areQuickActionsAllowed.value) return
        if (note.id == null) {
            insertNote(note.copy(noteStatus = NoteStatus.ARCHIVED))
        } else {
            noteRepository.archiveNotes(listOf(note))
        }
    }

    suspend fun unarchiveNote(note: Note) {
        if (!areQuickActionsAllowed.value) return
        if (note.id == null) {
            insertNote(note.copy(noteStatus = NoteStatus.ACTIVE))
        } else {
            noteRepository.restoreNotes(listOf(note))
        }
    }

    suspend fun binNote(note: Note) {
        if (!areQuickActionsAllowed.value) return
        if (note.id == null) {
            insertNote(note.copy(noteStatus = NoteStatus.BINNED))
        } else {
            noteRepository.binNotes(listOf(note))
        }
    }

    suspend fun restoreNote(note: Note) {
        if (!areQuickActionsAllowed.value) return
        if (note.id == null) {
            insertNote(note.copy(noteStatus = NoteStatus.ACTIVE))
        } else {
            noteRepository.restoreNotes(listOf(note))
        }
    }

    suspend fun deleteNoteFromBin(note: Note) {
        if (!areQuickActionsAllowed.value) return
        if (note.id != null) {
            noteRepository.permanentlyDeleteNotes(listOf(note))
        }
    }
}