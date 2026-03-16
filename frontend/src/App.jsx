import { useEffect, useMemo, useRef, useState } from "react";
import {
  approveRequest,
  assignEmployeeToProject,
  cancelInternalRequest,
  clearSession,
  createInternalRequest,
  createProject,
  forgotPassword,
  formatDateTime,
  getApprovalInbox,
  getEmployeeOptions,
  getInternalRequests,
  getMyProfile,
  getProjects,
  getReportSummary,
  getWorkflowPolicy,
  loadSession,
  loginUser,
  loginWithGoogle,
  registerUser,
  resetPassword,
  rejectRequest,
  removeProject,
  saveSession,
  submitInternalRequest,
  updateMyProfile,
  updateProject,
  updateWorkflowPolicy
} from "./api/client";

const EMPTY_LOGIN_FORM = { email: "", password: "" };
const EMPTY_REGISTER_FORM = { fullName: "", email: "", password: "", role: "EMPLOYEE" };
const EMPTY_FORGOT_FORM = { email: "" };
const EMPTY_RESET_FORM = { token: "", newPassword: "", confirmPassword: "" };
const EMPTY_PROJECT_FORM = { name: "", description: "" };
const EMPTY_LEAVE_FORM = { leaveType: "ANNUAL", startDate: "", endDate: "", reason: "", handoverNote: "" };
const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || "";

const ROLE_LABEL = {
  EMPLOYEE: "Nhân viên",
  MANAGER: "Quản lý",
  HR: "Nhân sự",
  ADMIN: "Quản trị hệ thống"
};

const MODULE_LABEL = {
  dashboard: "Tổng quan",
  profile: "Hồ sơ của tôi",
  leave: "Xin nghỉ phép",
  my_requests: "Đơn của tôi",
  approvals: "Phê duyệt",
  projects: "Dự án",
  reports: "Báo cáo",
  policy: "Chính sách quy trình"
};

function modulesByRole(role) {
  if (role === "ADMIN") return ["dashboard", "reports"];
  if (role === "MANAGER") {
    return ["dashboard", "profile", "projects", "approvals", "reports", "leave", "my_requests"];
  }
  if (role === "HR") {
    return ["dashboard", "profile", "projects", "approvals", "reports", "leave", "my_requests"];
  }
  return ["dashboard", "profile", "projects", "leave", "my_requests", "reports"];
}

function statusLabel(status) {
  const map = {
    DRAFT: "Nháp",
    SUBMITTED: "Đã gửi",
    IN_REVIEW: "Đang duyệt",
    APPROVED: "Đã duyệt",
    REJECTED: "Từ chối",
    CANCELLED: "Đã hủy"
  };
  return map[status] || status;
}

function leaveTypeLabel(type) {
  const map = {
    ANNUAL: "Nghỉ phép năm",
    SICK: "Nghỉ ốm",
    UNPAID: "Nghỉ không lương",
    MATERNITY: "Nghỉ thai sản",
    OTHER: "Khác"
  };
  return map[type] || type;
}

function requestTypeLabel(type) {
  const map = {
    LEAVE: "Nghỉ phép",
    OVERTIME: "Tăng ca",
    EXPENSE_REIMBURSEMENT: "Hoàn ứng",
    EQUIPMENT_PURCHASE: "Mua thiết bị",
    DOCUMENT_APPROVAL: "Phê duyệt tài liệu"
  };
  return map[type] || type;
}

function projectStatusLabel(status) {
  const map = {
    PLANNING: "Lập kế hoạch",
    ACTIVE: "Đang thực hiện",
    ON_HOLD: "Tạm dừng",
    COMPLETED: "Hoàn thành",
    ARCHIVED: "Lưu trữ"
  };
  return map[status] || status;
}

function toMessage(error) {
  if (!error) return "Có lỗi không xác định.";
  if (typeof error === "string") return error;
  return error.message || "Có lỗi không xác định.";
}

