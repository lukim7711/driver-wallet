package com.driverwallet.app.feature.input.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NoteInput(
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Tambah catatan...") },
        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
        singleLine = true,
    )
}
