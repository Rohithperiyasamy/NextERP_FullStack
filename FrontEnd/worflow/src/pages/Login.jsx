
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";

export default function Login() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    setForm({
      ...form,
      [event.target.name]: event.target.value,
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await api.post("/auth/login", form);
      const user = response.data;

      localStorage.setItem("token", user.token);
      localStorage.setItem(
        "user",
        JSON.stringify({
          userId: user.userId,
          name: user.name,
          role: user.role,
        })
      );

      navigate("/dashboard");
    } catch (err) {
      setError(
        typeof err.response?.data === "string"
          ? err.response.data
          : "Login failed. Check your email and password."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={handleSubmit}>
        <div className="brand-mark">W</div>

        <h1>Welcome back</h1>
        <p>Sign in to your Workflow Automation account.</p>

        {error && <div className="error-message">{error}</div>}

        <label htmlFor="email">Email address</label>
        <input
          id="email"
          name="email"
          type="email"
          value={form.email}
          onChange={handleChange}
          placeholder="you@company.com"
          required
        />

        <label htmlFor="password">Password</label>
        <input
          id="password"
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          placeholder="Enter your password"
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? "Signing in..." : "Sign in"}
        </button>

        <p>
          New to NextERP? <Link to="/register">Create an account</Link>
        </p>

        <span className="login-footer">
          Enterprise Workflow Automation
        </span>
      </form>
    </div>
  );
}
