"""
CSDN爬虫
爬取 Android/Compose/Kotlin 相关文章
"""
import json
import hashlib
from datetime import datetime
from typing import List, Optional
from crawlers.base import BaseCrawler
from models.article import Article, Source


class CsdnCrawler(BaseCrawler):
    """CSDN爬虫"""

    @property
    def source(self) -> Source:
        return Source.CSDN

    @property
    def base_url(self) -> str:
        return "https://blog.csdn.net"

    def get_articles(self, limit: int = 20) -> List[Article]:
        """
        获取文章列表

        使用CSDN搜索API获取Android相关文章
        """
        articles = []

        # CSDN搜索API（通过搜索获取相关文章）
        search_url = f"{self.base_url}/api/v1/search/list"
        params = {
            'q': 'Android Kotlin Compose',
            'p': 1,
            'pageSize': limit * 2,  # 多获取一些，后续过滤
            'searchType': 'blog',
            'sort': 'createTime'
        }

        try:
            # CSDN API需要Referer
            headers = {
                'Referer': f'{self.base_url}/',
                'Origin': self.base_url
            }
            response = self.session.get(search_url, params=params, headers=headers, timeout=self.timeout)
            response.raise_for_status()
            data = response.json()

            if data.get('code') != 200:
                print(f"CSDN API错误: {data.get('message', '未知错误')}")
                return articles

            result_list = data.get('data', {}).get('list', [])

            for item in result_list:
                article = self._parse_search_item(item)
                if article and self._is_related(article):
                    articles.append(article)
                    print(f"✓ 找到相关文章: {article.title[:30]}...")
                    if len(articles) >= limit:
                        break

        except Exception as e:
            print(f"CSDN爬取失败: {e}")

        return articles

    def parse_article(self, html: str) -> Optional[Article]:
        """解析单篇文章（暂不使用）"""
        return None

    def _parse_search_item(self, item: dict) -> Optional[Article]:
        """解析搜索API返回的单个文章项"""
        try:
            # 提取基本信息
            article_id = item.get('articleId', '')
            title = item.get('title', '').strip()

            # 清理HTML标签
            from bs4 import BeautifulSoup
            title = BeautifulSoup(title, 'lxml').get_text()

            # 标题不能为空
            if not title:
                return None

            # 摘要 - description
            description = item.get('description', '')
            summary = BeautifulSoup(description, 'lxml').get_text().strip()

            if not summary:
                summary = title  # 用标题作为摘要

            # 作者信息
            author = item.get('nickname', 'CSDN作者')

            # 构建URL
            url = item.get('url', f"{self.base_url}/{article_id}")

            # 提取封面图
            cover_image = item.get('picList', [None])[0]

            # 发布时间
            create_time = item.get('createTime', '')
            try:
                # CSDN返回的是ISO格式时间字符串
                published_at = datetime.fromisoformat(create_time.replace('Z', '+00:00'))
            except:
                published_at = datetime.now()

            # 阅读时间（按字数估算）
            content_len = len(summary)
            read_time = max(1, content_len // 400) if content_len > 0 else 5

            # 标签 - 从标签列表中提取
            tags = []
            tags_list = item.get('tags', [])
            if isinstance(tags_list, list):
                tags = [tag.get('labelName', '') for tag in tags_list if tag.get('labelName')]
            tags = [t for t in tags if t][:10]  # 最多10个标签

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
            print(f"解析CSDN文章失败: {e}")
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
            'gradle', 'activity', 'fragment', 'viewmodel',
            'harmonyos', '鸿蒙'
        ]

        # 检查标题和标签
        text_to_check = (article.title + ' ' + ' '.join(article.tags)).lower()

        return any(keyword.lower() in text_to_check for keyword in keywords)

    def _get_icon_url(self) -> str:
        """CSDN图标"""
        return "https://csdn.imgcdn.net/n/static/favicon.ico"
