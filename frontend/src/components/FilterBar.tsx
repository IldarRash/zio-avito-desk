import React from 'react';
import {SortOption} from '../types/api';

export interface Filters {
    minPrice: string;
    maxPrice: string;
    location: string;
    sort: SortOption;
}

interface FilterBarProps {
    filters: Filters;
    onChange: (filters: Filters) => void;
    onReset: () => void;
}

const SORT_LABELS: ReadonlyArray<[SortOption, string]> = [
    ['newest', 'Newest'],
    ['price_asc', 'Price ↑'],
    ['price_desc', 'Price ↓'],
];

function FilterBar({filters, onChange, onReset}: FilterBarProps) {
    const update = (patch: Partial<Filters>): void => onChange({...filters, ...patch});

    return (
        <div className="filterbar" role="group" aria-label="Filter and sort ads">
            <div className="filterbar__field">
                <label className="field__label" htmlFor="filter-min">Min price</label>
                <input
                    id="filter-min"
                    type="number"
                    className="field__input"
                    value={filters.minPrice}
                    onChange={e => update({minPrice: e.target.value})}
                    placeholder="0"
                    min="0"
                />
            </div>

            <div className="filterbar__field">
                <label className="field__label" htmlFor="filter-max">Max price</label>
                <input
                    id="filter-max"
                    type="number"
                    className="field__input"
                    value={filters.maxPrice}
                    onChange={e => update({maxPrice: e.target.value})}
                    placeholder="Any"
                    min="0"
                />
            </div>

            <div className="filterbar__field">
                <label className="field__label" htmlFor="filter-location">Location</label>
                <input
                    id="filter-location"
                    type="text"
                    className="field__input"
                    value={filters.location}
                    onChange={e => update({location: e.target.value})}
                    placeholder="Any city"
                />
            </div>

            <div className="filterbar__field">
                <label className="field__label" htmlFor="filter-sort">Sort</label>
                <select
                    id="filter-sort"
                    className="field__input field__select"
                    value={filters.sort}
                    onChange={e => update({sort: e.target.value as SortOption})}
                >
                    {SORT_LABELS.map(([value, label]) => (
                        <option key={value} value={value}>{label}</option>
                    ))}
                </select>
            </div>

            <button type="button" className="btn btn--ghost filterbar__reset" onClick={onReset}>
                Reset
            </button>
        </div>
    );
}

export default FilterBar;
