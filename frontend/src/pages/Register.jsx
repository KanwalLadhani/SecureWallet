import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import './Login.css'
import { postJson } from '../api'

export default function Register() {
  const navigate = useNavigate()

  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSuccess('')

    if (!fullName || !email || !password || !confirmPassword) {
      setError('Please fill in all fields.')
      return
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setError('')
    setLoading(true)

    const result = await postJson('/api/auth/register', {
      fullName,
      email,
      password,
    })

    setLoading(false)

    if (result.ok) {
      setSuccess('Account created! Taking you to log in...')
      setTimeout(() => navigate('/login'), 1500)
    } else if (result.status === 409) {
      setError('This email is already registered.')
    } else if (result.status === 0) {
      setError('Cannot reach the server. Is the backend running?')
    } else {
      setError('Could not create the account. Please check your details.')
      console.log('Register failed:', result)
    }
  }

  return (
    <div className="login-page">
      <header className="login-header">
        <span className="login-logo">SecureWallet</span>
      </header>

      <main className="login-main">
        <h1 className="login-title">Create your account</h1>
        <p className="login-subtitle">
          Already have an account? <Link to="/login">Log in</Link>
        </p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label htmlFor="fullName">Full name</label>
          <input
            id="fullName"
            type="text"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            autoComplete="name"
          />

          <label htmlFor="email">Your email address</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />

          <label htmlFor="password">Create a password</label>
          <div className="password-wrap">
            <input
              id="password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
            />
            <button
              type="button"
              className="show-btn"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? 'Hide' : 'Show'}
            </button>
          </div>

          <label htmlFor="confirmPassword">Confirm password</label>
          <input
            id="confirmPassword"
            type={showPassword ? 'text' : 'password'}
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            autoComplete="new-password"
          />

          {error && <div className="login-error">{error}</div>}
          {success && <div className="login-success">{success}</div>}

          <button type="submit" className="login-button" disabled={loading}>
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>
      </main>
    </div>
  )
}