package com.fitlog.ui.screens.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitlog.data.prefs.BodyData
import com.fitlog.ui.FitLogViewModelFactory
import com.fitlog.ui.theme.AvatarGradients
import com.fitlog.ui.theme.FitLogTheme
import com.fitlog.ui.theme.HoneyDeep
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun MineScreen(
    onSwitchUser: () -> Unit,
    onUserDeleted: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenWeight: () -> Unit,
    viewModel: MineViewModel = viewModel(factory = FitLogViewModelFactory.Factory),
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val currentId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val unit by viewModel.unit.collectAsStateWithLifecycle()
    val restSeconds by viewModel.restSeconds.collectAsStateWithLifecycle()
    val loadedBodyData by viewModel.bodyData.collectAsStateWithLifecycle()
    val bodyData = loadedBodyData ?: BodyData()
    val currentUser = users.firstOrNull { it.id == currentId }

    MineContent(
        userName = currentUser?.name ?: "",
        userSeed = currentUser?.id ?: 0L,
        unit = unit,
        restSeconds = restSeconds,
        bodyData = bodyData,
        bodyDataLoaded = loadedBodyData != null,
        onSwitchUser = onSwitchUser,
        onDeleteUser = {
            viewModel.deleteCurrentUser()
            onUserDeleted()
        },
        onSaveBodyData = viewModel::saveBodyData,
        onSetUnit = viewModel::setUnit,
        onSetRest = viewModel::setRestSeconds,
        onOpenLibrary = onOpenLibrary,
        onOpenWeight = onOpenWeight,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineContent(
    userName: String,
    userSeed: Long,
    unit: String,
    restSeconds: Int,
    bodyData: BodyData,
    bodyDataLoaded: Boolean,
    onSwitchUser: () -> Unit,
    onDeleteUser: () -> Unit,
    onSaveBodyData: (Double?, Int?, String?) -> Unit,
    onSetUnit: (String) -> Unit,
    onSetRest: (Int) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenWeight: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBodySheet by remember(userSeed) { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var comingSoon by remember { mutableStateOf<String?>(null) }
    var autoPrompted by remember(userSeed) { mutableStateOf(false) }

    // Wait for DataStore before deciding this is a first-time profile.
    LaunchedEffect(bodyDataLoaded, bodyData, userName, userSeed) {
        if (bodyDataLoaded && userName.isNotBlank() && !bodyData.isComplete && !autoPrompted) {
            showBodySheet = true
            autoPrompted = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        UserHeader(userName = userName, seed = userSeed)

        Spacer(Modifier.height(24.dp))
        SectionCard {
            MenuRow("身体资料", bodyDataSubtitle(bodyData), onClick = { showBodySheet = true })
            Divider()
            MenuRow("体重", "记录体重、查看 BMI 与趋势", onClick = onOpenWeight)
            Divider()
            MenuRow("训练设置", "单位 $unit · 组间休息 ${restLabel(restSeconds)}", onClick = { showSettingsSheet = true })
        }

        Spacer(Modifier.height(16.dp))
        SectionCard {
            MenuRow("动作库", "浏览动作、肌肉图与要领", onClick = onOpenLibrary)
            Divider()
            MenuRow("切换用户", "回到用户选择页", onClick = onSwitchUser)
            Divider()
            MenuRow("本地备份与恢复", "导出/导入本机数据", onClick = { comingSoon = "本地备份与恢复" })
        }

        Spacer(Modifier.height(16.dp))
        SectionCard {
            MenuRow("关于与开源许可", "版本信息与第三方声明", onClick = { comingSoon = "关于与开源许可" })
            Divider()
            MenuRow(
                title = "删除当前用户及数据",
                subtitle = "训练、体重等记录将一并删除",
                titleColor = Color(0xFFD9544D),
                onClick = { showDeleteDialog = true },
            )
        }
        Spacer(Modifier.height(28.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("删除当前用户？") },
            text = { Text("将删除「$userName」及其所有训练与体重记录，此操作无法撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteUser()
                }) { Text("删除", color = Color(0xFFD9544D)) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } },
        )
    }

    comingSoon?.let { feature ->
        AlertDialog(
            onDismissRequest = { comingSoon = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text(feature) },
            text = { Text("该功能将在 2.0 版本提供，敬请期待 🙌") },
            confirmButton = { TextButton(onClick = { comingSoon = null }) { Text("好的") } },
        )
    }

    if (showBodySheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = { showBodySheet = false }, sheetState = sheetState) {
            BodyDataSheet(
                initial = bodyData,
                onSave = { h, a, g ->
                    onSaveBodyData(h, a, g)
                    showBodySheet = false
                },
            )
        }
    }

    if (showSettingsSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = { showSettingsSheet = false }, sheetState = sheetState) {
            SettingsSheet(
                unit = unit,
                restSeconds = restSeconds,
                onSetUnit = onSetUnit,
                onSetRest = onSetRest,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BodyDataSheet(
    initial: BodyData,
    onSave: (Double?, Int?, String?) -> Unit,
) {
    var height by remember { mutableStateOf(initial.heightCm?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var age by remember { mutableStateOf(initial.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(initial.gender ?: "") }
    val heightValue = height.toDoubleOrNull()
    val ageValue = age.toIntOrNull()
    val canSave = heightValue?.let { it > 0.0 } == true && ageValue?.let { it > 0 } == true

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("身体资料", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(4.dp))
        Text("仅存于本机，用于 BMI 等参考", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = height,
            onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("身高") },
            suffix = { Text("cm") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = age,
            onValueChange = { age = it.filter { c -> c.isDigit() } },
            label = { Text("年龄") },
            suffix = { Text("岁") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Text("性别", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("男", "女", "其他").forEach { g ->
                FilterChip(selected = gender == g, onClick = { gender = g }, label = { Text(g) })
            }
        }

        Spacer(Modifier.height(20.dp))
        PrimaryButton(text = "保存", enabled = canSave) {
            onSave(heightValue, ageValue, gender.ifBlank { null })
        }
        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    unit: String,
    restSeconds: Int,
    onSetUnit: (String) -> Unit,
    onSetRest: (Int) -> Unit,
) {
    var rest by remember { mutableStateOf(restSeconds.toFloat()) }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("训练设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(16.dp))
        Text("重量单位", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("kg", "lb").forEach { u ->
                FilterChip(selected = unit == u, onClick = { onSetUnit(u) }, label = { Text(u) })
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("组间休息时长：${restLabel(rest.roundToInt())}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = rest,
            onValueChange = { rest = it },
            onValueChangeFinished = { onSetRest((rest / 15).roundToInt() * 15) },
            valueRange = 15f..300f,
            steps = (300 - 15) / 15 - 1,
        )
        Text("（15 秒 ~ 5 分钟，按 15 秒一档）", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

private fun bodyDataSubtitle(b: BodyData): String =
    if (!b.isComplete) {
        "未填写，点此完善"
    } else {
        buildList {
            add("身高 ${b.heightCm?.let { if (it % 1.0 == 0.0) it.toInt() else it }}cm")
            add("${b.age}岁")
            b.gender?.takeIf { it.isNotBlank() }?.let(::add)
        }.joinToString(" · ")
    }

private fun restLabel(seconds: Int): String =
    if (seconds % 60 == 0) "${seconds / 60} 分钟" else "$seconds 秒"

@Composable
private fun UserHeader(userName: String, seed: Long) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val gradient = AvatarGradients[((seed % AvatarGradients.size).toInt().absoluteValue)]
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        Spacer(Modifier.size(16.dp))
        Column {
            Text(
                text = userName.ifBlank { "未选择用户" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "本地用户 · 数据仅存于本机",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
    }
}

@Composable
private fun MenuRow(
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = titleColor)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(text = "›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)),
    )
}

@Preview(name = "我的", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MinePreview() {
    FitLogTheme {
        MineContent(
            userName = "Jason",
            userSeed = 2,
            unit = "kg",
            restSeconds = 90,
            bodyData = BodyData(175.0, 26, "男"),
            bodyDataLoaded = true,
            onSwitchUser = {},
            onDeleteUser = {},
            onSaveBodyData = { _, _, _ -> },
            onSetUnit = {},
            onSetRest = {},
            onOpenLibrary = {},
            onOpenWeight = {},
        )
    }
}
