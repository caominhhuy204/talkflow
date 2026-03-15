function TaskBoard({ tasks }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Tasks</h2>
        <span>{tasks.length} total</span>
      </div>
      {tasks.length === 0 ? (
        <p className="empty-state">Backend currently has no `/api/tasks` endpoint.</p>
      ) : null}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Project</th>
              <th>Assignee</th>
              <th>Priority</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {tasks.map((task) => (
              <tr key={task.id}>
                <td>{task.title}</td>
                <td>{task.project || task.projectName || "-"}</td>
                <td>{task.assignee || task.assignedUser || "-"}</td>
                <td>
                  <span className={`tag ${String(task.priority).toLowerCase()}`}>{task.priority}</span>
                </td>
                <td>
                  <span className="tag neutral">{String(task.status).replace("_", " ")}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default TaskBoard;
