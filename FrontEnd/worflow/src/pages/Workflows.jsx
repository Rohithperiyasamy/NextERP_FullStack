
import { useCallback, useEffect, useState } from "react";
import api from "../api/axios";

export default function Workflows() {
  const [workflows, setWorkflows] = useState([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [busy, setBusy] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const isManager = ["MANAGER", "ADMIN"].includes(user.role);

  const [form, setForm] = useState({
    title: "",
    description: "",
    type: "GENERAL",
  });

  const loadWorkflows = useCallback(async () => {
    try {
      const response = await api.get("/workflows");
      setWorkflows(response.data);
      setError("");
    } catch {
      setError("Could not load workflows.");
    }
  }, []);

  useEffect(() => {
    // Initial data fetch is an external synchronization; state updates happen after the HTTP request resolves.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadWorkflows();
  }, [loadWorkflows]);

  async function submitWorkflow(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setMessage("");

    try {
      await api.post("/workflows/submit", {
        ...form,
        userId: user.userId,
      });

      setForm({ title: "", description: "", type: "GENERAL" });
      setShowForm(false);
      setMessage("Workflow submitted successfully.");
      await loadWorkflows();
    } catch (err) {
      setError(
        typeof err.response?.data === "string"
          ? err.response.data
          : "Workflow submission failed."
      );
    } finally {
      setBusy(false);
    }
  }

  async function resubmitWorkflow(workflow) {
    const updatedDescription = window.prompt(
      "Update the workflow description with the requested information:",
      workflow.description || ""
    );
    if (updatedDescription === null) return;
    if (!updatedDescription.trim()) {
      setError("Please enter the updated information before resubmitting.");
      return;
    }
    setBusy(true);
    setError("");
    setMessage("");
    try {
      await api.put(`/workflows/${workflow.id}/resubmit`, {
        userId: user.userId,
        description: updatedDescription.trim(),
      });
      setMessage(`Workflow #${workflow.id} resubmitted for review.`);
      await loadWorkflows();
    } catch (err) {
      setError(typeof err.response?.data === "string" ? err.response.data : "Could not resubmit this workflow.");
    } finally {
      setBusy(false);
    }
  }

  async function reviewAction(id, action) {
    if (!user.userId) {
      setError("Your user ID is missing. Please sign in again.");
      return;
    }

    const labels = {
      review: "start reviewing",
      approve: "approve",
      reject: "reject",
      "more-info": "request more information for",
    };

    if (!window.confirm(`Are you sure you want to ${labels[action]} workflow #${id}?`)) {
      return;
    }

    setBusy(true);
    setError("");
    setMessage("");

    try {
      await api.put(
        `/workflows/${id}/${action}`,
        null,
        { params: { managerId: user.userId } }
      );

      setMessage(`Workflow #${id} updated successfully.`);
      await loadWorkflows();
    } catch (err) {
      setError(
        typeof err.response?.data === "string"
          ? err.response.data
          : "The workflow action failed."
      );
    } finally {
      setBusy(false);
    }
  }

  const filteredWorkflows = workflows.filter((workflow) => {
    const query = searchTerm.trim().toLowerCase();
    const matchesQuery = !query || [workflow.title, workflow.description, workflow.type, String(workflow.id)]
      .some((value) => String(value || "").toLowerCase().includes(query));
    return matchesQuery && (statusFilter === "ALL" || workflow.status === statusFilter);
  });

  return (
    <div>
      <div className="page-heading">
        <div>
          <p className="eyebrow">WORKSPACE</p>
          <h1>Workflows</h1>
          <p>Submit requests and track their approval status.</p>
        </div>

        <button onClick={() => setShowForm(!showForm)}>
          {showForm ? "Cancel" : "+ New workflow"}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {message && <div className="success-message">{message}</div>}

      {showForm && (
        <form className="content-card form-grid" onSubmit={submitWorkflow}>
          <h2>Submit a workflow</h2>

          <label>
            Title
            <input
              required
              value={form.title}
              onChange={(e) =>
                setForm({ ...form, title: e.target.value })
              }
              placeholder="e.g. Laptop purchase request"
            />
          </label>

          <label>
            Type
            <input
              required
              value={form.type}
              onChange={(e) =>
                setForm({ ...form, type: e.target.value })
              }
              placeholder="e.g. GENERAL"
            />
          </label>

          <label>
            Description
            <textarea
              required
              rows="3"
              value={form.description}
              onChange={(e) =>
                setForm({ ...form, description: e.target.value })
              }
              placeholder="Explain the request..."
            />
          </label>

          <button disabled={busy} type="submit">
            {busy ? "Submitting..." : "Submit workflow"}
          </button>
        </form>
      )}

      <div className="content-card">
        <div className="section-heading workflow-list-heading">
          <div><h2>All workflows</h2><p className="section-subtitle">Search requests or filter by approval status.</p></div>
          <span className="total-pill">{filteredWorkflows.length} shown</span>
        </div>
        <div className="workflow-filters">
          <label className="search-field">Search workflows
            <input value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} placeholder="Search title, description, type, or ID" />
          </label>
          <label>Status
            <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
              <option value="ALL">All statuses</option>
              <option value="PENDING">Pending</option>
              <option value="IN_REVIEW">In review</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
              <option value="NEEDS_MORE_INFO">Needs more information</option>
            </select>
          </label>
        </div>

        {filteredWorkflows.length === 0 ? (
          <p className="empty-state">{workflows.length === 0 ? "No workflows found." : "No workflows match your search and status filter."}</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Type</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {filteredWorkflows.map((workflow) => (
                  <tr key={workflow.id}>
                    <td>#{workflow.id}</td>
                    <td>
                      <strong>{workflow.title}</strong>
                      <div className="muted-text">
                        {workflow.description}
                      </div>
                    </td>
                    <td>{workflow.type}</td>
                    <td>
                      <span className={`status-badge status-${String(workflow.status || "unknown").toLowerCase().replaceAll("_", "-")}`}>
                        {String(workflow.status || "UNKNOWN").replaceAll("_", " ")}
                      </span>
                    </td>
                    <td>
                      {isManager && (
                        <div className="action-group">
                          {workflow.status === "PENDING" && (
                            <button
                              disabled={busy}
                              onClick={() =>
                                reviewAction(workflow.id, "review")
                              }
                            >
                              Review
                            </button>
                          )}

                          {workflow.status === "IN_REVIEW" && (
                            <>
                              <button
                                disabled={busy}
                                onClick={() =>
                                  reviewAction(workflow.id, "approve")
                                }
                              >
                                Approve
                              </button>
                              <button
                                className="secondary-button"
                                disabled={busy}
                                onClick={() =>
                                  reviewAction(workflow.id, "reject")
                                }
                              >
                                Reject
                              </button>
                              <button
                                className="secondary-button"
                                disabled={busy}
                                onClick={() =>
                                  reviewAction(workflow.id, "more-info")
                                }
                              >
                                More info
                              </button>
                            </>
                          )}
                        </div>
                      )}
                      {!isManager && workflow.status === "NEEDS_MORE_INFO" && String(workflow.submittedBy?.id) === String(user.userId) && (
                        <button className="secondary-button" disabled={busy} onClick={() => resubmitWorkflow(workflow)}>
                          Resubmit
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
