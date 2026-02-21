import { useEffect, useMemo, useRef, useState } from "react";

const API_BASE = "http://localhost:8081/api/tasks";

// How many minutes before due time to notify
const REMIND_BEFORE_MINUTES = 1; // change to 5 or 10 if you want

export default function App() {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [dueDate, setDueDate] = useState(""); // "YYYY-MM-DDTHH:mm"
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);

  // Search + filter + sort
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL"); // ALL | ACTIVE | COMPLETED | OVERDUE
  const [sortOrder, setSortOrder] = useState("ASC"); // ASC | DESC

  // Prevent duplicate notifications in this UI session
  const notifiedIdsRef = useRef(new Set());

  // Load tasks on page load
  useEffect(() => {
    fetchTasks();
  }, []);

  // Poll tasks every 10 seconds for UI + notification checks
  useEffect(() => {
    const t = setInterval(() => fetchTasks(false), 10000);
    return () => clearInterval(t);
  }, []);

  // Notification permission
  async function enableNotifications() {
    if (!("Notification" in window)) {
      alert("This browser does not support notifications.");
      return;
    }
    const perm = await Notification.requestPermission();
    if (perm !== "granted") {
      alert("Notification permission denied.");
    }
  }

  // Helper: parse dueDate safely
  function parseDueDate(dueDateStr) {
    // Backend returns like "2026-02-20T19:40:00"
    // new Date("YYYY-MM-DDTHH:mm:ss") works in modern browsers.
    const d = new Date(dueDateStr);
    return isNaN(d.getTime()) ? null : d;
  }

  function isOverdue(task) {
    if (task.completed) return false;
    const d = parseDueDate(task.dueDate);
    if (!d) return false;
    return d.getTime() < Date.now();
  }

  function isDueSoon(task, minutes = REMIND_BEFORE_MINUTES) {
    if (task.completed) return false;
    const d = parseDueDate(task.dueDate);
    if (!d) return false;

    const now = Date.now();
    const diffMs = d.getTime() - now;
    const diffMin = diffMs / (60 * 1000);

    // due in next X minutes (0 to X)
    return diffMin >= 0 && diffMin <= minutes;
  }

  async function fetchTasks(showAlertOnError = true) {
    try {
      setLoading(true);
      const res = await fetch(API_BASE);
      if (!res.ok) throw new Error("Failed to fetch tasks");
      const data = await res.json();
      const list = Array.isArray(data) ? data : [];
      setTasks(list);

      // Run notification check after fetching
      checkAndNotify(list);
    } catch (err) {
      console.error(err);
      if (showAlertOnError) alert("Failed to load tasks. Check backend + CORS.");
    } finally {
      setLoading(false);
    }
  }

  function checkAndNotify(list) {
    if (!("Notification" in window)) return;
    if (Notification.permission !== "granted") return;

    for (const t of list) {
      // Notify only once per task per UI session
      if (notifiedIdsRef.current.has(t.id)) continue;

      if (isDueSoon(t)) {
        notifiedIdsRef.current.add(t.id);

        new Notification("⏰ Task Reminder", {
          body: `${t.title} is due soon (${t.dueDate})`,
        });
      }
    }
  }

  async function addTask(e) {
    e.preventDefault();

    if (!title.trim()) return alert("Title is required");
    if (!dueDate) return alert("Due date is required");

    // Input gives: "YYYY-MM-DDTHH:mm"
    // Backend expects: "YYYY-MM-DDTHH:mm:ss"
    const formattedDueDate = `${dueDate}:00`;

    const payload = {
      title: title.trim(),
      description: description.trim(),
      dueDate: formattedDueDate,
      completed: false,
      notificationSent: false,
    };

    try {
      const res = await fetch(API_BASE, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const txt = await res.text();
        console.error("Add task failed:", txt);
        throw new Error("Add task failed");
      }

      setTitle("");
      setDescription("");
      setDueDate("");
      await fetchTasks();
    } catch (err) {
      console.error(err);
      alert("Failed to add task. Check backend + CORS.");
    }
  }

  async function deleteTask(id) {
    try {
      const res = await fetch(`${API_BASE}/${id}`, { method: "DELETE" });
      if (!res.ok) throw new Error("Delete failed");

      // remove notification state too
      notifiedIdsRef.current.delete(id);

      await fetchTasks(false);
    } catch (err) {
      console.error(err);
      alert("Failed to delete task.");
    }
  }

  async function toggleCompleted(task) {
    try {
      const res = await fetch(`${API_BASE}/${task.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...task,
          completed: !task.completed,
        }),
      });

      if (!res.ok) throw new Error("Update failed");
      await fetchTasks(false);
    } catch (err) {
      console.error(err);
      alert("Failed to update task.");
    }
  }

  // Apply search + filter + sort
  const visibleTasks = useMemo(() => {
    const q = search.trim().toLowerCase();

    let list = [...tasks];

    // Search
    if (q) {
      list = list.filter((t) => {
        const a = (t.title || "").toLowerCase();
        const b = (t.description || "").toLowerCase();
        return a.includes(q) || b.includes(q);
      });
    }

    // Filter
    if (filter === "ACTIVE") {
      list = list.filter((t) => !t.completed);
    } else if (filter === "COMPLETED") {
      list = list.filter((t) => t.completed);
    } else if (filter === "OVERDUE") {
      list = list.filter((t) => isOverdue(t));
    }

    // Sort by dueDate
    list.sort((x, y) => {
      const dx = parseDueDate(x.dueDate)?.getTime() ?? 0;
      const dy = parseDueDate(y.dueDate)?.getTime() ?? 0;
      return sortOrder === "ASC" ? dx - dy : dy - dx;
    });

    return list;
  }, [tasks, search, filter, sortOrder]);

  return (
    <div style={styles.page}>
      <h1 style={styles.heading}>✅ Smart Task Manager</h1>

      <div style={styles.toolbar}>
        <input
          style={styles.search}
          placeholder="Search tasks..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        <select
          style={styles.select}
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
        >
          <option value="ALL">All</option>
          <option value="ACTIVE">Active</option>
          <option value="COMPLETED">Completed</option>
          <option value="OVERDUE">Overdue</option>
        </select>

        <select
          style={styles.select}
          value={sortOrder}
          onChange={(e) => setSortOrder(e.target.value)}
        >
          <option value="ASC">Due: Oldest → Newest</option>
          <option value="DESC">Due: Newest → Oldest</option>
        </select>

        <button style={styles.notifyBtn} onClick={enableNotifications}>
          Enable Notifications
        </button>
      </div>

      <form onSubmit={addTask} style={styles.form}>
        <input
          style={styles.input}
          type="text"
          placeholder="Task title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />

        <textarea
          style={styles.textarea}
          placeholder="Description (optional)"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        <input
          style={styles.input}
          type="datetime-local"
          value={dueDate}
          onChange={(e) => setDueDate(e.target.value)}
        />

        <button style={styles.button} type="submit">
          Add Task
        </button>
      </form>

      <div style={{ marginTop: 20 }}>
        {loading ? (
          <p>Loading...</p>
        ) : visibleTasks.length === 0 ? (
          <p>No tasks found.</p>
        ) : (
          <div style={styles.list}>
            {visibleTasks.map((t) => {
              const overdue = isOverdue(t);

              return (
                <div key={t.id} style={styles.card}>
                  <div style={{ flex: 1 }}>
                    <div style={styles.row}>
                      <h3
                        style={{
                          margin: 0,
                          textDecoration: t.completed ? "line-through" : "none",
                        }}
                      >
                        {t.title}
                      </h3>

                      {overdue && (
                        <span style={styles.badge}>OVERDUE</span>
                      )}
                      {t.completed && (
                        <span style={{ ...styles.badge, background: "#2ecc71" }}>
                          DONE
                        </span>
                      )}
                    </div>

                    {t.description && (
                      <p style={{ margin: "6px 0", opacity: 0.8 }}>
                        {t.description}
                      </p>
                    )}

                    <p style={{ margin: "6px 0", fontSize: 14 }}>
                      ⏰ Due: {t.dueDate}
                    </p>
                  </div>

                  <div style={styles.actions}>
                    <button
                      style={styles.smallBtn}
                      onClick={() => toggleCompleted(t)}
                    >
                      {t.completed ? "Undo" : "Done"}
                    </button>

                    <button
                      style={{ ...styles.smallBtn, background: "#c0392b" }}
                      onClick={() => deleteTask(t.id)}
                    >
                      Delete
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: {
    maxWidth: 760,
    margin: "30px auto",
    padding: 20,
    fontFamily: "Arial, sans-serif",
  },
  heading: {
    fontSize: 34,
    marginBottom: 14,
  },
  toolbar: {
    display: "flex",
    gap: 10,
    flexWrap: "wrap",
    alignItems: "center",
    marginBottom: 16,
  },
  search: {
    flex: 1,
    minWidth: 200,
    padding: 10,
    borderRadius: 10,
    border: "1px solid #ccc",
    fontSize: 14,
  },
  select: {
    padding: 10,
    borderRadius: 10,
    border: "1px solid #ccc",
    fontSize: 14,
  },
  notifyBtn: {
    padding: 10,
    borderRadius: 10,
    border: "1px solid #222",
    background: "white",
    cursor: "pointer",
    fontSize: 14,
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: 12,
  },
  input: {
    padding: 12,
    fontSize: 16,
    borderRadius: 10,
    border: "1px solid #ccc",
  },
  textarea: {
    padding: 12,
    fontSize: 16,
    borderRadius: 10,
    border: "1px solid #ccc",
    minHeight: 70,
  },
  button: {
    padding: 12,
    fontSize: 16,
    borderRadius: 10,
    border: "none",
    background: "black",
    color: "white",
    cursor: "pointer",
  },
  list: {
    display: "flex",
    flexDirection: "column",
    gap: 12,
  },
  card: {
    display: "flex",
    gap: 12,
    alignItems: "center",
    padding: 14,
    borderRadius: 12,
    border: "1px solid #ddd",
  },
  row: {
    display: "flex",
    gap: 10,
    alignItems: "center",
    flexWrap: "wrap",
  },
  badge: {
    padding: "4px 8px",
    borderRadius: 999,
    background: "#e74c3c",
    color: "white",
    fontSize: 12,
    fontWeight: 700,
  },
  actions: {
    display: "flex",
    flexDirection: "column",
    gap: 8,
  },
  smallBtn: {
    padding: "8px 10px",
    borderRadius: 10,
    border: "none",
    background: "#2c3e50",
    color: "white",
    cursor: "pointer",
    minWidth: 90,
  },
};