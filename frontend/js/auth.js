// NexDesk 2.0 Client Application Controller

const $ = id => document.getElementById(id);
let currentUser = JSON.parse(localStorage.getItem("nexdeskUser") || "null");
let currentTickets = [];
let currentTechnicians = [];
let activeModalTicket = null;
let aiDebounceTimer = null;
let currentScope = 1;
let currentBlocker = 1;

// Device & Network Telemetry Snapshot
let deviceTelemetry = {
  os: "Unknown OS",
  browser: "Unknown Browser",
  screen: `${window.screen.width}x${window.screen.height}`,
  memory: navigator.deviceMemory ? `${navigator.deviceMemory} GB` : "8 GB",
  latency: "12ms",
  deviceName: "Workstation-Client"
};

// ==========================================
// TOAST NOTIFICATIONS
// ==========================================
function toast(msg, type = "success") {
  const c = $("toastContainer");
  const el = document.createElement("div");
  el.className = `toast ${type}`;
  el.innerHTML = `<span>${type === "success" ? "✅" : "⚠️"}</span> <span>${esc(msg)}</span>`;
  c.appendChild(el);
  setTimeout(() => {
    el.style.opacity = "0";
    el.style.transform = "translateY(10px)";
    setTimeout(() => el.remove(), 300);
  }, 3500);
}

// ==========================================
// API CLIENT (With JWT Bearer Support)
// ==========================================
async function api(url, opt = {}) {
  const headers = { "Content-Type": "application/json", ...(opt.headers || {}) };
  if (currentUser && currentUser.token) {
    headers["Authorization"] = `Bearer ${currentUser.token}`;
  }
  const r = await fetch(url, {
    headers,
    ...opt
  });
  const t = await r.text();
  let d = {};
  try {
    d = t ? JSON.parse(t) : {};
  } catch (err) {
    throw Error("Malformed server response: " + t.substring(0, 100));
  }
  if (!r.ok) {
    const errorMsg = d.message || d.error || `HTTP ${r.status}`;
    if (r.status === 401 && !url.includes("/login") && !url.includes("/signup") && !url.includes("/auth/")) {
      toast("Session expired or unauthorized. Please login again.", "error");
    }
    throw Error(errorMsg);
  }
  return d;
}

function esc(s) {
  return String(s ?? "").replace(/[&<>"']/g, c => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
  }[c]));
}

// ==========================================
// THEME MANAGEMENT
// ==========================================
function initTheme() {
  const saved = localStorage.getItem("nexdeskTheme") || "dark";
  document.documentElement.setAttribute("data-theme", saved);
  $("themeToggle").onclick = () => {
    const cur = document.documentElement.getAttribute("data-theme") || "dark";
    const next = cur === "dark" ? "light" : "dark";
    document.documentElement.setAttribute("data-theme", next);
    localStorage.setItem("nexdeskTheme", next);
    toast(`Switched to ${next} theme`, "success");
  };
}

// ==========================================
// TELEMETRY PROFILER
// ==========================================
async function collectTelemetry() {
  // Detect OS
  const ua = navigator.userAgent;
  if (ua.includes("Windows NT 10.0")) deviceTelemetry.os = "Windows 11 / 10";
  else if (ua.includes("Mac OS X")) deviceTelemetry.os = "macOS Sequoia / Sonoma";
  else if (ua.includes("Linux")) deviceTelemetry.os = "Linux (x86_64)";
  else deviceTelemetry.os = navigator.platform || "Standard OS";

  // Detect Browser
  if (ua.includes("Edg/")) deviceTelemetry.browser = "Microsoft Edge";
  else if (ua.includes("Chrome/")) deviceTelemetry.browser = "Google Chrome";
  else if (ua.includes("Firefox/")) deviceTelemetry.browser = "Mozilla Firefox";
  else if (ua.includes("Safari/")) deviceTelemetry.browser = "Apple Safari";

  // Measure Real Server Latency
  const start = performance.now();
  try {
    await fetch("/api/health");
    const diff = Math.round(performance.now() - start);
    deviceTelemetry.latency = `${diff}ms (Real-time)`;
  } catch (e) {
    deviceTelemetry.latency = "Offline / Cached";
  }

  // Render Telemetry Chips
  const container = $("telemetryChips");
  if (container) {
    container.innerHTML = `
      <div class="telemetry-chip">🖥️ OS: <strong>${deviceTelemetry.os}</strong></div>
      <div class="telemetry-chip">🌐 Browser: <strong>${deviceTelemetry.browser}</strong></div>
      <div class="telemetry-chip">⚡ Server RTT: <strong>${deviceTelemetry.latency}</strong></div>
      <div class="telemetry-chip">🧠 RAM: <strong>${deviceTelemetry.memory}</strong></div>
      <div class="telemetry-chip">📐 Display: <strong>${deviceTelemetry.screen}</strong></div>
    `;
  }
}

// ==========================================
// SYSTEM INFRASTRUCTURE HEALTH
// ==========================================
async function loadSystemHealth() {
  try {
    const res = await api("/api/system/status");
    const dot = $("statusDot");
    const text = $("statusText");

    if (res.overallStatus === "OPERATIONAL") {
      dot.className = "status-indicator operational pulse";
      text.textContent = "Infra: 100% Operational";
    } else {
      dot.className = "status-indicator degraded pulse";
      text.textContent = "Infra: Degraded (M365)";
    }

    // Active Incident Banner
    if (res.activeIncident && !sessionStorage.getItem("dismissOutage")) {
      $("outageTitle").textContent = `${res.activeIncident.incidentId}: ${res.activeIncident.title}`;
      $("outageDesc").textContent = res.activeIncident.impact;
      $("outageBanner").classList.remove("hidden");
    }

    // Modal List
    const listEl = $("infraServicesList");
    if (listEl && res.services) {
      listEl.innerHTML = res.services.map(s => `
        <div style="display:flex; align-items:center; justify-content:space-between; padding:10px 14px; background:rgba(255,255,255,0.03); border:1px solid var(--border-color); border-radius:var(--radius-sm);">
          <div>
            <strong style="font-size:14px;">${esc(s.name)}</strong>
            <div style="font-size:11px; color:var(--text-muted);">${esc(s.category)} &bull; Latency: ${esc(s.latency)}</div>
          </div>
          <div style="display:flex; align-items:center; gap:8px;">
            <span class="badge ${s.status === 'OPERATIONAL' ? 'badge-low' : 'badge-high'}">${esc(s.status)}</span>
            <span style="font-size:12px; font-family:var(--font-mono); color:var(--text-dim);">${esc(s.uptime)}</span>
          </div>
        </div>
      `).join("");
    }

    $("kpiDeflected").textContent = res.deflectedTicketsCount || 148;

  } catch (err) {
    console.warn("Could not fetch system health:", err);
  }
}

