from flask import Flask, request, jsonify
import mysql.connector
import pandas as pd
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import TfidfVectorizer

app = Flask(__name__)

# MySQL Database Configuration
db_config = {
    "host": "localhost",
    "user": "root",
    "password": "DDbb11$$",
    "database": "eshop"
}

def get_purchase_data():
    """ Fetches purchase history from MySQL """
    conn = mysql.connector.connect(**db_config)
    query = "SELECT user_id, article_id FROM cart_item"
    df = pd.read_sql(query, conn)
    conn.close()
    return df

def get_product_data():
    """ Fetches product data from the article table """
    conn = mysql.connector.connect(**db_config)
    query = "SELECT id AS article_id, title FROM article"
    df = pd.read_sql(query, conn)
    conn.close()
    return df

def collaborative_filtering(user_id):
    """ User-based Collaborative Filtering using Cosine Similarity """
    df = get_purchase_data()
    
    if df.empty:
        return []

    user_item_matrix = df.pivot_table(index='user_id', columns='article_id', aggfunc='size', fill_value=0)
    
    if user_id not in user_item_matrix.index:
        return []

    user_similarity = cosine_similarity(user_item_matrix)
    user_idx = list(user_item_matrix.index).index(user_id)
    similar_users = sorted(enumerate(user_similarity[user_idx]), key=lambda x: x[1], reverse=True)[1:4]

    recommended_items = set()
    for idx, _ in similar_users:
        similar_user_id = user_item_matrix.index[idx]
        recommended_items.update(df[df['user_id'] == similar_user_id]['article_id'])

    user_purchases = set(df[df['user_id'] == user_id]['article_id'])
    return list(recommended_items - user_purchases)

def content_based_filtering(user_id):
    """ Content-Based Filtering using TF-IDF on article titles """
    df = get_purchase_data()
    product_df = get_product_data()

    user_products = df[df['user_id'] == user_id]['article_id'].tolist()
    if not user_products or product_df.empty:
        return []

    tfidf = TfidfVectorizer(stop_words='english')
    tfidf_matrix = tfidf.fit_transform(product_df['title'].fillna(''))

    similarity_matrix = cosine_similarity(tfidf_matrix, tfidf_matrix)

    recommended_items = set()
    for product in user_products:
        if product in product_df['article_id'].values:
            product_idx = product_df.index[product_df['article_id'] == product].tolist()[0]
            similar_products = sorted(enumerate(similarity_matrix[product_idx]), key=lambda x: x[1], reverse=True)[1:4]
            recommended_items.update([product_df.iloc[i]['article_id'] for i, _ in similar_products])

    return list(recommended_items - set(user_products))

def hybrid_recommendation(user_id):
    """ Hybrid Recommendation (Combining Collaborative & Content-Based) """
    collab_recs = set(collaborative_filtering(user_id))
    content_recs = set(content_based_filtering(user_id))
    
    return list(collab_recs.union(content_recs))

@app.route("/recommend", methods=["GET"])
def recommend():
    """ API Endpoint to get recommendations based on chosen algorithm """
    user_id = request.args.get("user_id", type=int)
    algo = request.args.get("algo", default="hybrid")

    if not user_id:
        return jsonify({"error": "User ID required"}), 400

    if algo == "collaborative":
        recommendations = collaborative_filtering(user_id)
    elif algo == "content":
        recommendations = content_based_filtering(user_id)
    else:
        recommendations = hybrid_recommendation(user_id)

    return jsonify({"user_id": int(user_id), "recommended_articles": [int(article) for article in recommendations]})


if __name__ == "__main__":
    app.run(debug=True, port=5000)
