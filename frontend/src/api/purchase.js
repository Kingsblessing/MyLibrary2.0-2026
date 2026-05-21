import request from './request'

export function getPurchaseList(params) {
  return request.get('/purchase/list', { params })
}

export function getPurchase(id) {
  return request.get(`/purchase/${id}`)
}

export function addPurchase(data) {
  return request.post('/purchase/add', data)
}

export function payPurchase(id) {
  return request.put(`/purchase/${id}/pay`)
}

export function returnPurchase(id) {
  return request.put(`/purchase/${id}/return`)
}
