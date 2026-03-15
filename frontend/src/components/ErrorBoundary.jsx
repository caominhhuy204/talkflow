import React from "react";

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, message: "" };
  }

  static getDerivedStateFromError(error) {
    return {
      hasError: true,
      message: error?.message || "Unexpected frontend error."
    };
  }

  componentDidCatch(error) {
    // Keep this logging for operational debugging in browser console.
    console.error("UI runtime error:", error);
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="auth-page">
          <section className="auth-card">
            <p className="kicker">TaskFlow</p>
            <h1>Unexpected UI Error</h1>
            <p className="feedback error">{this.state.message}</p>
            <button type="button" onClick={() => window.location.reload()}>
              Reload Page
            </button>
          </section>
        </main>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
