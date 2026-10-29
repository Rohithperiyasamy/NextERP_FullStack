
import { useEffect, useState } from "react";
import { Users, RefreshCw } from "lucide-react";
import api from "../api/axios";

export default function Employees() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadEmployees = async () => {
    setLoading(true);
    setError("");

    try {
      const response = await api.get("/users");
      const allUsers = Array.isArray(response.data)
        ? response.data
        : [];

      setEmployees(
        allUsers.filter(
          (user) => user.role === "EMPLOYEE"
        )
      );
    } catch (err) {
      setError(
        err.response?.status === 403
          ? "You do not have permission to view employees."
          : "Unable to load employee details. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEmployees();
  }, []);

  return (
    <div>
      <div className="page-heading">
        <div>
          <p className="eyebrow">TEAM MANAGEMENT</p>
          <h1>Employee Details</h1>
          <p>View registered employees and their information.</p>
        </div>

        <button
          className="secondary-button"
          onClick={loadEmployees}
          disabled={loading}
        >
          <RefreshCw size={16} /> Refresh
        </button>
      </div>

      <div className="stats-grid">
        <div className="stat-card stat-blue">
          <div className="stat-top">
            <span>Total Employees</span>
            <Users size={20} />
          </div>
          <strong className="stat-value">
            {employees.length}
          </strong>
          <span className="stat-detail">
            Registered employee accounts
          </span>
        </div>
      </div>

      <section className="content-card">
        <div className="section-heading">
          <div>
            <h2>All Employees</h2>
            <p className="section-subtitle">
              Employee name, email and role
            </p>
          </div>
        </div>

        {loading ? (
          <p>Loading employees...</p>
        ) : error ? (
          <div className="error-message">{error}</div>
        ) : employees.length === 0 ? (
          <p className="empty-state">
            No employee accounts found.
          </p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Employee Name</th>
                  <th>Email Address</th>
                  <th>Role</th>
                </tr>
              </thead>

              <tbody>
                {employees.map((employee) => (
                  <tr key={employee.id}>
                    <td>{employee.id}</td>
                    <td>
                      <strong>{employee.name}</strong>
                    </td>
                    <td>{employee.email}</td>
                    <td>
                      <span className="status-badge">
                        {employee.role}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
