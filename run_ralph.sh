#!/bin/bash

################################################################################
# Ralph Loop - 交互式版本
# 功能：保持 Claude 交互界面，自动询问是否继续
################################################################################

while true; do
    echo ""
    echo "═══════════════════════════════════════════════════════════"
    echo "Ralph Loop - 执行下一个任务"
    echo "═══════════════════════════════════════════════════════════"
    echo ""

    # 调用 Claude（保持交互）
    claude --prompt "严格按照 CLAUDE.md 执行 TODO.md 中下一个未完成任务。完成后更新 TODO.md、git commit、更新 PROGRESS.md。如果全部完成，回复 EXIT_SIGNAL: true + DONE"

    # 检查是否完成
    if grep -q "EXIT_SIGNAL: true" TODO.md 2>/dev/null; then
        echo ""
        echo "🎉 所有任务已完成！"
        break
    fi

    echo ""
    echo "─────────────────────────────────────────────────────────────"
    echo "当前任务已完成"
    echo "按 Enter 继续下一个任务，或 Ctrl+C 退出"
    read
done
