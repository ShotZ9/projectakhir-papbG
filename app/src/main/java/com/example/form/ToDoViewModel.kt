package com.example.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ToDoViewModel : ViewModel() {
    private val repository = ToDoRepository()

    private val _toDoList = MutableStateFlow<List<ToDoItem>>(emptyList())
    val toDoList: StateFlow<List<ToDoItem>> = _toDoList

    private var allToDoItems: List<ToDoItem> = emptyList() // Simpan semua item

    fun loadToDoItems() {
        viewModelScope.launch {
            allToDoItems = repository.getToDoItems() // Simpan semua item
            _toDoList.value = allToDoItems // Ubah state UI setelah data didapat
        }
    }

    fun addToDoItem(title: String, description: String) {
        val newItem = ToDoItem(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            isDone = false
        )
        viewModelScope.launch {
            repository.addToDoItem(newItem) // Operasi asynchronous menyimpan ke Firestore
            loadToDoItems() // Memuat ulang data setelah menambahkan item baru
        }
    }

    fun deleteToDoItem(id: String) {
        viewModelScope.launch {
            repository.deleteToDoItem(id)
            loadToDoItems()  // Memuat ulang data setelah menghapus item
        }
    }

    // Fungsi untuk memperbarui item To-Do
    fun updateToDoItem(id: String, newTitle: String, newDescription: String) {
        viewModelScope.launch {
            val updatedItem = repository.getToDoItemById(id)?.copy(
                title = newTitle,
                description = newDescription
            )
            if (updatedItem != null) {
                repository.updateToDoItem(updatedItem)  // Update item di repository
                loadToDoItems()  // Memuat ulang data setelah update
            }
        }
    }

    fun searchToDoItems(query: String) {
        viewModelScope.launch {
            _toDoList.value = repository.searchToDoItems(query) // Panggil fungsi pencarian
        }
    }

    fun toggleToDoStatus(itemId: String, isDone: Boolean) {
        repository.toggleToDoStatus(itemId, isDone) { isSuccessful ->
            if (isSuccessful) {
                _toDoList.value = _toDoList.value.map {
                    if (it.id == itemId) it.copy(isDone = isDone) else it
                }
            } else {
                // Handle error
            }
        }
    }
}