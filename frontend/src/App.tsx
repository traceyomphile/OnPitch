import { useState } from "react";
import type { CSSProperties, ReactNode } from "react";

// ─── Types ────────────────────────────────────────────────────────────────────
type Role = "Player" | "Coach";
type View = "home" | "login" | "signup" | "dashboard";

interface User {
  name: string;
  email: string;
  role: Role;
}

interface ApiResponse {
  success: boolean;
  message?: string;
  user?: User;
}

interface BackendUser {
  name?: string;
  username?: string;
  email?: string;
  role?: string;
}

interface BackendResponse extends BackendUser {
  success?: boolean;
  message?: string;
  error?: string;
  user?: BackendUser;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

function normaliseRole(role?: string): Role {
  return role?.toLowerCase() === "coach" ? "Coach" : "Player";
}

function normaliseUser(data: BackendUser): User {
  return {
    name: data.name || data.username || "",
    email: data.email || "",
    role: normaliseRole(data.role),
  };
}

async function requestJson(path: string, body: Record<string, unknown>): Promise<BackendResponse> {
  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
  } catch {
    return {
      success: false,
      message: `Could not reach backend at ${API_BASE_URL}. Start your Java server and check CORS.`,
    };
  }

  const text = await response.text();
  let data: BackendResponse = {};

  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = { message: text };
    }
  }

  if (!response.ok) {
    return {
      success: false,
      message: data.message || data.error || `Request failed with status ${response.status}.`,
    };
  }

  return data;
}

function toApiResponse(data: BackendResponse, fallbackUser: User): ApiResponse {
  if ("success" in data && data.success === false) {
    return { success: false, message: data.message || "Request failed." };
  }

  const userData = "user" in data && data.user ? data.user : data;
  const user = normaliseUser(userData as BackendUser);

  return {
    success: true,
    message: "message" in data ? data.message : undefined,
    user: {
      name: user.name || fallbackUser.name,
      email: user.email || fallbackUser.email,
      role: user.role || fallbackUser.role,
    },
  };
}

// ─── API ─────────────────────────────────────────────────────────────────────
const api = {
  login: async (email: string, password: string): Promise<ApiResponse> => {
    const data = await requestJson("/api/auth/login", { email, password });
    return toApiResponse(data, { name: email.split("@")[0], email, role: "Player" });
  },

  signup: async (
    name: string,
    email: string,
    password: string,
    role: Role
  ): Promise<ApiResponse> => {
    const data = await requestJson("/api/auth/signup", { name, email, password, role });
    return toApiResponse(data, { name, email, role });
  },
};

// ─── Password strength ────────────────────────────────────────────────────────
function checkPassword(pw: string) {
  return {
    length: pw.length >= 8,
    upper: /[A-Z]/.test(pw),
    lower: /[a-z]/.test(pw),
    digit: /\d/.test(pw),
    special: /[!@#$%^&*()]/.test(pw),
  };
}
function isStrongPassword(pw: string) {
  const c = checkPassword(pw);
  return c.length && c.upper && c.lower && c.digit && c.special;
}

// ─── Input Field ──────────────────────────────────────────────────────────────
function Field({
  label, type = "text", value, onChange, placeholder, error, rightSlot,
}: {
  label: string; type?: string; value: string;
  onChange: (v: string) => void; placeholder?: string;
  error?: string; rightSlot?: ReactNode;
}) {
  return (
    <div style={{ marginBottom: 20 }}>
      <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: "#ccc", marginBottom: 6, letterSpacing: "0.04em" }}>
        {label.toUpperCase()}
      </label>
      <div style={{ position: "relative" }}>
        <input
          type={type}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          style={{
            width: "100%", boxSizing: "border-box",
            background: "#1a1a1a", border: `1.5px solid ${error ? "#ff5252" : "#2e2e2e"}`,
            borderRadius: 8, padding: "13px 16px",
            paddingRight: rightSlot ? 44 : 16,
            color: "white", fontSize: 15, outline: "none",
            transition: "border-color 0.2s",
            fontFamily: "inherit",
          }}
          onFocus={(e) => { if (!error) e.target.style.borderColor = "#00C853"; }}
          onBlur={(e) => { if (!error) e.target.style.borderColor = "#2e2e2e"; }}
        />
        {rightSlot && (
          <span style={{ position: "absolute", right: 14, top: "50%", transform: "translateY(-50%)", color: "#666", cursor: "pointer" }}>
            {rightSlot}
          </span>
        )}
      </div>
      {error && <p style={{ margin: "5px 0 0", fontSize: 12, color: "#ff5252" }}>{error}</p>}
    </div>
  );
}

