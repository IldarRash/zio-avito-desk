### Техническая документация: Frontend для Авито‑клона

#### Цель
Создать современный, отзывчивый и безопасный SPA‑интерфейс для платформы онлайн‑объявлений (аналог Авито) с поддержкой ленты объявлений, карточек, поиска/фильтров, чатов и (опционально) админ‑панели.

### Архитектура
- Тип: SPA
- Язык: TypeScript
- Фреймворк: React 18
- Роутинг: React Router v6
- API: GraphQL (Apollo Client), файлы — через REST/Axios
- Управление состоянием:
  - Apollo Client cache — серверное состояние (GraphQL)
  - Redux Toolkit — клиентское/кросс‑страничное UI‑состояние (например, фильтры, флаги, модалки)
- Формы: React Hook Form + Zod (валидация схем)
- UI: Material UI (MUI) + кастомные SCSS‑модули (или Tailwind, по выбору)
- Стилизация: SCSS modules (или Tailwind) + MUI Theme
- Аутентификация: JWT (access в httpOnly cookie, refresh по необходимости)
- Реал‑тайм: GraphQL Subscriptions (WebSocket) для чата
- Сборка: Vite (или CRA), ENV‑конфиги через `.env`
- Логи/метрики: Sentry (опц.), Web Vitals

### Структура проекта (предложение)
```
/src
├── assets/                 # статические ресурсы (иконки, изображения)
├── components/             # переиспользуемые компоненты (UI‑кит, формы, карточки)
├── pages/                  # страницы роутинга (route‑level components)
│   ├── Auth/               # Login, Register, ResetPassword
│   ├── Home/               # лента объявлений
│   ├── Listing/            # ItemDetail, Create, Edit
│   ├── Profile/            # профиль пользователя, мои объявления
│   ├── Categories/         # список и дерево категорий
│   ├── Chat/               # список диалогов, окно чата
│   └── Admin/              # опционально, лениво подгружается
├── services/               # API‑клиенты, Apollo, Axios, хелперы
│   ├── apollo.ts
│   ├── http.ts
│   └── auth.ts
├── store/                  # Redux Toolkit: slices, middleware
│   ├── index.ts
│   ├── authSlice.ts
│   ├── filtersSlice.ts
│   └── uiSlice.ts
├── graphql/                # схемы, фрагменты, запросы/мутации/сабскрипшены
│   ├── fragments.gql
│   ├── queries.gql
│   ├── mutations.gql
│   └── subscriptions.gql
├── styles/                 # глобальные стили, темы MUI/SCSS
│   ├── index.scss
│   └── theme.ts
├── App.tsx
└── main.tsx
```

### Роутинг
- `/` — Home (лента с поиском и фильтрами)
- `/login`, `/register`, `/reset-password` — аутентификация
- `/profile` — профиль и мои объявления (PrivateRoute)
- `/listing/:id` — карточка объявления
- `/listing/new` — создание (PrivateRoute)
- `/listing/:id/edit` — редактирование (PrivateRoute, автор + админ)
- `/categories` — список/дерево категорий
- `/chat` — список чатов (PrivateRoute)
- `/admin` — админка (PrivateRoute, роль admin), lazy

Компонент `ProtectedRoute` проверяет JWT (по cookie) и роль пользователя (из `authSlice`).

### Графическая тема и UI
- MUI Theme (светлая/тёмная), палитра и типографика в `styles/theme.ts`
- Унифицированные компоненты: `Button`, `TextField`, `Select`, `Chip`, `Dialog`, `Card`, `Avatar`, `Badge`
- Карточка объявления: изображение, название, цена, локация, категория, дата, избранное

### GraphQL API (клиент)
- Клиент: Apollo Client
- Транспорт: HTTP для queries/mutations, WebSocket для subscriptions
- Авторизация: автоматически прикреплять `X-CSRF` и cookie; если бэкенд требует Bearer — отправлять из cookie (или memory) через `setContext`

Пример схемы (упрощённо, для ориентира):
```graphql
# types
scalar Upload

type User { id: ID!, name: String!, email: String!, role: String! }

type Category { id: ID!, name: String!, parentId: ID, children: [Category!]! }

type Listing {
  id: ID!
  title: String!
  description: String!
  price: Float!
  location: String!
  images: [String!]!
  category: Category!
  owner: User!
  createdAt: String!
}

type ChatMessage { id: ID!, chatId: ID!, from: User!, text: String!, createdAt: String! }

type Query {
  me: User
  listings(
    q: String, categoryId: ID, minPrice: Float, maxPrice: Float, location: String,
    limit: Int = 20, offset: Int = 0
  ): [Listing!]!
  listing(id: ID!): Listing
  categories: [Category!]!
}

type Mutation {
  login(email: String!, password: String!): Boolean
  register(name: String!, email: String!, password: String!): Boolean
  createListing(input: CreateListingInput!): Listing!
  updateListing(id: ID!, input: UpdateListingInput!): Listing!
  deleteListing(id: ID!): Boolean!
  uploadListingImage(listingId: ID!, file: Upload!): String!
}

input CreateListingInput {
  title: String!
  description: String!
  price: Float!
  location: String!
  categoryId: ID!
}

input UpdateListingInput {
  title: String
  description: String
  price: Float
  location: String
  categoryId: ID
}

type Subscription { messageAdded(chatId: ID!): ChatMessage! }
```

