"""
基础爬虫类
"""
from abc import ABC, abstractmethod
from typing import List, Optional
import requests
from bs4 import BeautifulSoup
import sys
from pathlib import Path

# 修复导入路径
sys.path.insert(0, str(Path(__file__).parent.parent))

from models.article import Article, SourceInfo, Source


class BaseCrawler(ABC):
    """爬虫基类"""

    def __init__(self, timeout: int = 10):
        self.timeout = timeout
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'
        })

    @property
    @abstractmethod
    def source(self) -> Source:
        """数据源枚举"""
        pass

    @property
    @abstractmethod
    def base_url(self) -> str:
        """基础URL"""
        pass

    @abstractmethod
    def get_articles(self, limit: int = 20) -> List[Article]:
        """
        获取文章列表

        Args:
            limit: 最大文章数量

        Returns:
            文章列表
        """
        pass

    @abstractmethod
    def parse_article(self, html: str) -> Optional[Article]:
        """
        解析单篇文章HTML

        Args:
            html: HTML内容

        Returns:
            文章对象，解析失败返回None
        """
        pass

    def get_source_info(self) -> SourceInfo:
        """获取数据源信息"""
        return SourceInfo(
            name=self.source,
            icon=self._get_icon_url(),
            url=self.base_url
        )

    def _get_icon_url(self) -> str:
        """获取图标URL（子类可覆盖）"""
        return f"{self.base_url}/favicon.ico"

    def fetch_html(self, url: str) -> str:
        """
        获取HTML内容

        Args:
            url: 目标URL

        Returns:
            HTML内容

        Raises:
            requests.RequestException: 网络请求失败
        """
        response = self.session.get(url, timeout=self.timeout)
        response.raise_for_status()
        response.encoding = response.apparent_encoding
        return response.text

    def parse_html(self, html: str, parser: str = 'lxml') -> BeautifulSoup:
        """
        解析HTML为BeautifulSoup对象

        Args:
            html: HTML内容
            parser: 解析器类型

        Returns:
            BeautifulSoup对象
        """
        return BeautifulSoup(html, parser)

    def __del__(self):
        """清理资源"""
        if hasattr(self, 'session'):
            self.session.close()
