import request from './request'

export function login(data) {
  return request.post('/user/login', data)
}

export function logout() {
  return request.post('/user/logout')
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function getUserById(id) {
  return request.get(`/user/${id}`)
}

export function changePassword(data) {
  return request.put('/user/password', data)
}

export function getUserList(keyword) {
  return request.get('/user/list', { params: { keyword } })
}

export function addUser(data) {
  return request.post('/user/add', data)
}

export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}
