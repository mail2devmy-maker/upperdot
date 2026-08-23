package com.mail2dev.upperdot.ui.relationship_hierarchy

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipHierarchyScreen(
    onNavigateBack: () -> Unit,
    viewModel: RelationshipHierarchyViewModel
) {
    val groups by viewModel.groups.collectAsState()
    val expandedGroups by viewModel.expandedGroups.collectAsState()

    var showAddTagDialog by remember { mutableStateOf<String?>(null) }
    var showRenameGroupDialog by remember { mutableStateOf<String?>(null) }
    var showRenameTagDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var tempName by remember { mutableStateOf("") }

    if (showAddTagDialog != null) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = null },
            title = { Text("Add New Tag", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    placeholder = { Text("Tag Name", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onAddTag(showAddTagDialog!!, tempName)
                    showAddTagDialog = null
                    tempName = ""
                }) { Text("Add", color = AccentCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = null; tempName = "" }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Surface
        )
    }

    if (showRenameGroupDialog != null) {
        AlertDialog(
            onDismissRequest = { showRenameGroupDialog = null },
            title = { Text("Rename Group", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    placeholder = { Text("New Group Name", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onRenameGroup(showRenameGroupDialog!!, tempName)
                    showRenameGroupDialog = null
                    tempName = ""
                }) { Text("Rename", color = AccentCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameGroupDialog = null; tempName = "" }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Surface
        )
    }

    if (showRenameTagDialog != null) {
        AlertDialog(
            onDismissRequest = { showRenameTagDialog = null },
            title = { Text("Rename Tag", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    placeholder = { Text("New Tag Name", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onRenameTag(showRenameTagDialog!!.first, showRenameTagDialog!!.second, tempName)
                    showRenameTagDialog = null
                    tempName = ""
                }) { Text("Rename", color = AccentCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameTagDialog = null; tempName = "" }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Surface
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Contact Groups", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Organize your contacts", fontSize = 11.sp, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            items(groups, key = { it.id }) { group ->
                HierarchyGroupItem(
                    group = group,
                    isExpanded = expandedGroups.contains(group.id),
                    onToggle = { viewModel.toggleGroupExpansion(group.id) },
                    onAddTag = {
                        tempName = ""
                        showAddTagDialog = group.id
                    },
                    onRename = {
                        tempName = group.name
                        showRenameGroupDialog = group.id
                    },
                    onDelete = { viewModel.onDeleteGroup(group.id) },
                    onRenameTag = { tagId, tagName ->
                        tempName = tagName
                        showRenameTagDialog = group.id to tagId
                    },
                    onDeleteTag = { tagId ->
                        viewModel.onDeleteTag(group.id, tagId)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun HierarchyGroupItem(
    group: HierarchyGroup,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddTag: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTag: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Expand Arrow
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Title + Count Pill Row
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = group.name,
                        color = Color.White,
                        fontSize = 15.sp, // Reduced from 18.sp for a sleeker look
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccentCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = String.format("%,d", group.contactCount),
                            color = AccentCyan,
                            fontSize = 10.sp, // Sleek, clean badge text
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 3. Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onAddTag,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            contentDescription = "Add Tag",
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onRename,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = NegativeRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp, start = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.tags.forEach { tag ->
                    HierarchyTagItem(
                        tag = tag,
                        onRename = { onRenameTag(tag.id, tag.name) },
                        onDelete = { onDeleteTag(tag.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HierarchyTagItem(
    tag: HierarchyTag,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Label,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = tag.name,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Row {
                IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NegativeRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}