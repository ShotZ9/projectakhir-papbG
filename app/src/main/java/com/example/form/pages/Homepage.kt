package com.example.form.pages

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.form.AuthState
import com.example.form.AuthViewModel
import com.example.form.ToDoItem
import com.example.form.ToDoViewModel
import com.example.form.shared.Footer
import com.example.form.shared.Header
import java.util.Calendar

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    toDoViewModel: ToDoViewModel = viewModel()
) {
    val authState = authViewModel.authState.observeAsState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") } // State untuk menyimpan query pencarian
    var title by remember { mutableStateOf("") } // State untuk title
    var description by remember { mutableStateOf("") } // State untuk description

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.UnAuthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    val expiresAt = remember { authViewModel.getExpirationTime() }
    val formattedExpirationTime = if (expiresAt > System.currentTimeMillis()) {
        android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", java.util.Date(expiresAt)).toString()
    } else {
        "No active session or session expired"
    }

    val toDoList by toDoViewModel.toDoList.collectAsState()

    LaunchedEffect(Unit) {
        toDoViewModel.loadToDoItems()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { isDialogOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add To-Do")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp) // Provide space for the Sign Out button and Footer
            ) {
                item {
                    Header(title = "Home Page")

                    Text(
                        text = "Session Expires At: $formattedExpirationTime",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )

                    // TextField untuk pencarian
                    TextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            toDoViewModel.searchToDoItems(query) // Panggil fungsi pencarian
                        },
                        label = { Text("Search by Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(toDoList) { item ->
                    ToDoItemCard(
                        item = item,
                        onDelete = { toDoViewModel.deleteToDoItem(item.id) },
                        onUpdate = { newTitle, newDescription ->
                            toDoViewModel.updateToDoItem(item.id, newTitle, newDescription)
                        },
                        onToggleStatus = { isDone -> // Toggle status based on switch state
                            toDoViewModel.toggleToDoStatus(item.id, isDone)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp)) // Space before footer
                }
            }

                    // Sticky Column for Sign Out Button and Footer
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp) // Add padding for aesthetics
                    ) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(), // Fill the width
                            onClick = {
                                authViewModel.signout()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error // Set color to red (error)
                            )
                        ) {
                            Text(text = "Sign Out", color = MaterialTheme.colorScheme.error) // Optionally set text color as well
                        }

                        // Sticky Footer
                        Footer(
                            modifier = Modifier.fillMaxWidth() // Fill the width
                        )
                    }
                }

        // Dialog untuk menambahkan To-Do
        if (isDialogOpen) {
            AddToDoDialog(
                onDismiss = {
                    isDialogOpen = false
                    title = ""
                    description = ""
                },
                onAdd = { newTitle, newDescription ->
                    if (newTitle.isNotEmpty() && newDescription.isNotEmpty()) {
                        toDoViewModel.addToDoItem(newTitle, newDescription)
                        isDialogOpen = false
                        title = ""
                        description = ""
                    }
                },
                title = title,
                onTitleChange = { title = it },
                description = description,
                onDescriptionChange = { description = it },
                toDoList = toDoList // Pass toDoList for duplicate checking
            )
        }
    }
}

