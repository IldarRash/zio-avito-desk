import React, {useState} from 'react';
import {Category, CreateItemRequest} from '../types/api';
import Modal from './Modal';

interface CreateItemFormProps {
    categories: Category[];
    onCreated: () => void;
    onClose: () => void;
}

interface FieldErrors {
    name?: string;
    description?: string;
    price?: string;
    location?: string;
    categoryId?: string;
}

function CreateItemForm({categories, onCreated, onClose}: CreateItemFormProps) {
    const [name, setName] = useState<string>('');
    const [description, setDescription] = useState<string>('');
    const [price, setPrice] = useState<string>('');
    const [location, setLocation] = useState<string>('');
    const [imageUrl, setImageUrl] = useState<string>('');
    const [categoryId, setCategoryId] = useState<string>('');
    const [submitting, setSubmitting] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

    const validate = (): FieldErrors => {
        const errors: FieldErrors = {};
        if (!name.trim()) {
            errors.name = 'Give your ad a name';
        }
        if (!description.trim()) {
            errors.description = 'Add a short description';
        }
        const parsedPrice = Number(price);
        if (price.trim() === '' || Number.isNaN(parsedPrice)) {
            errors.price = 'Enter a valid price';
        } else if (parsedPrice < 0) {
            errors.price = 'Price cannot be negative';
        }
        if (!location.trim()) {
            errors.location = 'Where is it located?';
        }
        if (!categoryId) {
            errors.categoryId = 'Pick a category';
        }
        return errors;
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>): void => {
        e.preventDefault();
        setError(null);

        const errors = validate();
        setFieldErrors(errors);
        if (Object.keys(errors).length > 0) {
            return;
        }

        const trimmedImageUrl = imageUrl.trim();
        const payload: CreateItemRequest = {
            name: name.trim(),
            description: description.trim(),
            price: Number(price),
            categoryId,
            location: location.trim(),
            ...(trimmedImageUrl ? {imageUrl: trimmedImageUrl} : {}),
        };

        setSubmitting(true);
        fetch('/items', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload),
        })
            .then(res => {
                if (!res.ok) {
                    throw new Error(`Failed to create item (${res.status})`);
                }
                onCreated();
            })
            .catch((err: unknown) => {
                setError(err instanceof Error ? err.message : 'Failed to create item');
            })
            .finally(() => setSubmitting(false));
    };

    return (
        <Modal title="Post a new ad" onClose={onClose} className="modal--form">
            <h2 className="form__title">Post a new ad</h2>
            <p className="form__subtitle">Fill in the details to list your item.</p>

            {error && <div className="banner banner--error" role="alert">{error}</div>}

            <form className="form" onSubmit={handleSubmit} noValidate>
                <div className="field">
                    <label className="field__label" htmlFor="ad-name">Title</label>
                    <input
                        id="ad-name"
                        type="text"
                        className={`field__input${fieldErrors.name ? ' is-invalid' : ''}`}
                        value={name}
                        onChange={e => setName(e.target.value)}
                        placeholder="e.g. Vintage road bike"
                    />
                    {fieldErrors.name && <span className="field__error">{fieldErrors.name}</span>}
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="ad-desc">Description</label>
                    <textarea
                        id="ad-desc"
                        className={`field__input field__textarea${fieldErrors.description ? ' is-invalid' : ''}`}
                        value={description}
                        onChange={e => setDescription(e.target.value)}
                        placeholder="Condition, details, why you're selling…"
                    />
                    {fieldErrors.description && (
                        <span className="field__error">{fieldErrors.description}</span>
                    )}
                </div>

                <div className="field-row">
                    <div className="field">
                        <label className="field__label" htmlFor="ad-price">Price (USD)</label>
                        <input
                            id="ad-price"
                            type="number"
                            className={`field__input${fieldErrors.price ? ' is-invalid' : ''}`}
                            value={price}
                            onChange={e => setPrice(e.target.value)}
                            placeholder="0"
                            min="0"
                            step="any"
                        />
                        {fieldErrors.price && <span className="field__error">{fieldErrors.price}</span>}
                    </div>

                    <div className="field">
                        <label className="field__label" htmlFor="ad-location">Location</label>
                        <input
                            id="ad-location"
                            type="text"
                            className={`field__input${fieldErrors.location ? ' is-invalid' : ''}`}
                            value={location}
                            onChange={e => setLocation(e.target.value)}
                            placeholder="City"
                        />
                        {fieldErrors.location && (
                            <span className="field__error">{fieldErrors.location}</span>
                        )}
                    </div>
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="ad-category">Category</label>
                    <select
                        id="ad-category"
                        className={`field__input field__select${fieldErrors.categoryId ? ' is-invalid' : ''}`}
                        value={categoryId}
                        onChange={e => setCategoryId(e.target.value)}
                    >
                        <option value="">Select a category…</option>
                        {categories.map(category => (
                            <option key={category.id} value={category.id}>{category.name}</option>
                        ))}
                    </select>
                    {fieldErrors.categoryId && (
                        <span className="field__error">{fieldErrors.categoryId}</span>
                    )}
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="ad-image">Image URL <span className="field__optional">(optional)</span></label>
                    <input
                        id="ad-image"
                        type="url"
                        className="field__input"
                        value={imageUrl}
                        onChange={e => setImageUrl(e.target.value)}
                        placeholder="https://images.unsplash.com/..."
                    />
                </div>

                <div className="form__actions">
                    <button type="button" className="btn btn--ghost" onClick={onClose}>
                        Cancel
                    </button>
                    <button type="submit" className="btn btn--primary" disabled={submitting}>
                        {submitting ? (
                            <>
                                <span className="spinner spinner--sm" aria-hidden="true" /> Posting…
                            </>
                        ) : (
                            'Publish ad'
                        )}
                    </button>
                </div>
            </form>
        </Modal>
    );
}

export default CreateItemForm;
