import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Activity, CheckSquare, Clock3, GitBranch, RefreshCw, Users,
  AlertTriangle, ArrowRight, CircleCheck, ListTodo,
} from "lucide-react";
import api from "../api/axios";

const STATUS_ROWS = [
  { key: "pending", label: "Pending", color: "#d69b28" },
  { key: "inReview", label: "In review", color: "#6475d3" },
  { key: "approved", label: "Approved", color: "#2f9a68" },
  { key: "rejected", label: "Rejected", color: "#d65362" },
  { key: "needsMoreInfo", label: "Needs more information", color: "#9a6bd6" },
];

function formatDate(value) {
  if (!value) return "Date not available";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "Date not available"
    : date.toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" });
}

export default function Dashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const [summary, setSummary] = useState(null);
  const [recentWorkflows, setRecentWorkflows] = useState([]);
  const [myTasks, setMyTasks] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const requests = [api.get("/dashboard/summary"), api.get("/workflows")];
      if (user.userId != null) requests.push(api.get(`/tasks/user/${user.userId}`));
      const responses = await Promise.all(requests);
      setSummary(responses[0].data);
      const workflows = Array.isArray(responses[1].data) ? responses[1].data : [];
      setRecentWorkflows(
        [...workflows]
          .sort((a, b) => new Date(b.updatedAt || b.submittedAt || 0) - new Date(a.updatedAt || a.submittedAt || 0))
          .slice(0, 5)
      );
      setMyTasks(requests.length > 2 && Array.isArray(responses[2].data) ? responses[2].data : []);
    } catch {
      setError("Unable to load dashboard data. Please refresh or sign in again.");
    } finally {
      setLoading(false);
    }
  }, [user.userId]);

  // Initial data fetch is an external synchronization; the callback updates state after its HTTP requests resolve.
  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => { loadDashboard(); }, [loadDashboard]);

  if (loading && !summary) return <p>Loading your dashboard...</p>;
  if (error && !summary) return <div><p className="error-message">{error}</p><button onClick={loadDashboard}>Try again</button></div>;

  const workflows = summary?.workflows || {};
  const tasks = summary?.tasks || {};
  const cards = [
    { title: "Total users", value: summary?.users?.total ?? 0, detail: `${summary?.users?.employees ?? 0} employees`, icon: Users, tone: "blue" },
    { title: "Total workflows", value: workflows.total ?? 0, detail: `${workflows.pending ?? 0} awaiting review`, icon: GitBranch, tone: "purple" },
    { title: "Tasks completed", value: tasks.completed ?? 0, detail: `${tasks.total ?? 0} total tasks`, icon: CheckSquare, tone: "green" },
    { title: "Audit actions", value: summary?.auditActivity?.totalActions ?? 0, detail: `${summary?.auditActivity?.approvals ?? 0} approvals recorded`, icon: Activity, tone: "orange" },
  ];
  const totalWorkflows = Number(workflows.total || 0);
  const taskCompletion = tasks.total ? Math.round((Number(tasks.completed || 0) / Number(tasks.total)) * 100) : 0;
  const openTasks = myTasks.filter((task) => !["COMPLETED"].includes(task.status));
  const overdueTasks = myTasks.filter((task) => task.status === "OVERDUE" || (task.dueDate && new Date(task.dueDate) < new Date() && task.status !== "COMPLETED"));

  return (
    <div>
      <div className="page-heading dashboard-heading">
        <div>
          <p className="eyebrow">WORKSPACE OVERVIEW</p>
          <h1>Welcome back, {user.name || "there"}</h1>
          <p>Here is the latest activity across your workspace.</p>
        </div>
        <button className="secondary-button refresh-button" onClick={loadDashboard} disabled={loading}>
          <RefreshCw size={16} className={loading ? "spin-icon" : ""} /> Refresh
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="stats-grid">
        {cards.map(({ title, value, detail, icon: Icon, tone }) => (
          <div className={`stat-card stat-${tone}`} key={title}>
            <div className="stat-top"><span>{title}</span><span className="stat-icon"><Icon size={20} /></span></div>
            <strong className="stat-value">{value}</strong>
            <span className="stat-detail">{detail}</span>
          </div>
        ))}
      </div>

      <div className="dashboard-grid">
        <section className="content-card">
          <div className="section-heading"><div><h2>Workflow status</h2><p className="section-subtitle">A snapshot of all workflow requests</p></div><span className="total-pill">{totalWorkflows} total</span></div>
          {STATUS_ROWS.map(({ key, label, color }) => {
            const count = Number(workflows[key] || 0);
            const percent = totalWorkflows ? Math.min(100, (count / totalWorkflows) * 100) : 0;
            return <div className="status-metric" key={key}>
              <div className="status-metric-label"><span>{label}</span><strong>{count}</strong></div>
              <div className="progress-track"><span style={{ width: `${percent}%`, backgroundColor: color }} /></div>
            </div>;
          })}
          <button className="text-action" onClick={() => navigate("/workflows")}>View workflows <ArrowRight size={16} /></button>
        </section>

        <section className="content-card task-overview-card">
          <div className="section-heading"><div><h2>My task overview</h2><p className="section-subtitle">Tasks assigned to your account</p></div><ListTodo size={21} /></div>
          <div className="task-progress-top"><strong>{taskCompletion}%</strong><span>overall completion</span></div>
          <div className="progress-track task-progress"><span style={{ width: `${taskCompletion}%` }} /></div>
          <div className="task-mini-stats">
            <div><span className="mini-icon mini-blue"><Clock3 size={17} /></span><div><strong>{openTasks.length}</strong><span>Open tasks</span></div></div>
            <div><span className="mini-icon mini-red"><AlertTriangle size={17} /></span><div><strong>{overdueTasks.length}</strong><span>Overdue</span></div></div>
            <div><span className="mini-icon mini-green"><CircleCheck size={17} /></span><div><strong>{myTasks.filter((task) => task.status === "COMPLETED").length}</strong><span>Completed by me</span></div></div>
          </div>
          <button className="text-action" onClick={() => navigate("/tasks")}>Open my tasks <ArrowRight size={16} /></button>
        </section>
      </div>

      <section className="content-card recent-workflows-card">
        <div className="section-heading"><div><h2>Recently updated workflows</h2><p className="section-subtitle">The five most recently updated requests</p></div><button className="secondary-button" onClick={() => navigate("/workflows")}>All workflows <ArrowRight size={15} /></button></div>
        {recentWorkflows.length === 0 ? <p className="empty-state">No workflows have been submitted yet.</p> : <div className="table-wrapper"><table>
          <thead><tr><th>Workflow</th><th>Type</th><th>Status</th><th>Last updated</th></tr></thead>
          <tbody>{recentWorkflows.map((workflow) => <tr key={workflow.id}>
            <td><strong>{workflow.title || `Workflow #${workflow.id}`}</strong><div className="muted-text">#{workflow.id}</div></td>
            <td>{workflow.type || "General"}</td>
            <td><span className={`status-badge status-${String(workflow.status || "unknown").toLowerCase().replaceAll("_", "-")}`}>{String(workflow.status || "UNKNOWN").replaceAll("_", " ")}</span></td>
            <td>{formatDate(workflow.updatedAt || workflow.submittedAt)}</td>
          </tr>)}</tbody>
        </table></div>}
      </section>
    </div>
  );
}
