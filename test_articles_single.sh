#!/bin/bash

echo "🚀 开始测试技术文章功能..."

# 指定设备（根据实际情况选择）
DEVICE_ID=""
if [ -z "$DEVICE_ID" ]; then
    # 自动选择第一个设备
    DEVICE_ID=$(adb devices | grep -w "device" | head -1 | awk '{print $1}')
    echo "📱 自动选择设备: $DEVICE_ID"
fi

ADB="adb -s $DEVICE_ID"

# 1. 卸载旧应用
echo "📦 卸载旧应用..."
$ADB uninstall com.quanneng.memory 2>/dev/null || echo "旧应用不存在"

# 2. 安装新应用
echo "📲 安装新应用..."
$ADB install -r app/build/outputs/apk/debug/app-debug.apk

# 3. 清空日志
echo "🧹 清空旧日志..."
$ADB logcat -c

# 4. 直接启动技术文章页面
echo "🎯 启动技术文章页面..."
$ADB shell am start -n com.quanneng.memory/com.quanneng.memory.features.articles.ui.ArticleListActivity

echo "⏳ 等待 3 秒..."
sleep 3

# 5. 查看日志
echo "📋 查看日志（按 Ctrl+C 停止）..."
$ADB logcat -v time -s ArticleViewModel:V ArticleListScreen:V ArticleContent:V Ktor:V OkHttp:V System.err:W AndroidRuntime:E *:S
