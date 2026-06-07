#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Extract mermaid code blocks from markdown file and save as individual .mmd files
"""

import re
import os

input_file = "系统设计说明书_校园二手交易平台_新版.md"
output_dir = "系统设计图表"

with open(input_file, 'r', encoding='utf-8') as f:
    content = f.read()

# Find all mermaid code blocks
pattern = r'```mermaid\n(.*?)```'
matches = re.findall(pattern, content, re.DOTALL)

# Define the names for each diagram in order
diagram_names = [
    "1.3用例图",
    "2.3.1实体类关系图",
    "2.3.2服务层类关系图",
    "2.3.3系统整体架构类图",
    "3.1.1用户登录顺序图",
    "3.1.2创建订单顺序图",
    "3.1.3发送聊天消息顺序图",
    "3.2.1用户注册活动图",
    "3.2.2商品搜索活动图",
    "3.2.3订单处理流程活动图",
    "3.3.1订单状态图",
    "3.3.2商品状态图",
    "3.3.3用户账户状态图",
    "3.4.3表间关系图ER图",
    "4.1组件图",
    "4.2部署图",
]

os.makedirs(output_dir, exist_ok=True)

for i, (match, name) in enumerate(zip(matches, diagram_names)):
    filepath = os.path.join(output_dir, f"{name}.mmd")
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(match.strip())
    print(f"Created: {filepath}")

print(f"\nTotal: {len(matches)} mermaid diagrams extracted")
