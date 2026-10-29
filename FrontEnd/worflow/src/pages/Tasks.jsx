
import { useCallback, useEffect, useState } from "react";
import api from "../api/axios";

export default function Tasks() {
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const [tasks, setTasks] = useState([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [busyId, setBusyId] = useState(null);

  const loadTasks = useCallback(async () => {
    try {
      const response = await api.get(`/tasks/user/${user.userId}`);
      setTasks(response.data);
      setError("");
    } catch {
      setError("Unable to load tasks assigned to your account.");
    }
  }, [user.userId]);

  useEffect(() => {
    // Initial data fetch is an external synchronization; state updates happen after the HTTP request resolves.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadTasks();
  }, [loadTasks]);

  async function updateTask(taskId, action) {
    setBusyId(taskId);
    setError("");
    setMessage("");

    try {
      await api.put(`/tasks/${taskId}/${action}`);
      setMessage(`Task #${taskId} updated successfully.`);
      await loadTasks();
    } catch (err) {
      setError(
        typeof err.response?.data === "string"
          ? err.response.data
          : "Could not update this task."
      );
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div>
      <div className="page-heading">
        <div>
          <p className="eyebrow">MY WORK</p>
          <h1>Tasks</h1>
          <p>Track and complete the tasks assigned to you.</p>
        </div>

        <button className="secondary-button" onClick={loadTasks}>
          Refresh
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {message && <div className="success-message">{message}</div>}

      <div className="content-card">
        <h2>Assigned tasks</h2>

        {tasks.length === 0 ? (
          <p>No tasks assigned to your account yet.</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Task</th>
                  <th>Workflow</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Due date</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {tasks.map((task) => (
                  <tr key={task.id}>
                    <td>
                      <strong>{task.title}</strong>
                      <div className="muted-text">
                        {task.description}
                      </div>
                    </td>

                    <td>
                      {task.workflow?.title || `#${task.workflow?.id ?? "-"}`}
                    </td>

                    <td>{task.priority ?? "-"}</td>

                    <td>
                      <span className="status-badge">
                        {task.status}
                      </span>
                    </td>

                    <td>
                      {task.dueDate
                        ? new Date(task.dueDate).toLocaleDateString()
                        : "-"}
                    </td>

                    <td>
                      {task.status === "PENDING" && (
                        <button
                          disabled={busyId === task.id}
                          onClick={() => updateTask(task.id, "start")}
                        >
                          Start
                        </button>
                      )}

                      {task.status === "IN_PROGRESS" && (
                        <button
                          disabled={busyId === task.id}
                          onClick={() => updateTask(task.id, "complete")}
                        >
                          Complete
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
