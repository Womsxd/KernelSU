@file:OptIn(ExperimentalMaterial3Api::class)

package me.weishu.kernelsu.ui.component.profile

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.profile.Capabilities
import me.weishu.kernelsu.profile.Groups

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootProfileConfig(
    modifier: Modifier = Modifier,
    fixedName: Boolean,
    profile: Natives.Profile,
    onProfileChange: (Natives.Profile) -> Unit,
) {
    Column(modifier = modifier) {
        if (!fixedName) {
            OutlinedTextField(
                label = { Text(stringResource(R.string.profile_name)) },
                value = profile.name,
                onValueChange = { onProfileChange(profile.copy(name = it)) }
            )
        }

        var expanded by remember { mutableStateOf(false) }
        val currentNamespace = when (profile.namespace) {
            Natives.Profile.Namespace.Inherited.ordinal -> stringResource(R.string.profile_namespace_inherited)
            Natives.Profile.Namespace.Global.ordinal -> stringResource(R.string.profile_namespace_global)
            Natives.Profile.Namespace.Individual.ordinal -> stringResource(R.string.profile_namespace_individual)
            else -> stringResource(R.string.profile_namespace_inherited)
        }
        ListItem(headlineContent = {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    label = { Text(stringResource(R.string.profile_namespace)) },
                    value = currentNamespace,
                    onValueChange = {},
                    trailingIcon = {
                        if (expanded) Icon(Icons.Filled.ArrowDropUp, null)
                        else Icon(Icons.Filled.ArrowDropDown, null)
                    },
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.profile_namespace_inherited)) },
                        onClick = {
                            onProfileChange(profile.copy(namespace = Natives.Profile.Namespace.Inherited.ordinal))
                            expanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.profile_namespace_global)) },
                        onClick = {
                            onProfileChange(profile.copy(namespace = Natives.Profile.Namespace.Global.ordinal))
                            expanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.profile_namespace_individual)) },
                        onClick = {
                            onProfileChange(profile.copy(namespace = Natives.Profile.Namespace.Individual.ordinal))
                            expanded = false
                        },
                    )
                }
            }
        })

        ListItem(headlineContent = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("uid") },
                value = profile.uid.toString(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = {
                    if (it.isNotEmpty()) {
                        it.filter { symbol ->
                            symbol.isDigit()
                        }.let { filtered ->
                            filtered.ifEmpty { "0" }
                        }.let { value ->
                            onProfileChange(
                                profile.copy(
                                    uid = value.toInt(),
                                    rootUseDefault = false
                                )
                            )
                        }
                    }
                }

            )
        })

        ListItem(headlineContent = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("gid") },
                value = profile.gid.toString(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = {
                    if (it.isNotEmpty()) {
                        it.filter { symbol ->
                            symbol.isDigit()
                        }.let { filtered ->
                            filtered.ifEmpty { "0" }
                        }.let { value ->
                            onProfileChange(
                                profile.copy(
                                    gid = value.toInt(),
                                    rootUseDefault = false
                                )
                            )
                        }
                    }
                }
            )
        })

        val selectedGroups = profile.groups.ifEmpty { listOf(0) }.let { e ->
            e.mapNotNull { g ->
                Groups.values().find { it.gid == g }
            }
        }
        GroupsPanel(selectedGroups) {
            onProfileChange(
                profile.copy(
                    groups = it.map { group -> group.gid }.ifEmpty { listOf(0) },
                    rootUseDefault = false
                )
            )
        }

        val selectedCaps = profile.capabilities.ifEmpty {
            Capabilities.values().toList()
        }.let { e ->
            e.mapNotNull { cap ->
                Capabilities.values().find { it.cap == cap }
            }
        }
        CapsPanel(selectedCaps) {
            onProfileChange(
                profile.copy(
                    capabilities = it.map { cap -> cap.cap },
                    rootUseDefault = false
                )
            )
        }

        ListItem(headlineContent = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("SELinux context") },
                value = profile.context,
                onValueChange = {
                    onProfileChange(profile.copy(context = it, rootUseDefault = false))
                }
            )
        })


    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupsPanel(selected: List<Groups>, closeSelection: (selection: List<Groups>) -> Unit) {

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val groups = Groups.values()
        val options = groups.map { value ->
            ListOption(
                titleText = value.display,
                selected = selected.contains(value),
            )
        }

        val selection = mutableListOf<Groups>()
        ListDialog(
            state = rememberUseCaseState(visible = true, onFinishedRequest = {
                Log.i("mylog", "onFinishedRequest")
                closeSelection(selection)
            }, onCloseRequest = {
                showDialog = false
                Log.i("mylog", "onCloseRequest")
            }),
            selection = ListSelection.Multiple(
                showCheckBoxes = true,
                options = options
            ) { indecies, _ ->
                // Handle selection
                indecies.forEach { index ->
                    val group = groups[index]
                    selection.add(group)
                }
            }
        )
    }

    OutlinedCard(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .clickable {
            showDialog = true
        }) {

        Column(modifier = Modifier.padding(16.dp)) {
            Text("groups")
            FlowRow {
                selected.forEach { group ->
                    AssistChip(
                        modifier = Modifier.padding(3.dp),
                        onClick = { /*TODO*/ },
                        label = { Text(group.display) })
                }
            }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CapsPanel(
    selected: List<Capabilities>,
    closeSelection: (selection: List<Capabilities>) -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val caps = Capabilities.values()
        val options = caps.map { value ->
            ListOption(
                titleText = value.display,
                selected = selected.contains(value),
            )
        }

        val selection = mutableListOf<Capabilities>()
        ListDialog(
            state = rememberUseCaseState(visible = true, onFinishedRequest = {
                closeSelection(selection)
            }, onCloseRequest = {
                showDialog = false
            }),
            selection = ListSelection.Multiple(
                showCheckBoxes = true,
                options = options
            ) { indecies, _ ->
                // Handle selection
                indecies.forEach { index ->
                    val group = caps[index]
                    selection.add(group)
                }
            }
        )
    }

    OutlinedCard(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .clickable {
            showDialog = true
        }) {

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Capabilities")
            FlowRow {
                selected.forEach { group ->
                    AssistChip(
                        modifier = Modifier.padding(3.dp),
                        onClick = { /*TODO*/ },
                        label = { Text(group.display) })
                }
            }
        }

    }
}

@Preview
@Composable
private fun RootProfileConfigPreview() {
    var profile by remember { mutableStateOf(Natives.Profile("")) }
    RootProfileConfig(fixedName = true, profile = profile) {
        profile = it
    }
}
