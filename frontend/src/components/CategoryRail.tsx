import React from 'react';
import {Category} from '../types/api';

interface CategoryRailProps {
    categories: Category[];
    activeId: string | null;
    onSelect: (categoryId: string | null) => void;
}

function CategoryRail({categories, activeId, onSelect}: CategoryRailProps) {
    return (
        <nav className="rail" aria-label="Browse by category">
            <button
                type="button"
                className={`chip${activeId === null ? ' chip--active' : ''}`}
                onClick={() => onSelect(null)}
                aria-pressed={activeId === null}
            >
                All
            </button>
            {categories.map(category => (
                <button
                    key={category.id}
                    type="button"
                    className={`chip${activeId === category.id ? ' chip--active' : ''}`}
                    onClick={() => onSelect(category.id)}
                    aria-pressed={activeId === category.id}
                >
                    {category.name}
                </button>
            ))}
        </nav>
    );
}

export default CategoryRail;