// ─── Role Selector ────────────────────────────────────────────────────────────
function RoleSelector({ value, onChange }: { value: Role | ""; onChange: (r: Role) => void }) {
  const roles: { label: string; value: Role; desc: string; emoji: string }[] = [
    { label: "Player", value: "Player", desc: "Find coaches & teams", emoji: "🏏" },
    { label: "Coach", value: "Coach", desc: "Manage & recruit players", emoji: "📋" },
  ];
  return (
    <div style={{ marginBottom: 20 }}>
      <label style={{ display: "block", fontSize: 13, fontWeight: 600, color: "#ccc", marginBottom: 6, letterSpacing: "0.04em" }}>
        ROLE
      </label>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10 }}>
        {roles.map((r) => (
          <button
            key={r.value}
            onClick={() => onChange(r.value)}
            style={{
              background: value === r.value ? "rgba(0,200,83,0.12)" : "#1a1a1a",
              border: `1.5px solid ${value === r.value ? "#00C853" : "#2e2e2e"}`,
              borderRadius: 8, padding: "12px 14px", cursor: "pointer",
              textAlign: "left", transition: "all 0.2s",
            }}
          >
            <div style={{ fontSize: 20, marginBottom: 4 }}>{r.emoji}</div>
            <div style={{ color: "white", fontWeight: 600, fontSize: 14 }}>{r.label}</div>
            <div style={{ color: "#666", fontSize: 12, marginTop: 2 }}>{r.desc}</div>
          </button>
        ))}
      </div>
    </div>
  );
}

// ─── Password Rules ───────────────────────────────────────────────────────────
function PasswordRules({ password }: { password: string }) {
  const c = checkPassword(password);
  const rules = [
    { label: "8+ characters", ok: c.length },
    { label: "Uppercase letter", ok: c.upper },
    { label: "Lowercase letter", ok: c.lower },
    { label: "Number", ok: c.digit },
    { label: "Special character (!@#$%^&*())", ok: c.special },
  ];
  if (!password) return null;
  return (
    <div style={{ marginTop: -10, marginBottom: 18, padding: "12px 14px", background: "#131313", borderRadius: 8, border: "1px solid #222" }}>
      {rules.map((r) => (
        <div key={r.label} style={{ display: "flex", alignItems: "center", gap: 7, marginBottom: 4 }}>
          {r.ok ? <CheckIcon /> : <XIcon />}
          <span style={{ fontSize: 12, color: r.ok ? "#aaa" : "#555" }}>{r.label}</span>
        </div>
      ))}
    </div>
  );
}

function CricketIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 64 64" fill="none" aria-hidden="true">
      <rect width="64" height="64" rx="14" fill="#00C853" />
      <path d="M22 14v36M32 12v38M42 14v36" stroke="white" strokeWidth="4" strokeLinecap="round" />
      <path d="M16 18h32M15 50h34" stroke="white" strokeWidth="4" strokeLinecap="round" />
      <path d="M46 18l-28 28" stroke="#0a0a0a" strokeWidth="6" strokeLinecap="round" />
      <path d="M43 15l6 6" stroke="#0a0a0a" strokeWidth="8" strokeLinecap="round" />
      <circle cx="18" cy="46" r="6" fill="#ffffff" />
      <path d="M14 44c3 2 6 3 10 3" stroke="#00C853" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

