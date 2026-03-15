function formatCreatedAt(createdAt) {
  if (!createdAt) return "N/A";
  const date = new Date(createdAt);
  if (Number.isNaN(date.getTime())) return "N/A";

  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(date);
}

function ProjectBoard({ projects }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Projects</h2>
        <span>{projects.length} total</span>
      </div>
      {projects.length === 0 ? (
        <p className="empty-state">No project data returned from backend.</p>
      ) : null}
      <div className="project-list">
        {projects.map((project) => (
          <article key={project.id} className="project-card">
            <header>
              <h3>{project.name}</h3>
              <span>{formatCreatedAt(project.createdAt)}</span>
            </header>
            <p>{project.description || "No description provided."}</p>
            <footer>
              <span>Project ID: #{project.id}</span>
            </footer>
          </article>
        ))}
      </div>
    </section>
  );
}

export default ProjectBoard;