// ==========================================
// PASSWORD STRENGTH & VALIDATION ENGINE
// ==========================================
function evaluatePassword(password) {
  const p = password || "";
  const hasLen = p.length >= 8;
  const hasLower = /[a-z]/.test(p);
  const hasUpper = /[A-Z]/.test(p);
  const hasLetters = hasLower && hasUpper;
  const hasNum = /[0-9]/.test(p);
  const hasSym = /[^a-zA-Z0-9]/.test(p);

  let score = 0;
  if (hasLen) score++;
  if (hasLetters) score++;
  if (hasNum) score++;
  if (hasSym) score++;

  let label = "None";
  let color = "var(--text-dim)";
  let pct = 0;

  if (p.length > 0) {
    if (score <= 1) {
      label = "Weak (Insecure)";
      color = "#ef4444";
      pct = 25;
    } else if (score === 2) {
      label = "Fair (Needs more variety)";
      color = "#f59e0b";
      pct = 50;
    } else if (score === 3) {
      label = "Good (Almost there)";
      color = "#38bdf8";
      pct = 75;
    } else if (score === 4) {
      label = "Strong (Enterprise Grade)";
      color = "#10b981";
      pct = 100;
    }
  }

  return {
    score,
    label,
    color,
    pct,
    hasLen,
    hasLetters,
    hasNum,
    hasSym,
    isValid: hasLen && hasLetters && hasNum && hasSym
  };
}

function updatePasswordMeter(prefix, password) {
  const res = evaluatePassword(password);
  const bar = $(prefix + "PassStrengthBar");
  const text = $(prefix + "PassStrengthText");
  const rulePrefix = prefix === "signup" ? "Signup" : (prefix === "tech" ? "Tech" : "Forgot");
  const ruleLen = $("rule" + rulePrefix + "Len");
  const ruleLetters = $("rule" + rulePrefix + "Letters");
  const ruleNum = $("rule" + rulePrefix + "Num");
  const ruleSym = $("rule" + rulePrefix + "Sym");

  if (bar) {
    bar.style.width = res.pct + "%";
    bar.style.backgroundColor = res.color;
  }
  if (text) {
    text.textContent = "Password Strength: " + res.label;
    text.style.color = res.color;
  }
  if (ruleLen) {
    ruleLen.className = "rule-pill " + (res.hasLen ? "valid" : "");
    ruleLen.textContent = (res.hasLen ? "✔" : "✖") + " 8+ characters";
  }
  if (ruleLetters) {
    ruleLetters.className = "rule-pill " + (res.hasLetters ? "valid" : "");
    ruleLetters.textContent = (res.hasLetters ? "✔" : "✖") + " Upper & Lowercase";
  }
  if (ruleNum) {
    ruleNum.className = "rule-pill " + (res.hasNum ? "valid" : "");
    ruleNum.textContent = (res.hasNum ? "✔" : "✖") + " Number (0-9)";
  }
  if (ruleSym) {
    ruleSym.className = "rule-pill " + (res.hasSym ? "valid" : "");
    ruleSym.textContent = (res.hasSym ? "✔" : "✖") + " Special Symbol";
  }
  return res.isValid;
}

// ==========================================
// AUTHENTICATION LOGIC
// ==========================================
function initAuth() {
  $("tabLogin").onclick = () => {
    $("tabLogin").classList.add("active");
    $("tabSignup").classList.remove("active");
    $("formLogin").classList.remove("hidden");
    $("formSignup").classList.add("hidden");
    if ($("formForgot")) $("formForgot").classList.add("hidden");
  };

  $("tabSignup").onclick = () => {
    $("tabSignup").classList.add("active");
    $("tabLogin").classList.remove("active");
    $("formSignup").classList.remove("hidden");
    $("formLogin").classList.add("hidden");
    if ($("formForgot")) $("formForgot").classList.add("hidden");
    updatePasswordMeter("signup", $("signupPass") ? $("signupPass").value : "");
  };

  // Real-time signup password strength monitor
  const signupPassInput = $("signupPass");
  if (signupPassInput) {
    signupPassInput.addEventListener("input", () => {
      updatePasswordMeter("signup", signupPassInput.value);
    });
  }

  // Quick Admin 1-Click Login
  const quickAdminBtn = $("quickDemoAdmin");
  if (quickAdminBtn) {
    quickAdminBtn.onclick = () => {
      $("loginEmail").value = "admin@nexdesk.com";
      $("loginPass").value = "Admin@2026!";
      $("formLogin").dispatchEvent(new Event("submit"));
    };
  }

  // Forgot Password navigation
  const linkForgot = $("linkForgotPassword");
  const linkBack = $("linkBackToLogin");
  const forgotForm = $("formForgot");
  const forgotPassInput = $("forgotNewPass");

  if (linkForgot) {
    linkForgot.onclick = () => {
      $("formLogin").classList.add("hidden");
      $("formSignup").classList.add("hidden");
      if (forgotForm) forgotForm.classList.remove("hidden");
      $("tabLogin").classList.remove("active");
      $("tabSignup").classList.remove("active");
      if ($("forgotEmail") && $("loginEmail")) {
        $("forgotEmail").value = $("loginEmail").value;
      }
      updatePasswordMeter("forgot", forgotPassInput ? forgotPassInput.value : "");
    };
  }

  if (linkBack) {
    linkBack.onclick = () => {
      if (forgotForm) forgotForm.classList.add("hidden");
      $("formLogin").classList.remove("hidden");
      $("tabLogin").classList.add("active");
      $("tabSignup").classList.remove("active");
    };
  }

  if (forgotPassInput) {
    forgotPassInput.addEventListener("input", () => {
      updatePasswordMeter("forgot", forgotPassInput.value);
    });
  }

  if (forgotForm) {
    forgotForm.onsubmit = async e => {
      e.preventDefault();
      const email = $("forgotEmail").value.trim();
      const newPass = $("forgotNewPass").value;
      const confirmPass = $("forgotConfirmPass").value;

      if (newPass !== confirmPass) {
        toast("Passwords do not match. Please re-enter.", "error");
        return;
      }

      if (!evaluatePassword(newPass).isValid) {
        toast("New password must be at least 8 characters and contain a mix of uppercase, lowercase, numbers, and symbols.", "error");
        return;
      }

      try {
        await api("/api/auth/reset-password", {
          method: "POST",
          body: JSON.stringify({ email, newPassword: newPass })
        });
        toast("🎉 Password successfully reset! Please sign in with your new password.", "success");
        forgotForm.reset();
        forgotForm.classList.add("hidden");
        $("formLogin").classList.remove("hidden");
        $("tabLogin").classList.add("active");
        $("tabSignup").classList.remove("active");
        $("loginEmail").value = email;
        $("loginPass").value = "";
        $("loginPass").focus();
      } catch (err) {
        toast("Password reset failed: " + err.message, "error");
      }
    };
  }

  $("formLogin").onsubmit = async e => {
    e.preventDefault();
    try {
      currentUser = await api("/api/login", {
        method: "POST",
        body: JSON.stringify({ email: $("loginEmail").value, password: $("loginPass").value })
      });
      localStorage.setItem("nexdeskUser", JSON.stringify(currentUser));
      toast(`Welcome back, ${currentUser.name}!`, "success");
      showApp();
    } catch (err) {
      toast(err.message, "error");
    }
  };

  $("formSignup").onsubmit = async e => {
    e.preventDefault();
    const pass = $("signupPass").value;
    const isStrong = evaluatePassword(pass).isValid;
    if (!isStrong) {
      toast("Password must be at least 8 characters and contain a mix of uppercase, lowercase, numbers, and symbols.", "error");
      return;
    }

    try {
      currentUser = await api("/api/signup", {
        method: "POST",
        body: JSON.stringify({
          name: $("signupName").value,
          email: $("signupEmail").value,
          password: pass
        })
      });
      localStorage.setItem("nexdeskUser", JSON.stringify(currentUser));
      toast(`Account created for ${currentUser.name}!`, "success");
      showApp();
    } catch (err) {
      toast(err.message, "error");
    }
  };

  $("logoutBtn").onclick = () => {
    localStorage.removeItem("nexdeskUser");
    currentUser = null;
    location.reload();
  };

  $("dismissOutage").onclick = () => {
    $("outageBanner").classList.add("hidden");
    sessionStorage.setItem("dismissOutage", "1");
  };

  $("statusTicker").onclick = () => {
    $("infraModal").classList.add("open");
  };

  $("infraModalClose").onclick = () => {
    $("infraModal").classList.remove("open");
  };
}

