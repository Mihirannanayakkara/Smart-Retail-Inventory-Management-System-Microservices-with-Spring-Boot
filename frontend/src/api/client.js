import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:9090/gateway',
  headers: { 'Content-Type': 'application/json' },
});

export const userApi = {
  getAll: () => api.get('/users'),
  getById: (id) => api.get(`/users/${id}`),
  register: (data) => api.post('/users/register', data),
  login: (data) => api.post('/users/login', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  delete: (id) => api.delete(`/users/${id}`),
  activate: (id) => api.put(`/users/${id}/activate`),
  deactivate: (id) => api.put(`/users/${id}/deactivate`),
};

export const productApi = {
  getAll: () => api.get('/products'),
  getById: (id) => api.get(`/products/${id}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: (id) => api.delete(`/products/${id}`),
  increaseStock: (id, quantity) => api.put(`/products/${id}/increase-stock`, { quantity }),
  decreaseStock: (id, quantity) => api.put(`/products/${id}/decrease-stock`, { quantity }),
  getLowStock: () => api.get('/products/low-stock'),
  getByCategory: (cat) => api.get(`/products/category/${cat}`),
  search: (name) => api.get('/products/search', { params: { name } }),
};

export const orderApi = {
  getAll: () => api.get('/orders'),
  getById: (id) => api.get(`/orders/${id}`),
  create: (data) => api.post('/orders', data),
  update: (id, data) => api.put(`/orders/${id}`, data),
  delete: (id) => api.delete(`/orders/${id}`),
  cancel: (id) => api.put(`/orders/${id}/cancel`),
  complete: (id) => api.put(`/orders/${id}/complete`),
  getByUser: (userId) => api.get(`/orders/user/${userId}`),
  getByStatus: (status) => api.get(`/orders/status/${status}`),
};

export const supplierApi = {
  getAll: () => api.get('/suppliers'),
  getById: (id) => api.get(`/suppliers/${id}`),
  create: (data) => api.post('/suppliers', data),
  update: (id, data) => api.put(`/suppliers/${id}`, data),
  delete: (id) => api.delete(`/suppliers/${id}`),
};

export const restockApi = {
  getAll: () => api.get('/restocks'),
  getById: (id) => api.get(`/restocks/${id}`),
  create: (data) => api.post('/restocks', data),
  update: (id, data) => api.put(`/restocks/${id}`, data),
  delete: (id) => api.delete(`/restocks/${id}`),
  approve: (id) => api.put(`/restocks/${id}/approve`),
  cancel: (id) => api.put(`/restocks/${id}/cancel`),
  markDelivered: (id) => api.put(`/restocks/${id}/mark-delivered`),
  getByStatus: (status) => api.get(`/restocks/status/${status}`),
};

export const notificationApi = {
  getAll: () => api.get('/notifications'),
  getById: (id) => api.get(`/notifications/${id}`),
  delete: (id) => api.delete(`/notifications/${id}`),
  getByType: (type) => api.get(`/notifications/type/${type}`),
};

export default api;
