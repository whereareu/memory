"""
掘金爬虫
爬取 Android/Compose/Kotlin 相关文章
"""
import json
import hashlib
from datetime import datetime
from typing import List, Optional
from crawlers.base import BaseCrawler
from models.article import Article, Source


class JuejinCrawler(BaseCrawler):
    """掘金爬虫"""

    @property
    def source(self) -> Source:
        return Source.JUEJIN

    @property
    def base_url(self) -> str:
        return "https://juejin.cn"

    def get_articles(self, limit: int = 20) -> List[Article]:
        """
        获取文章列表

        使用掘金 API 获取推荐文章
        """
        articles = []

        # 掘金推荐文章 API
        api_url = f"{self.base_url}/recommend_api/v1/article/recommend_all_feed"
        params = {
            'id_type': 0,
            'sort_type': 200,
            'cursor': '0',
            'limit': limit * 2,  # 多获取一些，后续过滤
        }

        try:
            response = self.session.post(api_url, json=params, timeout=self.timeout)
            response.raise_for_status()
            data = response.json()

            if 'data' not in data:
                print(f"API响应错误: {data.get('err_msg', '未知错误')}")
                return articles

            # item_type=2 表示文章类型
            for item in data['data']:
                if item.get('item_type') != 2:
                    continue

                article = self._parse_api_item(item)
                if article and self._is_related(article):
                    articles.append(article)
                    print(f"✓ 找到相关文章: {article.title[:30]}...")
                    if len(articles) >= limit:
                        break

        except Exception as e:
            print(f"掘金爬取失败: {e}")

        return articles

    def parse_article(self, html: str) -> Optional[Article]:
        """解析单篇文章（暂不使用）"""
        return None

    def _parse_api_item(self, item: dict) -> Optional[Article]:
        """解析 API 返回的单个文章项"""
        try:
            # 新的API结构: item_info.article_info
            item_info = item.get('item_info', {})
            article_info = item_info.get('article_info', {})

            # 提取基本信息
            article_id = article_info.get('article_id', '')
            title = article_info.get('title', '').strip()

            # 标题不能为空
            if not title:
                return None

            # 摘要 - brief_content 或 content
            brief_content = article_info.get('brief_content', '')
            content = article_info.get('content', '')
            summary = brief_content if brief_content else (content[:200] + '...' if len(content) > 200 else content)
            summary = summary.strip()

            if not summary:
                summary = title  # 用标题作为摘要

            # 作者信息 - 需要额外请求，暂时用默认值
            author = article_info.get('author_user_info', {}).get('user_name', '掘金作者')

            # 构建URL
            url = f"{self.base_url}/post/{article_id}"

            # 提取封面图
            cover_image = article_info.get('cover_image')

            # 发布时间 - ctime 是字符串形式的秒级时间戳
            ctime = article_info.get('ctime', '0')
            try:
                published_at = datetime.fromtimestamp(int(ctime))
            except:
                published_at = datetime.now()

            # 阅读时间（按字数估算，content为空则默认5分钟）
            content_len = len(content)
            read_time = max(1, content_len // 400) if content_len > 0 else 5

            # 标签 - tag_ids 需要额外请求，暂时从标题中提取
            tags = []
            category_name = article_info.get('category', {}).get('category_name', '')
            if category_name:
                tags.append(category_name)

            # 生成唯一ID
            unique_id = hashlib.md5(url.encode()).hexdigest()[:12]

            return Article(
                id=unique_id,
                title=title,
                summary=summary,
                author=author,
                source=self.source,
                url=url,
                cover_image=cover_image,
                tags=tags,
                published_at=published_at,
                read_time_minutes=read_time
            )

        except Exception as e:
            print(f"解析文章失败: {e}")
            return None

    def _is_related(self, article: Article) -> bool:
        """
        判断文章是否相关（Android/Kotlin/Compose相关）

        Args:
            article: 文章对象

        Returns:
            是否相关
        """
        # 相关关键词
        keywords = [
            'android', 'kotlin', 'compose', 'jetpack',
            'flutter', 'dart', '移动开发', '安卓',
            'gradle', 'activity', 'fragment', 'viewmodel'
        ]

        # 检查标题和标签
        text_to_check = (article.title + ' ' + ' '.join(article.tags)).lower()

        return any(keyword.lower() in text_to_check for keyword in keywords)

    def _get_icon_url(self) -> str:
        """掘金图标"""
        return "https://juejin.cn/favicon.ico"
