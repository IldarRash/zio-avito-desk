export interface Item {
  id: string;
  name: string;
  categoryId: string;
}

export interface Category {
  id: string;
  name: string;
}

export interface CreateItemRequest {
  name: string;
  categoryId: string;
}

export interface UpdateItemRequest {
  id: string;
  name: string;
  categoryId: string;
}

export interface CreateCategoryRequest {
  name: string;
}

export interface UpdateCategoryRequest {
  id: string;
  name: string;
}