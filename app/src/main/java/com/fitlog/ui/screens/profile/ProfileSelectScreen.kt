package com.fitlog.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitlog.data.local.entity.UserProfile
import com.fitlog.ui.FitLogViewModelFactory
import com.fitlog.ui.theme.AvatarGradients
import com.fitlog.ui.theme.FitLogTheme
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

/**
 * Stateful entry: wires the ViewModel to the stateless [ProfileSelectContent].
 * Keeping the UI stateless lets us render it in @Preview without a database/ViewModel.
 */
@Composable
fun ProfileSelectScreen(
    onUserReady: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = FitLogViewModelFactory.Factory),
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    ProfileSelectContent(
        users = users,
        onSelect = { id ->
            viewModel.selectUser(id)
            onUserReady()
        },
        onCreate = { name -> viewModel.createUser(name) },
    )
}

/**
 * Netflix / YouTube-style profile picker: a horizontally swipeable carousel of
 * circular avatars, centered, focused one scaled up. Tap the centered avatar to enter;
 * the "add user" action sits just below the carousel. Stateless and previewable.
 */
@Composable
fun ProfileSelectContent(
    users: List<UserProfile>,
    onSelect: (Long) -> Unit,
    onCreate: (String) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))
            Text(
                text = "选择用户",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "每位用户的训练与体重数据相互独立 · 无需密码",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))

            if (users.isEmpty()) {
                EmptyProfileState(onAdd = { showAddDialog = true })
            } else {
                ProfileCarousel(users = users, onEnter = onSelect)
            }

            Spacer(Modifier.height(28.dp))
            AddUserButton(onClick = { showAddDialog = true })
            Spacer(Modifier.weight(1.4f))
        }
    }

    if (showAddDialog) {
        AddUserDialog(
            onConfirm = { name ->
                onCreate(name)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
private fun ProfileCarousel(
    users: List<UserProfile>,
    onEnter: (Long) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { users.size })
    val scope = rememberCoroutineScope()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val pageWidth = 170.dp
    val sidePadding = ((screenWidth - pageWidth) / 2).coerceAtLeast(16.dp)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = sidePadding),
            pageSpacing = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp),
        ) { page ->
            val isFocused = pagerState.currentPage == page
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ProfileAvatar(
                    user = users[page],
                    isFocused = isFocused,
                    modifier = Modifier.graphicsLayer {
                        val pageOffset = (
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            ).absoluteValue.coerceIn(0f, 1f)
                        val scale = lerp(0.66f, 1f, 1f - pageOffset)
                        scaleX = scale
                        scaleY = scale
                        alpha = lerp(0.35f, 1f, 1f - pageOffset)
                    },
                    onClick = {
                        if (page == pagerState.currentPage) {
                            onEnter(users[page].id)
                        } else {
                            scope.launch { pagerState.animateScrollToPage(page) }
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = users.getOrNull(pagerState.currentPage)?.name ?: "",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "点击头像进入",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        PageDots(count = users.size, current = pagerState.currentPage)
    }
}

@Composable
private fun ProfileAvatar(
    user: UserProfile,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val gradient = AvatarGradients[((user.id % AvatarGradients.size).toInt().absoluteValue)]
    val initial = user.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = modifier
            .requiredSize(132.dp)
            .shadow(if (isFocused) 14.dp else 0.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradient))
            .then(
                if (isFocused) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun PageDots(count: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { i ->
            val active = i == current
            Box(
                modifier = Modifier
                    .size(if (active) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        },
                    ),
            )
        }
    }
}

@Composable
private fun AddUserButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
    ) {
        Text(
            text = "＋  新增用户",
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun EmptyProfileState(onAdd: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "＋",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "还没有用户，先创建一个吧",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AddUserDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("新增用户") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("用户名") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

// ---------------------------------------------------------------------------
// In-IDE previews (no build / install / emulator needed). Edit the UI above and
// these update live in Android Studio's "Split" / "Design" view.
// ---------------------------------------------------------------------------

@Preview(name = "多用户 / Multiple profiles", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun ProfileSelectPreview() {
    FitLogTheme {
        ProfileSelectContent(
            users = listOf(
                UserProfile(id = 1, name = "小王"),
                UserProfile(id = 2, name = "Jason"),
                UserProfile(id = 3, name = "妈妈"),
            ),
            onSelect = {},
            onCreate = {},
        )
    }
}

@Preview(name = "空状态 / Empty", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun ProfileSelectEmptyPreview() {
    FitLogTheme {
        ProfileSelectContent(
            users = emptyList(),
            onSelect = {},
            onCreate = {},
        )
    }
}
