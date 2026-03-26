"""
基础测试
"""
import pytest
from src.crawlers.juejin import JuejinCrawler
from src.models.article import Article, Source


def test_juejin_crawler_init():
    """测试掘金爬虫初始化"""
    crawler = JuejinCrawler()
    assert crawler.source == Source.JUEJIN
    assert crawler.base_url == "https://juejin.cn"


def test_juejin_crawler_get_articles():
    """测试获取文章"""
    crawler = JuejinCrawler()
    articles = crawler.get_articles(limit=5)

    assert len(articles) <= 5
    for article in articles:
        assert isinstance(article, Article)
        assert article.source == Source.JUEJIN
        assert len(article.title) > 0
        assert article.url.startswith("https://")


def test_get_source_info():
    """测试获取数据源信息"""
    crawler = JuejinCrawler()
    source_info = crawler.get_source_info()

    assert source_info.name == Source.JUEJIN
    assert len(source_info.icon) > 0
    assert len(source_info.url) > 0


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
