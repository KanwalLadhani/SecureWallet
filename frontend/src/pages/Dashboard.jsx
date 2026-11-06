import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getJson } from '../api'
import './Dashboard.css'

function formatMoney(amount) {
  return '$' + Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

export default function Dashboard() {
  const navigate = useNavigate()
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const [balance, setBalance] = useState(null)
  const [balanceError, setBalanceError] = useState('')

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    navigate('/login')
  }

  useEffect(() => {
    async function loadWallet() {
      const result = await getJson('/api/wallet')

      if (result.status === 401) {
        // token expired or invalid
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login')
        return
      }
      if (result.ok) {
        setBalance(result.data.balance)
      } else if (result.status === 404) {
        setBalanceError('No wallet yet')
      } else {
        setBalanceError('Could not load balance')
      }
    }
    loadWallet()
  }, [navigate])

  let balanceText = 'Loading...'
  if (balance !== null) balanceText = formatMoney(balance)
  else if (balanceError) balanceText = balanceError

  return (
    <div className="dash-page">
      <header className="dash-header">
        <span className="dash-logo">SecureWallet</span>
        <button className="dash-logout" onClick={logout}>
          Log out
        </button>
      </header>

      <main className="dash-main">
        <h1 className="dash-title">
          Welcome{user ? `, ${user.fullName}` : ''}
        </h1>
        <p className="dash-sub">
          {user ? `${user.email} · Role: ${user.role}` : 'Not logged in'}
        </p>

        <div className="dash-card">
          <p className="dash-card-label">Your balance</p>
          <p className="dash-card-value">{balanceText}</p>
        </div>

        <button className="dash-send" onClick={() => navigate('/send')}>
          Send money
        </button>

        <button
          className="dash-send dash-history"
          onClick={() => navigate('/history')}
        >
          History
        </button>

        {user?.role === 'ADMIN' && (
          <button
            className="dash-send dash-history"
            onClick={() => navigate('/admin/alerts')}
          >
            Security alerts
          </button>
        )}
      </main>
    </div>
  )
}