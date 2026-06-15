import React, {useEffect, useState} from 'react';
import {Item} from '../types/api';
import {CategoryLookup} from '../App';
import {formatPrice, initialOf, placeholderStyle} from '../lib/format';
import Modal from './Modal';

interface ItemDetailProps {
    itemId: string;
    categoryName: CategoryLookup;
    onClose: () => void;
}

function ItemDetail({itemId, categoryName, onClose}: ItemDetailProps) {
    const [item, setItem] = useState<Item | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [imageFailed, setImageFailed] = useState<boolean>(false);

    useEffect(() => {
        let active = true;
        setLoading(true);
        setError(null);
        setImageFailed(false);
        fetch(`/items/${itemId}`)
            .then(res => {
                if (!res.ok) {
                    throw new Error(`Failed to load item (${res.status})`);
                }
                return res.json() as Promise<Item>;
            })
            .then(data => {
                if (active) {
                    setItem(data);
                }
            })
            .catch((err: unknown) => {
                if (active) {
                    setError(err instanceof Error ? err.message : 'Failed to load item');
                }
            })
            .finally(() => {
                if (active) {
                    setLoading(false);
                }
            });
        return () => {
            active = false;
        };
    }, [itemId]);

    return (
        <Modal title={item ? item.name : 'Ad details'} onClose={onClose} className="modal--detail">
            {loading && (
                <div className="detail__loading">
                    <span className="spinner" aria-hidden="true" />
                    <span>Loading ad…</span>
                </div>
            )}
            {error && <div className="banner banner--error" role="alert">{error}</div>}
            {item && (
                <div className="detail">
                    <div
                        className="detail__hero"
                        style={item.imageUrl.length > 0 && !imageFailed ? undefined : placeholderStyle(item.id)}
                    >
                        {item.imageUrl.length > 0 && !imageFailed ? (
                            <img
                                className="detail__img"
                                src={item.imageUrl}
                                alt={item.name}
                                onError={() => setImageFailed(true)}
                            />
                        ) : (
                            <span className="detail__initial" aria-hidden="true">{initialOf(item.name)}</span>
                        )}
                        <span className="card__chip">{categoryName(item.categoryId)}</span>
                    </div>

                    <div className="detail__head">
                        <h2 className="detail__title">{item.name}</h2>
                        <span className="detail__price">{formatPrice(item.price)}</span>
                    </div>

                    <p className="detail__location">
                        <span aria-hidden="true">📍</span> {item.location}
                    </p>

                    <p className="detail__desc">{item.description}</p>

                    <dl className="detail__meta">
                        <div className="detail__row">
                            <dt>Category</dt>
                            <dd>{categoryName(item.categoryId)}</dd>
                        </div>
                        <div className="detail__row">
                            <dt>Ad ID</dt>
                            <dd className="detail__mono">{item.id}</dd>
                        </div>
                    </dl>
                </div>
            )}
        </Modal>
    );
}

export default ItemDetail;