function App() {
  const [session, setSession] = useState(() => loadSession());
  const [profile, setProfile] = useState(null);
  const [activeModule, setActiveModule] = useState("dashboard");

  const [authMode, setAuthMode] = useState("login");
  const [loginForm, setLoginForm] = useState(EMPTY_LOGIN_FORM);
  const [registerForm, setRegisterForm] = useState(EMPTY_REGISTER_FORM);
  const [forgotForm, setForgotForm] = useState(EMPTY_FORGOT_FORM);
  const [resetForm, setResetForm] = useState(EMPTY_RESET_FORM);
  const [authLoading, setAuthLoading] = useState(false);
  const [authMessage, setAuthMessage] = useState("");

  const [projects, setProjects] = useState([]);
  const [projectForm, setProjectForm] = useState(EMPTY_PROJECT_FORM);
  const [editingProjectId, setEditingProjectId] = useState(null);
  const [projectMessage, setProjectMessage] = useState("");
  const [projectSearch, setProjectSearch] = useState("");
  const [assignProjectId, setAssignProjectId] = useState(null);
  const [employeeQuery, setEmployeeQuery] = useState("");
  const [employeeOptions, setEmployeeOptions] = useState([]);
  const [selectedEmployeeEmail, setSelectedEmployeeEmail] = useState("");

  const [leaveForm, setLeaveForm] = useState(EMPTY_LEAVE_FORM);
  const [leaveSaving, setLeaveSaving] = useState(false);
  const [leaveMessage, setLeaveMessage] = useState("");

  const [myRequests, setMyRequests] = useState([]);
  const [requestMessage, setRequestMessage] = useState("");
  const [requestStatusFilter, setRequestStatusFilter] = useState("ALL");

  const [approvalInbox, setApprovalInbox] = useState([]);
  const [approvalComment, setApprovalComment] = useState("");
  const [approvalMessage, setApprovalMessage] = useState("");

  const [report, setReport] = useState(null);
  const [reportMessage, setReportMessage] = useState("");
  const [reportFromDate, setReportFromDate] = useState("");
  const [reportToDate, setReportToDate] = useState("");

  const [policyThreshold, setPolicyThreshold] = useState("");
  const [policyMessage, setPolicyMessage] = useState("");

  const [profileForm, setProfileForm] = useState({ fullName: "", newPassword: "" });
  const [profileSaving, setProfileSaving] = useState(false);
  const [profileMessage, setProfileMessage] = useState("");
  const [toasts, setToasts] = useState([]);
  const googleButtonRef = useRef(null);

  const role = profile?.role || session?.role || "EMPLOYEE";
  const isAdmin = role === "ADMIN";
  const canApprove = ["MANAGER", "HR"].includes(role);
  const canViewReports = ["ADMIN", "MANAGER", "HR", "EMPLOYEE"].includes(role);
  const canManageProjects = role === "MANAGER";
  const canAssignProjects = role === "HR";
  const canUseRequests = !isAdmin;
  const modules = useMemo(() => modulesByRole(role), [role]);

  const filteredProjects = useMemo(() => {
    const q = projectSearch.trim().toLowerCase();
    if (!q) return projects;
    return projects.filter((p) => {
      const code = String(p.projectCode || "").toLowerCase();
      const name = String(p.name || "").toLowerCase();
      const desc = String(p.description || "").toLowerCase();
      const status = String(p.status || "").toLowerCase();
      const visibility = String(p.visibility || "").toLowerCase();
      return code.includes(q) || name.includes(q) || desc.includes(q) || status.includes(q) || visibility.includes(q);
    });
  }, [projects, projectSearch]);

  const filteredRequests = useMemo(() => {
    if (requestStatusFilter === "ALL") return myRequests;
    return myRequests.filter((item) => item.status === requestStatusFilter);
  }, [myRequests, requestStatusFilter]);

  const reportStatusChart = useMemo(() => {
    const entries = Object.entries(report?.byStatus || {});
    const max = Math.max(1, ...entries.map(([, value]) => Number(value || 0)));
    return entries.map(([key, value]) => {
      const count = Number(value || 0);
      return {
        key,
        label: statusLabel(key),
        count,
        ratio: Math.round((count / max) * 100)
      };
    });
  }, [report]);

  const reportTypeChart = useMemo(() => {
    const entries = Object.entries(report?.byType || {});
    const max = Math.max(1, ...entries.map(([, value]) => Number(value || 0)));
    return entries.map(([key, value]) => {
      const count = Number(value || 0);
      return {
        key,
        label: requestTypeLabel(key),
        count,
        ratio: Math.round((count / max) * 100)
      };
    });
  }, [report]);

  const reportProjectStatusChart = useMemo(() => {
    const entries = Object.entries(report?.byProjectStatus || {});
    const max = Math.max(1, ...entries.map(([, value]) => Number(value || 0)));
    return entries.map(([key, value]) => {
      const count = Number(value || 0);
      return {
        key,
        label: projectStatusLabel(key),
        count,
        ratio: Math.round((count / max) * 100)
      };
    });
  }, [report]);

  useEffect(() => {
    if (!modules.includes(activeModule)) {
      setActiveModule(modules[0] || "dashboard");
    }
  }, [modules, activeModule]);

  useEffect(() => {
    setProfileForm({
      fullName: profile?.fullName || "",
      newPassword: ""
    });
  }, [profile]);

  function pushToast(type, message) {
    const id = `${Date.now()}-${Math.random().toString(16).slice(2)}`;
    setToasts((prev) => [...prev, { id, type, message }]);
    window.setTimeout(() => {
      setToasts((prev) => prev.filter((item) => item.id !== id));
    }, 2600);
  }

  async function refreshProfile(currentSession) {
    if (!currentSession?.token) return;
    const me = await getMyProfile(currentSession.token);
    setProfile(me);
    if (me?.role && me.role !== currentSession.role) {
      const next = { ...currentSession, role: me.role, email: me.email || currentSession.email };
      saveSession(next);
      setSession(next);
    }
  }

  async function refreshProjects(currentSession) {
    if (!currentSession?.token) return;
    try {
      const data = await getProjects(currentSession.token);
      setProjects(Array.isArray(data) ? data : []);
    } catch (error) {
      setProjectMessage(toMessage(error));
    }
  }

  async function refreshRequests(currentSession) {
    if (!currentSession?.token || !canUseRequests) return;
    try {
      const data = await getInternalRequests(currentSession.token);
      setMyRequests(Array.isArray(data) ? data : []);
    } catch (error) {
      setRequestMessage(toMessage(error));
    }
  }

  async function refreshApprovalInbox(currentSession) {
    if (!currentSession?.token || !canApprove) return;
    try {
      const data = await getApprovalInbox(currentSession.token);
      setApprovalInbox(Array.isArray(data) ? data : []);
    } catch (error) {
      setApprovalMessage(toMessage(error));
    }
  }

  async function refreshReports(currentSession, range = {}) {
    if (!currentSession?.token || !canViewReports) return;
    try {
      const data = await getReportSummary(currentSession.token, range);
      setReport(data || null);
    } catch (error) {
      setReportMessage(toMessage(error));
      pushToast("error", toMessage(error));
    }
  }

  async function refreshPolicy(currentSession) {
    if (!currentSession?.token || !isAdmin) return;
    try {
      const data = await getWorkflowPolicy(currentSession.token);
      setPolicyThreshold(String(data?.equipmentAdminThreshold ?? ""));
    } catch (error) {
      setPolicyMessage(toMessage(error));
    }
  }

  useEffect(() => {
    async function loadData() {
      if (!session?.token) return;
      try {
        await refreshProfile(session);
      } catch {
        setProfile(null);
      }
      await refreshProjects(session);
      await refreshRequests(session);
      await refreshApprovalInbox(session);
      await refreshReports(session, { fromDate: reportFromDate || undefined, toDate: reportToDate || undefined });
      await refreshPolicy(session);
    }
    loadData();
  }, [session, canApprove, canViewReports, canUseRequests, isAdmin, profile?.email]);

  useEffect(() => {
    if (session || !GOOGLE_CLIENT_ID) return;

    let canceled = false;

    const initializeGoogleSignIn = () => {
      if (canceled || !window.google?.accounts?.id || !googleButtonRef.current) return;

      window.google.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: async (credentialResponse) => {
          const idToken = credentialResponse?.credential;
          if (!idToken) {
            setAuthMessage("Đăng nhập Google thất bại. Vui lòng thử lại.");
            pushToast("error", "Đăng nhập Google thất bại.");
            return;
          }

          setAuthLoading(true);
          setAuthMessage("");
          try {
            const result = await loginWithGoogle({ idToken });
            const next = { token: result.token, email: result.email, role: result.role };
            saveSession(next);
            setSession(next);
            pushToast("success", "Đăng nhập Google thành công.");
          } catch (error) {
            setAuthMessage(toMessage(error));
            pushToast("error", toMessage(error));
          } finally {
            setAuthLoading(false);
          }
        }
      });

      googleButtonRef.current.innerHTML = "";
      window.google.accounts.id.renderButton(googleButtonRef.current, {
        type: "standard",
        theme: "outline",
        size: "large",
        shape: "pill",
        text: "signin_with",
        locale: "vi"
      });
    };

    if (window.google?.accounts?.id) {
      initializeGoogleSignIn();
      return () => {
        canceled = true;
      };
    }

    const existingScript = document.getElementById("google-identity-services");
    if (existingScript) {
      existingScript.addEventListener("load", initializeGoogleSignIn, { once: true });
      return () => {
        canceled = true;
      };
    }

    const script = document.createElement("script");
    script.id = "google-identity-services";
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.onload = initializeGoogleSignIn;
    document.head.appendChild(script);

    return () => {
      canceled = true;
    };
  }, [session]);

  useEffect(() => {
    if (!session?.token || !canAssignProjects) return;

    let active = true;
    async function loadEmployees() {
      try {
        const data = await getEmployeeOptions(session.token, employeeQuery);
        if (!active) return;
        setEmployeeOptions(Array.isArray(data) ? data : []);
      } catch (error) {
        if (!active) return;
        pushToast("error", toMessage(error));
      }
    }

    loadEmployees();
    return () => {
      active = false;
    };
  }, [session, canAssignProjects, employeeQuery]);

  async function handleLogin(event) {
    event.preventDefault();
    setAuthLoading(true);
    setAuthMessage("");
    try {
      const result = await loginUser(loginForm);
      const next = { token: result.token, email: result.email, role: result.role };
      saveSession(next);
      setSession(next);
      setLoginForm(EMPTY_LOGIN_FORM);
      pushToast("success", "Đăng nhập thành công.");
    } catch (error) {
      setAuthMessage(toMessage(error));
      pushToast("error", toMessage(error));
    } finally {
      setAuthLoading(false);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    setAuthLoading(true);
    setAuthMessage("");
    try {
      const result = await registerUser(registerForm);
      const next = { token: result.token, email: result.email, role: result.role };
      saveSession(next);
      setSession(next);
      setRegisterForm(EMPTY_REGISTER_FORM);
      pushToast("success", "Tạo tài khoản thành công.");
    } catch (error) {
      setAuthMessage(toMessage(error));
      pushToast("error", toMessage(error));
    } finally {
      setAuthLoading(false);
    }
  }

  async function handleForgotPassword(event) {
    event.preventDefault();
    setAuthLoading(true);
    setAuthMessage("");
    try {
      const data = await forgotPassword(forgotForm);
      setAuthMessage(
        data?.resetToken
          ? `Token đặt lại mật khẩu: ${data.resetToken}`
          : "Nếu email tồn tại, yêu cầu đặt lại mật khẩu đã được ghi nhận."
      );
      setResetForm((prev) => ({ ...prev, token: data?.resetToken || "" }));
      setForgotForm(EMPTY_FORGOT_FORM);
      setAuthMode("reset");
      pushToast("success", "Đã gửi yêu cầu đặt lại mật khẩu.");
    } catch (error) {
      setAuthMessage(toMessage(error));
      pushToast("error", toMessage(error));
    } finally {
      setAuthLoading(false);
    }
  }

  async function handleResetPassword(event) {
    event.preventDefault();
    if (resetForm.newPassword !== resetForm.confirmPassword) {
      setAuthMessage("Mật khẩu xác nhận không khớp.");
      return;
    }

    setAuthLoading(true);
    setAuthMessage("");
    try {
      await resetPassword({
        token: resetForm.token,
        newPassword: resetForm.newPassword
      });
      setResetForm(EMPTY_RESET_FORM);
      setAuthMode("login");
      setAuthMessage("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập lại.");
      pushToast("success", "Đặt lại mật khẩu thành công.");
    } catch (error) {
      setAuthMessage(toMessage(error));
      pushToast("error", toMessage(error));
    } finally {
      setAuthLoading(false);
    }
  }

  function handleLogout() {
    clearSession();
    setSession(null);
    setProfile(null);
    setProjects([]);
    setMyRequests([]);
    setApprovalInbox([]);
    setReport(null);
    setProjectMessage("");
    setLeaveMessage("");
    setRequestMessage("");
    setApprovalMessage("");
    setReportMessage("");
    setPolicyMessage("");
    setProfileMessage("");
  }

  async function handleProfileSave(event) {
    event.preventDefault();
    if (!session?.token) return;
    setProfileSaving(true);
    setProfileMessage("");

    const payload = {};
    const fullName = profileForm.fullName.trim();
    if (fullName && fullName !== (profile?.fullName || "")) payload.fullName = fullName;
    if (profileForm.newPassword.trim()) payload.newPassword = profileForm.newPassword.trim();

    if (Object.keys(payload).length === 0) {
      setProfileSaving(false);
      setProfileMessage("Không có thay đổi để lưu.");
      return;
    }

    try {
      const updated = await updateMyProfile(session.token, payload);
      setProfile(updated);
      setProfileForm({ fullName: updated?.fullName || fullName, newPassword: "" });
      setProfileMessage("Đã cập nhật hồ sơ.");
      pushToast("success", "Đã cập nhật hồ sơ.");
    } catch (error) {
      setProfileMessage(toMessage(error));
      pushToast("error", toMessage(error));
    } finally {
      setProfileSaving(false);
    }
  }

  async function handleLeaveSubmit(event) {
    event.preventDefault();
    if (!session?.token || !canUseRequests) return;
    setLeaveSaving(true);
    setLeaveMessage("");
    try {
      await createInternalRequest(session.token, {
        type: "LEAVE",
        title: `Leave Request (${leaveTypeLabel(leaveForm.leaveType)})`,
        leave: {
          leaveType: leaveForm.leaveType,
          startDate: leaveForm.startDate,
          endDate: leaveForm.endDate,
          reason: leaveForm.reason,
          handoverNote: leaveForm.handoverNote || null
        }
      });
      setLeaveForm(EMPTY_LEAVE_FORM);
      await refreshRequests(session);
      setLeaveMessage("Đã tạo đơn.");
      setActiveModule("my_requests");
      pushToast("success", "Tạo đơn thành công.");
    } catch (error) {
      setLeaveMessage(toMessage(error));
      pushToast("error", toMessage(error));
    } finally {
      setLeaveSaving(false);
    }
  }

  async function handleMyRequestAction(requestId, action) {
    if (!session?.token || !canUseRequests) return;
    setRequestMessage("");
    try {
      if (action === "submit") {
        await submitInternalRequest(session.token, requestId);
      setRequestMessage("Đã gửi đơn chờ duyệt.");
      } else {
        await cancelInternalRequest(session.token, requestId);
        setRequestMessage("Đã hủy đơn.");
      }
      await refreshRequests(session);
      await refreshApprovalInbox(session);
      await refreshReports(session, { fromDate: reportFromDate || undefined, toDate: reportToDate || undefined });
      pushToast("success", action === "submit" ? "Đã gửi đơn chờ duyệt." : "Đã hủy đơn.");
    } catch (error) {
      setRequestMessage(toMessage(error));
      pushToast("error", toMessage(error));
    }
  }

  async function handleApprovalAction(requestId, action) {
    if (!session?.token || !canApprove) return;
    setApprovalMessage("");
    try {
      const payload = { comment: approvalComment || "" };
      if (action === "approve") await approveRequest(session.token, requestId, payload);
      else await rejectRequest(session.token, requestId, payload);
      setApprovalComment("");
      await refreshApprovalInbox(session);
      await refreshRequests(session);
      await refreshReports(session, { fromDate: reportFromDate || undefined, toDate: reportToDate || undefined });
      setApprovalMessage(action === "approve" ? "Đã duyệt đơn." : "Đã từ chối đơn.");
      pushToast("success", action === "approve" ? "Duyệt đơn thành công." : "Đã từ chối đơn.");
    } catch (error) {
      setApprovalMessage(toMessage(error));
      pushToast("error", toMessage(error));
    }
  }

  async function handleProjectSave(event) {
    event.preventDefault();
    if (!session?.token || !canManageProjects) return;
    setProjectMessage("");
    try {
      if (editingProjectId) await updateProject(session.token, editingProjectId, projectForm);
      else await createProject(session.token, projectForm);
      setProjectForm(EMPTY_PROJECT_FORM);
      setEditingProjectId(null);
      await refreshProjects(session);
      setProjectMessage("Đã lưu dự án.");
      pushToast("success", "Lưu dự án thành công.");
    } catch (error) {
      setProjectMessage(toMessage(error));
      pushToast("error", toMessage(error));
    }
  }

  async function handleProjectDelete(projectId) {
    if (!session?.token || !canManageProjects) return;
    if (!window.confirm("Bạn chắc chắn muốn xóa dự án này?")) return;
    setProjectMessage("");
    try {
      await removeProject(session.token, projectId);
      await refreshProjects(session);
      setProjectMessage("Đã xóa dự án.");
      pushToast("success", "Đã lưu trữ dự án.");
    } catch (error) {
      setProjectMessage(toMessage(error));
      pushToast("error", toMessage(error));
    }
  }

  async function handleProjectAssign(projectId) {
    if (!canAssignProjects) return;
    setAssignProjectId(projectId);
    setSelectedEmployeeEmail("");
    setEmployeeQuery("");
  }

  async function handleAssignSubmit(event) {
    event.preventDefault();
    if (!session?.token || !canAssignProjects || !assignProjectId || !selectedEmployeeEmail) return;
    setProjectMessage("");
    try {
      await assignEmployeeToProject(session.token, assignProjectId, { employeeEmail: selectedEmployeeEmail });
      setProjectMessage("Đã giao dự án cho nhân viên.");
      pushToast("success", "Giao dự án thành công.");
      setAssignProjectId(null);
      setSelectedEmployeeEmail("");
      await refreshProjects(session);
    } catch (error) {
      setProjectMessage(toMessage(error));
      pushToast("error", toMessage(error));
    }
  }

  async function handleApplyReportFilter() {
    if (!session?.token || !canViewReports) return;
    setReportMessage("");
    await refreshReports(session, { fromDate: reportFromDate || undefined, toDate: reportToDate || undefined });
    pushToast("success", "Đã cập nhật bộ lọc báo cáo.");
  }

  async function handleResetReportFilter() {
    if (!session?.token || !canViewReports) return;
    setReportFromDate("");
    setReportToDate("");
    setReportMessage("");
    await refreshReports(session, {});
    pushToast("success", "Đã bỏ bộ lọc báo cáo.");
  }

  async function handlePolicySave(event) {
    event.preventDefault();
    if (!session?.token || !isAdmin) return;
    setPolicyMessage("");
    try {
      await updateWorkflowPolicy(session.token, { equipmentAdminThreshold: Number(policyThreshold) });
      await refreshPolicy(session);
      setPolicyMessage("Đã cập nhật chính sách.");
      pushToast("success", "Đã cập nhật chính sách.");
    } catch (error) {
      setPolicyMessage(toMessage(error));
      pushToast("error", toMessage(error));
    }
  }

  if (!session) {
    return (
      <main className="auth-page">
        <section className="auth-card">
          <p className="kicker">TaskFlow</p>
          <h1>Cổng làm việc doanh nghiệp</h1>
          <p className="subtitle">Hệ thống vận hành nội bộ theo vai trò.</p>

          <div className="auth-switch">
            <button className={authMode === "login" ? "active" : ""} onClick={() => setAuthMode("login")}>
              Đăng nhập
            </button>
            <button className={authMode === "register" ? "active" : ""} onClick={() => setAuthMode("register")}>
              Đăng ký
            </button>
          </div>

          {authMode === "login" ? (
            <form className="form-grid" onSubmit={handleLogin}>
              <label>Email<input type="email" value={loginForm.email} onChange={(e) => setLoginForm((p) => ({ ...p, email: e.target.value }))} required /></label>
              <label>Mật khẩu<input type="password" value={loginForm.password} onChange={(e) => setLoginForm((p) => ({ ...p, password: e.target.value }))} required /></label>
              <button type="submit" disabled={authLoading}>{authLoading ? "Đang đăng nhập..." : "Đăng nhập"}</button>
              <button type="button" className="ghost" onClick={() => setAuthMode("forgot")}>Forgot password?</button>
            </form>
          ) : authMode === "register" ? (
            <form className="form-grid" onSubmit={handleRegister}>
              <label>Họ và tên<input type="text" value={registerForm.fullName} onChange={(e) => setRegisterForm((p) => ({ ...p, fullName: e.target.value }))} required /></label>
              <label>Email<input type="email" value={registerForm.email} onChange={(e) => setRegisterForm((p) => ({ ...p, email: e.target.value }))} required /></label>
              <label>Mật khẩu<input type="password" minLength={6} value={registerForm.password} onChange={(e) => setRegisterForm((p) => ({ ...p, password: e.target.value }))} required /></label>
              <label>Vai trò<select value={registerForm.role} onChange={(e) => setRegisterForm((p) => ({ ...p, role: e.target.value }))}>
                <option value="EMPLOYEE">Nhân viên</option>
                <option value="MANAGER">Quản lý</option>
                <option value="HR">HR</option>
              </select></label>
              <button type="submit" disabled={authLoading}>{authLoading ? "Đang đăng ký..." : "Tạo tài khoản"}</button>
            </form>
          ) : authMode === "forgot" ? (
            <form className="form-grid" onSubmit={handleForgotPassword}>
              <label>Email
                <input type="email" value={forgotForm.email} onChange={(e) => setForgotForm({ email: e.target.value })} required />
              </label>
              <button type="submit" disabled={authLoading}>{authLoading ? "Processing..." : "Send reset request"}</button>
              <button type="button" className="ghost" onClick={() => setAuthMode("login")}>Back to sign in</button>
            </form>
          ) : (
            <form className="form-grid" onSubmit={handleResetPassword}>
              <label>Reset token
                <input type="text" value={resetForm.token} onChange={(e) => setResetForm((p) => ({ ...p, token: e.target.value }))} required />
              </label>
              <label>New password
                <input type="password" minLength={6} value={resetForm.newPassword} onChange={(e) => setResetForm((p) => ({ ...p, newPassword: e.target.value }))} required />
              </label>
              <label>Confirm password
                <input type="password" minLength={6} value={resetForm.confirmPassword} onChange={(e) => setResetForm((p) => ({ ...p, confirmPassword: e.target.value }))} required />
              </label>
              <button type="submit" disabled={authLoading}>{authLoading ? "Processing..." : "Reset password"}</button>
              <button type="button" className="ghost" onClick={() => setAuthMode("login")}>Back to sign in</button>
            </form>
          )}

          {authMode === "login" ? (
            <div className="google-auth-block">
              <p className="muted">hoặc</p>
              {GOOGLE_CLIENT_ID ? (
                <div ref={googleButtonRef} className="google-button-slot" />
              ) : (
                <p className="feedback">Thiếu cấu hình VITE_GOOGLE_CLIENT_ID ở frontend.</p>
              )}
            </div>
          ) : null}

          {authMessage ? <p className="feedback error">{authMessage}</p> : null}
        </section>
      </main>
    );
  }

  return (
    <main className="enterprise-app">
      <aside className="enterprise-sidebar">
        <div className="brand-block">
          <p className="kicker">TaskFlow</p>
          <h3>Trung tâm điều hành</h3>
          <p className="muted">{ROLE_LABEL[role] || role}</p>
        </div>
        <nav className="module-nav">
          {modules.map((moduleId) => (
            <button
              key={moduleId}
              className={activeModule === moduleId ? "active" : ""}
              onClick={() => setActiveModule(moduleId)}
            >
              {MODULE_LABEL[moduleId]}
            </button>
          ))}
        </nav>
        <div className="sidebar-footer">
          <button className="ghost" onClick={handleLogout}>Đăng xuất</button>
        </div>
      </aside>

      <section className="enterprise-main">
        <header className="topbar enterprise">
          <div>
            <p className="kicker">Không gian làm việc</p>
            <h2>{MODULE_LABEL[activeModule]}</h2>
            <p className="subtitle">Màn hình vận hành cho {ROLE_LABEL[role] || role}</p>
          </div>
          <div className="session-box">
            <p>{profile?.fullName || session.email}</p>
            <strong>{profile?.email || session.email}</strong>
          </div>
        </header>

        <section className="stats-grid">
          <article className="stat-card"><p>Đơn của tôi</p><strong>{myRequests.length}</strong></article>
          <article className="stat-card"><p>Đang chờ duyệt</p><strong>{approvalInbox.length}</strong></article>
          <article className="stat-card"><p>Dự án</p><strong>{projects.length}</strong></article>
          <article className="stat-card"><p>Vai trò</p><strong>{ROLE_LABEL[role] || role}</strong></article>
        </section>

        {activeModule === "dashboard" ? (
          <section className="panel">
            <div className="panel-header">
              <h3>Tổng quan vận hành</h3>
              <span>Trạng thái không gian làm việc theo vai trò</span>
            </div>
            <div className="guide-box">
              {isAdmin ? (
                <>
                  <p>Bạn đang ở trang quản trị hệ thống.</p>
                  <p>Dùng các mục Dự án, Báo cáo và Chính sách để vận hành nền tảng.</p>
                </>
              ) : (
                <>
                  <p>Tạo đơn nghỉ phép ở mục Xin nghỉ phép.</p>
                  <p>Theo dõi đơn của bạn ở mục Đơn của tôi.</p>
                  <p>Nếu có quyền duyệt, xử lý tại mục Phê duyệt.</p>
                </>
              )}
            </div>
          </section>
        ) : null}

        {activeModule === "profile" ? (
          <section className="panel">
            <div className="panel-header"><h3>Hồ sơ của tôi</h3><span>Tự quản lý thông tin tài khoản</span></div>
            <form className="project-form" onSubmit={handleProfileSave}>
              <label>Họ và tên
                <input value={profileForm.fullName} onChange={(e) => setProfileForm((p) => ({ ...p, fullName: e.target.value }))} maxLength={255} required />
              </label>
              <label>Email (chỉ đọc)<input value={profile?.email || session.email} disabled /></label>
              <label>Vai trò (chỉ đọc)<input value={ROLE_LABEL[role] || role} disabled /></label>
              <label>Mật khẩu mới (không bắt buộc)
                <input type="password" minLength={6} value={profileForm.newPassword} onChange={(e) => setProfileForm((p) => ({ ...p, newPassword: e.target.value }))} />
              </label>
              <button type="submit" disabled={profileSaving}>{profileSaving ? "Đang lưu..." : "Lưu thay đổi"}</button>
            </form>
            {profileMessage ? <p className="feedback">{profileMessage}</p> : null}
          </section>
        ) : null}

        {activeModule === "leave" ? (
          <section className="panel">
            <div className="panel-header"><h3>Xin nghỉ phép</h3><span>Biểu mẫu thân thiện cho người dùng</span></div>
            <form className="project-form" onSubmit={handleLeaveSubmit}>
              <label>Loại nghỉ phép
                <select value={leaveForm.leaveType} onChange={(e) => setLeaveForm((p) => ({ ...p, leaveType: e.target.value }))}>
                  <option value="ANNUAL">Nghỉ phép năm</option>
                  <option value="SICK">Nghỉ ốm</option>
                  <option value="UNPAID">Nghỉ không lương</option>
                  <option value="MATERNITY">Nghỉ thai sản</option>
                  <option value="OTHER">Khác</option>
                </select>
              </label>
              <div className="date-grid">
                <label>Từ ngày<input type="date" value={leaveForm.startDate} onChange={(e) => setLeaveForm((p) => ({ ...p, startDate: e.target.value }))} required /></label>
                <label>Đến ngày<input type="date" value={leaveForm.endDate} onChange={(e) => setLeaveForm((p) => ({ ...p, endDate: e.target.value }))} required /></label>
              </div>
              <label>Lý do
                <textarea rows={3} value={leaveForm.reason} onChange={(e) => setLeaveForm((p) => ({ ...p, reason: e.target.value }))} maxLength={1000} required />
              </label>
              <label>Ghi chú bàn giao (không bắt buộc)
                <textarea rows={2} value={leaveForm.handoverNote} onChange={(e) => setLeaveForm((p) => ({ ...p, handoverNote: e.target.value }))} maxLength={1000} />
              </label>
              <button type="submit" disabled={leaveSaving}>{leaveSaving ? "Đang tạo..." : "Tạo đơn"}</button>
            </form>
            {leaveMessage ? <p className="feedback">{leaveMessage}</p> : null}
          </section>
        ) : null}

        {activeModule === "my_requests" ? (
          <section className="panel">
            <div className="panel-header"><h3>Đơn của tôi</h3><span>Chỉ hiển thị đơn do bạn tạo</span></div>
            {requestMessage ? <p className="feedback">{requestMessage}</p> : null}
            <div className="toolbar">
              <select value={requestStatusFilter} onChange={(e) => setRequestStatusFilter(e.target.value)}>
                <option value="ALL">Tất cả trạng thái</option>
                <option value="DRAFT">Nháp</option>
                <option value="IN_REVIEW">Đang duyệt</option>
                <option value="APPROVED">Đã duyệt</option>
                <option value="REJECTED">Từ chối</option>
                <option value="CANCELLED">Đã hủy</option>
              </select>
            </div>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr><th>Mã đơn</th><th>Tiêu đề</th><th>Loại</th><th>Trạng thái</th><th>Ngày tạo</th><th>Thao tác</th></tr>
                </thead>
                <tbody>
                  {filteredRequests.length === 0 ? (
                    <tr><td colSpan="6" className="muted">Không có đơn phù hợp.</td></tr>
                  ) : (
                    filteredRequests.map((item) => (
                      <tr key={item.id}>
                        <td>{item.requestCode}</td>
                        <td>{item.title}</td>
                        <td>{item.type}</td>
                        <td>{statusLabel(item.status)}</td>
                        <td>{formatDateTime(item.createdAt)}</td>
                        <td>
                          <div className="actions-row">
                            <button type="button" className="ghost" onClick={() => handleMyRequestAction(item.id, "submit")} disabled={item.status !== "DRAFT"}>Gửi duyệt</button>
                            <button type="button" className="danger" onClick={() => handleMyRequestAction(item.id, "cancel")} disabled={item.status === "APPROVED" || item.status === "REJECTED" || item.status === "CANCELLED"}>Hủy</button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>
        ) : null}

        {activeModule === "approvals" ? (
          <section className="panel">
            <div className="panel-header"><h3>Phê duyệt</h3><span>Danh sách chờ vai trò của bạn xử lý</span></div>
            <label>Ghi chú phê duyệt
              <input value={approvalComment} onChange={(e) => setApprovalComment(e.target.value)} maxLength={1000} />
            </label>
            {approvalMessage ? <p className="feedback">{approvalMessage}</p> : null}
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr><th>Mã đơn</th><th>Tiêu đề</th><th>Người tạo</th><th>Bước</th><th>Ngày gửi</th><th>Thao tác</th></tr>
                </thead>
                <tbody>
                  {approvalInbox.length === 0 ? (
                    <tr><td colSpan="6" className="muted">Không có đơn chờ duyệt.</td></tr>
                  ) : (
                    approvalInbox.map((item) => (
                      <tr key={item.requestId}>
                        <td>{item.requestCode}</td>
                        <td>{item.title}</td>
                        <td>{item.requesterName || item.requesterEmail}</td>
                        <td>{item.stepNo}</td>
                        <td>{formatDateTime(item.submittedAt)}</td>
                        <td>
                          <div className="actions-row">
                            <button type="button" onClick={() => handleApprovalAction(item.requestId, "approve")}>Duyệt</button>
                            <button type="button" className="danger" onClick={() => handleApprovalAction(item.requestId, "reject")}>Từ chối</button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>
        ) : null}

        {activeModule === "projects" ? (
          <section className="panel">
            <div className="panel-header"><h3>Dự án</h3><span>{canManageProjects ? "Quản lý có quyền tạo/sửa/lưu trữ dự án" : canAssignProjects ? "HR có quyền giao dự án cho nhân viên" : "Chế độ chỉ xem"}</span></div>
            {canManageProjects ? (
              <form className="project-form" onSubmit={handleProjectSave}>
              <label>Tên dự án<input value={projectForm.name} onChange={(e) => setProjectForm((p) => ({ ...p, name: e.target.value }))} maxLength={150} required /></label>
              <label>Mô tả<textarea rows={3} value={projectForm.description} onChange={(e) => setProjectForm((p) => ({ ...p, description: e.target.value }))} maxLength={1000} /></label>
                <div className="actions-row">
                  <button type="submit">{editingProjectId ? "Cập nhật dự án" : "Tạo dự án"}</button>
                  {editingProjectId ? <button type="button" className="ghost" onClick={() => { setEditingProjectId(null); setProjectForm(EMPTY_PROJECT_FORM); }}>Hủy</button> : null}
                </div>
              </form>
            ) : null}
            {projectMessage ? <p className="feedback">{projectMessage}</p> : null}
            {canAssignProjects ? (
              <form className="project-form assign-form" onSubmit={handleAssignSubmit}>
                <label>Dự án được chọn
                  <input value={assignProjectId ? `#${assignProjectId}` : ""} placeholder="Bấm 'Giao nhân viên' ở bảng bên dưới" disabled />
                </label>
                <label>Tìm nhân viên
                  <input
                    type="search"
                    value={employeeQuery}
                    onChange={(e) => setEmployeeQuery(e.target.value)}
                    placeholder="Tìm theo tên hoặc email"
                  />
                </label>
                <label>Chọn nhân viên
                  <select value={selectedEmployeeEmail} onChange={(e) => setSelectedEmployeeEmail(e.target.value)} required>
                    <option value="">-- Chọn nhân viên --</option>
                    {employeeOptions.map((item) => (
                      <option key={item.email} value={item.email}>
                        {item.fullName} - {item.email}
                      </option>
                    ))}
                  </select>
                </label>
                <div className="actions-row">
                  <button type="submit" disabled={!assignProjectId || !selectedEmployeeEmail}>Xác nhận giao dự án</button>
                  <button type="button" className="ghost" onClick={() => { setAssignProjectId(null); setSelectedEmployeeEmail(""); }}>
                    Hủy chọn
                  </button>
                </div>
              </form>
            ) : null}
            <div className="toolbar">
              <input type="search" placeholder="Tìm kiếm dự án" value={projectSearch} onChange={(e) => setProjectSearch(e.target.value)} />
            </div>
            <div className="table-wrap">
              <table className="data-table">
                <thead><tr><th>ID</th><th>Tên</th><th>Mô tả</th><th>Ngày tạo</th><th>Thao tác</th></tr></thead>
                <tbody>
                  {filteredProjects.length === 0 ? (
                    <tr><td colSpan="5" className="muted">Không có dự án phù hợp.</td></tr>
                  ) : (
                    filteredProjects.map((project) => (
                      <tr key={project.id}>
                        <td>#{project.id}</td>
                        <td>{project.name}</td>
                        <td>{project.description || "-"}</td>
                        <td>{formatDateTime(project.createdAt)}</td>
                        <td>
                          {canManageProjects ? (
                            <div className="actions-row">
                              <button type="button" className="ghost" onClick={() => { setEditingProjectId(project.id); setProjectForm({ name: project.name || "", description: project.description || "" }); }}>Sửa</button>
                              <button type="button" className="danger" onClick={() => handleProjectDelete(project.id)}>Xóa</button>
                            </div>
                          ) : canAssignProjects ? (
                            <div className="actions-row">
                              <button type="button" className="ghost" onClick={() => handleProjectAssign(project.id)}>Giao nhân viên</button>
                            </div>
                          ) : <span className="muted">Chỉ xem</span>}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>
        ) : null}

        {activeModule === "reports" ? (
          <section className="panel">
            <div className="panel-header"><h3>Báo cáo</h3><span>Biểu đồ tổng hợp theo trạng thái và loại đơn</span></div>
            <div className="toolbar report-filter">
              <label>Từ ngày
                <input type="date" value={reportFromDate} onChange={(e) => setReportFromDate(e.target.value)} />
              </label>
              <label>Đến ngày
                <input type="date" value={reportToDate} onChange={(e) => setReportToDate(e.target.value)} />
              </label>
              <button type="button" onClick={handleApplyReportFilter}>Áp dụng</button>
              <button type="button" className="ghost" onClick={handleResetReportFilter}>Bỏ lọc</button>
            </div>
            {reportMessage ? <p className="feedback error">{reportMessage}</p> : null}
            {report ? (
              <div className="report-grid">
                <article className="stat-card">
                  <p>Tổng số đơn</p>
                  <strong>{report.totalRequests}</strong>
                </article>
                <article className="stat-card">
                  <p>Tổng số dự án</p>
                  <strong>{report.totalProjects || 0}</strong>
                </article>
                <article className="stat-card chart-card">
                  <p>Theo trạng thái</p>
                  <div className="chart-list">
                    {reportStatusChart.length === 0 ? <p className="muted">Chưa có dữ liệu.</p> : reportStatusChart.map((item) => (
                      <div className="chart-row" key={item.key}>
                        <div className="chart-meta">
                          <span>{item.label}</span>
                          <strong>{item.count}</strong>
                        </div>
                        <div className="chart-track">
                          <div className="chart-fill" style={{ width: `${item.ratio}%` }} />
                        </div>
                      </div>
                    ))}
                  </div>
                </article>
                <article className="stat-card chart-card">
                  <p>Theo loại đơn</p>
                  <div className="chart-list">
                    {reportTypeChart.length === 0 ? <p className="muted">Chưa có dữ liệu.</p> : reportTypeChart.map((item) => (
                      <div className="chart-row" key={item.key}>
                        <div className="chart-meta">
                          <span>{item.label}</span>
                          <strong>{item.count}</strong>
                        </div>
                        <div className="chart-track">
                          <div className="chart-fill alt" style={{ width: `${item.ratio}%` }} />
                        </div>
                      </div>
                    ))}
                  </div>
                </article>
                <article className="stat-card chart-card">
                  <p>Trạng thái dự án</p>
                  <div className="chart-list">
                    {reportProjectStatusChart.length === 0 ? <p className="muted">Chưa có dữ liệu.</p> : reportProjectStatusChart.map((item) => (
                      <div className="chart-row" key={item.key}>
                        <div className="chart-meta">
                          <span>{item.label}</span>
                          <strong>{item.count}</strong>
                        </div>
                        <div className="chart-track">
                          <div className="chart-fill" style={{ width: `${item.ratio}%` }} />
                        </div>
                      </div>
                    ))}
                  </div>
                </article>
              </div>
            ) : <p className="muted">Chưa có dữ liệu báo cáo.</p>}
          </section>
        ) : null}

        {false && activeModule === "reports" ? (
          <section className="panel">
            <div className="panel-header"><h3>Báo cáo</h3><span>Chỉ số tổng hợp cho vai trò quản lý</span></div>
            {reportMessage ? <p className="feedback error">{reportMessage}</p> : null}
            {report ? (
              <div className="report-grid">
                <article className="stat-card">
                  <p>Tổng số đơn</p>
                  <strong>{report.totalRequests}</strong>
                </article>
                <article className="stat-card">
                  <p>Theo trạng thái</p>
                  <ul className="flat-list">{Object.entries(report.byStatus || {}).map(([k, v]) => <li key={k}>{k}: {v}</li>)}</ul>
                </article>
                <article className="stat-card">
                  <p>Theo loại</p>
                  <ul className="flat-list">{Object.entries(report.byType || {}).map(([k, v]) => <li key={k}>{k}: {v}</li>)}</ul>
                </article>
              </div>
            ) : <p className="muted">Chưa có dữ liệu báo cáo.</p>}
          </section>
        ) : null}

        {activeModule === "policy" ? (
          <section className="panel">
            <div className="panel-header"><h3>Chính sách quy trình</h3><span>Cấu hình ngưỡng cấp hệ thống</span></div>
            {policyMessage ? <p className="feedback">{policyMessage}</p> : null}
            <form className="project-form" onSubmit={handlePolicySave}>
              <label>Ngưỡng duyệt thiết bị (equipmentAdminThreshold)
                <input type="number" min="0.01" step="0.01" value={policyThreshold} onChange={(e) => setPolicyThreshold(e.target.value)} />
              </label>
              <button type="submit">Lưu chính sách</button>
            </form>
          </section>
        ) : null}
      </section>

      <section className="toast-stack">
        {toasts.map((toast) => (
          <article key={toast.id} className={`toast-item ${toast.type === "error" ? "error" : "success"}`}>
            {toast.message}
          </article>
        ))}
      </section>
    </main>
  );
}

export default App;
