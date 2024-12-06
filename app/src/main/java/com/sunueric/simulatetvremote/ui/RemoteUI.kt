package com.sunueric.simulatetvremote.ui

import android.app.Activity
import android.content.Context.TV_INPUT_SERVICE
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.media.tv.TvView
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.sunueric.simulatetvremote.MainActivity


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TestPassThrough() {
    val context = LocalContext.current
    val tvInputManager = context.getSystemService(TV_INPUT_SERVICE) as TvInputManager
    val tvInputList = tvInputManager.tvInputList

    // Define a data class for input name and ID
    data class Input(val name: String, val id: String)

    // Create a list of inputs with name and ID
    val inputList = remember {
        tvInputList.map { inputId ->
            val inputInfo = tvInputManager.getTvInputInfo(inputId.id)
            val name = inputInfo?.loadLabel(context)?.toString() ?: "Unknown ($inputId)"
            Input(name, inputId.id)
        }
    }

    // Duration options and their corresponding values
    val durationOptions = listOf(
        "Indefinite",
        "10 seconds",
        "30 seconds",
        "1 minute",
        "5 minutes"
    )
    val durationValues = listOf(
        -1L, // Indefinite
        10000L, // 10 seconds
        30000L, // 30 seconds
        60000L, // 1 minute
        300000L // 5 minutes
    )

    // State variables for selected input and duration
    var selectedInputIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedDurationIndex by rememberSaveable { mutableIntStateOf(0) }
    var inputMenuExpanded by remember { mutableStateOf(false) }
    var durationMenuExpanded by remember { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val returnIntent = Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    // ActivityResultLauncher for launching the TV input activity
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle activity result if needed
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Display selected input and duration
        Text(text = "Selected Input: ${inputList.getOrNull(selectedInputIndex)?.name ?: "N/A"}")
        Text(text = "Selected Duration: ${durationOptions.getOrElse(selectedDurationIndex) { "N/A" }}")

        // Input Dropdown
        DropdownMenuWithNoKeyboard(
            label = "Select Input",
            options = inputList.map { it.name }, // Transform the Input list to a list of names
            selectedIndex = selectedInputIndex,
            onSelect = { selectedInputIndex = it },
            expanded = inputMenuExpanded,
            onExpandedChange = { inputMenuExpanded = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Duration Dropdown
        DropdownMenuWithNoKeyboard(
            label = "Select Duration",
            options = durationOptions,
            selectedIndex = selectedDurationIndex,
            onSelect = { selectedDurationIndex = it },
            expanded = durationMenuExpanded,
            onExpandedChange = { durationMenuExpanded = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // "Show" Button
        Button(
            onClick = {
                handler.removeCallbacksAndMessages(null)
                val selectedInput = inputList.getOrNull(selectedInputIndex)
                selectedInput?.id?.let { inputId ->
                    val uri = TvContract.buildChannelUriForPassthroughInput(inputId)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        if (durationValues[selectedDurationIndex] == -1L) {
                            context.startActivity(intent)
                        } else {
                            launcher.launch(intent)
                            handler.postDelayed({
                                context.startActivity(returnIntent)
                            }, durationValues[selectedDurationIndex])
                        }
                    } else {
                        Log.e("TVViewWithActivityResult", "No app can handle the intent for $uri")
                    }
                } ?: run {
                    Log.e("TVViewWithActivityResult", "Input ID is null for selected input.")
                }
            },
            modifier = Modifier
                .padding(30.dp)
                .fillMaxWidth()
        ) {
            Text("Show")
        }
    }
}

@Composable
fun DropdownMenuWithNoKeyboard(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val shape  = RoundedCornerShape(24.dp) // Define a shape with increased corner radius

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(shape)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = shape
            )
            .border(width = 1.dp, shape = shape, color = MaterialTheme.colorScheme.onSurface)
            .clickable { onExpandedChange(true) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )  {
        Text(
            text = options.getOrNull(selectedIndex) ?: label,
            color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
        )
        DropdownMenu(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = shape
                ),
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    onClick = {
                        onSelect(index)
                        onExpandedChange(false)
                    }
                ) {
                    Text(option)
                }
            }
        }
    }
}
@Preview
@Composable
fun DropdownMenuWithNoKeyboardPreview() {
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedIndex by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    DropdownMenuWithNoKeyboard(
        label = "Select an option",
        options = options,
        selectedIndex = selectedIndex,
        onSelect = { index -> selectedIndex = index },
        expanded = expanded,
        onExpandedChange = { expanded = it }
    )
}
@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
fun TestPassThroughPreview() {
    TestPassThrough()
}