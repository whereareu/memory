#!/bin/bash

################################################################################
# Ralph Loop - 极简自主开发循环系统
# 功能：让 AI 持续工作，Claude 输出直接显示在终端
################################################################################

set -o pipefail

# ============== 配置区 ==============
LOG_DIR=".ralph_logs"
LOG_FILE="$LOG_DIR/ralph_$(date +%Y%m%d_%H%M%S).log"
PROMPT_FILE="ralph_prompt.md"
BACKUP_DIR=".ralph_backup"
MAX_CONTINUOUS_ERRORS=3
ERROR_COOLDOWN=30
SUCCESS_COOLDOWN=5

# ============== 颜色定义 ==============
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

# ============== 初始化 ==============
init() {
    mkdir -p "$LOG_DIR" "$BACKUP_DIR"

    # 检查必要文件
    for file in "$PROMPT_FILE" "CLAUDE.md" "TODO.md" "PROGRESS.md"; do
        if [ ! -f "$file" ]; then
            echo -e "${RED}错误: 缺少必要文件 $file${NC}"
            exit 1
        fi
    done

    # 创建备份
    local backup_name="backup_$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR/$backup_name"
    cp CLAUDE.md TODO.md PROGRESS.md "$BACKUP_DIR/$backup_name/" 2>/dev/null

    # 清理旧备份（保留最近 5 个）
    ls -t "$BACKUP_DIR" | tail -n +6 | xargs -I {} rm -rf "$BACKUP_DIR/{}"
}

# ============== 日志函数 ==============
log_info() {
    echo -e "${BLUE}[INFO]${NC} $(date '+%H:%M:%S') - $*" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $(date '+%H:%M:%S') - $*" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $(date '+%H:%M:%S') - $*" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%H:%M:%S') - $*" | tee -a "$LOG_FILE"
}

# ============== 执行单次循环 ==============
execute_loop() {
    local loop_num=$1

    echo ""
    echo -e "${PURPLE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${PURPLE}循环 #$loop_num${NC}"
    echo -e "${PURPLE}═══════════════════════════════════════════════════════════${NC}"
    echo ""

    # 读取 prompt
    local prompt
    prompt=$(cat "$PROMPT_FILE" 2>/dev/null)
    if [ -z "$prompt" ]; then
        log_error "无法读取 prompt 文件"
        return 1
    fi

    # 直接调用 Claude，输出到终端和日志
    if claude --dangerously-skip-permissions -p "$prompt" 2>&1 | tee -a "$LOG_FILE"; then
        log_success "循环 #$loop_num 完成"
        return 0
    else
        log_error "循环 #$loop_num 失败"
        return 1
    fi
}

# ============== 主循环 ==============
main() {
    init

    echo ""
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║                    Ralph Loop 启动中                       ║"
    echo "╠════════════════════════════════════════════════════════════╣"
    echo "║  日志文件: 另开终端运行 'tail -f $LOG_FILE'              ║"
    echo "║  中断方法: Ctrl+C                                         ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""

    local loop_count=0
    local continuous_errors=0

    # 主循环
    while true; do
        loop_count=$((loop_count + 1))

        # 执行循环
        if execute_loop "$loop_count"; then
            continuous_errors=0

            # 检查完成信号
            if grep -q "EXIT_SIGNAL: true" "$LOG_FILE" 2>/dev/null; then
                log_success "🎉 所有任务已完成！"
                break
            fi

            log_info "等待 ${SUCCESS_COOLDOWN} 秒后继续..."
            sleep "$SUCCESS_COOLDOWN"
        else
            continuous_errors=$((continuous_errors + 1))
            log_warning "连续错误: $continuous_errors/$MAX_CONTINUOUS_ERRORS"

            if [ $continuous_errors -ge $MAX_CONTINUOUS_ERRORS ]; then
                log_error "达到最大连续错误次数，停止"
                break
            fi

            log_info "等待 ${ERROR_COOLDOWN} 秒后重试..."
            sleep "$ERROR_COOLDOWN"
        fi
    done

    log_success "Ralph Loop 已结束"
}

# ============== 信号处理 ==============
trap 'echo ""; log_warning "收到中断信号，正在退出..."; exit 130' INT TERM

# ============== 启动 ==============
cd "$(dirname "$0")" || exit 1
main "$@"
