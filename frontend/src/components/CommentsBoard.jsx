function CommentsBoard({ comments }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Comments</h2>
        <span>{comments.length} updates</span>
      </div>
      {comments.length === 0 ? (
        <p className="empty-state">Backend currently has no `/api/comments` endpoint.</p>
      ) : null}
      <div className="comment-list">
        {comments.map((comment) => (
          <article key={comment.id} className="comment-card">
            <header>
              <strong>{comment.author}</strong>
              <span>{comment.createdAt}</span>
            </header>
            <p className="comment-task">{comment.taskTitle}</p>
            <p>{comment.content}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

export default CommentsBoard;
