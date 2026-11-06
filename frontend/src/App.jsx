import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Dashboard from './pages/Dashboard.jsx'
import SendMoney from './pages/SendMoney.jsx'
import History from './pages/History.jsx'
import AdminAlerts from './pages/AdminAlerts.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/send"
        element={
          <ProtectedRoute>
            <SendMoney />
          </ProtectedRoute>
        }
      />
      <Route
        path="/history"
        element={
          <ProtectedRoute>
            <History />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/alerts"
        element={
          <ProtectedRoute adminOnly>
            <AdminAlerts />
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}

export default App