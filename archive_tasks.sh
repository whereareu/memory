#!/bin/bash

################################################################################
# 任务归档脚本 - 将已完成的任务移到 DONE.md
################################################################################

TODO_FILE="TODO.md"
DONE_FILE="DONE.md"

# 检查 TODO.md 是否存在
if [ ! -f "$TODO_FILE" ]; then
    echo "✗ 错误: 找不到 TODO.md"
    exit 1
fi

# 创建 DONE.md 如果不存在
if [ ! -f "$DONE_FILE" ]; then
    echo "# 已完成任务归档" > "$DONE_FILE"
    echo "" >> "$DONE_FILE"
fi

# 读取 TODO.md 内容
CONTENT=$(cat "$TODO_FILE")

# 统计已完成的任务
COMPLETED_COUNT=$(echo "$CONTENT" | grep -c "^- \[x\]" || echo 0)

if [ "$COMPLETED_COUNT" -eq 0 ]; then
    echo "✓ 没有已完成的任务需要归档"
    exit 0
fi

# 提取已完成任务
COMPLETED_TASKS=$(echo "$CONTENT" | sed -n '/^- \[x\]/p' | sed 's/^- \[x\]/- [x]/')

# 添加到 DONE.md
echo "" >> "$DONE_FILE"
echo "## 归档于 $(date '+%Y-%m-%d %H:%M:%S')" >> "$DONE_FILE"
echo "$COMPLETED_TASKS" >> "$DONE_FILE"

# 从 TODO.md 中删除已完成的任务（保留未完成的）
NEW_CONTENT=$(echo "$CONTENT" | grep -v "^- \[x\]" | grep -v "^  - \[x\]")

# 写回 TODO.md
echo "$NEW_CONTENT" > "$TODO_FILE"

echo "✓ 已归档 $COMPLETED_COUNT 个完成的任务到 $DONE_FILE"
echo "✓ TODO.md 已更新，可以添加新的待办任务"
