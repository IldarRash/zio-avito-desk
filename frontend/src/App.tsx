import React, {useEffect, useState} from 'react';
import './App.css';
import {Item} from "./types/api";

function App() {

    const [items, setItems] = useState<Item[]>([]);
    const [search, setSearch] = useState('');

    useEffect(() => {
        fetch('/items')
            .then(res => res.json())
            .then(setItems);
    }, []);

    const searchItems = () => {
        fetch(`/items/search/${search}`)
            .then(res => res.json())
            .then(setItems);
    };


    return (
        <div className="container">
            <h1>Avito Desk</h1>
            <div className="search-container">
                <input type="text" value={search} onChange={e => setSearch(e.target.value)} placeholder="Search for items..." />
                <button onClick={searchItems}>Search</button>
            </div>
            <div className="items-grid">
                {items.map(item => (
                    <div key={item.id} className="item-card">
                        <h2>{item.name}</h2>
                        <p>{item.description}</p>
                        <p className="price">{item.price}</p>
                        <p className="location">{item.location}</p>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default App;
