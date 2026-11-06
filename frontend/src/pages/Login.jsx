import { useState } from 'react'
import './Login.css'
import { Link, useNavigate } from 'react-router-dom'
import { postJson } from '../api'

export default function Login() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSuccess('')

    if (!email || !password) {
      setError('Please enter your email and password.')
      return
    }

    setError('')
    setLoading(true)

    const result = await postJson('/api/auth/login', { email, password })

    setLoading(false)

    if (result.ok && result.data && result.data.token) {
      localStorage.setItem('token', result.data.token)
      localStorage.setItem('user', JSON.stringify(result.data.user))
      navigate('/dashboard')
    } else if (result.status === 401 || result.status === 403) {
      setError('Wrong email or password.')
    } else if (result.status === 0) {
      setError('Cannot reach the server. Is the backend running?')
    } else {
      setError('Something went wrong. Please try again.')
      console.log('Login failed:', result)
    }
  }

  return (
    <div className="login-page">
      <header className="login-header">
        <span className="login-logo">SecureWallet</span>
      </header>

      <main className="login-main">
        <h1 className="login-title">Welcome back</h1>
        <p className="login-subtitle">
          New to SecureWallet? <Link to="/register">Sign up</Link>
        </p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label htmlFor="email">Your email address</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />

          <label htmlFor="password">Your password</label>
          <div className="password-wrap">
            <input
              id="password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
            <button
              type="button"
              className="show-btn"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? 'Hide' : 'Show'}
            </button>
          </div>

          {error && <div className="login-error">{error}</div>}
          {success && <div className="login-success">{success}</div>}

          <button type="submit" className="login-button" disabled={loading}>
            {loading ? 'Logging in...' : 'Log in'}
          </button>

          <a href="#help" className="login-help">
            Trouble logging in?
          </a>
        </form>
      </main>
    </div>
  )
}