function CricketHeroGraphic() {
  return (
    <div style={{ position: "relative", width: 190, height: 150, marginBottom: 40 }}>
      <div
        style={{
          position: "absolute",
          left: 76,
          top: 20,
          width: 18,
          height: 112,
          borderRadius: 10,
          background: "linear-gradient(180deg, #d8a85f 0%, #8b5a25 100%)",
          transform: "rotate(42deg)",
          transformOrigin: "center",
          boxShadow: "0 16px 36px rgba(0,0,0,0.35)",
        }}
      />
      <div
        style={{
          position: "absolute",
          left: 38,
          top: 94,
          width: 26,
          height: 54,
          borderRadius: 12,
          background: "#4a2d14",
          transform: "rotate(42deg)",
        }}
      />
      <div
        style={{
          position: "absolute",
          right: 22,
          bottom: 12,
          display: "flex",
          gap: 8,
          alignItems: "flex-end",
        }}
        aria-hidden="true"
      >
        {[0, 1, 2].map((stump) => (
          <span
            key={stump}
            style={{
              width: 8,
              height: 78,
              borderRadius: 6,
              background: "#f4f4f4",
              boxShadow: "0 0 18px rgba(255,255,255,0.12)",
            }}
          />
        ))}
      </div>
      <div
        style={{
          position: "absolute",
          right: 17,
          bottom: 90,
          width: 62,
          height: 7,
          borderRadius: 10,
          background: "#f4f4f4",
        }}
      />
      <div
        style={{
          position: "absolute",
          left: 18,
          top: 18,
          width: 42,
          height: 42,
          borderRadius: "50%",
          background: "#d81935",
          border: "3px solid #ff6b7e",
          boxShadow: "0 0 24px rgba(216,25,53,0.42)",
          animation: "pulse 2s ease-in-out infinite",
        }}
      >
        <span
          style={{
            position: "absolute",
            left: 18,
            top: 2,
            width: 6,
            height: 32,
            borderRadius: 999,
            borderLeft: "2px solid rgba(255,255,255,0.75)",
            borderRight: "2px solid rgba(255,255,255,0.75)",
            transform: "rotate(-18deg)",
          }}
        />
      </div>
    </div>
  );
}

function CheckIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M20 6 9 17l-5-5" stroke="#00C853" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function XIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M18 6 6 18M6 6l12 12" stroke="#555" strokeWidth="3" strokeLinecap="round" />
    </svg>
  );
}

function EyeIcon({ open }: { open: boolean }) {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      {open ? (
        <>
          <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" stroke="currentColor" strokeWidth="2" />
          <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2" />
        </>
      ) : (
        <>
          <path d="M3 3l18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          <path d="M10.6 6.2A10.6 10.6 0 0 1 12 6c6.5 0 10 6 10 6a17.8 17.8 0 0 1-3 3.8M6.8 6.8C3.7 8.7 2 12 2 12s3.5 6 10 6c1.9 0 3.5-.5 4.9-1.2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        </>
      )}
    </svg>
  );
}

function Spinner() {
  return <span className="spinner" aria-hidden="true" />;
}

