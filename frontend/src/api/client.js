const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
const STORAGE_KEY = "taskflow.session";

function buildApiUrl(path) {
  const base = String(API_BASE_URL || "").trim();
  const normalizedPath = String(path || "").startsWith("/") ? String(path) : `/${String(path || "")}`;

  if (!base) {
    return normalizedPath;
  }

  const normalizedBase = base.endsWith("/") ? base.slice(0, -1) : base;
  const normalizedApiPath =
    normalizedBase.endsWith("/api") && normalizedPath.startsWith("/api/")
      ? normalizedPath.slice(4)
      : normalizedPath;

  return `${normalizedBase}${normalizedApiPath}`;
}

function readPayloadSafely(text) {
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function unwrapApiResponse(payload) {
  if (payload && typeof payload === "object" && "data" in payload) {
    return payload.data;
  }
  return payload;
}

function toApiError(status, payload, fallbackMessage) {
  const message = payload?.message || fallbackMessage;
  const error = new Error(message);
  error.status = status;
  error.details = payload?.errors || null;
  return error;
}

async function request(path, { method = "GET", body, token } = {}) {
  const headers = {};
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(buildApiUrl(path), {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined
  });

  const raw = await response.text();
  const payload = readPayloadSafely(raw);

  if (!response.ok) {
    throw toApiError(response.status, payload, `Request failed (${response.status})`);
  }

  return unwrapApiResponse(payload);
}

export function loadSession() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (!parsed?.token || !parsed?.role) return null;
    return parsed;
  } catch {
    return null;
  }
}

export function saveSession(session) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

export function clearSession() {
  localStorage.removeItem(STORAGE_KEY);
}

export function formatDateTime(value) {
  if (!value) return "N/A";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "N/A";
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(date);
}

export async function registerUser(payload) {
  return request("/api/auth/register", { method: "POST", body: payload });
}

export async function loginUser(payload) {
  return request("/api/auth/login", { method: "POST", body: payload });
}

export async function loginWithGoogle(payload) {
  return request("/api/auth/google", { method: "POST", body: payload });
}

export async function forgotPassword(payload) {
  return request("/api/auth/forgot-password", { method: "POST", body: payload });
}

export async function resetPassword(payload) {
  return request("/api/auth/reset-password", { method: "POST", body: payload });
}

export async function getProjects(token) {
  return request("/api/projects", { token });
}

export async function createProject(token, payload) {
  return request("/api/projects", { method: "POST", token, body: payload });
}

export async function updateProject(token, id, payload) {
  return request(`/api/projects/${id}`, { method: "PUT", token, body: payload });
}

export async function removeProject(token, id) {
  return request(`/api/projects/${id}`, { method: "DELETE", token });
}

export async function assignEmployeeToProject(token, id, payload) {
  return request(`/api/projects/${id}/assign-employee`, { method: "POST", token, body: payload });
}

export async function getEmployeeOptions(token, query = "") {
  const q = String(query || "").trim();
  const suffix = q ? `?query=${encodeURIComponent(q)}` : "";
  return request(`/api/users/employee-options${suffix}`, { token });
}

export async function getMyProfile(token) {
  return request("/api/auth/me", { token });
}

export async function updateMyProfile(token, payload) {
  return request("/api/auth/me", { method: "PUT", token, body: payload });
}

export async function createInternalRequest(token, payload) {
  return request("/api/requests", { method: "POST", token, body: payload });
}

export async function getInternalRequests(token) {
  return request("/api/requests", { token });
}

export async function submitInternalRequest(token, id) {
  return request(`/api/requests/${id}/submit`, { method: "POST", token });
}

export async function cancelInternalRequest(token, id) {
  return request(`/api/requests/${id}/cancel`, { method: "POST", token });
}

export async function getApprovalInbox(token) {
  return request("/api/approvals/inbox", { token });
}

export async function approveRequest(token, requestId, payload) {
  return request(`/api/approvals/${requestId}/approve`, {
    method: "POST",
    token,
    body: payload
  });
}

export async function rejectRequest(token, requestId, payload) {
  return request(`/api/approvals/${requestId}/reject`, {
    method: "POST",
    token,
    body: payload
  });
}

export async function getReportSummary(token, { fromDate, toDate } = {}) {
  const params = new URLSearchParams();
  if (fromDate) params.set("fromDate", fromDate);
  if (toDate) params.set("toDate", toDate);
  const query = params.toString();
  return request(`/api/reports/summary${query ? `?${query}` : ""}`, { token });
}

export async function getWorkflowPolicy(token) {
  return request("/api/workflow-policies", { token });
}

export async function updateWorkflowPolicy(token, payload) {
  return request("/api/workflow-policies", { method: "PUT", token, body: payload });
}
