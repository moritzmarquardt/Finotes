package de.marquisproject.finotes.ui.viewmodels

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import de.marquisproject.finotes.NoteRoute
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.repositories.NoteRepository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _noteIsLoaded = MutableStateFlow(false)
    private val _currentNote = MutableStateFlow(Note())
    private val _currentBodyTextFieldValue = MutableStateFlow(TextFieldValue())

    val currentBodyTextFieldValue: StateFlow<TextFieldValue> = _currentBodyTextFieldValue.asStateFlow()
    val currentNote: StateFlow<Note> = _currentNote.asStateFlow()
    val noteIsLoaded: StateFlow<Boolean> = _noteIsLoaded.asStateFlow()

    init {
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
            _currentBodyTextFieldValue.value = TextFieldValue(text = note.body)
            _noteIsLoaded.value = true

            Log.d("NoteViewModel", "Note loaded: ${note.id}")
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
                        Log.d("NoteViewModel", "Debounced save triggered for note ID: ${noteToSave.id} and body: ${noteToSave.body}")
                        insertNewOrUpdateNote(noteToSave)
                    } catch (e: Exception) {
                        Log.e("NoteViewModel", "Failed auto-saving note", e)
                    }
                }
        }
    }

    // This function is now responsible for both inserting new notes and updating existing ones.
    private fun insertNewOrUpdateNote(updatedNote: Note) {
        viewModelScope.launch {
            if (updatedNote.id == null) {
                // New note: insert and update the ID
                Log.d("NoteViewModel", "Inserting new note into database.")
                val newId = noteRepository.insertNote(updatedNote)
                _currentNote.update { it.copy(id = newId) } // Update the editable note with the new ID
                Log.d("NoteViewModel", "New note inserted with ID: $newId")
            } else {
                // Existing note: update
                Log.d("NoteViewModel", "Updating existing note with ID: ${updatedNote.id}")
                noteRepository.updateNotes(listOf(updatedNote))
            }
        }
    }

    fun saveCurrentNote() {
        viewModelScope.launch {
            Log.d("NoteViewModel", "Updating current note with ID: ${_currentNote.value.id}")
            noteRepository.updateNotes(listOf(_currentNote.value))
        }
    }

    // UI-facing update functions that directly modify _currentNote
    fun updateCurrentNoteTitle(title: String) {
        _currentNote.update { it.copy(title = title) }
    }

    fun updateCurrentNoteBody(newTextFieldValue: TextFieldValue) {
        _currentBodyTextFieldValue.update { newTextFieldValue }
        _currentNote.update { it.copy(body = newTextFieldValue.text) }
    }

    fun updateCurrentNoteIsPinned(isPinned: Boolean) {
        _currentNote.update { it.copy(isPinned = isPinned) }
    }

    // --- Actions that affect note status and often navigate away ---
    fun archiveNote(note: Note) {
        viewModelScope.launch {
            noteRepository.archiveNotes(listOf(note))
        }
    }

    fun unarchiveNote(note: Note) {
        viewModelScope.launch {
            noteRepository.restoreNotes(listOf(note))
        }
    }

    fun binNote(note: Note) {
        viewModelScope.launch {
            noteRepository.binNotes(listOf(note))
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            noteRepository.restoreNotes(listOf(note))
        }
    }

    fun deleteNoteFromBin(note: Note) {
        viewModelScope.launch {
            noteRepository.permanentlyDeleteNotes(listOf(note))
        }
    }
}