// ─── Home / Landing Page ──────────────────────────────────────────────────────
function HomePage({ onNav }: { onNav: (v: View) => void }) {
  const stats = [
    { value: "12K+", label: "Players" },
    { value: "800+", label: "Coaches" },
    { value: "60+", label: "Clubs" },
  ];
  return (
    <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      {/* Nav */}
      <nav style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "20px 40px", borderBottom: "1px solid #1a1a1a" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <CricketIcon />
          <span style={{ fontSize: 20, fontWeight: 800, color: "white", letterSpacing: "-0.5px" }}>OnPitch</span>
        </div>
        <div style={{ display: "flex", gap: 12 }}>
          <button onClick={() => onNav("login")} style={ghostBtn}>Log In</button>
          <button onClick={() => onNav("signup")} style={greenBtn}>Sign Up</button>
        </div>
      </nav>

      {/* Hero */}
      <main style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "80px 24px", textAlign: "center" }}>
        {/* Cricket graphic */}
        <CricketHeroGraphic />

        <div style={{ display: "inline-flex", alignItems: "center", gap: 7, background: "rgba(0,200,83,0.1)", border: "1px solid rgba(0,200,83,0.25)", borderRadius: 20, padding: "6px 14px", marginBottom: 28 }}>
          <span style={{ width: 7, height: 7, background: "#00C853", borderRadius: "50%", display: "inline-block" }} />
          <span style={{ fontSize: 12, color: "#00C853", fontWeight: 600, letterSpacing: "0.06em" }}>NOW IN BETA</span>
        </div>

        <h1 style={{ fontSize: "clamp(40px, 6vw, 72px)", fontWeight: 900, color: "white", lineHeight: 1.05, letterSpacing: "-2px", margin: "0 0 20px", maxWidth: 700 }}>
          Your game.<br />
          <span style={{ color: "#00C853" }}>Your platform.</span>
        </h1>

        <p style={{ fontSize: 18, color: "#777", maxWidth: 480, margin: "0 0 40px", lineHeight: 1.6 }}>
          OnPitch connects players and coaches to build careers, teams, and legacies — all from one place.
        </p>

        <div style={{ display: "flex", gap: 14, flexWrap: "wrap", justifyContent: "center" }}>
          <button onClick={() => onNav("signup")} style={{ ...greenBtn, fontSize: 16, padding: "14px 32px" }}>
            Get started free
          </button>
          <button onClick={() => onNav("login")} style={{ ...ghostBtn, fontSize: 16, padding: "14px 32px" }}>
            Log in
          </button>
        </div>

        {/* Stats */}
        <div style={{ display: "flex", gap: 48, marginTop: 72, flexWrap: "wrap", justifyContent: "center" }}>
          {stats.map((s) => (
            <div key={s.label} style={{ textAlign: "center" }}>
              <div style={{ fontSize: 32, fontWeight: 900, color: "white", letterSpacing: "-1px" }}>{s.value}</div>
              <div style={{ fontSize: 13, color: "#555", marginTop: 2 }}>{s.label}</div>
            </div>
          ))}
        </div>
      </main>

      {/* Footer */}
      <footer style={{ borderTop: "1px solid #1a1a1a", padding: "20px 40px", display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: 10 }}>
        <span style={{ fontSize: 13, color: "#444" }}>© 2026 OnPitch</span>
        <span style={{ fontSize: 12, color: "#333" }}>Built with Java + H2</span>
      </footer>
    </div>
  );
}

// ─── Auth Shell ───────────────────────────────────────────────────────────────
function AuthShell({ title, subtitle, onBack, children }: {
  title: string; subtitle: string; onBack: () => void; children: ReactNode;
}) {
  return (
    <div style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", padding: 24 }}>
      <div style={{ width: "100%", maxWidth: 420 }}>
        {/* Back + Logo */}
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 36 }}>
          <button onClick={onBack} style={{ background: "none", border: "none", color: "#555", cursor: "pointer", fontSize: 13, display: "flex", alignItems: "center", gap: 5, padding: 0 }}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <polyline points="15 18 9 12 15 6" />
            </svg>
            Back
          </button>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <CricketIcon />
            <span style={{ fontWeight: 800, color: "white", fontSize: 17 }}>OnPitch</span>
          </div>
        </div>

        <h2 style={{ fontSize: 28, fontWeight: 800, color: "white", margin: "0 0 6px", letterSpacing: "-0.8px" }}>{title}</h2>
        <p style={{ fontSize: 14, color: "#666", margin: "0 0 30px" }}>{subtitle}</p>

        {children}
      </div>
    </div>
  );
}

