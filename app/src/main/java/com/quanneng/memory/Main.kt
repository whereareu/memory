package com.quanneng.memory

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class Main : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                MainScreen(
                    onEditClick = {
                        startActivity(Intent(this, Edit::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(onEditClick: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "欢迎使用 Memory",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "点击按钮开始编辑内容",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onEditClick) {
                    Text("编辑内容")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        MainScreen(onEditClick = {})
    }
}