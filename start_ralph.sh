#!/bin/bash

################################################################################
# Ralph Loop 启动器 - 弹出新终端窗口运行
################################################################################

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "正在启动 Ralph Loop..."

# 检测操作系统
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS - 使用 AppleScript 打开新的 Terminal 窗口
    osascript <<EOF
tell application "Terminal"
    activate
    do script "cd '$SCRIPT_DIR' && ./ralph.sh"
end tell
EOF
else
    # Linux - 尝试使用不同的终端模拟器
    if command -v gnome-terminal &> /dev/null; then
        gnome-terminal -- bash -c "cd '$SCRIPT_DIR' && ./ralph.sh; exec bash"
    elif command -v xterm &> /dev/null; then
        xterm -e "cd '$SCRIPT_DIR' && ./ralph.sh" &
    elif command -v konsole &> /dev/null; then
        konsole -e bash -c "cd '$SCRIPT_DIR' && ./ralph.sh; exec bash" &
    else
        echo "无法自动打开终端窗口，请手动运行: ./ralph.sh"
        exit 1
    fi
fi

echo "Ralph Loop 已在新窗口中启动！"
echo "原窗口可以关闭或继续使用其他操作。"