Пример клиентской настройки Apollo:
```ts
// services/apollo.ts
import { ApolloClient, InMemoryCache, split, HttpLink } from '@apollo/client';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { getMainDefinition } from '@apollo/client/utilities';

const httpLink = new HttpLink({ uri: import.meta.env.VITE_API_URL + '/graphql', credentials: 'include' });

const wsLink = new GraphQLWsLink(createClient({
  url: (import.meta.env.VITE_WS_URL ?? '').replace('http', 'ws') + '/graphql',
  connectionParams: async () => ({ /* CSRF, locale etc. */ })
}));

const splitLink = split(
  ({ query }) => {
    const def = getMainDefinition(query);
    return def.kind === 'OperationDefinition' && def.operation === 'subscription';
  },
  wsLink,
  httpLink
);

export const apollo = new ApolloClient({
  link: splitLink,
  cache: new InMemoryCache(),
});
```

Пример запроса листинга:
```graphql
# graphql/queries.gql
query Listing($id: ID!) {
  listing(id: $id) {
    id title price location
    images
    owner { id name }
    category { id name }
  }
}
```

### REST/Axios (загрузка файлов, служебные вызовы)
```ts
// services/http.ts
import axios from 'axios';
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
});
```

Загрузка изображения:
```ts
export async function uploadListingImage(listingId: string, file: File) {
  const form = new FormData();
  form.append('file', file);
  return http.post(`/files/listings/${listingId}`, form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}
```

### Redux Toolkit (пример срезов)
- `authSlice`: токены/пользователь, флаги аутентификации
- `filtersSlice`: ключевые слова, категория, диапазон цены, локация
- `uiSlice`: глобальные модалки, уведомления, прогресс

```ts
// store/authSlice.ts
import { createSlice } from '@reduxjs/toolkit';

interface AuthState { isAuth: boolean; user?: { id: string; name: string; role: string } }
const initialState: AuthState = { isAuth: false };

export const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setUser(state, { payload }) { state.isAuth = true; state.user = payload; },
    signOut(state) { state.isAuth = false; state.user = undefined; },
  }
});
```

### Основные модули
1) Пользователь
- Регистрация/авторизация: GraphQL `register`, `login` (ответ через cookie httpOnly). CSRF — двойная отправка (cookie + header `X-CSRF`).
- Профиль/мои объявления: `me`, `listings(ownerId: ME)`
- Сброс пароля: запрос письма, подтверждение токена, ввод нового пароля

2) Объявления
- Лента: бесконечная прокрутка (IntersectionObserver), кеширование Apollo, дебаунс поиска
- Карточка: детальная страница с галереей, похожие объявления
- Создание/редактирование: React Hook Form + Zod, предпросмотр изображений, drag‑and‑drop
- Изображения: upload через Axios/REST + хранение ссылок в GraphQL

3) Поиск и фильтрация
- Полнотекстовый поиск `q`, фильтры: категория, цена (min/max), локация
- Состояние фильтров — в `filtersSlice`, синхронизация с URL (query string)

4) Категории
- Дерево категорий (lazy‑load подкатегорий), выбор в форме создания

5) Чат
- Список диалогов, список сообщений, отправка/приём через GraphQL Subscriptions
- Оптимистичные апдейты, прокрутка к последнему сообщению

6) Админка (опционально)
- Управление категориями/пользователями/объявлениями, отдельная роль `admin`
- Отдельный layout, ленивые чанки

### Безопасность
- JWT в httpOnly cookie, `SameSite=Lax/Strict`, `Secure` в проде
- CSRF: двойная отправка токена (cookie + `X-CSRF`), проверки на сервере
- XSS: экранирование вывода, sanitizer для HTML‑описаний, CSP заголовки
- Валидация форм через Zod, серверная повторная валидация
- Ограничение размера файлов, проверка MIME, антивирус (по возможности)
- Контроль ролей: маршруты и UI на основе ролей, guard на сервере

### Производительность
- Код‑сплиттинг страниц (React.lazy + Suspense)
- Мемоизация (`useMemo`, `useCallback`), `React.memo`
- Виртуализация списков (react‑window) для ленты
- Изображения: responsive, lazy loading, WebP/AVIF, CDN

### Доступность и i18n
- ARIA‑атрибуты, контраст, фокус‑стили
- Локализация (i18next), перевод UI строк

### Тестирование
- Unit: Jest + React Testing Library
- E2E: Cypress
- Визуальные снапшоты (опц.): Chromatic/Storybook

### Конфигурация окружений
- `.env` переменные:
  - `VITE_API_URL=https://api.example.com`
  - `VITE_WS_URL=wss://api.example.com`
  - `VITE_SENTRY_DSN=...` (опц.)

### CI/CD (GitHub Actions)
- Линт/тайпчек/сборка на PR: `npm ci && npm run lint && npm run typecheck && npm run build`
- Публикация артефактов сборки (опц.) и деплой (Vercel/Netlify/S3 + CloudFront)

### Гайд по код‑стайлу
- ESLint + Prettier, коммиты по Conventional Commits
- Именование: PascalCase для компонентов, camelCase для переменных/функций
- Файлы компонентов: по одному компоненту на файл, рядом тест и стили

### Минимальные примеры использования
Компонент ленты c поиском (высокоуровнево):
```tsx
function FeedPage() {
  const [q, setQ] = useState('');
  const { data, fetchMore } = useListingsQuery({ variables: { q, limit: 20, offset: 0 } });
  // IntersectionObserver → fetchMore({ variables: { offset: data.listings.length } })
  return (
    <>
      <SearchBar value={q} onChange={setQ} />
      <ListingGrid items={data?.listings ?? []} />
    </>
  );
}
```

Эта документация служит ориентиром для реализации фронтенда. Конкретные детали (набор полей, роуты, схема GraphQL) должны быть синхронизированы с бекенд‑командой перед интеграцией в продакшен.