// ─── Login Page ───────────────────────────────────────────────────────────────
function LoginPage({ onNav, onLogin }: { onNav: (v: View) => void; onLogin: (u: User) => void }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    setError("");
    if (!email || !password) { setError("Please fill in all fields."); return; }
    setLoading(true);
    const res = await api.login(email, password);
    setLoading(false);
    if (res.success && res.user) {
      onLogin(res.user);
      onNav("dashboard");
    } else {
      setError(res.message || "Login failed.");
    }
  };

  return (
    <AuthShell title="Welcome back" subtitle="Log in to your OnPitch account." onBack={() => onNav("home")}>
      <Field label="Email" type="email" value={email} onChange={setEmail} placeholder="you@example.com" />
      <Field
        label="Password" type={showPw ? "text" : "password"}
        value={password} onChange={setPassword} placeholder="Your password"
        rightSlot={<span onClick={() => setShowPw(!showPw)}><EyeIcon open={showPw} /></span>}
      />

      {error && (
        <div style={{ background: "rgba(255,82,82,0.1)", border: "1px solid rgba(255,82,82,0.25)", borderRadius: 8, padding: "10px 14px", marginBottom: 16, fontSize: 13, color: "#ff5252" }}>
          {error}
        </div>
      )}

      <button onClick={handleSubmit} disabled={loading} style={{ ...greenBtn, width: "100%", fontSize: 15, padding: "14px", marginBottom: 20, justifyContent: "center", display: "flex", alignItems: "center", gap: 8 }}>
        {loading ? <><Spinner /> Logging in…</> : "Log In"}
      </button>

      <p style={{ textAlign: "center", fontSize: 14, color: "#555" }}>
        No account?{" "}
        <button onClick={() => onNav("signup")} style={{ background: "none", border: "none", color: "#00C853", cursor: "pointer", fontSize: 14, fontWeight: 600, padding: 0 }}>
          Sign up
        </button>
      </p>

    </AuthShell>
  );
}

// ─── Sign Up Page ─────────────────────────────────────────────────────────────
function SignupPage({ onNav, onLogin }: { onNav: (v: View) => void; onLogin: (u: User) => void }) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [role, setRole] = useState<Role | "">("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [apiError, setApiError] = useState("");
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const e: Record<string, string> = {};
    if (!name.trim()) e.name = "Name is required.";
    if (!email.trim()) e.email = "Email is required.";
    else if (!/\S+@\S+\.\S+/.test(email)) e.email = "Enter a valid email.";
    if (!password) e.password = "Password is required.";
    else if (!isStrongPassword(password)) e.password = "Password doesn't meet requirements.";
    if (!role) e.role = "Please select a role.";
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async () => {
    setApiError("");
    if (!validate()) return;
    setLoading(true);
    const res = await api.signup(name, email, password, role as Role);
    setLoading(false);
    if (res.success && res.user) {
      onLogin(res.user);
      onNav("dashboard");
    } else {
      setApiError(res.message || "Signup failed.");
    }
  };

  return (
    <AuthShell title="Join OnPitch" subtitle="Create your free account — takes 30 seconds." onBack={() => onNav("home")}>
      <Field label="Username" value={name} onChange={setName} placeholder="Your name" error={errors.name} />
      <Field label="Email" type="email" value={email} onChange={setEmail} placeholder="you@example.com" error={errors.email} />
      <Field
        label="Password" type={showPw ? "text" : "password"}
        value={password} onChange={setPassword} placeholder="Create a strong password"
        error={errors.password}
        rightSlot={<span onClick={() => setShowPw(!showPw)}><EyeIcon open={showPw} /></span>}
      />
      <PasswordRules password={password} />
      <RoleSelector value={role} onChange={setRole} />
      {errors.role && <p style={{ margin: "-10px 0 14px", fontSize: 12, color: "#ff5252" }}>{errors.role}</p>}

      {apiError && (
        <div style={{ background: "rgba(255,82,82,0.1)", border: "1px solid rgba(255,82,82,0.25)", borderRadius: 8, padding: "10px 14px", marginBottom: 16, fontSize: 13, color: "#ff5252" }}>
          {apiError}
        </div>
      )}

      <button onClick={handleSubmit} disabled={loading} style={{ ...greenBtn, width: "100%", fontSize: 15, padding: "14px", marginBottom: 20, justifyContent: "center", display: "flex", alignItems: "center", gap: 8 }}>
        {loading ? <><Spinner /> Creating account…</> : "Create Account"}
      </button>

      <p style={{ textAlign: "center", fontSize: 14, color: "#555" }}>
        Already have an account?{" "}
        <button onClick={() => onNav("login")} style={{ background: "none", border: "none", color: "#00C853", cursor: "pointer", fontSize: 14, fontWeight: 600, padding: 0 }}>
          Log in
        </button>
      </p>
    </AuthShell>
  );
}

