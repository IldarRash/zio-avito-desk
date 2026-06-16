import React, {useCallback, useEffect, useMemo, useState} from 'react';
import './App.css';
import {Category, Item, ItemQuery} from './types/api';
import {CategoryLookup} from './lib/format';
import {api} from './services/api';
import {useAuth} from './context/AuthContext';
import Navbar from './components/Navbar';
import CategoryRail from './components/CategoryRail';
import FilterBar, {Filters} from './components/FilterBar';
import ItemCard from './components/ItemCard';
import ItemDetail from './components/ItemDetail';
import ItemForm from './components/ItemForm';
import AuthForm, {AuthMode} from './components/AuthForm';

const PAGE_SIZE = 24;
const MY_LISTINGS_LIMIT = 100;

const DEFAULT_FILTERS: Filters = {
    minPrice: '',
    maxPrice: '',
    location: '',
    sort: 'newest',
};

type FormState = {mode: 'create'} | {mode: 'edit'; item: Item};

function parsePrice(value: string): number | undefined {
    const trimmed = value.trim();
    if (trimmed === '') return undefined;
    const n = Number(trimmed);
    return Number.isNaN(n) ? undefined : n;
}

function App() {
    const {user} = useAuth();

    const [items, setItems] = useState<Item[]>([]);
    const [total, setTotal] = useState<number>(0);
    const [categories, setCategories] = useState<Category[]>([]);

    const [search, setSearch] = useState<string>('');
    const [activeQuery, setActiveQuery] = useState<string>('');
    const [categoryId, setCategoryId] = useState<string | null>(null);
    const [filters, setFilters] = useState<Filters>(DEFAULT_FILTERS);
    const [page, setPage] = useState<number>(0);
    const [mine, setMine] = useState<boolean>(false);

    const [selectedId, setSelectedId] = useState<string | null>(null);
    const [formState, setFormState] = useState<FormState | null>(null);
    const [authMode, setAuthMode] = useState<AuthMode | null>(null);

    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // Reset pagination whenever any board input changes.
    useEffect(() => {
        setPage(0);
    }, [activeQuery, categoryId, filters, mine]);

    // Logging out while viewing "My listings" returns to the full board.
    useEffect(() => {
        if (!user && mine) {
            setMine(false);
        }
    }, [user, mine]);

    const loadItems = useCallback((): (() => void) => {
        let active = true;
        setLoading(true);
        setError(null);

        const query: ItemQuery = mine
            ? {limit: MY_LISTINGS_LIMIT, offset: 0, sort: filters.sort}
            : {
                  q: activeQuery,
                  categoryId: categoryId ?? undefined,
                  minPrice: parsePrice(filters.minPrice),
                  maxPrice: parsePrice(filters.maxPrice),
                  location: filters.location,
                  sort: filters.sort,
                  limit: PAGE_SIZE,
                  offset: page * PAGE_SIZE,
              };

        api.listItems(query)
            .then(result => {
                if (!active) return;
                if (mine && user) {
                    const owned = result.items.filter(item => item.ownerId === user.id);
                    setItems(owned);
                    setTotal(owned.length);
                } else {
                    setItems(result.items);
                    setTotal(result.total);
                }
            })
            .catch((err: unknown) => {
                if (active) setError(err instanceof Error ? err.message : 'Failed to load ads');
            })
            .finally(() => {
                if (active) setLoading(false);
            });

        return () => {
            active = false;
        };
    }, [activeQuery, categoryId, filters, page, mine, user]);

    useEffect(() => loadItems(), [loadItems]);

    useEffect(() => {
        let active = true;
        api.listCategories()
            .then(data => {
                if (active) setCategories(data);
            })
            .catch(() => {
                /* Categories are non-fatal: cards fall back to a default label. */
            });
        return () => {
            active = false;
        };
    }, []);

    const categoryName = useMemo<CategoryLookup>(() => {
        const map = new Map(categories.map(c => [c.id, c.name]));
        return (id: string) => map.get(id) ?? 'Uncategorized';
    }, [categories]);

    const goHome = useCallback((): void => {
        setActiveQuery('');
        setSearch('');
        setCategoryId(null);
        setFilters(DEFAULT_FILTERS);
        setMine(false);
    }, []);

    const submitSearch = (): void => setActiveQuery(search.trim());

    const deleteItem = (id: string): void => {
        setError(null);
        api.deleteItem(id)
            .then(() => {
                setItems(prev => prev.filter(item => item.id !== id));
                setTotal(prev => Math.max(0, prev - 1));
                setSelectedId(prev => (prev === id ? null : prev));
            })
            .catch((err: unknown) => {
                setError(err instanceof Error ? err.message : 'Failed to delete ad');
            });
    };

    const handleSaved = (saved: Item): void => {
        setFormState(null);
        // Refetch so ordering/filters stay consistent with the backend.
        loadItems();
        setSelectedId(prev => (prev === saved.id ? saved.id : prev));
    };

    const openEdit = (item: Item): void => {
        setSelectedId(null);
        setFormState({mode: 'edit', item});
    };

    const showingMine = mine && user !== null;
    const hasMore = !showingMine && items.length < total;

    const headingTitle = showingMine
        ? 'My listings'
        : activeQuery
          ? 'Search results'
          : categoryId
            ? categoryName(categoryId)
            : 'Fresh on the market';

    const headingSubtitle = showingMine
        ? 'Ads you have posted.'
        : activeQuery
          ? `Showing matches for “${activeQuery}”`
          : 'Browse the latest classified ads from the community.';

    return (
        <div className="app">
            <Navbar
                search={search}
                onSearchChange={setSearch}
                onSearchSubmit={submitSearch}
                onHome={goHome}
                onNewAd={() => setFormState({mode: 'create'})}
                onOpenAuth={setAuthMode}
            />

            <main className="page">
                <CategoryRail categories={categories} activeId={categoryId} onSelect={setCategoryId} />

                <div className="board__bar">
                    <FilterBar
                        filters={filters}
                        onChange={setFilters}
                        onReset={() => setFilters(DEFAULT_FILTERS)}
                    />
                    {user && (
                        <button
                            type="button"
                            className={`btn ${showingMine ? 'btn--primary' : 'btn--ghost'}`}
                            onClick={() => setMine(m => !m)}
                            aria-pressed={showingMine}
                        >
                            {showingMine ? 'Viewing my listings' : 'My listings'}
                        </button>
                    )}
                </div>

                <div className="page__heading">
                    <h1 className="page__title">{headingTitle}</h1>
                    <p className="page__subtitle">{headingSubtitle}</p>
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
                            {showingMine ? "You haven't posted any ads" : 'No ads found'}
                        </h2>
                        <p className="empty__text">
                            {showingMine
                                ? 'Post your first ad — list something you no longer need.'
                                : 'Try different keywords or clear your filters to see everything.'}
                        </p>
                        {showingMine ? (
                            <button
                                type="button"
                                className="btn btn--primary"
                                onClick={() => setFormState({mode: 'create'})}
                            >
                                <span aria-hidden="true">＋</span> Post an ad
                            </button>
                        ) : (
                            <button type="button" className="btn btn--ghost" onClick={goHome}>
                                Clear filters
                            </button>
                        )}
                    </div>
                ) : (
                    <>
                        <ul className="grid">
                            {items.map(item => (
                                <ItemCard
                                    key={item.id}
                                    item={item}
                                    categoryName={categoryName(item.categoryId)}
                                    owned={user !== null && item.ownerId === user.id}
                                    onSelect={setSelectedId}
                                    onEdit={openEdit}
                                    onDelete={deleteItem}
                                />
                            ))}
                        </ul>

                        {hasMore && (
                            <div className="board__more">
                                <button
                                    type="button"
                                    className="btn btn--ghost"
                                    onClick={() => setPage(p => p + 1)}
                                >
                                    Load more ({items.length} of {total})
                                </button>
                            </div>
                        )}
                    </>
                )}
            </main>

            {selectedId && (
                <ItemDetail
                    itemId={selectedId}
                    categoryName={categoryName}
                    currentUserId={user?.id ?? null}
                    onEdit={openEdit}
                    onDelete={deleteItem}
                    onClose={() => setSelectedId(null)}
                />
            )}

            {formState && (
                <ItemForm
                    categories={categories}
                    item={formState.mode === 'edit' ? formState.item : undefined}
                    onSaved={handleSaved}
                    onClose={() => setFormState(null)}
                />
            )}

            {authMode && (
                <AuthForm mode={authMode} onClose={() => setAuthMode(null)} onSwitchMode={setAuthMode} />
            )}
        </div>
    );
}

export default App;