@Composable
fun AddToDoDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    toDoList: List<ToDoItem> // Keep this for duplicate checking
) {
    // State untuk pesan kesalahan
    var titleError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    var duplicateError by remember { mutableStateOf(false) } // State untuk error duplikasi
//    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) } // State for selected date
//    var showDatePicker by remember { mutableStateOf(false) } // State to show date picker

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Validasi input sebelum menambahkan To-Do
                titleError = title.isEmpty()
                descriptionError = description.isEmpty()
                duplicateError = toDoList.any { it.title == title } // Periksa duplikasi

                if (!titleError && !descriptionError && !duplicateError) {
                    onAdd(title, description)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add New To-Do") },
        text = {
            Column {
                // TextField untuk Title
                TextField(
                    value = title,
                    onValueChange = {
                        onTitleChange(it)
                        titleError = it.isEmpty() // Hapus pesan kesalahan saat pengguna mengetik
                        duplicateError = toDoList.any { item -> item.title == it } // Periksa duplikasi saat mengetik
                    },
                    label = { Text("Title") },
                    placeholder = { Text("Enter title here") },
                    isError = titleError || duplicateError // Tampilkan indikator error jika input kosong atau duplikat
                )
                if (titleError) {
                    // Tampilkan pesan kesalahan untuk Title
                    Text(
                        text = "Title cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (duplicateError) {
                    // Tampilkan pesan kesalahan untuk duplikasi
                    Text(
                        text = "Title already exists",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // TextField untuk Description
                TextField(
                    value = description,
                    onValueChange = {
                        onDescriptionChange(it)
                        descriptionError = it.isEmpty() // Hapus pesan kesalahan saat pengguna mengetik
                    },
                    label = { Text("Description") },
                    placeholder = { Text("Enter description here") },
                    isError = descriptionError // Tampilkan indikator error jika input kosong
                )
                if (descriptionError) {
                    // Tampilkan pesan kesalahan untuk Description
                    Text(
                        text = "Description cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

//                // Date Picker Button
//                TextButton(onClick = {
//                    showDatePicker = true // Show the date picker when the button is clicked
//                }) {
//                    // Format date to display only year, month, and day
//                    val formattedDate = android.text.format.DateFormat.format("yyyy-MM-dd", Date(selectedDate))
//                    Text("Select Date: $formattedDate")
//                }
//
//                // Show date picker if showDatePicker is true
//                if (showDatePicker) {
//                    DatePicker(
//                        initialDate = selectedDate,
//                        onDateSelected = { selectedDate = it }, // Update the selected date
//                        onDismiss = { showDatePicker = false } // Close the date picker
//                    )
//                }
            }
        }
    )
}

@Composable
fun DatePicker(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialDate
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                // Reset time components to 0
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                onDateSelected(calendar.timeInMillis) // Pass the selected date
                onDismiss() // Dismiss the date picker
            },
            year, month, day
        ).show()
    }
}

@Composable
fun DeleteConfirmationDialog(
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Confirmation") },
        text = { Text("Are you sure you want to delete this item?") },
        confirmButton = {
            TextButton(onClick = {
                onDelete()
                onDismiss()
            }) {
                Text("Confirm", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ToDoItemCard(
    item: ToDoItem,
    onDelete: () -> Unit,
    onUpdate: (String, String) -> Unit,
    onToggleStatus: (Boolean) -> Unit
) {
    var isUpdateDialogOpen by remember { mutableStateOf(false) }
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    var updatedTitle by remember { mutableStateOf(item.title) }
    var updatedDescription by remember { mutableStateOf(item.description) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row untuk judul, deskripsi, dan tombol Update/Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Mengatur jarak antara kiri dan kanan
                verticalAlignment = Alignment.CenterVertically // Menempatkan item secara vertikal sejajar di tengah
            ) {
                // Column untuk menampilkan judul dan deskripsi di kiri
                Column(
                    modifier = Modifier.weight(1f) // Memberikan lebih banyak ruang ke kiri
                ) {
                    Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                    Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
                    Text(text = if (item.isDone) "Status : Done" else "Status : Pending", style = MaterialTheme.typography.bodySmall)
                }
                Text(text = "Status ")
                Switch(
                        checked = item.isDone,
                onCheckedChange = { isDone ->
                    onToggleStatus(isDone)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onBackground
                )
                )

                // Column untuk tombol Update dan Delete di kanan
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(onClick = { isUpdateDialogOpen = true }) {
                        Text(text = "Update", color = MaterialTheme.colorScheme.primary)
                    }

                    TextButton(onClick = { isDeleteDialogOpen = true }) {
                        Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            // Dialog for updating the To-Do item
            if (isUpdateDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isUpdateDialogOpen = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if (updatedTitle.isNotEmpty() && updatedDescription.isNotEmpty()) {
                                onUpdate(updatedTitle, updatedDescription)
                                isUpdateDialogOpen = false
                            }
                        }) {
                            Text("Update")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isUpdateDialogOpen = false }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Update To-Do") },
                    text = {
                        Column {
                            TextField(
                                value = updatedTitle,
                                onValueChange = { updatedTitle = it },
                                label = { Text("Title") },
                                placeholder = { Text("Enter new title") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = updatedDescription,
                                onValueChange = { updatedDescription = it },
                                label = { Text("Description") },
                                placeholder = { Text("Enter new description") }
                            )
                        }
                    }
                )
            }
            if (isDeleteDialogOpen) {
                DeleteConfirmationDialog(
                    onDelete = onDelete,
                    onDismiss = { isDeleteDialogOpen = false }
                )
            }
        }
    }
}