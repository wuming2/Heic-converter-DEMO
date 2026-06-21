package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    PrivacyPolicyScreen(
                        modifier = Modifier.padding(innerPadding),
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC)) // Light gray-blue background to match main screen
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Back Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .testTag("privacy_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = context.getString(R.string.privacy_back),
                    tint = Color(0xFF1A1C1E)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = context.getString(R.string.privacy_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
        }

        // Policy Content Card
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                Text(
                    text = "HEIC/HEIF Image Converter Privacy Policy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF001D36)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "This Privacy Policy describes how our application collects, uses, and shares information when you use our HEIC/HEIF Image Converter mobile application.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF444746)
                )

                SectionDivider()

                PolicySection(
                    title = "1. Local & Secure Processing (本地与安全转换)",
                    content = "Our application is designed as an offline-first tool. All image conversion, EXIF metadata preservation, and quality processing operations are performed completely on your physical device. We DO NOT upload your photos, personal files, or converted outputs to any remote web servers. Your private data remains solely details of your own device storage.\n\n" +
                            "本应用设计为离线优先的本地工具。所有图像格式转换、EXIF 元数据保留以及质量压缩等操作均完全在您的物理设备本地运行。我们不会将您的任何照片、个人文件或转换后的结果上传至任何远程网络服务器。您的隐私数据安全地保留在您的设备存储中。"
                )

                SectionDivider()

                PolicySection(
                    title = "2. Telemetry and Analytics (统计与数据分析)",
                    content = "To improve application performance, diagnose system crashes, and optimize user experience, we integrate a statistiscal telemetry and analysis SDK (Firebase Analytics / Crashlytics).\n" +
                            "This SDK may automatically gather essential non-personally identifiable telemetry such as:\n" +
                            " • Application launch/startup signals (应用启动/开启频率)\n" +
                            " • Diagnostic system crashes and trace reports (系统意外崩溃与诊断堆栈)\n" +
                            " • Successful/failed HEIC conversion statistics (HEIC 转换成功或失败的批量统计数据)\n\n" +
                            "We use this aggregated data purely for performance diagnostics, product enhancement, and bug tracking. No private personal data, image payloads, or location metadata is targeted or transmitted by these logs.\n\n" +
                            "为了提升应用整体性能、诊断系统故障并优化用戶体验，我们在应用中配有统计与分析 SDK（Firebase Analytics / Crashlytics）。\n" +
                            "该 SDK 可能会自动搜集基本且非个人身份相关的监控参数，例如：\n" +
                            " • 应用每次启动与初始化的信号\n" +
                            " • 应用非预期的崩溃和诊断性日志报告\n" +
                            " • 图像成功/失败转换次数的聚合统计\n\n" +
                            "我们利用这些聚合数据仅作性能优化、软件升级与缺陷追踪之用途。此类统计数据绝对不包含、也不可能包含您的个人隐私、照片底层像素内容、网络位置或其他敏感数据。"
                )

                SectionDivider()

                PolicySection(
                    title = "3. Third-Party Services (第三方服务)",
                    content = "The application links with service providers as below:\n" +
                            " • Google Play Services (Core system features)\n" +
                            " • Google Analytics for Firebase (Service usage statistics)\n" +
                            " • Firebase Crashlytics (Diagnostic crash logging)\n\n" +
                            "These services maintain their distinct privacy guidelines which you can review transparently."
                )

                SectionDivider()

                PolicySection(
                    title = "4. Consent & Future Modifications (同意与修订说明)",
                    content = "By launching and initiating image conversions via this utility app, you declare acceptance to these terms and telemetry structures. We may post occasional updates to this document as additional local modules are introduced. If you have inquiries, please contact our support team."
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Last updated: June 21, 2026",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun PolicySection(title: String, content: String) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF001D36)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = Color(0xFF444746)
        )
    }
}

@Composable
fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = Color(0xFFE1E2E6)
    )
}
