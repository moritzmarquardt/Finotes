package de.marquisproject.finotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.marquisproject.finotes.NoteRoute
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.data.notes.repositories.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    // private vals to hold the state of the UI internally (marked with _)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedNotes = MutableStateFlow<List<Note>>(emptyList())
    private val _inSelectionMode = _selectedNotes.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    private val _currentNote = MutableStateFlow(Note())
    private val _currentNoteIsNeverEdited = MutableStateFlow(false)

    private val _notesList = _searchQuery
        .flatMapLatest { searchQuery ->
            if (searchQuery.isBlank()) {
                noteRepository.fetchAllNotes()
            } else {
                noteRepository.fetchNotesWithQuery(searchQuery)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _archivedList = noteRepository.fetchAllArchivedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _binList = MutableStateFlow<List<Note>>(emptyList())

    // public vals to expose the state of the UI to the UI layer (marked with val)
    val searchQuery: StateFlow<String> = _searchQuery
    val notesList: StateFlow<List<Note>> = _notesList
    val archivedList: StateFlow<List<Note>> = _archivedList
    val binList: StateFlow<List<Note>> = _binList
    val currentNote: StateFlow<Note> = _currentNote
    val currentNoteIsNeverEdited: StateFlow<Boolean> = _currentNoteIsNeverEdited
    val inSelectionMode: StateFlow<Boolean> = _inSelectionMode
    val selectedNotes: StateFlow<List<Note>> = _selectedNotes



    // Functions to update the state of the UI and perform repository operations which in turn interact with the database
    // These functions are called from the UI layer

    fun fetchBinNotes() {
        viewModelScope.launch {
            noteRepository.fetchAllDeletedNotes().collect { notes ->
                _binList.value = notes
            }
        }
    }

    // Pure interactions with the database
    private fun updateNote(note: Note) {
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch {
            noteRepository.archiveNote(note)
        }
    }

    fun unarchiveNote (note: Note) {
        viewModelScope.launch {
            noteRepository.unarchiveNote(note)
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
            noteRepository.deleteNoteFromBin(note)
        }
    }

    // Functions to handle the UI state and perform database operations
    // set methods are to update the UI state

    private fun setCurrentNote(note: Note) {
        _currentNote.update { note }
    }

    private fun setCurrentNoteIsNeverEdited(isNeverEdited: Boolean) {
        _currentNoteIsNeverEdited.update { isNeverEdited }
    }

    fun setQuery(query: String) {
        _searchQuery.update { query }
    }

    fun setNewEmptyNote() {
        /** Create a new empty note which has never been edited
         * - Create empty note and set it as current note
         * - Set current note as never edited (only gets set to true here)
         */
        viewModelScope.launch {
            val emptyNote = Note()
            setCurrentNote(note = emptyNote)
            setCurrentNoteIsNeverEdited(true)
        }
    }

    private fun insertNewOrUpdateNote(updatedNote: Note, neverEdited: Boolean) {
        /** If the updatedNote has never been edited (neverEdited = true), set it as edited and insert it into the database
         *  If the note has been edited before, update it in the database
         */
        setCurrentNote(updatedNote)
        if (neverEdited) {
            setCurrentNoteIsNeverEdited(false)
            viewModelScope.launch {
                val newId = noteRepository.insertNote(updatedNote)
                setCurrentNote(updatedNote.copy(id = newId))
            }
        } else {
            updateNote(updatedNote)
        }
    }

    fun updateCurrentNoteTitle(title: String) {
        /** Update the current note title and set it as current note
         * - If the note has never been edited, set it as edited and insert it into the database
         * - If the note has been edited, update it in the database
         */
        val updatedNote = _currentNote.value.copy(title = title)
        val neverEdited = _currentNoteIsNeverEdited.value
        insertNewOrUpdateNote(updatedNote, neverEdited)
    }

    fun updateCurrentNoteBody(body: String) {
        /** Update the current note body and set it as current note
         * - If the note has never been edited, set it as edited and insert it into the database
         * - If the note has been edited, update it in the database
         */
        val updatedNote = _currentNote.value.copy(body = body)
        val neverEdited = _currentNoteIsNeverEdited.value
        insertNewOrUpdateNote(updatedNote, neverEdited)
    }

    fun updateCurrentNoteIsPinned(isPinned: Boolean) {
        /** Update the current note isPinned and set it as current note
         * - If the note has never been edited, set it as edited and insert it into the database
         * - If the note has been edited, update it in the database
         */
        val updatedNote = _currentNote.value.copy(isPinned = isPinned)
        val neverEdited = _currentNoteIsNeverEdited.value
        insertNewOrUpdateNote(updatedNote, neverEdited)
    }

    fun longClickSelect(note: Note) {
        /*if (!_inSelectionMode.value) {
            _uiState.value = _uiState.value.copy(inSelectionMode = true)
        }*/
        if (!_selectedNotes.value.contains(note)) {
            _selectedNotes.update { it + note }
        }
    }

    fun shortClickSelect(note: Note, navController: NavController) {
        if (_inSelectionMode.value) {
            if (_selectedNotes.value.contains(note)) {
                _selectedNotes.update { it - note }
            } else {
                _selectedNotes.update { it + note }
            }
        } else {
            setCurrentNote(note)
            navController.navigate(NoteRoute)
        }
    }

    fun clearSelection() {
        _selectedNotes.update { emptyList() }
    }

    fun selectAllBinned() {
        _selectedNotes.update { _binList.value }
    }

    fun archiveSelectedNotes() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { note ->
                noteRepository.archiveNote(note)
            }
            clearSelection()
        }
    }

    fun unarchiveSelectedNotes() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { note ->
                noteRepository.unarchiveNote(note)
            }
            clearSelection()
        }
    }

    fun binSelectedNotes() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { note ->
                noteRepository.binNote(note)
            }
            clearSelection()
        }
    }

    fun restoreSelectedNotes() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { note ->
                noteRepository.restoreNote(note)
            }
            clearSelection()
        }
    }

    fun permanentlyDeleteSelection() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { note ->
                noteRepository.deleteNoteFromBin(note)
            }
            clearSelection()
        }
    }

    fun pinSelectedNotes() {
        viewModelScope.launch {
            selectedNotes.value.forEach { note ->
                noteRepository.updateNote(note.copy(isPinned = true))
            }
            clearSelection()
        }
    }

    fun unpinSelectedNotes() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { note ->
                noteRepository.updateNote(note.copy(isPinned = false))
            }
            clearSelection()
        }
    }

}