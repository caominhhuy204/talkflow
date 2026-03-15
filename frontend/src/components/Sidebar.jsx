const menuItems = [
  { key: "overview", label: "Overview" },
  { key: "projects", label: "Projects" },
  { key: "tasks", label: "Tasks" },
  { key: "users", label: "Users" },
  { key: "comments", label: "Comments" }
];

function Sidebar({ activeTab, onChange }) {
  return (
    <aside className="sidebar">
      <div className="brand">
        <p className="brand-kicker">Task Management</p>
        <h1>TaskFlow</h1>
      </div>
      <nav>
        {menuItems.map((item) => (
          <button
            key={item.key}
            className={`menu-btn ${activeTab === item.key ? "active" : ""}`}
            onClick={() => onChange(item.key)}
          >
            {item.label}
          </button>
        ))}
      </nav>
    </aside>
  );
}

export default Sidebar;
