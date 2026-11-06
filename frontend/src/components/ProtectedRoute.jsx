import { Navigate } from 'react-router-dom'

export default function ProtectedRoute({ children, adminOnly = false }) {
  const token = localStorage.getItem('token')

  if (!token) {
    return <Navigate to="/login" replace />
  }

  if (adminOnly) {
    let role = null
    try {
      role = JSON.parse(localStorage.getItem('user'))?.role
    } catch {
      role = null
    }

    if (role !== 'ADMIN') {
      return <Navigate to="/dashboard" replace />
    }
  }

  return children
}