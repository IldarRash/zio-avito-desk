import React, {useCallback, useEffect, useMemo, useState} from 'react';
import './App.css';
import {Category, Item} from './types/api';
import ItemCard from './components/ItemCard';
import ItemDetail from './components/ItemDetail';
import CreateItemForm from './components/CreateItemForm';

export type CategoryLookup = (categoryId: string) => string;

function App() {
    const [items, setItems] = useState<Item[]>([]);
    const [categories, setCategories] = useState<Category[]>([]);
    const [search, setSearch] = useState<string>('');
    const [activeQuery, setActiveQuery] = useState<string>('');
    const [selectedId, setSelectedId] = useState<string | null>(null);
    const [createOpen, setCreateOpen] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const loadItems = useCallback((url: string): void => {
        setLoading(true);
        setError(null);
        fetch(url)
            .then(res => {
                if (!res.ok) {
                    throw new Error(`Failed to load items (${res.status})`);
                }
                return res.json() as Promise<Item[]>;
            })
            .then(setItems)
            .catch((err: unknown) => {
                setError(err instanceof Error ? err.message : 'Failed to load items');
            })
            .finally(() => setLoading(false));
    }, []);

    const refresh = useCallback((): void => {
        setActiveQuery('');
        setSearch('');
        loadItems('/items');
    }, [loadItems]);

    useEffect(() => {
        loadItems('/items');
    }, [loadItems]);

    useEffect(() => {
        let active = true;
        fetch('/categories')
            .then(res => {
                if (!res.ok) {
                    throw new Error(`Failed to load categories (${res.status})`);
                }
                return res.json() as Promise<Category[]>;
            })
            .then(data => {
                if (active) {
                    setCategories(data);
                }
            })
            .catch(() => {
                /* Categories are non-fatal: cards fall back to the raw id. */
            });
        return () => {
            active = false;
        };
    }, []);

    const categoryName = useMemo<CategoryLookup>(() => {
        const map = new Map(categories.map(c => [c.id, c.name]));
        return (id: string) => map.get(id) ?? 'Uncategorized';
    }, [categories]);

    const submitSearch = (e: React.FormEvent<HTMLFormElement>): void => {
        e.preventDefault();
        const query = search.trim();
        setActiveQuery(query);
        loadItems(query ? `/items/search/${encodeURIComponent(query)}` : '/items');
    };

    const deleteItem = (id: string): void => {
        setError(null);
        fetch(`/items/${id}`, {method: 'DELETE'})
            .then(res => {
                if (!res.ok) {
                    throw new Error(`Failed to delete item (${res.status})`);
                }
                setItems(prev => prev.filter(item => item.id !== id));
            })
            .catch((err: unknown) => {
                setError(err instanceof Error ? err.message : 'Failed to delete item');
            });
    };

    const handleCreated = (): void => {
        setCreateOpen(false);
        refresh();
    };

    return (
        <div className="app">
            <header className="navbar">
                <div className="navbar__inner">
                    <button
                        type="button"
                        className="brand"
                        onClick={refresh}
                        aria-label="Avito Desk home"
                    >
                        <span className="brand__mark" aria-hidden="true">A</span>
                        <span className="brand__name">Avito&nbsp;Desk</span>
                    </button>

                    <form className="searchbar" onSubmit={submitSearch} role="search">
                        <span className="searchbar__icon" aria-hidden="true">⌕</span>
                        <input
                            type="search"
                            className="searchbar__input"
                            value={search}
                            onChange={e => setSearch(e.target.value)}
                            placeholder="Search ads…"
                            aria-label="Search ads"
                        />
                        <button type="submit" className="searchbar__submit">Search</button>
                    </form>

                    <button
                        type="button"
                        className="btn btn--primary navbar__cta"
                        onClick={() => setCreateOpen(true)}
                    >
                        <span aria-hidden="true">＋</span> New ad
                    </button>
                </div>
            </header>

            <main className="page">
                <div className="page__heading">
                    <h1 className="page__title">
                        {activeQuery ? 'Search results' : 'Fresh on the market'}
                    </h1>
                    <p className="page__subtitle">
                        {activeQuery
                            ? `Showing matches for “${activeQuery}”`
                            : 'Browse the latest classified ads from the community.'}
                    </p>
                </div>

                {error && <div className="banner banner--error" role="alert">{error}</div>}

                {loading ? (
                    <ul className="grid" aria-hidden="true">
                        {Array.from({length: 8}).map((_, i) => (
                            <li key={i} className="skeleton-card">
                                <div className="skeleton-card__media" />
                                <div className="skeleton-card__body">
                                    <span className="skeleton-line skeleton-line--lg" />
                                    <span className="skeleton-line" />
                                    <span className="skeleton-line skeleton-line--sm" />
                                </div>
                            </li>
                        ))}
                    </ul>
                ) : items.length === 0 ? (
                    <div className="empty">
                        <div className="empty__art" aria-hidden="true">🗂️</div>
                        <h2 className="empty__title">
                            {activeQuery ? 'No ads match your search' : 'No ads yet'}
                        </h2>
                        <p className="empty__text">
                            {activeQuery
                                ? 'Try a different keyword, or clear the search to see everything.'
                                : 'Be the first to post — list something you no longer need.'}
                        </p>
                        {activeQuery ? (
                            <button type="button" className="btn btn--ghost" onClick={refresh}>
                                Clear search
                            </button>
                        ) : (
                            <button
                                type="button"
                                className="btn btn--primary"
                                onClick={() => setCreateOpen(true)}
                            >
                                <span aria-hidden="true">＋</span> Post the first ad
                            </button>
                        )}
                    </div>
                ) : (
                    <ul className="grid">
                        {items.map(item => (
                            <ItemCard
                                key={item.id}
                                item={item}
                                categoryName={categoryName(item.categoryId)}
                                onSelect={setSelectedId}
                                onDelete={deleteItem}
                            />
                        ))}
                    </ul>
                )}
            </main>

            {selectedId && (
                <ItemDetail
                    itemId={selectedId}
                    categoryName={categoryName}
                    onClose={() => setSelectedId(null)}
                />
            )}

            {createOpen && (
                <CreateItemForm
                    categories={categories}
                    onCreated={handleCreated}
                    onClose={() => setCreateOpen(false)}
                />
            )}
        </div>
    );
}

export default App;
