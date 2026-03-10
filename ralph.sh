#!/bin/bash

################################################################################
# Ralph Loop - 自动循环执行，显示进度
################################################################################

set -o pipefail

LOG_DIR=".ralph_logs"
LOG_FILE="$LOG_DIR/ralph_$(date +%Y%m%d_%H%M%S).log"
PROMPT_FILE="ralph_prompt.md"
BACKUP_DIR=".ralph_backup"
MAX_ERRORS=3
ERROR_COOLDOWN=30
SUCCESS_COOLDOWN=5

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

init() {
    mkdir -p "$LOG_DIR" "$BACKUP_DIR"

    # 检查必要文件
    for file in "$PROMPT_FILE" "CLAUDE.md" "TODO.md" "PROGRESS.md"; do
        if [ ! -f "$file" ]; then
            echo -e "${RED}✗ 缺少文件: $file${NC}"
            exit 1
        fi
    done

    # 备份
    local backup="backup_$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR/$backup"
    cp CLAUDE.md TODO.md PROGRESS.md "$BACKUP_DIR/$backup/" 2>/dev/null
    ls -t "$BACKUP_DIR" | tail -n +6 | xargs -I {} rm -rf "$BACKUP_DIR/{}"

    echo -e "${BLUE}日志: $LOG_FILE${NC}"
}

execute_loop() {
    local num=$1
    local start=$(date +%s)

    echo ""
    echo -e "${PURPLE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${PURPLE}  循环 #$num${NC} - $(date '+%H:%M:%S')"
    echo -e "${PURPLE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""

    # 读取当前 TODO 状态
    local total=$(grep -c "^- \[ \]" TODO.md 2>/dev/null || echo 0)
    local done=$(grep -c "^- \[x\]" TODO.md 2>/dev/null || echo 0)

    echo -e "${BLUE}📋 当前进度: $done / $((total + done)) 任务完成${NC}"
    echo -e "${BLUE}🔄 正在调用 Claude...${NC}"
    echo -e "${BLUE}   查看日志: tail -f $LOG_FILE${NC}"
    echo ""

    # 执行 Claude（后台运行，显示进度动画）
    local prompt=$(cat "$PROMPT_FILE" 2>/dev/null)

    # 创建进度指示器
    (
        while true; do
            for c in "⠋" "⠙" "⠹" "⠸" "⠼" "⠴" "⠦" "⠧" "⠇" "⠏"; do
                echo -ne "\r${BLUE}正在处理... $c${NC} "
                sleep 0.1
            done
        done
    ) &
    local spinner_pid=$!

    # 实际执行
    if claude --dangerously-skip-permissions -p "$prompt" >> "$LOG_FILE" 2>&1; then
        kill $spinner_pid 2>/dev/null
        echo -ne "\r✓ "

        local end=$(date +%s)
        local duration=$((end - start))

        echo -e "${GREEN}完成 (耗时: ${duration}秒)${NC}"
        echo -e "${BLUE}   日志已保存${NC}"

        # 显示最后的输出摘要
        echo ""
        echo -e "${BLUE}═══ Claude 输出摘要 ═══${NC}"
        tail -n 20 "$LOG_FILE" | grep -v "^\[" | tail -n 10
        echo -e "${BLUE}══════════════════════════${NC}"

        return 0
    else
        kill $spinner_pid 2>/dev/null
        echo -ne "\r✗ "
        echo -e "${RED}失败${NC}"
        echo -e "${RED}   查看错误: tail $LOG_FILE${NC}"
        return 1
    fi
}

main() {
    init

    echo ""
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║                   Ralph Loop 启动                         ║"
    echo "╠════════════════════════════════════════════════════════════╣"
    echo "║  Ctrl+C 退出                                              ║"
    echo "╚════════════════════════════════════════════════════════════╝"

    local count=0
    local errors=0

    while true; do
        count=$((count + 1))

        if execute_loop $count; then
            errors=0

            # 检查完成
            if grep -q "EXIT_SIGNAL: true" "$LOG_FILE" 2>/dev/null; then
                echo ""
                echo -e "${GREEN}🎉 所有任务已完成！${NC}"
                break
            fi

            echo ""
            echo -e "${BLUE}⏳ ${SUCCESS_COOLDOWN}秒后继续...${NC}"
            sleep $SUCCESS_COOLDOWN
        else
            errors=$((errors + 1))
            echo -e "${YELLOW}⚠️  连续错误: $errors/$MAX_ERRORS${NC}"

            if [ $errors -ge $MAX_ERRORS ]; then
                echo -e "${RED}✗ 达到最大错误次数，停止${NC}"
                break
            fi

            echo -e "${YELLOW}⏳ ${ERROR_COOLDOWN}秒后重试...${NC}"
            sleep $ERROR_COOLDOWN
        fi
    done

    echo ""
    echo -e "${GREEN}✓ Ralph Loop 结束 - 共完成 $count 次循环${NC}"
}

trap 'echo ""; echo -e "${YELLOW}中断退出${NC}"; exit 130' INT TERM

cd "$(dirname "$0")" || exit 1
main "$@"
