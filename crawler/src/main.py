# -*- coding: utf-8 -*-
"""
主程序
协调所有爬虫并生成最终数据
"""
import json
import sys
from pathlib import Path
from datetime import datetime

# 修复导入路径
sys.path.insert(0, str(Path(__file__).parent))

from crawlers.juejin import JuejinCrawler
from models.article import ArticleData


def main():
    """主函数"""
    print("🚀 开始爬取技术文章...")

    # 初始化数据容器
    data = ArticleData()

    # 爬取各个数据源
    crawlers = [
        JuejinCrawler(),
        # 后续添加更多爬虫
        # CsdnCrawler(),
        # MediumCrawler(),
    ]

    for crawler in crawlers:
        print(f"\n📡 正在爬取 {crawler.source.value}...")

        try:
            # 获取数据源信息
            data.add_source(crawler.get_source_info())

            # 获取文章
            articles = crawler.get_articles(limit=20)
            print(f"✅ 成功获取 {len(articles)} 篇文章")

            # 添加到数据容器
            data.add_articles(articles)

        except Exception as e:
            print(f"❌ {crawler.source.value} 爬取失败: {e}")

    # 按发布时间排序
    data.sort_by_publish_time(reverse=True)

    # 限制总文章数量
    data.limit(50)

    # 更新时间戳
    data.last_updated = datetime.now()

    # 输出统计信息
    print(f"\n📊 爬取完成统计:")
    print(f"  - 数据源: {len(data.sources)} 个")
    print(f"  - 文章总数: {len(data.articles)} 篇")

    # 保存数据
    output_path = Path(__file__).parent.parent / "data" / "articles.json"
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(
            data.model_dump(mode='json'),
            f,
            ensure_ascii=False,
            indent=2
        )

    print(f"\n💾 数据已保存到: {output_path}")
    print(f"🕐 最后更新: {data.last_updated.strftime('%Y-%m-%d %H:%M:%S')}")


if __name__ == "__main__":
    main()
