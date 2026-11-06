import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { getJson } from "../api";
import "./AdminAlerts.css";

export default function AdminAlerts() {
  const navigate = useNavigate();
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function fetchAlerts() {
    const result = await getJson("/api/admin/alerts");

    if (result.status === 401) {
      navigate("/login");
      return;
    }

    if (result.status === 403) {
      setError("This page is for admins only.");
    } else if (!result.ok) {
      setError("Could not load alerts. Is the backend running?");
    } else {
      setAlerts(result.data);
    }
    setLoading(false);
  }

  useEffect(() => {
    fetchAlerts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleRefresh() {
    setLoading(true);
    setError("");
    fetchAlerts();
  }

  return (
    <div className="aa-page">
      <div className="aa-header">
        <div>
          <h1 className="aa-title">Security alerts</h1>
          <p className="aa-subtitle">Newest 100 alerts from Login Guard and Fraud Detector</p>
        </div>
        <div className="aa-actions">
          <button className="aa-btn aa-btn-light" onClick={handleRefresh}>
            Refresh
          </button>
          <Link className="aa-btn aa-btn-light" to="/dashboard">
            Back to dashboard
          </Link>
        </div>
      </div>

      {loading && <p className="aa-message">Loading alerts...</p>}

      {!loading && error && <p className="aa-message aa-error">{error}</p>}

      {!loading && !error && alerts.length === 0 && (
        <p className="aa-message">No alerts yet. All quiet.</p>
      )}

      {!loading && !error && alerts.length > 0 && (
        <div className="aa-table-wrap">
          <table className="aa-table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Type</th>
                <th>Severity</th>
                <th>Email</th>
                <th>Message</th>
              </tr>
            </thead>
            <tbody>
              {alerts.map((a) => (
                <tr key={a.id}>
                  <td>{new Date(a.createdAt).toLocaleString()}</td>
                  <td>{a.alertType}</td>
                  <td>
                    <span className={`aa-sev aa-sev-${a.severity.toLowerCase()}`}>
                      {a.severity}
                    </span>
                  </td>
                  <td>{a.email || "—"}</td>
                  <td>{a.message}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}