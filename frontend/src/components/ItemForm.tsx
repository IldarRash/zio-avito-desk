import React, {useEffect, useState} from 'react';
import {Category, Item, ItemRequest} from '../types/api';
import {api} from '../services/api';
import Modal from './Modal';

interface ItemFormProps {
    categories: Category[];
    /** When provided the form is in edit mode and prefills from this item. */
    item?: Item;
    /** Called with the resulting item after a successful create/edit (+ upload). */
    onSaved: (item: Item) => void;
    onClose: () => void;
}

interface FieldErrors {
    name?: string;
    description?: string;
    price?: string;
    location?: string;
    categoryId?: string;
    image?: string;
}

const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
const ACCEPTED_TYPES = ['image/png', 'image/jpeg', 'image/webp', 'image/gif'];

function ItemForm({categories, item, onSaved, onClose}: ItemFormProps) {
    const isEdit = item !== undefined;
    const [name, setName] = useState<string>(item?.name ?? '');
    const [description, setDescription] = useState<string>(item?.description ?? '');
    const [price, setPrice] = useState<string>(item ? String(item.price) : '');
    const [location, setLocation] = useState<string>(item?.location ?? '');
    const [categoryId, setCategoryId] = useState<string>(item?.categoryId ?? '');
    const [file, setFile] = useState<File | null>(null);
    const [preview, setPreview] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

    // Build/cleanup an object URL for the chosen file preview.
    useEffect(() => {
        if (!file) {
            setPreview(null);
            return;
        }
        const url = URL.createObjectURL(file);
        setPreview(url);
        return () => URL.revokeObjectURL(url);
    }, [file]);

    const existingImage = item && item.imageUrl.length > 0 ? item.imageUrl : null;
    const shownImage = preview ?? existingImage;

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

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
        const chosen = e.target.files?.[0] ?? null;
        if (!chosen) {
            setFile(null);
            return;
        }
        if (!ACCEPTED_TYPES.includes(chosen.type)) {
            setFieldErrors(prev => ({...prev, image: 'Use a PNG, JPEG, WebP or GIF image'}));
            setFile(null);
            return;
        }
        if (chosen.size > MAX_IMAGE_BYTES) {
            setFieldErrors(prev => ({...prev, image: 'Image must be 5MB or smaller'}));
            setFile(null);
            return;
        }
        setFieldErrors(prev => ({...prev, image: undefined}));
        setFile(chosen);
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>): Promise<void> => {
        e.preventDefault();
        setError(null);
        const errors = validate();
        setFieldErrors(errors);
        if (Object.keys(errors).some(k => errors[k as keyof FieldErrors])) {
            return;
        }

        const payload: ItemRequest = {
            name: name.trim(),
            description: description.trim(),
            price: Number(price),
            categoryId,
            location: location.trim(),
        };

        setSubmitting(true);
        try {
            const saved = isEdit
                ? await api.updateItem(item.id, payload)
                : await api.createItem(payload);
            const finalItem = file ? await api.uploadImage(saved.id, file) : saved;
            onSaved(finalItem);
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : 'Failed to save ad');
        } finally {
            setSubmitting(false);
        }
    };

    const title = isEdit ? 'Edit your ad' : 'Post a new ad';

    return (
        <Modal title={title} onClose={onClose} className="modal--form">
            <h2 className="form__title">{title}</h2>
            <p className="form__subtitle">
                {isEdit ? 'Update the details of your listing.' : 'Fill in the details to list your item.'}
            </p>

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
                    <label className="field__label" htmlFor="ad-image">
                        Photo <span className="field__optional">(optional, max 5MB)</span>
                    </label>
                    {shownImage && (
                        <img className="image-preview" src={shownImage} alt="Ad preview" />
                    )}
                    <input
                        id="ad-image"
                        type="file"
                        className="field__file"
                        accept="image/png,image/jpeg,image/webp,image/gif"
                        onChange={handleFileChange}
                    />
                    {fieldErrors.image && <span className="field__error">{fieldErrors.image}</span>}
                </div>

                <div className="form__actions">
                    <button type="button" className="btn btn--ghost" onClick={onClose}>
                        Cancel
                    </button>
                    <button type="submit" className="btn btn--primary" disabled={submitting}>
                        {submitting ? (
                            <>
                                <span className="spinner spinner--sm" aria-hidden="true" />{' '}
                                {isEdit ? 'Saving…' : 'Posting…'}
                            </>
                        ) : isEdit ? (
                            'Save changes'
                        ) : (
                            'Publish ad'
                        )}
                    </button>
                </div>
            </form>
        </Modal>
    );
}

export default ItemForm;
