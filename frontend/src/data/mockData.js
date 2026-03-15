export const mockSummary = {
  activeProjects: 4,
  inProgressTasks: 13,
  completedTasks: 27,
  teamMembers: 6
};

export const mockProjects = [
  {
    id: 1,
    name: "TaskFlow Core API",
    description: "Build Spring Boot modules for users, projects, tasks and comments.",
    owner: "Minh Huy",
    progress: 72
  },
  {
    id: 2,
    name: "JWT Authentication",
    description: "Implement login, token refresh and protected API routes.",
    owner: "Bao Anh",
    progress: 45
  },
  {
    id: 3,
    name: "Redis Caching",
    description: "Cache project list and task list endpoints for better performance.",
    owner: "Huu Phuc",
    progress: 20
  }
];

export const mockTasks = [
  {
    id: 101,
    title: "Create ProjectService",
    project: "TaskFlow Core API",
    assignee: "Minh Huy",
    priority: "HIGH",
    status: "IN_PROGRESS"
  },
  {
    id: 102,
    title: "Build task update endpoint",
    project: "TaskFlow Core API",
    assignee: "Bao Anh",
    priority: "MEDIUM",
    status: "TODO"
  },
  {
    id: 103,
    title: "Add JWT filter",
    project: "JWT Authentication",
    assignee: "Huu Phuc",
    priority: "HIGH",
    status: "IN_REVIEW"
  },
  {
    id: 104,
    title: "Cache project list",
    project: "Redis Caching",
    assignee: "Minh Huy",
    priority: "LOW",
    status: "DONE"
  }
];

export const mockUsers = [
  {
    id: 1,
    username: "minhhuy",
    email: "minhhuy@taskflow.dev",
    role: "ADMIN"
  },
  {
    id: 2,
    username: "baoanh",
    email: "baoanh@taskflow.dev",
    role: "MANAGER"
  },
  {
    id: 3,
    username: "huuphuc",
    email: "huuphuc@taskflow.dev",
    role: "USER"
  }
];

export const mockComments = [
  {
    id: 701,
    taskTitle: "Create ProjectService",
    author: "Bao Anh",
    content: "I can help with validation and DTO mapping.",
    createdAt: "2026-03-10 14:20"
  },
  {
    id: 702,
    taskTitle: "Add JWT filter",
    author: "Minh Huy",
    content: "Remember to handle token expiration clearly.",
    createdAt: "2026-03-11 09:05"
  }
];
