
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  GitBranch,
  CheckSquare,
  LogOut,
  Workflow,
  Users,
} from "lucide-react";

export default function Layout() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "{}");

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/login");
  }

  const links = [
    { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
    { to: "/workflows", label: "Workflows", icon: GitBranch },
    { to: "/tasks", label: "Tasks", icon: CheckSquare },
    ...(user.role === "MANAGER" || user.role === "ADMIN"
      ? [{ to: "/employees", label: "Employees", icon: Users }]
      : []),
  ];

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <Workflow size={27} />
          <span>NextERP</span>
        </div>

        <p className="nav-heading">WORKSPACE</p>

        <nav>
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} className="nav-link">
              <Icon size={19} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="user-avatar">
            {(user.name || "U").charAt(0).toUpperCase()}
          </div>

          <div className="user-details">
            <strong>{user.name || "User"}</strong>
            <span>{user.role || "EMPLOYEE"}</span>
          </div>

          <button
            className="icon-button"
            onClick={logout}
            title="Log out"
          >
            <LogOut size={18} />
          </button>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <span>NextERP · Workflow Management</span>
          <span className="role-label">{user.role || "EMPLOYEE"}</span>
        </header>

        <section className="page-content">
          <Outlet />
        </section>
      </main>
    </div>
  );
}
