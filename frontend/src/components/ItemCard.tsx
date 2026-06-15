import React, {useEffect, useState} from 'react';
import {Item} from '../types/api';
import {formatPrice, initialOf, placeholderStyle} from '../lib/format';

interface ItemCardProps {
    item: Item;
    categoryName: string;
    onSelect: (id: string) => void;
    onDelete: (id: string) => void;
}

function ItemCard({item, categoryName, onSelect, onDelete}: ItemCardProps) {
    const [confirming, setConfirming] = useState<boolean>(false);
    const [imageFailed, setImageFailed] = useState<boolean>(false);

    const showImage = item.imageUrl.length > 0 && !imageFailed;

    useEffect(() => {
        if (!confirming) {
            return;
        }
        const timer = window.setTimeout(() => setConfirming(false), 3000);
        return () => window.clearTimeout(timer);
    }, [confirming]);

    const handleDeleteClick = (e: React.MouseEvent<HTMLButtonElement>): void => {
        e.stopPropagation();
        if (confirming) {
            onDelete(item.id);
        } else {
            setConfirming(true);
        }
    };

    const open = (): void => onSelect(item.id);
    const onKeyDown = (e: React.KeyboardEvent<HTMLElement>): void => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            open();
        }
    };

    return (
        <li className="card">
            <article
                className="card__hit"
                role="button"
                tabIndex={0}
                onClick={open}
                onKeyDown={onKeyDown}
                aria-label={`View ${item.name}`}
            >
                <div className="card__media" style={showImage ? undefined : placeholderStyle(item.id)}>
                    {showImage ? (
                        <img
                            className="card__img"
                            src={item.imageUrl}
                            alt={item.name}
                            loading="lazy"
                            onError={() => setImageFailed(true)}
                        />
                    ) : (
                        <span className="card__initial" aria-hidden="true">{initialOf(item.name)}</span>
                    )}
                    <span className="card__chip">{categoryName}</span>
                </div>

                <div className="card__body">
                    <div className="card__top">
                        <h2 className="card__title">{item.name}</h2>
                        <span className="card__price">{formatPrice(item.price)}</span>
                    </div>
                    <p className="card__desc">{item.description}</p>
                    <div className="card__foot">
                        <span className="card__location">
                            <span aria-hidden="true">📍</span> {item.location}
                        </span>
                        <button
                            type="button"
                            className={`icon-btn icon-btn--danger${confirming ? ' is-confirming' : ''}`}
                            onClick={handleDeleteClick}
                            aria-label={confirming ? `Confirm delete ${item.name}` : `Delete ${item.name}`}
                            title={confirming ? 'Click again to confirm' : 'Delete ad'}
                        >
                            {confirming ? 'Confirm?' : '🗑'}
                        </button>
                    </div>
                </div>
            </article>
        </li>
    );
}

export default ItemCard;
