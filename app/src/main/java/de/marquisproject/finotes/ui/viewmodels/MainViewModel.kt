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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    // Expose screen UI state
    private val _uiState = MutableStateFlow(MainUiState())
    private val _searchQuery = MutableStateFlow("")
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _notesList = _searchQuery
        .flatMapLatest { searchQuery ->
            if (searchQuery.isBlank()) {
                noteRepository.getAllNotes()
            } else {
                noteRepository.searchNotes(searchQuery)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _archivedList = noteRepository.getAllArchivedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _binList = noteRepository.getAllDeletedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val uiState = combine(_uiState, _notesList, _searchQuery, _archivedList, _binList) {
        uiState, notesList, searchQuery, archivedList, binList ->
        uiState.copy(
            notesList = notesList,
            searchQuery = searchQuery,
            archivedList = archivedList,
            binList = binList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())


    private fun updateNote(note: Note) {
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    fun insertAndShowNewEmptyNote() {
        viewModelScope.launch {
            val emptyNote = Note()
            val newId = noteRepository.insertNote(emptyNote)
            setCurrentNote(emptyNote.copy(id = newId))
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
        //viewmodel scope because we use suspend functions
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
        //viewmodel scope because we use suspend functions
        viewModelScope.launch {
            noteRepository.deleteNoteFromBin(note)
        }
    }

    private fun setCurrentNote(note: Note) {
        _uiState.value = _uiState.value.copy(currentNote = note)
    }

    fun updateCurrentNoteTitle(title: String) {
        _uiState.value = _uiState.value.copy(currentNote = _uiState.value.currentNote.copy(title = title))
        updateNote(_uiState.value.currentNote)
    }

    fun updateCurrentNoteBody(body: String) {
        _uiState.value = _uiState.value.copy(currentNote = _uiState.value.currentNote.copy(body = body))
        updateNote(_uiState.value.currentNote)
    }

    fun updateCurrentNoteIsPinned(isPinned: Boolean) {
        _uiState.value = _uiState.value.copy(currentNote = _uiState.value.currentNote.copy(isPinned = isPinned))
        updateNote(_uiState.value.currentNote)
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    fun longClickSelect(note: Note) {
        if (!_uiState.value.inSelectionMode) {
            _uiState.value = _uiState.value.copy(inSelectionMode = true)
        }
        val selectedNotes = _uiState.value.selectedNotes.toMutableList()
        if (!selectedNotes.contains(note)) {
            selectedNotes.add(note)
        }
        _uiState.value = _uiState.value.copy(selectedNotes = selectedNotes)
    }

    fun shortClickSelect(note: Note, navController: NavController) {
        if(_uiState.value.inSelectionMode) {
            val selectedNotes = _uiState.value.selectedNotes.toMutableList()
            if (selectedNotes.contains(note)) {
                selectedNotes.remove(note)
                if (selectedNotes.isEmpty()) {
                    _uiState.value = _uiState.value.copy(inSelectionMode = false)
                }
            } else {
                selectedNotes.add(note)
            }
            _uiState.value = _uiState.value.copy(selectedNotes = selectedNotes)
        } else {
            setCurrentNote(note)
            navController.navigate(NoteRoute)
        }
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedNotes = emptyList(), inSelectionMode = false)
    }

    fun selectAllBinned() {
        _uiState.value = _uiState.value.copy(selectedNotes = _binList.value, inSelectionMode = true)
    }

    fun archiveSelectedNotes() {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.archiveNote(note)
            }
            clearSelection()
        }
    }

    fun unarchiveSelectedNotes() {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.unarchiveNote(note)
            }
            clearSelection()
        }
    }

    fun binSelectedNotes() {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.binNote(note)
            }
            clearSelection()
        }
    }

    fun restoreSelectedNotes() {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.restoreNote(note)
            }
            clearSelection()
        }
    }

    fun permanentlyDeleteSelection() {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.deleteNoteFromBin(note)
            }
            clearSelection()
        }
    }

    fun pinSelectedNotes() {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.updateNote(note.copy(isPinned = true))
            }
            clearSelection()
        }
    }

    fun unpinSelectedNotes() {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.updateNote(note.copy(isPinned = false))
            }
            clearSelection()
        }
    }

}