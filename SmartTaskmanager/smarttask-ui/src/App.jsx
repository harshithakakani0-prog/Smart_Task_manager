import { useEffect, useMemo, useState } from "react";

const API = "http://localhost:8080/api/tasks";

export default function App() {
  const [tasks, setTasks] = useState([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [dueDate, setDueDate] = useState(""); // datetime-local
  const [loading, setLoading] = useState(false);

  // ✅ Fetch tasks
  const loadTasks = async () => {
    setLoading(true);
    try {
      const res = await fetch(API);
      const data = await res.json();
      setTasks(data);
    } catch (e) {
      alert("Backend not running or API issue!");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
  }, []);

  // ✅ Sort UI also (extra safety)
  const sortedTasks = useMemo(() => {
    return [...tasks].sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate));
  }, [tasks]);

  // ✅ Notification permission
  useEffect(() => {
    if ("Notification" in window && Notification.permission === "default") {
      Notification.requestPermission();
    }
  }, []);

  // ✅ Reminder popup every 15 sec
  useEffect(() => {
    const id = setInterval(() => {
      const now = new Date();

      sortedTasks.forEach((t) => {
        if (t.completed) return;

        const due = new Date(t.dueDate);
        const diffMs = due - now;

        if (diffMs > 0 && diffMs <= 10 * 60 * 1000) {
          if ("Notification" in window && Notification.permission === "granted") {
            new Notification("⏰ Task Reminder", {
              body: `${t.title} is due at ${due.toLocaleString()}`,
            });
          }
        }
      });
    }, 15000);

    return () => clearInterval(id);
  }, [sortedTasks]);

  // ✅ Create task
  const addTask = async (e) => {
    e.preventDefault();
    if (!title || !dueDate) return alert("Title and Due Date are required!");

    // datetime-local gives: "2026-02-20T17:30"
    // backend wants LocalDateTime: "2026-02-20T17:30:00"
    const payload = {
      title,
      description,
      dueDate: dueDate.length === 16 ? `${dueDate}:00` : dueDate,
      completed: false,
      notificationSent: false,
    };

    try {
      const res = await fetch(API, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const text = await res.text();
        alert("Error adding task: " + text);
        return;
      }

      setTitle("");
      setDescription("");
      setDueDate("");
      loadTasks();
    } catch (e) {
      alert("Failed to add task. Check backend + CORS.");
    }
  };

  // ✅ Mark completed
  const toggleComplete = async (task) => {
    try {
      await fetch(`${API}/${task.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...task, completed: !task.completed }),
      });
      loadTasks();
    } catch (e) {
      alert("Update failed!");
    }
  };

  // ✅ Delete manually
  const deleteTask = async (id) => {
    try {
      await fetch(`${API}/${id}`, { method: "DELETE" });
      loadTasks();
    } catch (e) {
      alert("Delete failed!");
    }
  };

  return (
    <div style={{ maxWidth: 800, margin: "40px auto", padding: 16, fontFamily: "Arial" }}>
      <h1>✅ Smart Task Manager</h1>

      <form onSubmit={addTask} style={{ display: "grid", gap: 10, marginBottom: 20 }}>
        <input
          placeholder="Task title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          style={{ padding: 10, borderRadius: 8, border: "1px solid #ccc" }}
        />

        <textarea
          placeholder="Description (optional)"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          style={{ padding: 10, borderRadius: 8, border: "1px solid #ccc" }}
        />

        <input
          type="datetime-local"
          value={dueDate}
          onChange={(e) => setDueDate(e.target.value)}
          style={{ padding: 10, borderRadius: 8, border: "1px solid #ccc" }}
        />

        <button
          type="submit"
          style={{ padding: 12, borderRadius: 10, border: "none", background: "black", color: "white" }}
        >
          Add Task
        </button>
      </form>

      {loading ? (
        <p>Loading...</p>
      ) : sortedTasks.length === 0 ? (
        <p>No tasks yet.</p>
      ) : (
        <div style={{ display: "grid", gap: 12 }}>
          {sortedTasks.map((t) => (
            <div
              key={t.id}
              style={{
                padding: 12,
                borderRadius: 12,
                border: "1px solid #ccc",
                display: "flex",
                justifyContent: "space-between",
                gap: 10,
              }}
            >
              <div>
                <h3 style={{ margin: 0, textDecoration: t.completed ? "line-through" : "none" }}>
                  {t.title}
                </h3>
                <p style={{ margin: "6px 0", color: "#444" }}>{t.description}</p>
                <small>📅 Due: {new Date(t.dueDate).toLocaleString()}</small>
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                <button onClick={() => toggleComplete(t)} style={{ padding: 8 }}>
                  {t.completed ? "Undo" : "Complete"}
                </button>
                <button onClick={() => deleteTask(t.id)} style={{ padding: 8 }}>
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}