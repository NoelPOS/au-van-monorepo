export type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  error?: string;
  message?: string;
};

export type PageResponse<T> = {
  content: T[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
};
