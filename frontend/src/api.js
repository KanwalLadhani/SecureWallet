const API_URL = "http://localhost:8080";

// Sends a POST request with JSON and returns { ok, status, data }
export async function postJson(path, body) {
  try {
    const response = await fetch(API_URL + path, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    let data = null;
    try {
      data = await response.json();
    } catch {
      // some responses have no body
    }

    return { ok: response.ok, status: response.status, data };
  } catch {
    // backend is off or unreachable
    return { ok: false, status: 0, data: null };
  }
}

// Sends a GET request with the saved JWT and returns { ok, status, data }
export async function getJson(path) {
  try {
    const token = localStorage.getItem("token");
    const response = await fetch(API_URL + path, {
      method: "GET",
      headers: { Authorization: `Bearer ${token}` },
    });

    let data = null;
    try {
      data = await response.json();
    } catch {
      // some responses have no body
    }

    return { ok: response.ok, status: response.status, data };
  } catch {
    // backend is off or unreachable
    return { ok: false, status: 0, data: null };
  }
}


// Sends a POST request with JSON AND the saved JWT, returns { ok, status, data }
export async function postJsonAuth(path, body) {
  try {
    const token = localStorage.getItem("token");
    const response = await fetch(API_URL + path, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    });

    let data = null;
    try {
      data = await response.json();
    } catch {
      // some responses have no body
    }

    return { ok: response.ok, status: response.status, data };
  } catch {
    // backend is off or unreachable
    return { ok: false, status: 0, data: null };
  }
}