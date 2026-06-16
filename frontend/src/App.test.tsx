import React from 'react';
import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from './App';
import {AuthProvider} from './context/AuthContext';
import {Category, Item, Page} from './types/api';

const CATEGORIES: Category[] = [{id: 'cat-1', name: 'Electronics'}];

const ITEMS: Item[] = [
    {
        id: 'item-1',
        name: 'Vintage Camera',
        description: 'A lovely old camera',
        price: 120,
        categoryId: 'cat-1',
        location: 'Berlin',
        imageUrl: '',
        createdAt: '2026-01-01T00:00:00Z',
        ownerId: null,
    },
];

function jsonResponse(body: unknown, status = 200): Response {
    return {
        ok: status >= 200 && status < 300,
        status,
        json: () => Promise.resolve(body),
    } as unknown as Response;
}

/** Routes a fetch call to canned responses based on the URL/method. */
function mockFetch(): jest.Mock {
    return jest.fn((input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
        const url = typeof input === 'string' ? input : input.toString();
        const method = init?.method ?? 'GET';

        if (url.startsWith('/auth/me')) {
            return Promise.resolve(jsonResponse({error: 'unauthorized'}, 401));
        }
        if (url.startsWith('/categories')) {
            return Promise.resolve(jsonResponse(CATEGORIES));
        }
        if (url.startsWith('/items') && method === 'GET') {
            const page: Page<Item> = {items: ITEMS, total: ITEMS.length, limit: 24, offset: 0};
            return Promise.resolve(jsonResponse(page));
        }
        return Promise.resolve(jsonResponse({error: 'not found'}, 404));
    });
}

function renderApp(): void {
    render(
        <AuthProvider>
            <App />
        </AuthProvider>,
    );
}

beforeEach(() => {
    global.fetch = mockFetch() as unknown as typeof fetch;
});

afterEach(() => {
    jest.restoreAllMocks();
});

test('board renders items fetched from the API', async () => {
    renderApp();
    expect(await screen.findByText('Vintage Camera')).toBeInTheDocument();
    expect(screen.getByText('Berlin')).toBeInTheDocument();
});

test('logged-out navbar shows Log in and Sign up actions', async () => {
    renderApp();
    expect(await screen.findByRole('button', {name: /log in/i})).toBeInTheDocument();
    expect(screen.getByRole('button', {name: /sign up/i})).toBeInTheDocument();
});

test('register form shows a validation error when fields are empty', async () => {
    renderApp();

    const signUp = await screen.findByRole('button', {name: /sign up/i});
    userEvent.click(signUp);

    const submit = await screen.findByRole('button', {name: /create account/i});
    userEvent.click(submit);

    expect(await screen.findByText(/pick a display name/i)).toBeInTheDocument();
    expect(screen.getByText(/enter your email/i)).toBeInTheDocument();
});

test('item form shows a validation error on empty name', async () => {
    // Logged-in user so the "New ad" button is available.
    global.fetch = jest.fn((input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
        const url = typeof input === 'string' ? input : input.toString();
        const method = init?.method ?? 'GET';
        if (url.startsWith('/auth/me')) {
            return Promise.resolve(
                jsonResponse({
                    id: 'user-1',
                    email: 'demo@avito.example',
                    displayName: 'Demo',
                    createdAt: '2026-01-01T00:00:00Z',
                }),
            );
        }
        if (url.startsWith('/categories')) {
            return Promise.resolve(jsonResponse(CATEGORIES));
        }
        if (url.startsWith('/items') && method === 'GET') {
            const page: Page<Item> = {items: ITEMS, total: ITEMS.length, limit: 24, offset: 0};
            return Promise.resolve(jsonResponse(page));
        }
        return Promise.resolve(jsonResponse({error: 'not found'}, 404));
    }) as unknown as typeof fetch;

    renderApp();

    const newAd = await screen.findByRole('button', {name: /new ad/i});
    userEvent.click(newAd);

    const publish = await screen.findByRole('button', {name: /publish ad/i});
    userEvent.click(publish);

    expect(await screen.findByText(/give your ad a name/i)).toBeInTheDocument();
});

test('logged-out user does not see the New ad button', async () => {
    renderApp();
    await waitFor(() => expect(screen.getByText('Vintage Camera')).toBeInTheDocument());
    expect(screen.queryByRole('button', {name: /new ad/i})).not.toBeInTheDocument();
});
