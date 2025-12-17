package de.marquisproject.finotes.ui.viewmodels

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.model.NoteStatus
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import de.marquisproject.finotes.utils.handleListLogic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Holds the ID passed from navigation. null for new note
    private val _currentNoteId: MutableStateFlow<Long?> = MutableStateFlow(
        savedStateHandle.get<Long>("noteId")
    )
    private val _currentNoteStatus: MutableStateFlow<NoteStatus> = MutableStateFlow(
        savedStateHandle.get<NoteStatus>("noteStatus") ?: NoteStatus.ACTIVE
    )
    private val _noteIsLoaded = MutableStateFlow(false)

    private val _editableNote = MutableStateFlow(Note())
    private val _editableBodyTextFieldValue = MutableStateFlow(TextFieldValue())

    val currentBodyTextFieldValue: StateFlow<TextFieldValue> = _editableBodyTextFieldValue.asStateFlow()
    val currentNote: StateFlow<Note> = _editableNote.asStateFlow()
    val noteIsLoaded: StateFlow<Boolean> = _noteIsLoaded.asStateFlow()

    init {
        viewModelScope.launch {
            val id = _currentNoteId
                .filter { it != null } // Wait for a valid ID
                .take(1) // Only want the first valid ID
                .first() // Use first() to suspend until value is emitted
            val status = _currentNoteStatus
                .take(1) // Only want the first valid status
                .first() // Use first() to suspend until value is emitted

            val fetchedNote = id?.let { id ->
                Log.d("NoteViewModel", "Fetching note with ID: $id")
                Log.d("NoteViewModel", "Fetching note with status: $status")
                noteRepository.fetchNoteById(id).filterNotNull().map { it.copy() }.first()
            } ?: run {
                Log.d("NoteViewModel", "Creating new empty note.")
                Note()
            }

            _editableNote.update { fetchedNote }
            _editableBodyTextFieldValue.value = TextFieldValue(
                text = fetchedNote.body,
                selection = TextRange(fetchedNote.body.length)
            )
            _noteIsLoaded.value = true

            Log.d("NoteViewModel", "Note loaded: ${fetchedNote.id}")
        }

        // Debounce mechanism for saving the _editableNote
        viewModelScope.launch {
            _editableNote
                .debounce(500L) // Wait 500ms after the last change
                .distinctUntilChanged() // Only emit if the value has changed
                .filter { noteToSave ->
                    val isExistingNote = noteToSave.id != null
                    val isNewNoteWithContent = noteToSave.id == null && (noteToSave.title.isNotBlank() || noteToSave.body.isNotBlank())
                    isExistingNote || isNewNoteWithContent
                }  // Only emit if it's a new note with content or an existing note
                .collect { noteToSave ->
                    Log.d("NoteViewModel", "Debounced save triggered for note ID: ${noteToSave.id} and body: ${noteToSave.body}")
                    insertNewOrUpdateNote(noteToSave)
                }
        }
    }

    // Pure interactions with the database
    private suspend fun saveNoteToDatabase(note: Note) {
        if (note.id == null) {
            // New note: insert and update the ID
            Log.d("NoteViewModel", "Inserting new note into database.")
            val newId = noteRepository.insertNote(note)
            _editableNote.update { it.copy(id = newId) } // Update the editable note with the new ID
            _currentNoteId.value = newId // Also update the currentNoteId to reflect the new ID
            Log.d("NoteViewModel", "New note inserted with ID: $newId")
        } else {
            // Existing note: update
            Log.d("NoteViewModel", "Updating existing note with ID: ${note.id}")
            noteRepository.updateNote(note)
        }
    }

    // This function is now responsible for both inserting new notes and updating existing ones.
    private fun insertNewOrUpdateNote(updatedNote: Note) {
        viewModelScope.launch {
            saveNoteToDatabase(updatedNote)
        }
    }

    // UI-facing update functions that directly modify _editableNote
    fun updateCurrentNoteTitle(title: String) {
        _editableNote.update { it.copy(title = title) }
    }

    fun updateCurrentNoteBody(newTextFieldValue: TextFieldValue) {
        val processedTextFieldValue = handleListLogic(_editableBodyTextFieldValue.value, newTextFieldValue)
        _editableBodyTextFieldValue.update { processedTextFieldValue }
        _editableNote.update { it.copy(body = processedTextFieldValue.text) }
    }

    fun updateCurrentNoteIsPinned(isPinned: Boolean) {
        _editableNote.update { it.copy(isPinned = isPinned) }
    }

    // --- Actions that affect note status and often navigate away ---
    fun archiveNote(note: Note) {
        viewModelScope.launch {
            noteRepository.archiveNote(note)
        }
    }

    fun unarchiveNote(note: Note) {
        viewModelScope.launch {
            noteRepository.restoreNote(note)
        }
    }

    fun binNote(note: Note) {
        viewModelScope.launch {
            noteRepository.binNote(note)
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            noteRepository.restoreNote(note)
        }
    }

    fun deleteNoteFromBin(note: Note) {
        viewModelScope.launch {
            noteRepository.permanentlyDeleteNote(note)
        }
    }
}