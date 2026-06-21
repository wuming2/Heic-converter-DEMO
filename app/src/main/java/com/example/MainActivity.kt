package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAnalyticsHelper.init(this)
        FirebaseAnalyticsHelper.logAppOpen()
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ConverterScreen(
                        modifier = Modifier.padding(innerPadding),
                        activityContext = this
                    )
                }
            }
        }
    }
}

// Helper extension for drawing clean minimalist dashed borders
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp = 12.dp,
    strokeWidth: Dp = 1.5.dp
): Modifier = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

@Composable
fun ConverterScreen(
    modifier: Modifier = Modifier,
    activityContext: Context,
    viewModel: HeicConverterViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedImages by viewModel.selectedImages.collectAsState()
    val outputFormat by viewModel.outputFormat.collectAsState()
    val compressQuality by viewModel.compressQuality.collectAsState()
    val preserveMetadata by viewModel.preserveMetadata.collectAsState()
    val customSavePathUri by viewModel.customSavePathUri.collectAsState()
    val customSavePathName by viewModel.customSavePathName.collectAsState()
    val isSettingsExpanded by viewModel.isSettingsExpanded.collectAsState()
    val isConverting by viewModel.isConverting.collectAsState()
    val logText by viewModel.logText.collectAsState()
    val filterOnlyHeic by viewModel.filterOnlyHeic.collectAsState()

    // Activity launcher for choosing multiple visual files
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = PickMultipleImagesContract()
    ) { uris ->
        if (uris != null && uris.isNotEmpty()) {
            viewModel.addSelectedImages(context, uris)
        }
    }

    // Activity launcher for selecting custom directory SAF
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val documentFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)
            val name = documentFile?.name ?: context.getString(R.string.picker_custom_folder)
            viewModel.setCustomSavePath(context, uri, name)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC)) // Light gray-blue background
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFD3E4FF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFF001D36),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = context.getString(R.string.app_name),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E),
                        modifier = Modifier.testTag("app_title")
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = context.getString(R.string.offline_processing),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1C1E).copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // More options menu
            Box {
                var menuExpanded by remember { mutableStateOf(false) }

                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .testTag("more_options_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = context.getString(R.string.menu_more),
                        tint = Color(0xFF1A1C1E)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = context.getString(R.string.menu_privacy_policy),
                                color = Color(0xFF1A1C1E),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            val intent = Intent(activityContext, PrivacyPolicyActivity::class.java)
                            activityContext.startActivity(intent)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = context.getString(R.string.menu_web_version),
                                color = Color(0xFF1A1C1E),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                            activityContext.startActivity(intent)
                        }
                    )
                }
            }
        }

        // Large Unified Center Selection / Queue Card
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color(0xFFE1E2E6))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (selectedImages.isEmpty()) {
                    // Empty visual state matching mockup HTML
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("empty_picker_card")
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Single elegant card representing files empty state
                        Box(
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .size(90.dp)
                                .background(Color(0xFFF1F3F4), RoundedCornerShape(16.dp))
                                .dashedBorder(Color(0xFFC4C7C5), 16.dp, 1.5.dp)
                                .clickable { imagePickerLauncher.launch(if (filterOnlyHeic) "heic_heif" else "image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF004A77),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = context.getString(R.string.tap_select_heic),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1C1E),
                            modifier = Modifier.clickable { imagePickerLauncher.launch(if (filterOnlyHeic) "heic_heif" else "image/*") }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = context.getString(R.string.batch_support_desc),
                            fontSize = 11.sp,
                            color = Color(0xFF747775)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch(if (filterOnlyHeic) "heic_heif" else "image/*") },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF004A77)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF004A77)),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(context.getString(R.string.select_images_now), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setFilterOnlyHeic(!filterOnlyHeic) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .background(Color(0xFFF1F3F4).copy(alpha = 0.5f))
                        ) {
                            Checkbox(
                                checked = filterOnlyHeic,
                                onCheckedChange = { viewModel.setFilterOnlyHeic(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.outline,
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.size(24.dp).testTag("filter_heic_checkbox")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = context.getString(R.string.filter_only_heic),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1C1E)
                                )
                                Text(
                                    text = context.getString(R.string.filter_only_heic_desc),
                                    fontSize = 10.sp,
                                    color = Color(0xFF747775),
                                    lineHeight = 13.sp,
                                    modifier = Modifier.widthIn(max = 240.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Queue state - nicely in the same card frame!
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = context.getString(R.string.queue_title, selectedImages.size),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF444746)
                            )
                            Text(
                                text = context.getString(R.string.add_more),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF004A77),
                                modifier = Modifier
                                    .clickable { imagePickerLauncher.launch(if (filterOnlyHeic) "heic_heif" else "image/*") }
                                    .testTag("add_more_button")
                            )
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(selectedImages) { index, item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("image_item_$index"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF7F9FC)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE1E2E6).copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1A1C1E),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${formatFileSize(item.size)} • HEIC",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF747775)
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                when (item.status) {
                                                    ConversionStatus.PENDING -> {
                                                        IconButton(
                                                            onClick = { viewModel.removeImage(context, index) },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = context.getString(R.string.remove),
                                                                tint = Color(0xFF747775),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                    ConversionStatus.CONVERTING -> {
                                                        Text(
                                                            text = context.getString(R.string.processing_percent, (item.progress * 100).toInt()),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF004A77)
                                                        )
                                                    }
                                                    ConversionStatus.SUCCESS -> {
                                                        Surface(
                                                            color = Color(0xFFDCFCE7),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = null,
                                                                    tint = Color(0xFF16A34A),
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(3.dp))
                                                                Text(
                                                                    text = item.outputFormat,
                                                                    fontSize = 10.sp,
                                                                    color = Color(0xFF15803D),
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }

                                                        IconButton(
                                                            onClick = { shareSingleImage(context, item) },
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .testTag("share_item_$index")
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Share,
                                                                contentDescription = context.getString(R.string.share),
                                                                tint = Color(0xFF004A77),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                    ConversionStatus.FAILED -> {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Warning,
                                                                contentDescription = null,
                                                                tint = Color.Red,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = context.getString(R.string.failed),
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.Red
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (item.status == ConversionStatus.CONVERTING) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { item.progress },
                                                modifier = Modifier.fillMaxWidth(),
                                                color = Color(0xFF004A77),
                                                trackColor = Color(0xFFE1E2E6)
                                            )
                                        } else if (item.status == ConversionStatus.FAILED && item.errorMsg != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.errorMsg,
                                                color = Color.Red,
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Format Selection & Quality Bar at the bottom of the Card (Unified HTML representation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F9FC))
                        .border(BorderStroke(0.5.dp, Color(0xFFE1E2E6).copy(alpha = 0.7f)))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = context.getString(R.string.export_format),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF444746),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (outputFormat == "JPEG") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.setOutputFormat("JPEG") }
                                    .testTag("radio_jpeg")
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "JPEG",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (outputFormat == "JPEG") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (outputFormat == "PNG") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.setOutputFormat("PNG") }
                                    .testTag("radio_png")
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "PNG",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (outputFormat == "PNG") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(Color(0xFFE1E2E6))
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = context.getString(R.string.quality),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF444746),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (outputFormat == "JPEG") "${compressQuality}%" else context.getString(R.string.lossless),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004A77)
                        )
                    }
                }
            }
        }

        // Core Metadata Controls Section
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Option 1: Preserve original EXIF metadata
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setPreserveMetadata(!preserveMetadata) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, Color(0xFFE1E2E6).copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE8DEF8), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF1D192B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = context.getString(R.string.preserve_metadata),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1C1E)
                            )
                            Text(
                                text = context.getString(R.string.preserve_metadata_desc),
                                fontSize = 11.sp,
                                color = Color(0xFF747775)
                            )
                        }
                    }

                    Switch(
                        checked = preserveMetadata,
                        onCheckedChange = { viewModel.setPreserveMetadata(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF004A77)
                        ),
                        modifier = Modifier.testTag("switch_preserve_metadata")
                    )
                }
            }
        }

        // Collapsible Advanced Settings (Disclosed smoothly upon button click)
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .clickable { viewModel.toggleSettingsExpanded() }
                        .testTag("settings_expanded_toggle")
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isSettingsExpanded) {
                            context.getString(R.string.hide_advanced)
                        } else {
                            context.getString(R.string.show_advanced)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF747775)
                    )
                }

                AnimatedVisibility(visible = isSettingsExpanded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, Color(0xFFE1E2E6).copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Advanced section Title: Saved destination
                            Column {
                                Text(
                                    text = context.getString(R.string.custom_save_dir),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF444746)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = customSavePathName ?: context.getString(R.string.default_sandbox_path),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (customSavePathUri != null) Color(0xFF004A77) else Color(0xFF747775),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (customSavePathUri != null) {
                                            IconButton(
                                                onClick = { viewModel.setCustomSavePath(context, null, null) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = context.getString(R.string.clear_custom_path),
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = { folderPickerLauncher.launch(null) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFD3E4FF),
                                                contentColor = Color(0xFF001D36)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .height(32.dp)
                                                .testTag("pick_folder_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(context.getString(R.string.choose_directory), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // Quality slider detail (Advanced view)
                            val isQualityEnabled = outputFormat == "JPEG"
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = context.getString(R.string.adjust_quality),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isQualityEnabled) Color(0xFF444746) else Color(0xFF747775).copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "${compressQuality}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isQualityEnabled) Color(0xFF004A77) else Color(0xFF747775).copy(alpha = 0.5f)
                                    )
                                }
                                Slider(
                                    value = compressQuality.toFloat(),
                                    onValueChange = { viewModel.setCompressQuality(it.toInt()) },
                                    valueRange = 10f..100f,
                                    enabled = isQualityEnabled,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("quality_slider"),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF004A77),
                                        activeTrackColor = Color(0xFF004A77),
                                        inactiveTrackColor = Color(0xFFE1E2E6)
                                    )
                                )
                            }

                            // Dynamic Live Log Console Terminal
                            if (logText.isNotEmpty()) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = context.getString(R.string.log_console),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF747775)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        val scrollState = rememberScrollState()
                                        LaunchedEffect(logText) {
                                            scrollState.animateScrollTo(scrollState.maxValue)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp)
                                                .verticalScroll(scrollState)
                                        ) {
                                            Text(
                                                text = logText,
                                                color = Color(0xFF38BDF8),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom sticky footer & convert actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedImages.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Trash clean all action
                    OutlinedButton(
                        onClick = { viewModel.clearAllImages(context) },
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("clear_all_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        ),
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                        enabled = !isConverting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = context.getString(R.string.clear_all),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Share all success items
                    val successItems = selectedImages.filter { it.status == ConversionStatus.SUCCESS && it.convertedUri != null }
                    if (successItems.isNotEmpty() && customSavePathUri == null) {
                        Button(
                            onClick = { shareMultipleImages(context, selectedImages) },
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f)
                                .testTag("share_all_success_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD3E4FF),
                                contentColor = Color(0xFF001D36)
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val shareBtnText = if (successItems.size == 1) {
                                context.getString(R.string.export_share)
                            } else {
                                context.getString(R.string.batch_export_share, successItems.size)
                            }
                            Text(shareBtnText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Main Start batch conversion button
                    val pendingExist = selectedImages.any { it.status == ConversionStatus.PENDING || it.status == ConversionStatus.FAILED }
                    if (pendingExist || isConverting) {
                        Button(
                            onClick = { viewModel.startConversion(context) },
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1.5f)
                                .testTag("start_convert_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (pendingExist) Color(0xFF004A77) else Color(0xFF004A77).copy(alpha = 0.5f),
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(26.dp),
                            enabled = !isConverting && pendingExist
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isConverting) {
                                Text(context.getString(R.string.converting_batch), fontSize = 14.sp, color = Color.White)
                            } else {
                                val startBtnText = if (selectedImages.size == 1) {
                                    context.getString(R.string.start_convert_save)
                                } else {
                                    context.getString(R.string.start_batch_convert)
                                }
                                Text(startBtnText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                // If list is empty, bottom CTA triggers select images instantly!
                Button(
                    onClick = { imagePickerLauncher.launch(if (filterOnlyHeic) "heic_heif" else "image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_convert_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF004A77),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = context.getString(R.string.import_start_convert),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // High aesthetic footnote
            Text(
                text = context.getString(R.string.no_network_footnote),
                fontSize = 10.sp,
                color = Color(0xFF747775),
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


class PickMultipleImagesContract : ActivityResultContracts.GetMultipleContents() {
    override fun createIntent(context: Context, input: String): Intent {
        val mimeType = if (input == "heic_heif") "image/*" else input
        val intent = super.createIntent(context, mimeType)
        if (input == "heic_heif") {
            intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "image/heic",
                    "image/heif",
                    "image/heic-sequence",
                    "image/heif-sequence"
                )
            )
        }
        return intent
    }
}

// Share helpers
private fun shareSingleImage(context: Context, item: ImageItem) {
    val uri = item.convertedUri ?: return
    val textExt = item.outputFormat.lowercase()
    val mime = if (item.outputFormat == "PNG") "image/png" else "image/jpeg"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val fileDisplayName = "${item.name.substringBeforeLast(".")}.$textExt"
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_image_title, fileDisplayName)))
}

private fun shareMultipleImages(context: Context, items: List<ImageItem>) {
    val successUris = items.filter { it.status == ConversionStatus.SUCCESS && it.convertedUri != null }
        .map { it.convertedUri!! }

    if (successUris.isEmpty()) return

    val intent = Intent().apply {
        if (successUris.size == 1) {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, successUris[0])
            val isPng = items.firstOrNull { it.status == ConversionStatus.SUCCESS }?.outputFormat == "PNG"
            type = if (isPng) "image/png" else "image/jpeg"
        } else {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(successUris))
            type = "image/*"
        }
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_multiple_images_title)))
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
