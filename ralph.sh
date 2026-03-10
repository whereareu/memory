#!/bin/bash

################################################################################
# Ralph Loop - 核弹级自主开发循环系统
# 功能：让 AI 在你睡觉时持续工作数小时，具备完整容错和恢复机制
################################################################################

set -o pipefail

# ============== 配置区 ==============
LOG_DIR=".ralph_logs"
LOG_FILE="$LOG_DIR/ralph_$(date +%Y%m%d_%H%M%S).log"
STATS_FILE="$LOG_DIR/stats.json"
PROMPT_FILE="ralph_prompt.md"
BACKUP_DIR=".ralph_backup"
MAX_LOOP_TIME=600        # 单次循环最大时间（秒）- 防止卡死
MAX_CONTINUOUS_ERRORS=3  # 最大连续错误数
ERROR_COOLDOWN=30        # 错误后冷却时间（秒）
SUCCESS_COOLDOWN=5       # 成功后冷却时间（秒）
ENABLE_BACKUP=true       # 是否启用备份
ENABLE_NOTIFY=true       # 是否启用完成通知
GIT_AUTO_COMMIT=true     # 是否自动 commit
MAX_LOG_SIZE=100M        # 单日志文件最大大小
MAX_LOG_FILES=10         # 保留日志文件数量

# ============== 颜色定义 ==============
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ============== 初始化 ==============
init() {
    mkdir -p "$LOG_DIR" "$BACKUP_DIR"
    touch "$LOG_FILE" "$STATS_FILE"

    # 初始化统计
    if [ ! -s "$STATS_FILE" ]; then
        echo '{"loop_count":0,"success_count":0,"error_count":0,"total_time":0,"start_time":"'$(date -Iseconds)'"}' > "$STATS_FILE"
    fi

    # 检测系统并设置 timeout 命令
    detect_system

    # 创建备份
    if [ "$ENABLE_BACKUP" = true ]; then
        create_backup
    fi

    # 检查必要文件
    check_prerequisites
}

# ============== 系统检测 ==============
detect_system() {
    # 检测 macOS 或 Linux
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS: 尝试使用 gtimeout（如果安装了 coreutils）
        if command -v gtimeout &> /dev/null; then
            TIMEOUT_CMD="gtimeout"
        else
            # macOS 没有 coreutils，禁用超时功能
            log_warning "macOS 检测到 gtimeout 未安装，超时功能已禁用"
            log_warning "安装方法: brew install coreutils"
            TIMEOUT_CMD=""
        fi
    else
        # Linux: 使用标准 timeout
        TIMEOUT_CMD="timeout"
    fi
}

# ============== 检查前提条件 ==============
check_prerequisites() {
    local missing=0

    for file in "$PROMPT_FILE" "CLAUDE.md" "TODO.md" "PROGRESS.md"; do
        if [ ! -f "$file" ]; then
            log_error "缺失必要文件: $file"
            missing=$((missing + 1))
        fi
    done

    if [ $missing -gt 0 ]; then
        log_error "缺少 $missing 个必要文件，无法启动"
        exit 1
    fi

    # 检查 git 状态
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        log_warning "不是 git 仓库，自动 commit 将被禁用"
        GIT_AUTO_COMMIT=false
    fi
}

# ============== 创建备份 ==============
create_backup() {
    local backup_name="backup_$(date +%Y%m%d_%H%M%S)"
    local backup_path="$BACKUP_DIR/$backup_name"

    mkdir -p "$backup_path"

    log_info "创建备份: $backup_name"

    # 备份关键文件
    cp CLAUDE.md "$backup_path/" 2>/dev/null
    cp TODO.md "$backup_path/" 2>/dev/null
    cp PROGRESS.md "$backup_path/" 2>/dev/null

    # 保留最近 5 个备份
    ls -t "$BACKUP_DIR" | tail -n +6 | xargs -I {} rm -rf "$BACKUP_DIR/{}"
}

# ============== 日志函数 ==============
log_info() {
    echo -e "${BLUE}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $*" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $*" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $*" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $*" | tee -a "$LOG_FILE"
}

log_loop() {
    echo -e "${PURPLE}[LOOP]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $*" | tee -a "$LOG_FILE"
}

