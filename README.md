# SecureWallet

A banking/wallet web app with built-in security automation. Built for the Information Security course.

Users can register, log in, see their balance, send money and view their transaction history. Behind the scenes, three **n8n agents** watch for suspicious activity and email the admin. An **admin-only page** shows all security alerts.

## Features

- Register and login with JWT authentication and roles (USER / ADMIN)
- Wallet with balance, send money, transaction history
- **Login Guard**: detects 5 failed logins in 5 minutes for one email and raises an alert
- **Fraud Detector**: checks every transfer and marks it COMPLETED, FLAGGED or BLOCKED
- **Alert Bot**: emails the admin whenever an alert is raised
- **Admin security alerts page**: table of all alerts, visible only to ADMIN users
- n8n webhooks protected with a shared secret header (`X-Webhook-Secret`)

## Tech stack

| Part | Technology |
|------|------------|
| Frontend | React + Vite |
| Backend | Spring Boot (Java 21) |
| Database | PostgreSQL 16 (Docker) |
| Security agents | n8n (Docker) |
| Alert emails | Gmail SMTP |

## How the pieces talk to each other

```
Browser (React, :5173)
   |
   v
Spring Boot (:8080) ----> PostgreSQL (:5433 on your PC)
   |
   |  X-Webhook-Secret header
   v
n8n (:5678)  [Login Guard, Fraud Detector, Alert Bot]
   |
   +--> PostgreSQL (saves attempts and alerts)
   +--> Gmail (emails the admin)
```

## Prerequisites

Install these first:

| Tool | Version | Check command (PowerShell) |
|------|---------|----------------------------|
| Docker Desktop | recent | `docker --version` |
| Java JDK | 21 | `java -version` |
| Node.js | 20 or newer | `node --version` |
| Git | any | `git --version` |

You also need a **Gmail account for the Alert Bot**. A dedicated new account is best, not your personal one.

## Getting started

Follow these steps in order.

### 1. Get the code

```powershell
git clone https://github.com/<your-username>/SecureWallet.git
cd SecureWallet
```

All commands below are run from this project folder (the "project root") unless a step says otherwise. In VS Code, **Terminal → New Terminal** opens in the project root.

### 2. Configure secrets (.env)

Secrets are kept in a `.env` file, which is **git-ignored** and never committed. A template with no real values is provided as `.env.example`.

1. Copy the template (PowerShell):

```powershell
Copy-Item .env.example .env
```

2. Open `.env` in VS Code and replace every placeholder with a real value.

| Variable | What it is |
|----------|------------|
| `DB_PASSWORD` | Password for the PostgreSQL user `wallet_admin` |
| `JWT_SECRET` | Secret used to sign login tokens. Long random string, 32+ characters |
| `N8N_WEBHOOK_SECRET` | Shared secret sent in the `X-Webhook-Secret` header. Spring Boot and n8n must use the **same** value |

Generate a strong random value for each one (PowerShell):

```powershell
-join ((1..32) | ForEach-Object { '{0:x2}' -f (Get-Random -Maximum 256) })
```

> **Set `DB_PASSWORD` before the first `docker compose up`.** PostgreSQL stores the password inside its data volume the first time it starts. Changing `DB_PASSWORD` later does not change the real database password.

Never commit `.env` and never share it.

### 3. Create a Gmail App Password for the Alert Bot

1. Turn on **2-Step Verification** for the Alert Bot's Gmail account.
2. Create a Google **App Password** (Google Account → Security → App passwords). Google shows a 16-character password once. Copy it and keep it safe.

Gmail will **not** accept your normal password for SMTP when 2-Step Verification is on. You must use the App Password. Never commit it to git.

### 4. Start PostgreSQL and n8n

Make sure Docker Desktop is **running** (whale icon in the system tray), then from the project root:

```powershell
docker compose up -d
```

Check that both containers are up:

```powershell
docker compose ps
```

You should see `securewallet-db` and `securewallet-n8n` with status `Up`.

| Service | Address |
|---------|---------|
| PostgreSQL | `127.0.0.1:5433` (database `securewallet`, user `wallet_admin`) |
| n8n | http://localhost:5678 |

To stop them at the end of the day:

```powershell
docker compose stop
```

### 5. Set up n8n (first time only)

The workflow files are in the `n8n-workflows/` folder. n8n does not export passwords, so you must create the credentials yourself **before** importing.

1. Open http://localhost:5678 and create the n8n owner account when asked.
2. Create the **webhook secret credential**: Credentials → Create credential → **Header Auth**.

| Field | Value |
|-------|-------|
| Credential name | `SecureWallet Webhook Secret` (exactly this name) |
| Name | `X-Webhook-Secret` |
| Value | the same value as `N8N_WEBHOOK_SECRET` in your `.env` |

