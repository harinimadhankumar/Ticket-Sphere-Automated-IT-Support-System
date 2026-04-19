import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { fetchAllKnowledgeBase } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn } from '../../utils/auth';

export default function KnowledgeBase() {
    const navigate = useNavigate();
    const [articles, setArticles] = useState([]);
    const [filteredArticles, setFilteredArticles] = useState([]);
    const [search, setSearch] = useState('');
    const [categoryFilter, setCategoryFilter] = useState('all');

    const categories = ['network', 'email', 'account', 'hardware', 'software'];

    const getCategoryCount = (category) => {
        if (category === 'all') return articles.length;
        return articles.filter(a => a.category?.toLowerCase() === category.toLowerCase()).length;
    };

    const loadArticles = useCallback(async () => {
        if (!isLoggedIn()) {
            navigate('/login', { replace: true });
            return;
        }
        try {
            console.log('Loading knowledge base from backend...');
            const data = await fetchAllKnowledgeBase();
            console.log('KB data from backend:', data);

            let kbArticles = [];
            if (Array.isArray(data)) {
                kbArticles = data;
            } else if (data?.data && Array.isArray(data.data)) {
                kbArticles = data.data.map(kb => ({
                    id: kb.id,
                    title: kb.issueTitle,
                    category: kb.category,
                    description: kb.solutionSteps,
                    keywords: kb.keywords,
                    issueType: kb.issueType
                }));
            } else if (data?.articles && Array.isArray(data.articles)) {
                kbArticles = data.articles;
            } else if (data?.items && Array.isArray(data.items)) {
                kbArticles = data.items;
            }

            console.log('Parsed KB articles:', kbArticles);
            setArticles(kbArticles);

            if (kbArticles.length === 0) {
                showToast.info('ℹ No knowledge base articles available');
            }
        } catch (error) {
            const friendlyMessage = getUserFriendlyMessage(error.message || 'Failed to load knowledge base articles');
            showToast.error(friendlyMessage);
            setArticles([]);
        }
    }, [navigate]);

    useEffect(() => {
        loadArticles();
    }, [loadArticles]);

    useEffect(() => {
        let result = articles;

        // Apply category filter
        if (categoryFilter !== 'all') {
            result = result.filter(article =>
                article.category?.toLowerCase() === categoryFilter.toLowerCase()
            );
        }

        // Apply search filter
        if (search.trim()) {
            const searchLower = search.toLowerCase();
            result = result.filter(article =>
                article.title?.toLowerCase().includes(searchLower) ||
                article.category?.toLowerCase().includes(searchLower) ||
                article.description?.toLowerCase().includes(searchLower)
            );
        }

        setFilteredArticles(result);
    }, [search, categoryFilter, articles]);

    return (
        <Layout showSidebar={true} showSecondaryNav={true} >
            <div className="page-header">
                <div>
                    <h1>Knowledge Base</h1>
                    <p>Browse self-service solutions for common issues</p>
                </div>
            </div>

            {/* Search and Filter Bar */}
            <div className="filters-bar">
                <div className="filter-tabs">
                    <button className={`filter-tab ${categoryFilter === 'all' ? 'active' : ''}`} onClick={() => setCategoryFilter('all')}>
                        All <span className="count">{getCategoryCount('all')}</span>
                    </button>
                    {categories.map(cat => (
                        <button
                            key={cat}
                            className={`filter-tab ${categoryFilter === cat ? 'active' : ''}`}
                            onClick={() => setCategoryFilter(cat)}
                        >
                            {cat.charAt(0).toUpperCase() + cat.slice(1)} <span className="count">{getCategoryCount(cat)}</span>
                        </button>
                    ))}
                </div>
                <div className="filter-actions">
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search articles..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        style={{ width: 280, flexShrink: 0 }}
                    />
                </div>
            </div>

            {filteredArticles.length === 0 ? (
                <div className="empty-state">
                    <h3>{articles.length === 0 ? 'No articles found' : 'No results found'}</h3>
                    <p>{search || categoryFilter !== 'all' ? 'Try adjusting your search or filters' : 'No articles available in the knowledge base'}</p>
                </div>
            ) : (
                <div className="grid-3">
                    {filteredArticles.map((article, idx) => (
                        <div key={article.id || idx} className="card">
                            <div className="card-body">
                                {article.category && (
                                    <div style={{ marginBottom: '12px' }}>
                                        <span className="category-tag">{article.category}</span>
                                    </div>
                                )}
                                <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '12px', color: '#1E293B' }}>
                                    {article.title || 'Untitled'}
                                </h3>
                                <p style={{ fontSize: '13px', color: '#6B7280', lineHeight: '1.6', margin: 0 }}>
                                    {article.description || article.content || 'No description available'}
                                </p>
                                {article.keywords && (
                                    <div style={{ marginTop: '12px', paddingTop: '12px', borderTop: '1px solid #E5E7EB' }}>
                                        <p style={{ fontSize: '11px', color: '#94A3B8', margin: '0 0 6px 0' }}>
                                            Keywords: {article.keywords}
                                        </p>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </Layout>
    );
}