# ============== 更新统计 ==============
update_stats() {
    local result=$1
    local duration=$2

    local stats=$(cat "$STATS_FILE")
    local loop_count=$(echo "$stats" | jq -r '.loop_count + 1')
    local success_count=$(echo "$stats" | jq -r '.success_count')
    local error_count=$(echo "$stats" | jq -r '.error_count')
    local total_time=$(echo "$stats" | jq -r '.total_time + '"$duration"'')

    if [ "$result" = "success" ]; then
        success_count=$((success_count + 1))
    else
        error_count=$((error_count + 1))
    fi

    jq -n \
        --argjson loop_count "$loop_count" \
        --argjson success_count "$success_count" \
        --argjson error_count "$error_count" \
        --argjson total_time "$total_time" \
        --arg start_time "$(echo "$stats" | jq -r '.start_time')" \
        '{loop_count: $loop_count, success_count: $success_count, error_count: $error_count, total_time: $total_time, start_time: $start_time}' > "$STATS_FILE"
}

# ============== 显示统计 ==============
show_stats() {
    local stats=$(cat "$STATS_FILE")
    local loop_count=$(echo "$stats" | jq -r '.loop_count')
    local success_count=$(echo "$stats" | jq -r '.success_count')
    local error_count=$(echo "$stats" | jq -r '.error_count')
    local total_time=$(echo "$stats" | jq -r '.total_time')
    local avg_time=0

    if [ "$loop_count" -gt 0 ]; then
        avg_time=$((total_time / loop_count))
    fi

    echo ""
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║                    Ralph Loop 统计报告                      ║"
    echo "╠════════════════════════════════════════════════════════════╣"
    printf "║  循环次数: %-20d 成功: %-10d 失败: %-10d║\n" "$loop_count" "$success_count" "$error_count"
    printf "║  总耗时: %-6d秒              平均: %-10d秒/轮          ║\n" "$total_time" "$avg_time"
    printf "║  成功率: %-6.1f%%                                           ║\n" "$(echo "scale=1; $success_count * 100 / $loop_count" | bc 2>/dev/null || echo 0)"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
}

# ============== 检查完成信号 ==============
check_exit_signal() {
    local output="$1"

    if echo "$output" | grep -q "EXIT_SIGNAL: true"; then
        log_success "🎉 检测到完成信号！所有任务已完成！"
        return 0
    fi

    if grep -q "EXIT_SIGNAL: true" "$LOG_FILE" 2>/dev/null; then
        log_success "🎉 检测到完成信号！所有任务已完成！"
        return 0
    fi

    return 1
}

# ============== 健康检查 ==============
health_check() {
    local output="$1"

    # 检查是否有明显的错误响应
    if echo "$output" | grep -qi "error\|failed\|exception"; then
        log_warning "检测到可能的错误响应"
        return 1
    fi

    # 检查响应是否过短（可能表示 AI 没有正常工作）
    local response_length=$(echo "$output" | wc -c)
    if [ "$response_length" -lt 100 ]; then
        log_warning "响应过短 ($response_length 字符)，可能存在问题"
        return 1
    fi

    return 0
}

# ============== 自动提交 ==============
auto_commit() {
    if [ "$GIT_AUTO_COMMIT" != true ]; then
        return
    fi

    # 检查是否有变更
    if git diff --quiet && git diff --cached --quiet; then
        return
    fi

    log_info "检测到变更，自动 commit..."

    # 添加所有变更
    git add -A

    # 创建 commit 消息
    local commit_msg="chore: Ralph Loop 自动提交 - 循环 #$LOOP_COUNT"

    # 提交
    if git commit -m "$commit_msg" 2>>"$LOG_FILE"; then
        log_success "自动 commit 成功"
    else
        log_warning "自动 commit 失败（可能无变更或冲突）"
    fi
}

# ============== 执行单次循环 ==============
execute_loop() {
    local loop_num=$1
    local loop_start=$(date +%s)

    log_loop "═══════════════════════════════════════════════════════════"
    log_loop "开始循环 #$loop_num"
    log_loop "═══════════════════════════════════════════════════════════"

    # 读取 prompt
    local prompt
    prompt=$(cat "$PROMPT_FILE" 2>/dev/null)
    if [ -z "$prompt" ]; then
        log_error "无法读取 prompt 文件"
        return 1
    fi

    # 执行 Claude（带超时，如果可用）
    local output
    local exit_code=0

    if [ -n "$TIMEOUT_CMD" ]; then
        output=$($TIMEOUT_CMD "$MAX_LOOP_TIME" claude --dangerously-skip-permissions -p "$prompt" 2>&1) || exit_code=$?
    else
        output=$(claude --dangerously-skip-permissions -p "$prompt" 2>&1) || exit_code=$?
    fi

    # 计算耗时
    local loop_end=$(date +%s)
    local duration=$((loop_end - loop_start))

    # 输出到日志
    echo "$output" >> "$LOG_FILE"

    # 检查超时
    if [ $exit_code -eq 124 ]; then
        log_error "循环超时（${MAX_LOOP_TIME}秒），强制终止"
        update_stats "error" "$duration"
        return 1
    fi

    # 检查其他错误
    if [ $exit_code -ne 0 ]; then
        log_error "Claude 执行失败，退出码: $exit_code"
        update_stats "error" "$duration"
        return 1
    fi

    # 健康检查
    if ! health_check "$output"; then
        log_warning "健康检查未通过"
        update_stats "error" "$duration"
        return 1
    fi

    # 检查完成信号
    if check_exit_signal "$output"; then
        update_stats "success" "$duration"
        return 2  # 特殊返回码表示完成
    fi

    # 自动提交
    auto_commit

    # 成功
    log_success "循环 #$loop_num 完成 (耗时: ${duration}秒)"
    update_stats "success" "$duration"

    return 0
}

