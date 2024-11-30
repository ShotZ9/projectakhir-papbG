package com.example.form

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ToDoRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addToDoItem(item: ToDoItem) {
        db.collection("todos")
            .document(item.id)
            .set(item)
            .await() // Menyimpan ke Firestore dengan coroutines
    }

    suspend fun getToDoItems(): List<ToDoItem> {
        val result = db.collection("todos").get().await()
        return result.documents.mapNotNull { doc ->
            doc.toObject(ToDoItem::class.java)
        }
    }

    // Fungsi untuk menghapus item To-Do berdasarkan ID dari Firestore
    suspend fun deleteToDoItem(id: String) {
        db.collection("todos")
            .document(id)
            .delete()
            .await() // Menghapus dari Firestore dengan coroutines
    }

    // Fungsi untuk mendapatkan item To-Do berdasarkan ID dari Firestore
    suspend fun getToDoItemById(id: String): ToDoItem? {
        val document = db.collection("todos")
            .document(id)
            .get()
            .await()
        return document.toObject(ToDoItem::class.java)
    }

    // Fungsi untuk memperbarui item To-Do yang ada di Firestore
    suspend fun updateToDoItem(updatedItem: ToDoItem) {
        db.collection("todos")
            .document(updatedItem.id)
            .set(updatedItem)
            .await() // Memperbarui item di Firestore dengan coroutines
    }

    // Fungsi baru untuk mencari item To-Do berdasarkan judul
    suspend fun searchToDoItems(query: String): List<ToDoItem> {
        val allItems = getToDoItems()
        return allItems.filter { it.title.contains(query, ignoreCase = true) }
    }

    fun toggleToDoStatus(itemId: String, isDone: Boolean, onComplete: (Boolean) -> Unit) {
        val updatedStatus = if (isDone) true else false
        val taskRef = db.collection("todos").document(itemId)

        taskRef.update("done", updatedStatus)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
    private val auth = FirebaseAuth.getInstance()
    suspend fun signup(email: String, password: String, nama: String, nim: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
        val userId = auth.currentUser?.uid ?: throw Exception("User ID not found")
        val user = mapOf(
            "email" to email,
            "nama" to nama,
            "nim" to nim
        )
        db.collection("users").document(userId).set(user).await()
    }
}