// ==========================================
// WORKSPACE NAVIGATION
// ==========================================
function showApp() {
  $("authSection").classList.add("hidden");
  $("appSection").classList.remove("hidden");
  $("userHeaderControls").classList.remove("hidden");
  $("userHeaderControls").style.display = "flex";
  $("mobileBottomNav")?.classList.remove("hidden");

  $("navUserName").textContent = currentUser.name;
  $("navUserRole").textContent = currentUser.role;
  $("navUserRole").className = `badge ${currentUser.role === 'ADMIN' ? 'badge-critical' : 'badge-medium'}`;

  buildTabs();
  collectTelemetry();
  loadSystemHealth();
}

function buildTabs() {
  const tabsNav = $("viewTabs");
  const bottomNav = $("mobileBottomNav");
  tabsNav.innerHTML = "";
  if (bottomNav) bottomNav.innerHTML = "";

  if (currentUser.role === "ADMIN") {
    const tabs = [
      { id: "viewAdminKanban", label: "📊 Kanban Cockpit", shortLabel: "Board", icon: "📊", badge: null },
      { id: "viewAdminTable", label: "📋 All Tickets Queue", shortLabel: "Queue", icon: "📋", badge: null },
      { id: "viewAdminAnalytics", label: "📈 Velocity Analytics", shortLabel: "Analytics", icon: "📈", badge: null },
      { id: "viewAdminTechs", label: "👨‍🔧 Technician Fleet", shortLabel: "Techs", icon: "👨‍🔧", badge: null }
    ];

    tabs.forEach((t, i) => {
      // Desktop Tab
      const btn = document.createElement("button");
      btn.className = `view-tab ${i === 0 ? "active" : ""}`;
      btn.dataset.view = t.id;
      btn.innerHTML = `${t.label} ${t.badge ? `<span class="tab-badge">${t.badge}</span>` : ""}`;
      btn.onclick = () => switchView(t.id);
      tabsNav.appendChild(btn);

      // Android Bottom Nav Item
      if (bottomNav) {
        const mBtn = document.createElement("button");
        mBtn.className = `mobile-nav-item ${i === 0 ? "active" : ""}`;
        mBtn.dataset.view = t.id;
        mBtn.innerHTML = `
          <span class="mobile-nav-icon">${t.icon}</span>
          <span class="mobile-nav-label">${t.shortLabel}</span>
          ${t.badge ? `<span class="mobile-nav-badge">${t.badge}</span>` : ""}
        `;
        mBtn.onclick = () => switchView(t.id);
        bottomNav.appendChild(mBtn);
      }
    });

    switchView("viewAdminKanban");
    loadAdminData();

  } else {
    const tabs = [
      { id: "viewUserCreate", label: "⚡ Report Issue & AI Co-Pilot", shortLabel: "Report", icon: "⚡", badge: null },
      { id: "viewUserTickets", label: "📋 My Tickets", shortLabel: "My Tickets", icon: "📋", badge: "0" }
    ];

    tabs.forEach((t, i) => {
      // Desktop Tab
      const btn = document.createElement("button");
      btn.className = `view-tab ${i === 0 ? "active" : ""}`;
      btn.dataset.view = t.id;
      btn.innerHTML = `${t.label} ${t.badge ? `<span class="tab-badge" id="myTicketTabCount">${t.badge}</span>` : ""}`;
      btn.onclick = () => switchView(t.id);
      tabsNav.appendChild(btn);

      // Android Bottom Nav Item
      if (bottomNav) {
        const mBtn = document.createElement("button");
        mBtn.className = `mobile-nav-item ${i === 0 ? "active" : ""}`;
        mBtn.dataset.view = t.id;
        mBtn.innerHTML = `
          <span class="mobile-nav-icon">${t.icon}</span>
          <span class="mobile-nav-label">${t.shortLabel}</span>
          ${t.badge ? `<span class="mobile-nav-badge" id="myTicketBottomCount">${t.badge}</span>` : ""}
        `;
        mBtn.onclick = () => switchView(t.id);
        bottomNav.appendChild(mBtn);
      }
    });

    // Android Bottom Nav Status pill shortcut
    if (bottomNav) {
      const statusBtn = document.createElement("button");
      statusBtn.className = "mobile-nav-item";
      statusBtn.innerHTML = `
        <span class="mobile-nav-icon">🌐</span>
        <span class="mobile-nav-label">Radar</span>
      `;
      statusBtn.onclick = () => $("infraModal").classList.add("open");
      bottomNav.appendChild(statusBtn);
    }

    switchView("viewUserCreate");
    loadUserData();
  }
}

