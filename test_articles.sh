#!/bin/bash

echo "🚀 开始测试技术文章功能..."

# 1. 卸载旧应用
echo "📦 卸载旧应用..."
adb uninstall com.quanneng.memory 2>/dev/null || echo "旧应用不存在"

# 2. 安装新应用
echo "📲 安装新应用..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. 启动应用并跳转到技术文章页面
echo "🎯 启动应用并跳转到技术文章页面..."
adb shell am start -n com.quanneng.memory/.Main

# 等待应用启动
sleep 2

# 4. 模拟点击文章按钮（坐标可能需要调整）
echo "👆 点击文章按钮..."
# 尝试通过文本查找并点击
adb shell input tap 540 1000  # 根据屏幕高度调整

# 或者使用 Intent 直接启动
adb shell am start -n com.quanneng.memory/com.quanneng.memory.features.articles.ui.ArticleListActivity

# 5. 查看日志
echo "📋 查看日志（按 Ctrl+C 停止）..."
adb logcat -c  # 清空旧日志
adb logcat -v time | grep -E "ArticleViewModel|ArticleListScreen|ArticleContent|Ktor|OkHttp|Exception|Error" --line-buffered
