import {
    Category,
    Item,
    ItemQuery,
    ItemRequest,
    LoginRequest,
    Page,
    RegisterRequest,
    User,
} from '../types/api';

/**
 * Error thrown for any non-2xx API response. `status` is the HTTP code and
 * `message` is the server-provided `{ error }` text when present.
 */
export class ApiError extends Error {
    readonly status: number;

    constructor(status: number, message: string) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}

async function parseError(res: Response): Promise<never> {
    let message = `Request failed (${res.status})`;
    try {
        const body = (await res.json()) as { error?: unknown };
        if (body && typeof body.error === 'string') {
            message = body.error;
        }
    } catch {
        /* Body was not JSON; keep the generic message. */
    }
    throw new ApiError(res.status, message);
}

async function request<T>(input: string, init?: RequestInit): Promise<T> {
    const res = await fetch(input, {credentials: 'include', ...init});
    if (!res.ok) {
        return parseError(res);
    }
    return res.json() as Promise<T>;
}

async function requestVoid(input: string, init?: RequestInit): Promise<void> {
    const res = await fetch(input, {credentials: 'include', ...init});
    if (!res.ok) {
        await parseError(res);
    }
}

function jsonInit(method: string, body: unknown): RequestInit {
    return {
        method,
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body),
    };
}

function buildItemsUrl(query: ItemQuery): string {
    const params = new URLSearchParams();
    if (query.categoryId) params.set('categoryId', query.categoryId);
    if (query.q && query.q.trim()) params.set('q', query.q.trim());
    if (query.minPrice !== undefined) params.set('minPrice', String(query.minPrice));
    if (query.maxPrice !== undefined) params.set('maxPrice', String(query.maxPrice));
    if (query.location && query.location.trim()) params.set('location', query.location.trim());
    if (query.sort) params.set('sort', query.sort);
    if (query.limit !== undefined) params.set('limit', String(query.limit));
    if (query.offset !== undefined) params.set('offset', String(query.offset));
    const qs = params.toString();
    return qs ? `/items?${qs}` : '/items';
}

export const api = {
    // ----- Items -----
    listItems(query: ItemQuery): Promise<Page<Item>> {
        return request<Page<Item>>(buildItemsUrl(query));
    },

    getItem(id: string): Promise<Item> {
        return request<Item>(`/items/${id}`);
    },

    createItem(body: ItemRequest): Promise<Item> {
        return request<Item>('/items', jsonInit('POST', body));
    },

    updateItem(id: string, body: ItemRequest): Promise<Item> {
        return request<Item>(`/items/${id}`, jsonInit('PUT', body));
    },

    uploadImage(id: string, file: File): Promise<Item> {
        return request<Item>(`/items/${id}/image`, {
            method: 'POST',
            headers: {'Content-Type': file.type},
            body: file,
        });
    },

    deleteItem(id: string): Promise<void> {
        return requestVoid(`/items/${id}`, {method: 'DELETE'});
    },

    // ----- Categories -----
    listCategories(): Promise<Category[]> {
        return request<Category[]>('/categories');
    },

    // ----- Auth -----
    me(): Promise<User> {
        return request<User>('/auth/me');
    },

    register(body: RegisterRequest): Promise<User> {
        return request<User>('/auth/register', jsonInit('POST', body));
    },

    login(body: LoginRequest): Promise<User> {
        return request<User>('/auth/login', jsonInit('POST', body));
    },

    logout(): Promise<void> {
        return requestVoid('/auth/logout', {method: 'POST'});
    },
};
