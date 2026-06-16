export interface Item {
    id: string;
    name: string;
    description: string;
    price: number;
    categoryId: string;
    location: string;
    imageUrl: string;
    createdAt: string;
    ownerId: string | null;
}

export interface Category {
    id: string;
    name: string;
}

export interface User {
    id: string;
    email: string;
    displayName: string;
    createdAt: string;
}

export interface Page<T> {
    items: T[];
    total: number;
    limit: number;
    offset: number;
}

export interface ItemRequest {
    name: string;
    description: string;
    price: number;
    categoryId: string;
    location: string;
    imageUrl?: string;
}

export type SortOption = 'newest' | 'price_asc' | 'price_desc';

export interface ItemQuery {
    categoryId?: string;
    q?: string;
    minPrice?: number;
    maxPrice?: number;
    location?: string;
    sort?: SortOption;
    limit?: number;
    offset?: number;
}

export interface RegisterRequest {
    email: string;
    password: string;
    displayName: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}