// ─── Dashboard Page ───────────────────────────────────────────────────────────
function DashboardPage({ user, onLogout }: { user: User; onLogout: () => void }) {
  const initials = user.name.split(" ").map((n) => n[0]).join("").toUpperCase().slice(0, 2);
  const cards = user.role === "Player"
    ? [
        { icon: "🔍", label: "Find Coaches", desc: "Browse coaches by sport, location, and specialty." },
        { icon: "📅", label: "Book a Session", desc: "Schedule training sessions with your coach." },
        { icon: "📈", label: "Track Progress", desc: "Log your performance and milestones." },
      ]
    : [
        { icon: "👥", label: "Manage Players", desc: "View and update your roster." },
        { icon: "📋", label: "Training Plans", desc: "Create and assign training sessions." },
        { icon: "📊", label: "Analytics", desc: "See your team's performance data." },
      ];

  return (
    <div style={{ minHeight: "100vh" }}>
      {/* Topbar */}
      <nav style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "18px 40px", borderBottom: "1px solid #1a1a1a", position: "sticky", top: 0, background: "#0a0a0a", zIndex: 10 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <CricketIcon />
          <span style={{ fontWeight: 800, color: "white", fontSize: 18 }}>OnPitch</span>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <div style={{ textAlign: "right" }}>
            <div style={{ fontSize: 14, fontWeight: 600, color: "white" }}>{user.name}</div>
            <div style={{ fontSize: 12, color: "#555" }}>{user.role}</div>
          </div>
          <div style={{ width: 38, height: 38, borderRadius: "50%", background: "linear-gradient(135deg, #00C853, #009624)", display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 700, fontSize: 14, color: "white" }}>
            {initials}
          </div>
          <button onClick={onLogout} style={{ background: "none", border: "1px solid #2e2e2e", borderRadius: 7, color: "#666", cursor: "pointer", fontSize: 13, padding: "7px 14px" }}>
            Log out
          </button>
        </div>
      </nav>

      {/* Content */}
      <main style={{ maxWidth: 900, margin: "0 auto", padding: "48px 24px" }}>
        <div style={{ marginBottom: 48 }}>
          <p style={{ fontSize: 13, color: "#00C853", fontWeight: 600, letterSpacing: "0.06em", margin: "0 0 8px" }}>
            {user.role.toUpperCase()} DASHBOARD
          </p>
          <h1 style={{ fontSize: 36, fontWeight: 900, color: "white", margin: "0 0 8px", letterSpacing: "-1px" }}>
            Good to have you, {user.name.split(" ")[0]}.
          </h1>
          <p style={{ fontSize: 15, color: "#555", margin: 0 }}>
            Here's what you can do on OnPitch.
          </p>
        </div>

        {/* Cards */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))", gap: 16, marginBottom: 48 }}>
          {cards.map((c) => (
            <div key={c.label} style={{ background: "#111", border: "1px solid #1e1e1e", borderRadius: 12, padding: 24, cursor: "pointer", transition: "border-color 0.2s" }}
              onMouseEnter={(e) => (e.currentTarget.style.borderColor = "#00C853")}
              onMouseLeave={(e) => (e.currentTarget.style.borderColor = "#1e1e1e")}
            >
              <div style={{ fontSize: 28, marginBottom: 14 }}>{c.icon}</div>
              <div style={{ fontWeight: 700, color: "white", fontSize: 15, marginBottom: 6 }}>{c.label}</div>
              <div style={{ fontSize: 13, color: "#555", lineHeight: 1.5 }}>{c.desc}</div>
            </div>
          ))}
        </div>

        {/* Profile card */}
        <div style={{ background: "#111", border: "1px solid #1e1e1e", borderRadius: 12, padding: 24, display: "flex", alignItems: "center", gap: 20 }}>
          <div style={{ width: 52, height: 52, borderRadius: "50%", background: "linear-gradient(135deg, #00C853, #009624)", display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 800, fontSize: 18, color: "white", flexShrink: 0 }}>
            {initials}
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ fontWeight: 700, color: "white", fontSize: 16 }}>{user.name}</div>
            <div style={{ fontSize: 13, color: "#555" }}>{user.email}</div>
          </div>
          <div style={{ background: "rgba(0,200,83,0.1)", border: "1px solid rgba(0,200,83,0.2)", borderRadius: 20, padding: "5px 14px", fontSize: 12, color: "#00C853", fontWeight: 600 }}>
            {user.role}
          </div>
        </div>
      </main>
    </div>
  );
}