To copy the secret from `.env` to your clipboard without showing it (PowerShell, from the project root):

```powershell
((Get-Content .env | Where-Object { $_ -match '^N8N_WEBHOOK_SECRET=' }) -replace '^N8N_WEBHOOK_SECRET=','').Trim() | Set-Clipboard
```

3. Create the **SMTP credential** for the Alert Bot: Credentials → Create credential → **SMTP**.

| Field | Value |
|-------|-------|
| User | the full Gmail address of the Alert Bot account |
| Password | the 16-character **App Password** from step 3 (not the normal password) |
| Host | `smtp.gmail.com` |
| Port | `465` |
| SSL/TLS | ON |

Name it (for example `SecureWallet SMTP`) and save.

4. Import each file: open Workflows, use the menu at the top right, choose **Import from file**, and select:
   - `n8n-workflows/login-guard.json`
   - `n8n-workflows/fraud-detector.json`
   - `n8n-workflows/alert-bot.json`
5. In every imported workflow, open each node that shows a warning and select the credential you just created (`SecureWallet Webhook Secret`, and the SMTP credential for the Send Email node).
6. In **Alert Bot**, open the Send Email node and replace two placeholders:
   - `ALERT_SENDER_EMAIL_HERE` in the **From** field with the Alert Bot Gmail address (the same one used in the SMTP credential)
   - `ADMIN_EMAIL_HERE` in the **To** field with the admin's real email address
7. Save each workflow and switch it to **Active** (toggle at the top right). Inactive workflows do not receive requests.

### 6. Start the Spring Boot backend

Open a **new** PowerShell window in the project root (keep it open while the app runs):

```powershell
cd backend
.\mvnw spring-boot:run
```

The first run downloads dependencies, so it can take a few minutes. The backend is ready when the log shows a line like `Started ... in ... seconds`. It listens on http://localhost:8080.

Stop it any time with `Ctrl + C`.

The backend reads `DB_PASSWORD`, `JWT_SECRET` and `N8N_WEBHOOK_SECRET` automatically from the `.env` file in the project root (it is loaded through `spring.config.import` in `application.properties`). Run the command from the `backend` folder, as shown above.

> **If startup fails with** `Could not resolve placeholder 'DB_PASSWORD'` (or `JWT_SECRET`, or `N8N_WEBHOOK_SECRET`), the `.env` file is missing or the variable is not in it. Check that `.env` exists in the project root and that all three variables are filled in.

### 7. Start the React frontend

Open another **new** PowerShell window in the project root:

```powershell
cd frontend
npm install
npm run dev
```

`npm install` downloads the packages. You only need it the first time (and again if `package.json` changes). `npm run dev` starts the Vite dev server.

Open http://localhost:5173/ in your browser.

Stop it any time with `Ctrl + C`.

### 8. Verify it works

With Docker running and the Fraud Detector workflow **Active**, run these from the project root (PowerShell). A sample request is in `n8n-workflows/samples/fraud-test.json`.

**1. Without the secret, the request must be rejected:**

```powershell
try {
  Invoke-RestMethod -Method Post -Uri "http://localhost:5678/webhook/fraud-check" -ContentType "application/json" -InFile "n8n-workflows\samples\fraud-test.json"
} catch { "Rejected with status: " + $_.Exception.Response.StatusCode.value__ }
```

Expected: `Rejected with status: 403`

**2. With the secret, the request must work:**

```powershell
$secret = ((Get-Content .env | Where-Object { $_ -match '^N8N_WEBHOOK_SECRET=' }) -replace '^N8N_WEBHOOK_SECRET=','').Trim()
Invoke-RestMethod -Method Post -Uri "http://localhost:5678/webhook/fraud-check" -ContentType "application/json" -Headers @{ "X-Webhook-Secret" = $secret } -InFile "n8n-workflows\samples\fraud-test.json"
```

Expected:

```
decision reason
-------- ------
FLAGGED  Large amount: 900
```

A flagged transfer also triggers the Alert Bot, which emails the admin.

## How the backend talks to n8n

The Spring Boot backend calls these n8n webhooks (set in `application.properties`):

| Agent | Property | Address |
|-------|----------|---------|
| Login Guard | `securewallet.n8n.login-guard-url` | `http://localhost:5678/webhook/login-guard` |
| Fraud Detector | `securewallet.n8n.fraud-check-url` | `http://localhost:5678/webhook/fraud-check` |

Every request carries the header `X-Webhook-Secret` (the value of `N8N_WEBHOOK_SECRET` from `.env`). A request without the correct secret is rejected by n8n.

The Alert Bot is not called by the backend. Login Guard and Fraud Detector call it internally when they raise an alert.

> These are **production** webhook URLs (`/webhook/...`, not `/webhook-test/...`). They only answer while the workflow is **Active**.