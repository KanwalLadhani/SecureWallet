import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import './Login.css'
import './History.css'
import { getJson } from '../api'

// Reads the email stored inside the JWT (display only, not for security)
function getMyEmail() {
  try {
    const token = localStorage.getItem('token')
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(payload)).sub
  } catch {
    return ''
  }
}

export default function History() {
  const navigate = useNavigate()
  const myEmail = getMyEmail()

  const [transactions, setTransactions] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      const result = await getJson('/api/transactions')
      setLoading(false)

      if (result.ok) {
        setTransactions(result.data)
      } else if (result.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login')
      } else if (result.status === 0) {
        setError('Cannot reach the server. Is the backend running?')
      } else {
        setError('Could not load your transactions. Please try again.')
        console.log('History failed:', result)
      }
    }
    load()
  }, [navigate])

  return (
    <div className="login-page">
      <header className="login-header">
        <span className="login-logo">SecureWallet</span>
      </header>

      <main className="history-main">
        <h1 className="login-title">Transaction history</h1>
        <p className="login-subtitle">
          <Link to="/dashboard">Back to dashboard</Link>
        </p>

        {loading && <p>Loading...</p>}
        {error && <div className="login-error">{error}</div>}
        {!loading && !error && transactions.length === 0 && (
          <p>No transactions yet.</p>
        )}

        {transactions.length > 0 && (
          <div className="history-table-wrap">
            <table className="history-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Type</th>
                  <th>With</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Note</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((t) => {
                  const sent = t.senderEmail === myEmail
                  return (
                    <tr key={t.id}>
                      <td>{new Date(t.createdAt).toLocaleString()}</td>
                      <td>{sent ? 'Sent' : 'Received'}</td>
                      <td>{sent ? t.receiverEmail : t.senderEmail}</td>
                      <td className={sent ? 'amount-out' : 'amount-in'}>
                        {sent ? '-' : '+'}${Number(t.amount).toFixed(2)}
                      </td>
                      <td>
                        <span className={`status status-${t.status.toLowerCase()}`}>
                          {t.status}
                        </span>
                      </td>
                      <td>{t.note || '-'}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  )
}