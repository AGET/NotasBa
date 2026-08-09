package com.aget.notesba.presentation.notes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aget.notesba.domain.model.AttachmentType
import java.io.File

@Composable
fun AttachmentPreview(
    path: String,
    type: AttachmentType?
) {
    when (type) {

        AttachmentType.IMAGE,
        AttachmentType.DRAWING -> {

            AsyncImage(
                model = File(path),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(
                        RoundedCornerShape(22.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        }

        AttachmentType.FILE -> {

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color =
                    MaterialTheme.colorScheme
                        .secondaryContainer
            ) {

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.AttachFile,
                        contentDescription = null
                    )

                    Text(
                        text = File(path).name,
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        null -> Unit
    }
}