# ============== 通知用户 ==============
notify_user() {
    if [ "$ENABLE_NOTIFY" != true ]; then
        return
    fi

    local title="Ralph Loop 完成"
    local message="所有任务已完成！查看日志: $LOG_FILE"

    # macOS
    if command -v osascript &> /dev/null; then
        osascript -e "display notification \"$message\" with title \"$title\"" 2>/dev/null
    fi

    # Linux
    if command -v notify-send &> /dev/null; then
        notify-send "$title" "$message" 2>/dev/null
    fi
}

# ============== 日志轮转 ==============
rotate_logs() {
    # 检查当前日志大小
    if [ ! -f "$LOG_FILE" ]; then
        return
    fi

    local log_size=$(stat -f%z "$LOG_FILE" 2>/dev/null || stat -c%s "$LOG_FILE" 2>/dev/null)

    # 转换为 MB
    local log_size_mb=$((log_size / 1024 / 1024))

    if [ "$log_size_mb" -ge 100 ]; then
        log_warning "日志文件过大 ($log_size_mb MB)，创建新日志"
        LOG_FILE="$LOG_DIR/ralph_$(date +%Y%m%d_%H%M%S).log"
    fi

    # 清理旧日志
    ls -t "$LOG_DIR"/ralph_*.log 2>/dev/null | tail -n +$((MAX_LOG_FILES + 1)) | xargs rm -f
}

# ============== 主循环 ==============
main() {
    init

    log_info "═══════════════════════════════════════════════════════════"
    log_info "           Ralph Loop 核弹级自主开发循环系统"
    log_info "═══════════════════════════════════════════════════════════"
    log_info "启动时间: $(date)"
    log_info "日志文件: $LOG_FILE"
    log_info "统计文件: $STATS_FILE"
    log_info "最大单次时长: ${MAX_LOOP_TIME}秒"
    log_info "最大连续错误: ${MAX_CONTINUOUS_ERRORS}"
    log_info "自动提交: $GIT_AUTO_COMMIT"
    log_info "═══════════════════════════════════════════════════════════"
    echo ""

    local loop_count=0
    local continuous_errors=0

    # 主循环
    while true; do
        loop_count=$((loop_count + 1))
        LOOP_COUNT=$loop_count  # 导出给 auto_commit 使用

        # 执行循环
        execute_loop "$loop_count"
        local result=$?

        # 显示当前统计
        show_stats

        # 检查完成
        if [ $result -eq 2 ]; then
            notify_user
            log_success "🎊 Ralph Loop 已完成所有任务！"
            break
        fi

        # 处理错误
        if [ $result -ne 0 ]; then
            continuous_errors=$((continuous_errors + 1))
            log_warning "连续错误次数: $continuous_errors/$MAX_CONTINUOUS_ERRORS"

            if [ $continuous_errors -ge $MAX_CONTINUOUS_ERRORS ]; then
                log_error "达到最大连续错误次数，Ralph Loop 停止"
                break
            fi

            log_info "冷却 ${ERROR_COOLDOWN} 秒后重试..."
            sleep "$ERROR_COOLDOWN"
        else
            continuous_errors=0
            log_info "冷却 ${SUCCESS_COOLDOWN} 秒后继续下一轮..."
            sleep "$SUCCESS_COOLDOWN"
        fi

        # 日志轮转
        rotate_logs
    done

    # 最终统计
    log_info "═══════════════════════════════════════════════════════════"
    log_info "           Ralph Loop 最终统计报告"
    log_info "═══════════════════════════════════════════════════════════"
    show_stats
    log_info "结束时间: $(date)"
    log_info "日志文件: $LOG_FILE"
    log_info "═══════════════════════════════════════════════════════════"
}

# ============== 信号处理 ==============
trap 'log_warning "收到中断信号，正在优雅退出..."; show_stats; exit 130' INT TERM

# ============== 启动 ==============
cd "$(dirname "$0")" || exit 1
main "$@"