function switchView(viewId) {
  document.querySelectorAll(".view-content").forEach(el => el.classList.add("hidden"));
  const target = $(viewId);
  if (target) {
    target.classList.remove("hidden");
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // Synchronize desktop tabs
  document.querySelectorAll(".view-tab").forEach(b => {
    b.classList.toggle("active", b.dataset.view === viewId);
  });

  // Synchronize Android bottom nav
  document.querySelectorAll(".mobile-nav-item").forEach(b => {
    b.classList.toggle("active", b.dataset.view === viewId);
  });
}

// ==========================================
// USER TICKET REPORTING & AI CO-PILOT
// ==========================================
function initUserForm() {
  const titleInput = $("ticketTitle");
  const descInput = $("ticketDesc");
  const categorySelect = $("ticketCategory");

  const runAi = () => {
    clearTimeout(aiDebounceTimer);
    aiDebounceTimer = setTimeout(triggerAiDiagnosis, 300);
  };

  titleInput.addEventListener("input", runAi);
  descInput.addEventListener("input", runAi);
  categorySelect.addEventListener("change", runAi);

  // Blast Radius Matrix Clicks
  $("scopeButtons").querySelectorAll(".matrix-btn").forEach(btn => {
    btn.onclick = () => {
      $("scopeButtons").querySelectorAll(".matrix-btn").forEach(b => b.classList.remove("selected"));
      btn.classList.add("selected");
      currentScope = parseInt(btn.dataset.scope);
      calculateImpactScore();
    };
  });

  $("blockerButtons").querySelectorAll(".matrix-btn").forEach(btn => {
    btn.onclick = () => {
      $("blockerButtons").querySelectorAll(".matrix-btn").forEach(b => b.classList.remove("selected"));
      btn.classList.add("selected");
      currentBlocker = parseInt(btn.dataset.blocker);
      calculateImpactScore();
    };
  });

  // Copy command button
  $("copyCmdBtn").onclick = () => {
    const txt = $("aiCmdText").textContent;
    navigator.clipboard.writeText(txt);
    toast("Command copied to clipboard!", "success");
  };

  // Ticket Deflection (Self-Solved)
  $("btnDeflectResolved").onclick = async () => {
    try {
      await api("/api/tickets/deflect", { method: "POST" });
      $("newTicketForm").reset();
      $("aiCopilotBox").classList.add("hidden");
      toast("🎉 Issue marked self-resolved! Ticket deflected.", "success");
    } catch (e) {
      toast("Great job solving it!", "success");
    }
  };

  // Submit Ticket
  $("newTicketForm").onsubmit = async e => {
    e.preventDefault();
    const attachDiag = $("attachTelemetryCheck").checked;
    const impactScore = calculateImpactScore();

    const payload = {
      userId: currentUser.userId,
      title: $("ticketTitle").value,
      description: $("ticketDesc").value,
      category: $("ticketCategory").value,
      priority: $("ticketPriority").value,
      impactScore: String(impactScore)
    };

    if (attachDiag) {
      payload.os = deviceTelemetry.os;
      payload.browser = deviceTelemetry.browser;
      payload.latency = deviceTelemetry.latency;
      payload.memory = deviceTelemetry.memory;
      payload.screen = deviceTelemetry.screen;
      payload.deviceName = deviceTelemetry.deviceName;
    }

    try {
      const res = await api("/api/tickets", {
        method: "POST",
        body: JSON.stringify(payload)
      });
      toast(`Ticket #${res.ticketId} created with SLA target!`, "success");
      $("newTicketForm").reset();
      $("aiCopilotBox").classList.add("hidden");
      loadUserData();
      // Switch to tickets tab
      switchView("viewUserTickets");
    } catch (err) {
      toast(err.message, "error");
    }
  };

  $("btnNewTicketShortcut").onclick = () => {
    switchView("viewUserCreate");
  };
}

function calculateImpactScore() {
  // Score formula: (scope * 15) + (blocker * 10)
  const score = Math.min(100, (currentScope * 15) + (currentBlocker * 10));
  const badge = $("impactScoreBadge");
  badge.textContent = `Impact Score: ${score}/100`;

  let suggested = "MEDIUM";
  if (score >= 80) suggested = "CRITICAL";
  else if (score >= 55) suggested = "HIGH";
  else if (score >= 35) suggested = "MEDIUM";
  else suggested = "LOW";

  $("ticketPriority").value = suggested;
  badge.className = `matrix-score-badge badge-${suggested.toLowerCase()}`;
  return score;
}

async function triggerAiDiagnosis() {
  const title = $("ticketTitle").value.trim();
  const desc = $("ticketDesc").value.trim();
  const cat = $("ticketCategory").value;

  if (title.length < 4 && desc.length < 5) {
    $("aiCopilotBox").classList.add("hidden");
    return;
  }

  try {
    const res = await api("/api/ai/diagnose", {
      method: "POST",
      body: JSON.stringify({ title, description: desc, category: cat })
    });

    if (res && res.confidence >= 70) {
      $("aiCopilotBox").classList.remove("hidden");
      $("aiConfidence").textContent = `${res.confidence}% Confidence`;
      $("aiFixTime").textContent = `~${res.estimatedSelfFixTime || '2m'} Fix`;
      $("aiRootCause").textContent = `Hypothesis: ${res.rootCause}`;

      if (res.duplicateIncidentWarning) {
        $("aiDuplicateNotice").textContent = res.duplicateIncidentWarning;
        $("aiDuplicateNotice").classList.remove("hidden");
      } else {
        $("aiDuplicateNotice").classList.add("hidden");
      }

      const stepsContainer = $("aiRunbookSteps");
      stepsContainer.innerHTML = (res.runbookSteps || []).map((step, i) => `
        <div class="ai-runbook-step">
          <span class="step-num">${i + 1}</span>
          <span>${esc(step)}</span>
        </div>
      `).join("");

      if (res.remediationCommand) {
        $("aiCmdContainer").classList.remove("hidden");
        $("aiCmdText").textContent = res.remediationCommand;
      } else {
        $("aiCmdContainer").classList.add("hidden");
      }
    } else {
      $("aiCopilotBox").classList.add("hidden");
    }
  } catch (err) {
    console.warn("AI diagnosis skipped:", err);
  }
}

// ==========================================
// USER TICKETS VIEW
// ==========================================
async function loadUserData() {
  try {
    const rows = await api(`/api/tickets?userId=${currentUser.userId}`);
    currentTickets = rows;

    const countEl = $("myTicketTabCount");
    if (countEl) countEl.textContent = rows.length;
    const bottomCountEl = $("myTicketBottomCount");
    if (bottomCountEl) bottomCountEl.textContent = rows.length;

    const container = $("userTicketsList");
    if (!rows.length) {
      container.innerHTML = `
        <div style="text-align:center; padding:40px; color:var(--text-muted);">
          <p style="font-size:16px; margin-bottom:8px;">No tickets submitted yet.</p>
          <p style="font-size:13px;">Need help? Use the AI Co-Pilot report form above for instant resolution.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = rows.map(t => renderUserTicketCard(t)).join("");

  } catch (err) {
    toast(err.message, "error");
  }
}

function renderUserTicketCard(t) {
  const prioClass = `badge-${(t.priority || "medium").toLowerCase()}`;
  const statusClass = t.status === "RESOLVED" || t.status === "CLOSED" ? "badge-low" : "badge-medium";
  const isBreached = t.isSlaBreached;
  const slaText = isBreached ? "⚠️ SLA BREACHED" : `${t.remainingSlaMinutes > 0 ? Math.round(t.remainingSlaMinutes / 60) + 'h left' : 'On Track'}`;
  const slaClass = isBreached ? "sla-breached" : (t.remainingSlaMinutes < 60 ? "sla-risk" : "sla-ok");

  return `
    <div class="ticket-card" onclick="openTicketModal(${t.ticketId || t.ticket_id})">
      <div class="ticket-header">
        <span class="ticket-id">#${t.ticketId || t.ticket_id}</span>
        <div style="display:flex; gap:6px;">
          <span class="badge ${prioClass}">${t.priority}</span>
          <span class="badge ${statusClass}">${t.status}</span>
        </div>
      </div>
      <div class="ticket-title">${esc(t.title)}</div>
      <p style="font-size:13px; color:var(--text-muted); line-height:1.4; margin-bottom:8px;">
        ${esc((t.description || "").substring(0, 120))}${t.description && t.description.length > 120 ? '...' : ''}
      </p>
      <div class="ticket-meta">
        <span>📂 ${esc(t.category || "General")} &bull; ${t.createdAt ? t.createdAt.substring(0, 16) : ""}</span>
        <span class="sla-badge ${slaClass}">${slaText}</span>
      </div>
    </div>
  `;
}

// ==========================================
// ADMIN WORKSPACE & KANBAN COCKPIT
// ==========================================
async function loadAdminData() {
  try {
    const [tickets, techs, analytics] = await Promise.all([
      api("/api/admin/tickets"),
      api("/api/technicians"),
      api("/api/analytics")
    ]);

    currentTickets = tickets;
    currentTechnicians = techs;

    renderKanban(tickets);
    renderAdminTable(tickets);
    renderAnalytics(analytics);
    renderTechFleet(techs);

  } catch (err) {
    toast("Failed loading admin data: " + err.message, "error");
  }
}

function renderKanban(tickets) {
  const cols = {
    OPEN: $("colOpen"),
    IN_PROGRESS: $("colProgress"),
    RESOLVED: $("colResolved"),
    CLOSED: $("colClosed")
  };

  const counts = {
    OPEN: 0,
    IN_PROGRESS: 0,
    RESOLVED: 0,
    CLOSED: 0
  };

  Object.values(cols).forEach(col => { if (col) col.innerHTML = ""; });

  const query = ($("adminSearchInput")?.value || "").toLowerCase();

  tickets.forEach(t => {
    if (query && !t.title.toLowerCase().includes(query) && !String(t.ticketId).includes(query)) {
      return;
    }

    let status = (t.status || "OPEN").toUpperCase();
    if (status === "WAITING" || status === "WAITING ON USER") status = "IN_PROGRESS";
    if (!cols[status]) status = "OPEN";
    counts[status] = (counts[status] || 0) + 1;

    const prioClass = `badge-${(t.priority || "medium").toLowerCase()}`;
    const isBreached = t.isSlaBreached;
    const slaText = isBreached ? "⚠️ BREACHED" : `${t.remainingSlaMinutes > 0 ? Math.round(t.remainingSlaMinutes / 60) + 'h SLA' : 'SLA OK'}`;
    const slaClass = isBreached ? "sla-breached" : (t.remainingSlaMinutes < 60 ? "sla-risk" : "sla-ok");

    const card = document.createElement("div");
    card.className = "ticket-card";
    card.onclick = () => openTicketModal(t.ticketId || t.ticket_id);
    card.innerHTML = `
      <div class="ticket-header">
        <span class="ticket-id">#${t.ticketId || t.ticket_id}</span>
        <span class="badge ${prioClass}">${t.priority}</span>
      </div>
      <div class="ticket-title">${esc(t.title)}</div>
      <div style="font-size:12px; color:var(--text-muted); margin-bottom:6px;">
        Requester: <strong>${esc(t.userName || t.user_name || "Employee")}</strong>
      </div>
      <div class="ticket-meta">
        <span>📂 ${esc(t.category || "General")}</span>
        <span class="sla-badge ${slaClass}">${slaText}</span>
      </div>
    `;

    if (cols[status]) cols[status].appendChild(card);
  });

  if ($("countOpen")) $("countOpen").textContent = counts.OPEN;
  if ($("countProgress")) $("countProgress").textContent = counts.IN_PROGRESS;
  if ($("countResolved")) $("countResolved").textContent = counts.RESOLVED;
  if ($("countClosed")) $("countClosed").textContent = counts.CLOSED;
}

function renderAdminTable(tickets) {
  const tbody = $("adminTableBody");
  if (!tbody) return;

  tbody.innerHTML = tickets.map(t => {
    const prioClass = `badge-${(t.priority || "medium").toLowerCase()}`;
    const isBreached = t.isSlaBreached;
    const slaClass = isBreached ? "sla-breached" : "sla-ok";
    return `
      <tr style="border-bottom:1px solid var(--border-color); cursor:pointer;" onclick="openTicketModal(${t.ticketId || t.ticket_id})">
        <td style="padding:10px;"><span class="ticket-id">#${t.ticketId || t.ticket_id}</span></td>
        <td style="padding:10px; font-weight:600;">${esc(t.title)}</td>
        <td style="padding:10px;">${esc(t.userName || t.user_name || "Employee")}</td>
        <td style="padding:10px;">${esc(t.category || "General")}</td>
        <td style="padding:10px;"><span class="badge ${prioClass}">${t.priority}</span></td>
        <td style="padding:10px;"><span class="badge badge-medium">${t.status}</span></td>
        <td style="padding:10px;"><span class="sla-badge ${slaClass}">${t.slaDeadline ? t.slaDeadline.substring(5, 16) : 'None'}</span></td>
        <td style="padding:10px;">${t.assignedTo ? 'Tech #' + t.assignedTo : '<span class="text-dim">Unassigned</span>'}</td>
        <td style="padding:10px;"><button class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); openTicketModal(${t.ticketId || t.ticket_id})">Inspect</button></td>
      </tr>
    `;
  }).join("");
}

function renderAnalytics(a) {
  if (!a) return;
  $("kpiTotal").textContent = a.totalTickets || 0;
  $("kpiMttr").textContent = (a.mttrHours || 1.8) + "h";
  $("kpiSla").textContent = a.slaComplianceRate || "94.6%";

  // Category distribution bars
  const catContainer = $("categoryBars");
  if (catContainer && a.categoryDistribution) {
    const total = Math.max(1, a.totalTickets || 1);
    catContainer.innerHTML = Object.entries(a.categoryDistribution).map(([cat, cnt]) => {
      const pct = Math.round((cnt / total) * 100);
      return `
        <div>
          <div style="display:flex; justify-content:space-between; font-size:12px; margin-bottom:4px;">
            <span>${esc(cat)}</span>
            <strong>${cnt} tickets (${pct}%)</strong>
          </div>
          <div style="background:rgba(255,255,255,0.06); height:6px; border-radius:3px; overflow:hidden;">
            <div style="background:var(--accent-cyan); height:100%; width:${pct}%;"></div>
          </div>
        </div>
      `;
    }).join("");
  }

  // Tech Leaderboard
  const leaderContainer = $("techLeaderboard");
  if (leaderContainer) {
    leaderContainer.innerHTML = (currentTechnicians || []).map((tech, i) => `
      <div style="display:flex; align-items:center; justify-content:space-between; padding:8px 12px; background:rgba(255,255,255,0.03); border-radius:var(--radius-sm);">
        <div style="display:flex; align-items:center; gap:8px;">
          <span style="font-weight:700; color:var(--accent-cyan);">#${i + 1}</span>
          <div>
            <strong style="font-size:13px;">${esc(tech.full_name)}</strong>
            <div style="font-size:11px; color:var(--text-muted);">${esc(tech.specialization)}</div>
          </div>
        </div>
        <span class="badge badge-low">98.2% CSAT</span>
      </div>
    `).join("");
  }
}

function renderTechFleet(techs) {
  const container = $("technicianCardsGrid");
  if (!container) return;

  if (!techs || techs.length === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; padding: 24px; color: var(--text-muted);">No technicians found. Click "+ Add New Technician" to provision one.</div>`;
    return;
  }

  container.innerHTML = techs.map(tech => `
    <div class="glass-card" style="padding:16px;">
      <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:8px;">
        <span class="badge ${tech.status === 'AVAILABLE' ? 'badge-low' : (tech.status === 'BUSY' ? 'badge-medium' : 'badge-high')}">${esc(tech.status || "AVAILABLE")}</span>
        <span style="font-size:11px; color:var(--text-dim);">ID #${tech.technicianId || tech.technician_id}</span>
      </div>
      <h3 style="font-size:15px; margin-bottom:4px;">${esc(tech.fullName || tech.full_name)}</h3>
      <div style="font-size:12px; color:var(--accent-cyan); margin-bottom:8px;">${esc(tech.specialization)}</div>
      <div style="font-size:12px; color:var(--text-muted);">${esc(tech.email)}</div>
    </div>
  `).join("");
}

// ==========================================
// TICKET MODAL & DISCUSSION THREAD
// ==========================================
async function openTicketModal(ticketId) {
  const ticket = currentTickets.find(t => (t.ticketId || t.ticket_id) === ticketId);
  if (!ticket) return;

  activeModalTicket = ticket;
  $("ticketModal").classList.add("open");

  $("modalTicketId").textContent = `#${ticket.ticketId || ticket.ticket_id}`;
  $("modalTicketTitle").textContent = ticket.title;
  $("modalTicketDesc").textContent = ticket.description;

  const prioClass = `badge-${(ticket.priority || "medium").toLowerCase()}`;
  $("modalPriorityBadge").className = `badge ${prioClass}`;
  $("modalPriorityBadge").textContent = ticket.priority;

  $("modalStatusBadge").className = "badge badge-medium";
  $("modalStatusBadge").textContent = ticket.status;

  // Render Telemetry Snapshot
  const telemBox = $("modalTelemetryBox");
  const telemChips = $("modalTelemetryChips");
  if (ticket.diagnostics && typeof ticket.diagnostics === "object") {
    telemBox.classList.remove("hidden");
    const d = ticket.diagnostics;
    telemChips.innerHTML = `
      <div class="telemetry-chip">🖥️ OS: <strong>${esc(d.os || "Windows 11")}</strong></div>
      <div class="telemetry-chip">🌐 Browser: <strong>${esc(d.browser || "Chrome")}</strong></div>
      <div class="telemetry-chip">⚡ Latency: <strong>${esc(d.latency || "14ms")}</strong></div>
      <div class="telemetry-chip">🧠 RAM: <strong>${esc(d.memory || "16 GB")}</strong></div>
      <div class="telemetry-chip">📐 Screen: <strong>${esc(d.screen || "1920x1080")}</strong></div>
    `;
  } else {
    telemBox.classList.add("hidden");
  }

  // Admin Controls Area
  if (currentUser.role === "ADMIN") {
    $("adminControlsArea").classList.remove("hidden");
    $("internalNoteLabel").classList.remove("hidden");

    let curStatus = (ticket.status || "OPEN").toUpperCase();
    if (curStatus === "WAITING" || curStatus === "WAITING ON USER") curStatus = "IN_PROGRESS";
    $("modalStatusSelect").value = curStatus;
    $("modalStatusSelect").onchange = async () => {
      try {
        await api("/api/admin/tickets/status", {
          method: "PUT",
          body: JSON.stringify({ ticketId: ticket.ticketId || ticket.ticket_id, status: $("modalStatusSelect").value })
        });
        toast("Status updated to " + $("modalStatusSelect").value, "success");
        loadAdminData();
        loadComments(ticket.ticketId || ticket.ticket_id);
      } catch (e) { toast(e.message, "error"); }
    };

    // Populate Technicians dropdown
    const techSelect = $("modalTechSelect");
    techSelect.innerHTML = `<option value="">-- Unassigned --</option>` +
      (currentTechnicians || []).map(tc => `
        <option value="${tc.technician_id}" ${ticket.assignedTo === tc.technician_id ? 'selected' : ''}>
          ${esc(tc.full_name)} (${esc(tc.specialization)})
        </option>
      `).join("");

    techSelect.onchange = async () => {
      try {
        await api("/api/admin/tickets/assign", {
          method: "PUT",
          body: JSON.stringify({ ticketId: ticket.ticketId || ticket.ticket_id, technicianId: techSelect.value })
        });
        toast("Technician assigned!", "success");
        loadAdminData();
        loadComments(ticket.ticketId || ticket.ticket_id);
      } catch (e) { toast(e.message, "error"); }
    };

  } else {
    $("adminControlsArea").classList.add("hidden");
    $("internalNoteLabel").classList.add("hidden");
  }

  // Hide AI Tech Box initially
  $("techAiBox").classList.add("hidden");

  // Load comments
  await loadComments(ticket.ticketId || ticket.ticket_id);
}

async function loadComments(ticketId) {
  try {
    const listEl = $("modalCommentsList");
    listEl.innerHTML = `<div style="text-align:center; padding:10px; color:var(--text-dim);">Loading conversation...</div>`;

    const comments = await api(`/api/tickets/comments?ticketId=${ticketId}&includeInternal=${currentUser.role === 'ADMIN'}`);

    if (!comments.length) {
      listEl.innerHTML = `<p style="font-size:12px; color:var(--text-dim); text-align:center; padding:12px;">No activity yet. Type below to post an update.</p>`;
      return;
    }

    listEl.innerHTML = comments.map(c => `
      <div class="comment-bubble ${c.isInternal ? 'internal' : ''}">
        <div class="comment-meta">
          <span><strong>${esc(c.authorName)}</strong> &bull; <span class="badge ${c.authorRole === 'ADMIN' ? 'badge-critical' : 'badge-low'}" style="font-size:9px; padding:1px 5px;">${esc(c.authorRole)}</span></span>
          <span>${c.isInternal ? '🔒 Internal Note &bull; ' : ''}${esc(c.createdAt || "")}</span>
        </div>
        <div>${esc(c.message)}</div>
      </div>
    `).join("");

    listEl.scrollTop = listEl.scrollHeight;

  } catch (err) {
    console.warn("Could not load comments:", err);
  }
}

function initModals() {
  // Cross-Platform Launch & Publish Hub controls
  const mobileAppModal = $("mobileAppModal");
  const btnMobileAppInfo = $("btnMobileAppInfo");
  const mobileAppModalClose = $("mobileAppModalClose");
  const tabPublishMobile = $("tabPublishMobile");
  const tabPublishLaptop = $("tabPublishLaptop");
  const panelPublishMobile = $("panelPublishMobile");
  const panelPublishLaptop = $("panelPublishLaptop");
  const btnCopyMobileUrl = $("btnCopyMobileUrl");
  const mobileDirectUrlInput = $("mobileDirectUrlInput");
  const btnTriggerPwaInstall = $("btnTriggerPwaInstall");
  const btnInstallDesktopPwa = $("btnInstallDesktopPwa");

  function updatePublishLinks() {
    const isLocal = location.hostname === 'localhost' || location.hostname === '127.0.0.1';
    const hostUrl = isLocal ? 'http://192.168.0.118:8080/' : (window.location.origin + '/');

    if (mobileDirectUrlInput) mobileDirectUrlInput.value = hostUrl;
    if ($("btnOpenPhoneBrowser")) $("btnOpenPhoneBrowser").href = hostUrl;
    if ($("mobileQrCodeImg")) {
      $("mobileQrCodeImg").src = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(hostUrl)}`;
    }
  }

  if (tabPublishMobile && tabPublishLaptop) {
    tabPublishMobile.onclick = () => {
      tabPublishMobile.classList.add("active");
      tabPublishLaptop.classList.remove("active");
      panelPublishMobile?.classList.remove("hidden");
      panelPublishLaptop?.classList.add("hidden");
    };
    tabPublishLaptop.onclick = () => {
      tabPublishLaptop.classList.add("active");
      tabPublishMobile.classList.remove("active");
      panelPublishLaptop?.classList.remove("hidden");
      panelPublishMobile?.classList.add("hidden");
    };
  }

  if (btnCopyMobileUrl) {
    btnCopyMobileUrl.onclick = async () => {
      if (mobileDirectUrlInput) {
        try {
          await navigator.clipboard.writeText(mobileDirectUrlInput.value);
          toast("Mobile access URL copied to clipboard!", "success");
        } catch {
          mobileDirectUrlInput.select();
          document.execCommand("copy");
          toast("URL copied to clipboard!", "success");
        }
      }
    };
  }

  const triggerInstallFlow = async () => {
    if (deferredPwaPrompt) {
      deferredPwaPrompt.prompt();
      const choice = await deferredPwaPrompt.userChoice;
      if (choice.outcome === 'accepted') {
        toast("NexDesk installation initiated!", "success");
      }
      deferredPwaPrompt = null;
    } else {
      toast("To install: Click browser address bar icon (⊕) or menu (⋮) -> 'Install App'", "info");
    }
  };

  if (btnTriggerPwaInstall) btnTriggerPwaInstall.onclick = triggerInstallFlow;
  if (btnInstallDesktopPwa) btnInstallDesktopPwa.onclick = triggerInstallFlow;

  if (btnMobileAppInfo) {
    btnMobileAppInfo.onclick = () => {
      updatePublishLinks();
      if (mobileAppModal) mobileAppModal.classList.add("open");
    };
  }

  if (mobileAppModalClose) {
    mobileAppModalClose.onclick = () => {
      if (mobileAppModal) mobileAppModal.classList.remove("open");
    };
  }

  if (mobileAppModal) {
    mobileAppModal.onclick = (e) => {
      if (e.target === mobileAppModal) mobileAppModal.classList.remove("open");
    };
  }

  // Add Technician Modal controls
  const addTechModal = $("addTechModal");
  const btnOpenAddTech = $("btnOpenAddTechModal");
  const btnCloseAddTech = $("addTechModalClose");
  const btnCancelAddTech = $("btnCancelAddTech");
  const formAddTech = $("formAddTech");
  const techPassInput = $("techPassword");

  if (btnOpenAddTech) {
    btnOpenAddTech.onclick = () => {
      if (formAddTech) formAddTech.reset();
      updatePasswordMeter("tech", "");
      if (addTechModal) addTechModal.classList.add("open");
    };
  }

  const closeTechModal = () => {
    if (addTechModal) addTechModal.classList.remove("open");
  };

  if (btnCloseAddTech) btnCloseAddTech.onclick = closeTechModal;
  if (btnCancelAddTech) btnCancelAddTech.onclick = closeTechModal;
  if (addTechModal) {
    addTechModal.onclick = e => {
      if (e.target === addTechModal) closeTechModal();
    };
  }

  if (techPassInput) {
    techPassInput.addEventListener("input", () => {
      updatePasswordMeter("tech", techPassInput.value);
    });
  }

  if (formAddTech) {
    formAddTech.onsubmit = async e => {
      e.preventDefault();
      const pwd = techPassInput ? techPassInput.value : "";
      if (!evaluatePassword(pwd).isValid) {
        toast("Technician password must be at least 8 characters and contain uppercase, lowercase, numbers, and symbols.", "error");
        return;
      }

      try {
        const payload = {
          fullName: $("techFullName").value.trim(),
          email: $("techEmail").value.trim(),
          specialization: $("techSpecialization").value,
          status: $("techStatus").value,
          password: pwd
        };
        const newTech = await api("/api/technicians", {
          method: "POST",
          body: JSON.stringify(payload)
        });
        toast(`🎉 Technician "${newTech.fullName || newTech.full_name}" successfully provisioned in Oracle Database!`, "success");
        closeTechModal();
        loadAdminData();
      } catch (err) {
        toast("Failed to add technician: " + err.message, "error");
      }
    };
  }

  $("modalCloseBtn").onclick = () => {
    $("ticketModal").classList.remove("open");
  };

  // Close modal when clicking backdrop
  $("ticketModal").onclick = e => {
    if (e.target === $("ticketModal")) {
      $("ticketModal").classList.remove("open");
    }
  };

  // Submit comment
  $("commentForm").onsubmit = async e => {
    e.preventDefault();
    if (!activeModalTicket) return;

    const msg = $("commentInput").value.trim();
    if (!msg) return;

    const isInternal = $("commentIsInternal").checked;

    try {
      await api("/api/tickets/comments", {
        method: "POST",
        body: JSON.stringify({
          ticketId: String(activeModalTicket.ticketId || activeModalTicket.ticket_id),
          authorName: currentUser.name,
          authorRole: currentUser.role,
          message: msg,
          isInternal: isInternal ? "true" : "false"
        })
      });
      $("commentInput").value = "";
      toast(isInternal ? "Internal note added." : "Message posted.", "success");
      loadComments(activeModalTicket.ticketId || activeModalTicket.ticket_id);
    } catch (err) {
      toast(err.message, "error");
    }
  };

  // Technician AI Resolution Generator
  $("btnGenerateAiFix").onclick = async () => {
    if (!activeModalTicket) return;
    try {
      toast("Analyzing issue with AI Co-Pilot...", "success");
      const res = await api("/api/ai/resolve", {
        method: "POST",
        body: JSON.stringify({
          title: activeModalTicket.title,
          description: activeModalTicket.description,
          category: activeModalTicket.category,
          priority: activeModalTicket.priority
        })
      });

      $("techAiBox").classList.remove("hidden");
      $("techAiChecklist").innerHTML = `
        <div style="font-weight:600; margin-bottom:6px; color:var(--text-main);">🔍 Root Cause Diagnosis:</div>
        <p style="margin-bottom:8px; color:var(--accent-cyan);">${esc(res.diagnosticSummary)}</p>
        <div style="font-weight:600; margin-bottom:4px;">📋 Action Checklist:</div>
        ${(res.technicalChecklist || []).map(it => `<div>&bull; ${esc(it)}</div>`).join("")}
      `;
      $("techAiScript").textContent = res.automationScript || "";

      // Copy tech script
      $("copyTechScriptBtn").onclick = () => {
        navigator.clipboard.writeText(res.automationScript);
        toast("Diagnostic script copied to clipboard!", "success");
      };

      // Insert draft into comment input
      $("btnInsertDraft").onclick = () => {
        $("commentInput").value = res.customerReplyDraft || "";
        toast("Draft response inserted in comment box!", "success");
      };

    } catch (err) {
      toast(err.message, "error");
    }
  };

  // Admin search filter
  $("adminSearchInput")?.addEventListener("input", () => {
    renderKanban(currentTickets);
  });

  $("btnRefreshAdmin")?.addEventListener("click", () => {
    toast("Refreshing IT queue...", "success");
    loadAdminData();
  });
}

// ==========================================
// PWA, MOBILE & KEYBOARD ACCESSIBILITY
// ==========================================
let deferredPwaPrompt = null;

function initKanbanMobileFilters() {
  const container = $("kanbanMobileFilters");
  if (!container) return;

  container.querySelectorAll(".mobile-filter-pill").forEach(btn => {
    btn.onclick = () => {
      container.querySelectorAll(".mobile-filter-pill").forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      const filter = btn.dataset.filter;

      const cols = document.querySelectorAll(".kanban-col");
      cols.forEach(col => {
        if (filter === "ALL") {
          col.style.display = "flex";
        } else {
          col.style.display = col.dataset.status === filter ? "flex" : "none";
        }
      });

      if (filter !== "ALL") {
        const targetCol = document.querySelector(`.kanban-col[data-status="${filter}"]`);
        if (targetCol) {
          targetCol.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
        }
      }
    };
  });
}

function initPwaAndMobile() {
  // Register Service Worker for Android PWA & Offline Support
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js').then(reg => {
      console.log('NexDesk PWA ServiceWorker registered:', reg.scope);
    }).catch(err => {
      console.warn('NexDesk ServiceWorker registration skipped:', err);
    });
  }

  // Handle Android / Chrome PWA install prompt
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredPwaPrompt = e;
    const banner = $("pwaInstallBanner");
    if (banner && !sessionStorage.getItem("dismissPwaBanner")) {
      banner.classList.remove("hidden");
    }
  });

  $("pwaInstallBtn")?.addEventListener("click", async () => {
    if (deferredPwaPrompt) {
      deferredPwaPrompt.prompt();
      const choice = await deferredPwaPrompt.userChoice;
      if (choice.outcome === 'accepted') {
        toast("NexDesk app installation initiated!", "success");
      }
      deferredPwaPrompt = null;
    } else {
      toast("To install NexDesk on Android: tap browser menu (⋮) and select 'Add to Home Screen'", "info");
    }
    $("pwaInstallBanner")?.classList.add("hidden");
  });

  $("pwaDismissBtn")?.addEventListener("click", () => {
    $("pwaInstallBanner")?.classList.add("hidden");
    sessionStorage.setItem("dismissPwaBanner", "1");
  });

  // Laptop / Desktop Keyboard shortcuts
  window.addEventListener("keydown", (e) => {
    // ESC to close any open modal
    if (e.key === "Escape") {
      document.querySelectorAll(".modal-overlay.open").forEach(m => m.classList.remove("open"));
    }

    // Ctrl+K or Ctrl+/ to focus admin quick search
    if ((e.ctrlKey || e.metaKey) && (e.key === "k" || e.key === "/")) {
      const searchInput = $("adminSearchInput");
      if (searchInput && !$("viewAdminKanban").classList.contains("hidden")) {
        e.preventDefault();
        searchInput.focus();
        searchInput.select();
      }
    }

    // Alt + 1..4 quick view switcher for Laptop
    if (e.altKey && ["1", "2", "3", "4"].includes(e.key)) {
      const index = parseInt(e.key, 10) - 1;
      const tabs = document.querySelectorAll(".view-tab");
      if (tabs[index]) {
        e.preventDefault();
        tabs[index].click();
      }
    }
  });

  initKanbanMobileFilters();
}

// ==========================================
// BOOTSTRAP INITIALIZATION
// ==========================================
window.addEventListener("DOMContentLoaded", () => {
  initTheme();
  initAuth();
  initUserForm();
  initModals();
  initPwaAndMobile();

  if (currentUser) {
    showApp();
  }
});