// ─── Button styles ────────────────────────────────────────────────────────────
const greenBtn: CSSProperties = {
  background: "#00C853", border: "none", borderRadius: 8,
  color: "white", fontWeight: 700, fontSize: 14,
  padding: "10px 20px", cursor: "pointer",
  transition: "opacity 0.2s",
};
const ghostBtn: CSSProperties = {
  background: "none", border: "1px solid #2e2e2e", borderRadius: 8,
  color: "#aaa", fontWeight: 600, fontSize: 14,
  padding: "10px 20px", cursor: "pointer",
};

// ─── App Root ─────────────────────────────────────────────────────────────────
export default function App() {
  const [view, setView] = useState<View>("home");
  const [user, setUser] = useState<User | null>(null);

  const handleLogin = (u: User) => setUser(u);
  const handleLogout = () => { setUser(null); setView("home"); };

  return (
    <div style={{ background: "#0a0a0a", minHeight: "100vh", fontFamily: "'Inter', 'Segoe UI', sans-serif", color: "white" }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap');
        * { box-sizing: border-box; }
        body { margin: 0; background: #0a0a0a; }
        input::placeholder { color: #3a3a3a; }
        input:-webkit-autofill { -webkit-box-shadow: 0 0 0 30px #1a1a1a inset !important; -webkit-text-fill-color: white !important; }
        @keyframes spin { to { transform: rotate(360deg); } }
        @keyframes pulse { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.6; transform: scale(0.85); } }
        button:disabled { opacity: 0.6; cursor: not-allowed; }
        ::-webkit-scrollbar { width: 6px; } ::-webkit-scrollbar-track { background: #111; } ::-webkit-scrollbar-thumb { background: #2a2a2a; border-radius: 3px; }
      `}</style>

      {view === "home" && <HomePage onNav={setView} />}
      {view === "login" && <LoginPage onNav={setView} onLogin={handleLogin} />}
      {view === "signup" && <SignupPage onNav={setView} onLogin={handleLogin} />}
      {view === "dashboard" && user && <DashboardPage user={user} onLogout={handleLogout} />}
    </div>
  );
}
