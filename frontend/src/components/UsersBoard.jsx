function UsersBoard({ users }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Users</h2>
        <span>{users.length} members</span>
      </div>
      {users.length === 0 ? (
        <p className="empty-state">Backend currently has no `/api/users` endpoint.</p>
      ) : null}
      <div className="user-grid">
        {users.map((user) => (
          <article key={user.id} className="user-card">
            <h3>{user.username}</h3>
            <p>{user.email}</p>
            <span className="tag neutral">{user.role}</span>
          </article>
        ))}
      </div>
    </section>
  );
}

export default UsersBoard;
