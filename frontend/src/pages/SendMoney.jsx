import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import './Login.css'
import { postJsonAuth } from '../api'

export default function SendMoney() {
  const navigate = useNavigate()

  const [receiverEmail, setReceiverEmail] = useState('')
  const [amount, setAmount] = useState('')
  const [note, setNote] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSuccess('')

    if (!receiverEmail || !amount) {
      setError('Please enter the receiver email and the amount.')
      return
    }
    if (Number(amount) <= 0) {
      setError('Amount must be greater than 0.')
      return
    }

    setError('')
    setLoading(true)

    const result = await postJsonAuth('/api/transactions/transfer', {
      receiverEmail,
      amount: Number(amount),
      note,
    })

    setLoading(false)

    if (result.ok) {
      setSuccess(
        `Sent $${Number(result.data.amount).toFixed(2)} to ${result.data.receiverEmail}.`
      )
      setReceiverEmail('')
      setAmount('')
      setNote('')
    } else if (result.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      navigate('/login')
    } else if (result.status === 404) {
      setError('No user found with that email.')
    } else if (result.status === 400) {
      setError(
        'Transfer rejected. You cannot send to yourself, send more than your balance, or use an invalid amount.'
      )
    } else if (result.status === 0) {
      setError('Cannot reach the server. Is the backend running?')
    } else {
      setError('Something went wrong. Please try again.')
      console.log('Transfer failed:', result)
    }
  }

  return (
    <div className="login-page">
      <header className="login-header">
        <span className="login-logo">SecureWallet</span>
      </header>

      <main className="login-main">
        <h1 className="login-title">Send money</h1>
        <p className="login-subtitle">
          <Link to="/dashboard">Back to dashboard</Link>
        </p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label htmlFor="receiverEmail">Receiver email</label>
          <input
            id="receiverEmail"
            type="email"
            value={receiverEmail}
            onChange={(e) => setReceiverEmail(e.target.value)}
            autoComplete="off"
          />

          <label htmlFor="amount">Amount</label>
          <input
            id="amount"
            type="number"
            step="0.01"
            min="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
          />

          <label htmlFor="note">Note (optional)</label>
          <input
            id="note"
            type="text"
            maxLength={140}
            value={note}
            onChange={(e) => setNote(e.target.value)}
          />

          {error && <div className="login-error">{error}</div>}
          {success && <div className="login-success">{success}</div>}

          <button type="submit" className="login-button" disabled={loading}>
            {loading ? 'Sending...' : 'Send money'}
          </button>
        </form>
      </main>
    </div>
  )
}