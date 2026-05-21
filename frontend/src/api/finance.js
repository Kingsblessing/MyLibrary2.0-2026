import request from './request'

export function getBills(params) {
  return request.get('/finance/bills', { params })
}

export function getReport(params) {
  return request.get('/finance/report', { params })
}
