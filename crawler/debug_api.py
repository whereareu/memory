# -*- coding: utf-8 -*-
"""
调试掘金 API 响应
"""
import requests
import json

# 掘金推荐文章 API
api_url = "https://juejin.cn/recommend_api/v1/article/recommend_all_feed"
params = {
    'id_type': 0,
    'sort_type': 200,
    'cursor': '0',
    'limit': 5,
}

session = requests.Session()
session.headers.update({
    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'
})

try:
    response = session.post(api_url, json=params, timeout=10)
    response.raise_for_status()
    data = response.json()

    # 保存完整响应用于调试
    with open('api_response.json', 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print("API 响应已保存到 api_response.json")
    print(f"响应结构: {list(data.keys())}")
    print(f"数据条数: {len(data.get('data', []))}")

    # 打印第一条数据结构
    if data.get('data'):
        first_item = data['data'][0]
        print(f"\n第一条数据结构:")
        print(f"Keys: {list(first_item.keys())}")

        if 'article' in first_item:
            article = first_item['article']
            print(f"\narticle Keys: {list(article.keys())}")

            if 'article_info' in article:
                article_info = article['article_info']
                print(f"\narticle_info Keys: {list(article_info.keys())}")
                print(f"标题: {article_info.get('title', '')}")

except Exception as e:
    print(f"错误: {e}")
