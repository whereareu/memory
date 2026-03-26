"""
文章数据模型
"""
from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, Field, field_validator
from enum import Enum


class Source(str, Enum):
    """文章来源"""
    JUEJIN = "掘金"
    CSDN = "CSDN"
    MEDIUM = "Medium"
    BLOG_ANDROID = "Android Developers Blog"


class Article(BaseModel):
    """单篇文章数据"""
    id: str = Field(..., description="唯一标识")
    title: str = Field(..., min_length=1, max_length=200, description="文章标题")
    summary: str = Field(..., min_length=1, max_length=500, description="文章摘要")
    author: str = Field(..., min_length=1, max_length=50, description="作者名")
    source: Source = Field(..., description="文章来源")
    url: str = Field(..., description="原文链接")
    cover_image: Optional[str] = Field(None, description="封面图片URL")
    tags: List[str] = Field(default_factory=list, description="标签列表")
    published_at: datetime = Field(..., description="发布时间")
    read_time_minutes: int = Field(default=5, ge=1, le=60, description="阅读时间(分钟)")

    @field_validator('tags')
    @classmethod
    def validate_tags(cls, v: List[str]) -> List[str]:
        """验证并清理标签"""
        # 去重、去空、限制数量
        tags = [tag.strip() for tag in v if tag.strip()]
        return list(set(tags))[:10]  # 最多10个标签

    @field_validator('url')
    @classmethod
    def validate_url(cls, v: str) -> str:
        """验证URL格式"""
        if not v.startswith(('http://', 'https://')):
            raise ValueError('URL must start with http:// or https://')
        return v


class SourceInfo(BaseModel):
    """数据源信息"""
    name: Source = Field(..., description="来源名称")
    icon: str = Field(..., description="图标URL")
    url: str = Field(..., description="官网链接")


class ArticleData(BaseModel):
    """完整的文章数据容器"""
    version: str = Field(default="1.0", description="数据版本")
    last_updated: datetime = Field(default_factory=datetime.now, description="最后更新时间")
    sources: List[SourceInfo] = Field(default_factory=list, description="数据源列表")
    articles: List[Article] = Field(default_factory=list, description="文章列表")

    def add_source(self, source: SourceInfo) -> None:
        """添加数据源"""
        # 避免重复
        if not any(s.name == source.name for s in self.sources):
            self.sources.append(source)

    def add_articles(self, articles: List[Article]) -> None:
        """添加文章列表，自动去重"""
        existing_ids = {a.id for a in self.articles}
        for article in articles:
            if article.id not in existing_ids:
                self.articles.append(article)
                existing_ids.add(article.id)

    def sort_by_publish_time(self, reverse: bool = True) -> None:
        """按发布时间排序"""
        self.articles.sort(key=lambda x: x.published_at, reverse=reverse)

    def limit(self, n: int) -> None:
        """限制文章数量"""
        self.articles = self.articles[:n]
