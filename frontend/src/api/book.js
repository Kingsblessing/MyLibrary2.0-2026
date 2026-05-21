import request from './request'

export function getBookList(params) {
  return request.get('/book/list', { params })
}

export function getBook(id) {
  return request.get(`/book/${id}`)
}

export function addBook(data) {
  return request.post('/book/add', data)
}

export function updateBook(id, data) {
  return request.put(`/book/${id}`, data)
}

export function deleteBook(id) {
  return request.delete(`/book/${id}`)
}

export function stockIn(data) {
  return request.post('/book/stock-in', data)
}
