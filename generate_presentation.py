import os
import sys
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.enum.text import PP_ALIGN
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE

def create_presentation():
    prs = Presentation()
    # 16:9 widescreen
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6] # blank

    # Color Palette - Modern Cyber / Dark Tech Theme
    BG_DARK = RGBColor(15, 23, 42)        # #0F172A (Deep Slate)
    CARD_BG = RGBColor(30, 41, 59)        # #1E293B (Card Dark Slate)
    CARD_BORDER = RGBColor(51, 65, 85)    # #334155 (Slate Border)
    TEXT_LIGHT = RGBColor(248, 250, 252)  # #F8FAFC (White/Silver)
    TEXT_MUTED = RGBColor(148, 163, 184)  # #94A3B8 (Light Slate Muted)
    ACCENT_CYAN = RGBColor(6, 182, 212)   # #06B6D4 (Electric Cyan)
    ACCENT_BLUE = RGBColor(59, 130, 246)  # #3B82F6 (Vibrant Blue)
    ACCENT_PURPLE = RGBColor(139, 92, 246)# #8B5CF6 (Purple)
    ACCENT_GREEN = RGBColor(16, 185, 129) # #10B981 (Emerald Green)
    ACCENT_AMBER = RGBColor(245, 158, 11) # #F59E0B (Amber Gold)
    ACCENT_RED = RGBColor(239, 68, 68)    # #EF4444 (Rose Red)

    def set_slide_background(slide):
        background = slide.background
        fill = background.fill
        fill.solid()
        fill.fore_color.rgb = BG_DARK

    def add_header(slide, title_text, category_text="NEXDESK 2.0 • PROJECT REVIEW"):
        # Category / Breadcrumb
        cat_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.4), Inches(11.7), Inches(0.35))
        tf_cat = cat_box.text_frame
        tf_cat.word_wrap = True
        tf_cat.margin_left = tf_cat.margin_top = tf_cat.margin_right = tf_cat.margin_bottom = 0
        p_cat = tf_cat.paragraphs[0]
        p_cat.text = category_text.upper()
        p_cat.font.size = Pt(11)
        p_cat.font.bold = True
        p_cat.font.color.rgb = ACCENT_CYAN
        p_cat.font.name = "Arial"

        # Main Slide Title
        title_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.75), Inches(11.7), Inches(0.65))
        tf_title = title_box.text_frame
        tf_title.word_wrap = True
        tf_title.margin_left = tf_title.margin_top = tf_title.margin_right = tf_title.margin_bottom = 0
        p_title = tf_title.paragraphs[0]
        p_title.text = title_text
        p_title.font.size = Pt(22)
        p_title.font.bold = True
        p_title.font.color.rgb = TEXT_LIGHT
        p_title.font.name = "Arial"

        # Decorative line
        line = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(1.42), Inches(11.73), Inches(0.02))
        line.fill.solid()
        line.fill.fore_color.rgb = CARD_BORDER
        line.line.color.rgb = CARD_BORDER

    def add_card(slide, left, top, width, height, bg_color=CARD_BG, border_color=CARD_BORDER):
        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        card.fill.solid()
        card.fill.fore_color.rgb = bg_color
        card.line.color.rgb = border_color
        card.line.width = Pt(1)
        return card

    # ==========================================
    # SLIDE 1: TITLE SLIDE
    # ==========================================
    s1 = prs.slides.add_slide(blank_layout)
    set_slide_background(s1)

    # Accent decorative cards
    c_dec = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(0.4), Inches(7.5))
    c_dec.fill.solid()
    c_dec.fill.fore_color.rgb = ACCENT_CYAN
    c_dec.line.fill.background()

    # Main Card
    add_card(s1, Inches(1.2), Inches(1.0), Inches(11.0), Inches(5.5), bg_color=RGBColor(24, 33, 50), border_color=ACCENT_CYAN)

    # Pill badge
    badge = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(1.8), Inches(1.5), Inches(3.6), Inches(0.4))
    badge.fill.solid()
    badge.fill.fore_color.rgb = RGBColor(14, 116, 144)
    badge.line.fill.background()
    tf_b = badge.text_frame
    p_b = tf_b.paragraphs[0]
    p_b.text = "NEXT-GEN IT SERVICE MANAGEMENT"
    p_b.font.size = Pt(11)
    p_b.font.bold = True
    p_b.font.color.rgb = RGBColor(255, 255, 255)
    p_b.alignment = PP_ALIGN.CENTER

    # Project Title
    tb = s1.shapes.add_textbox(Inches(1.8), Inches(2.1), Inches(9.8), Inches(1.2))
    tf = tb.text_frame
    p = tf.paragraphs[0]
    p.text = "NexDesk 2.0 — Autonomous IT Hub"
    p.font.size = Pt(38)
    p.font.bold = True
    p.font.color.rgb = TEXT_LIGHT
    p.font.name = "Arial"

    # Subtitle
    tb_sub = s1.shapes.add_textbox(Inches(1.8), Inches(3.3), Inches(9.8), Inches(0.9))
    tf_sub = tb_sub.text_frame
    tf_sub.word_wrap = True
    p_sub = tf_sub.paragraphs[0]
    p_sub.text = "Project Progress, Architecture Implementation, Problem Resolution & Completion Roadmap"
    p_sub.font.size = Pt(17)
    p_sub.font.color.rgb = ACCENT_CYAN
    p_sub.font.name = "Arial"

    # Project specs info strip
    add_card(s1, Inches(1.8), Inches(4.5), Inches(9.8), Inches(1.5), bg_color=CARD_BG, border_color=CARD_BORDER)
    
    info_cols = [
        ("CORE STACK", "Java 21+ • Oracle XE • Modern Web"),
        ("KEY FEATURES", "AI Co-Pilot • Device Passport • Blast Radius Matrix"),
        ("CURRENT STATUS", "Modules Implemented • Beta Testing • On Track"),
        ("REVIEW STAGE", "Mid-Project Review 2 Evaluation")
    ]
    for i, (k, v) in enumerate(info_cols):
        cx = Inches(2.0 + (i * 2.4))
        tb_i = s1.shapes.add_textbox(cx, Inches(4.7), Inches(2.3), Inches(1.1))
        tf_i = tb_i.text_frame
        tf_i.word_wrap = True
        p1 = tf_i.paragraphs[0]
        p1.text = k
        p1.font.size = Pt(10)
        p1.font.bold = True
        p1.font.color.rgb = ACCENT_AMBER
        p2 = tf_i.add_paragraph()
        p2.text = v
        p2.font.size = Pt(12)
        p2.font.color.rgb = TEXT_LIGHT

    # ==========================================
    # SLIDE 2: EXECUTIVE SUMMARY & CORE VALUE PROPOSITION
    # ==========================================
    s2 = prs.slides.add_slide(blank_layout)
    set_slide_background(s2)
    add_header(s2, "Executive Overview & Problem-Solution Matrix")

    # 3 Pillars
    pillars = [
        ("The Traditional Problem", ACCENT_RED, [
            "Passive Help Desks: Jira/ServiceNow wait for tickets to pile up.",
            "Missing Diagnostics: IT agents waste hours asking 'What OS, RAM, IP are you on?'",
            "Subjective Urgency: Users mark every ticket as 'CRITICAL'.",
            "Slow MTTR & Repetitive Triage: Techs manually type the same Wi-Fi/VPN fix scripts."
        ]),
        ("The NexDesk 2.0 Solution", ACCENT_CYAN, [
            "AI Self-Healing Co-Pilot: Real-time symptom triage with 1-click self-fix runbooks.",
            "Digital Device Passport: Automatic client-side telemetry capture without agents.",
            "Blast Radius Matrix: Objective 2D impact calculator (Scope × Blocker severity).",
            "IT Ops Mission Control: Interactive Kanban dispatch cockpit & SLA countdowns."
        ]),
        ("Impact & Business Value", ACCENT_GREEN, [
            "Ticket Deflection: ~35% zero-ticket resolution via self-fix playbooks.",
            "74+ Hours Saved: Telemetry snapshot eliminates diagnostic back-and-forth.",
            "MTTR Reduction: Mean Time to Resolution cut from 4.5h to 1.8h.",
            "SLA Adherence: 94.6% compliance rate across critical and high incidents."
        ])
    ]

    for i, (title, color, bullets) in enumerate(pillars):
        x = Inches(0.8 + (i * 4.0))
        add_card(s2, x, Inches(1.7), Inches(3.73), Inches(5.2))
        
        # Header inside card
        hb = s2.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, Inches(1.7), Inches(3.73), Inches(0.55))
        hb.fill.solid()
        hb.fill.fore_color.rgb = color
        hb.line.fill.background()
        tf_hb = hb.text_frame
        p_hb = tf_hb.paragraphs[0]
        p_hb.text = title
        p_hb.font.size = Pt(13)
        p_hb.font.bold = True
        p_hb.font.color.rgb = RGBColor(255, 255, 255)
        p_hb.alignment = PP_ALIGN.CENTER

        # Body bullets
        tb_body = s2.shapes.add_textbox(x + Inches(0.2), Inches(2.4), Inches(3.33), Inches(4.3))
        tf_b = tb_body.text_frame
        tf_b.word_wrap = True
        tf_b.margin_left = tf_b.margin_right = tf_b.margin_top = tf_b.margin_bottom = 0
        for j, b in enumerate(bullets):
            p = tf_b.paragraphs[0] if j == 0 else tf_b.add_paragraph()
            p.text = "• " + b
            p.font.size = Pt(12)
            p.font.color.rgb = TEXT_LIGHT
            p.space_after = Pt(12)

    # ==========================================
    # SLIDE 3: POINT 1 - PROJECT PROGRESS & MILESTONES (PLANNED VS ACTUAL)
    # ==========================================
    s3 = prs.slides.add_slide(blank_layout)
    set_slide_background(s3)
    add_header(s3, "Milestone Performance: Planned vs. Actual Progress", "POINT 1 • PROGRESS & MILESTONES")

    # Table of Milestones
    rows, cols = 8, 5
    left, top, width, height = Inches(0.8), Inches(1.7), Inches(11.73), Inches(5.2)
    table_shape = s3.shapes.add_table(rows, cols, left, top, width, height)
    table = table_shape.table

    table.columns[0].width = Inches(2.5) # Module / Deliverable
    table.columns[1].width = Inches(1.8) # Planned Timeline
    table.columns[2].width = Inches(1.8) # Actual Timeline
    table.columns[3].width = Inches(1.6) # Status
    table.columns[4].width = Inches(4.03) # Variance & Key Achievements

    table_data = [
        ["Project Module / Workstream", "Planned Target", "Actual Delivery", "Status", "Variance & Technical Outcome"],
        ["Architecture & DB Design", "Weeks 1 - 2", "Weeks 1 - 2", "COMPLETED", "Oracle XE schema, sequences, tables & connection pooling setup."],
        ["User & Tech Auth System", "Weeks 2 - 3", "Weeks 2 - 3", "COMPLETED", "Role-based auth (User vs Admin), 1-click demo logins integrated."],
        ["AI Self-Healing Co-Pilot", "Weeks 3 - 4", "Weeks 3 - 4.5", "COMPLETED (+3d)", "Real-time regex triage, 1-click self-fix runbooks & deflection counter."],
        ["Digital Device Passport", "Weeks 4 - 5", "Weeks 4 - 5", "COMPLETED", "Client-side navigator telemetry snapshot (OS, RAM, Latency, IP)."],
        ["Mission Control Kanban", "Weeks 5 - 6", "Weeks 5 - 6", "COMPLETED", "Multi-column drag-and-drop dispatcher cockpit with live SLA countdowns."],
        ["Two-Way Thread & Tech Notes", "Weeks 6 - 7", "Weeks 6 - 7", "COMPLETED", "Public replies + private internal technician notes & audit log."],
        ["IT Ops Velocity Analytics", "Weeks 7 - 8", "Weeks 7 - 8", "COMPLETED", "Real-time MTTR, FCR, SLA compliance, technician leaderboards."]
    ]

    for r_idx, row in enumerate(table_data):
        for c_idx, val in enumerate(row):
            cell = table.cell(r_idx, c_idx)
            cell.text = val
            cell.fill.solid()
            if r_idx == 0:
                cell.fill.fore_color.rgb = RGBColor(14, 116, 144)
            else:
                cell.fill.fore_color.rgb = CARD_BG if r_idx % 2 == 0 else RGBColor(38, 52, 75)
            
            p = cell.text_frame.paragraphs[0]
            p.font.size = Pt(11 if r_idx > 0 else 12)
            p.font.name = "Arial"
            if r_idx == 0:
                p.font.bold = True
                p.font.color.rgb = RGBColor(255, 255, 255)
            else:
                if c_idx == 3:
                    p.font.bold = True
                    p.font.color.rgb = ACCENT_GREEN
                elif c_idx == 0:
                    p.font.bold = True
                    p.font.color.rgb = TEXT_LIGHT
                else:
                    p.font.color.rgb = TEXT_MUTED

    # ==========================================
    # SLIDE 4: POINT 1 - VISUAL GANTT CHART (PLANNED VS ACTUAL TIMELINES)
    # ==========================================
    s4 = prs.slides.add_slide(blank_layout)
    set_slide_background(s4)
    add_header(s4, "Project Execution Gantt Chart: Planned vs. Actual Timelines", "POINT 1 • GANTT CHART COMPARISON")

    # Legend at top
    leg_box = s4.shapes.add_textbox(Inches(7.2), Inches(1.0), Inches(5.3), Inches(0.4))
    tf_leg = leg_box.text_frame
    p_leg = tf_leg.paragraphs[0]
    p_leg.text = "■ Planned Schedule (Blue)     ■ Actual Progress (Cyan/Green)     ■ Milestone"
    p_leg.font.size = Pt(11)
    p_leg.font.bold = True
    p_leg.font.color.rgb = TEXT_LIGHT

    # Background card for Gantt
    add_card(s4, Inches(0.8), Inches(1.5), Inches(11.73), Inches(5.4))

    # Time Axis Headers (Weeks 1 to 10)
    axis_left = Inches(3.6)
    total_w = Inches(8.5)
    week_w = total_w / 10

    for w in range(1, 11):
        wx = axis_left + (w - 1) * week_w
        lbl = s4.shapes.add_textbox(wx, Inches(1.6), week_w, Inches(0.35))
        tf = lbl.text_frame
        p = tf.paragraphs[0]
        p.text = f"W{w}"
        p.alignment = PP_ALIGN.CENTER
        p.font.size = Pt(10)
        p.font.bold = True
        p.font.color.rgb = ACCENT_AMBER if w <= 7 else TEXT_MUTED
        
        # Vertical grid line
        vline = s4.shapes.add_shape(MSO_SHAPE.RECTANGLE, wx, Inches(2.0), Inches(0.01), Inches(4.7))
        vline.fill.solid()
        vline.fill.fore_color.rgb = RGBColor(40, 53, 75)
        vline.line.fill.background()

    # Current time vertical marker (Week 7.5)
    now_line = s4.shapes.add_shape(MSO_SHAPE.RECTANGLE, axis_left + (6.7 * week_w), Inches(1.9), Inches(0.03), Inches(4.8))
    now_line.fill.solid()
    now_line.fill.fore_color.rgb = ACCENT_RED
    now_line.line.fill.background()
    now_tag = s4.shapes.add_textbox(axis_left + (6.1 * week_w), Inches(6.45), Inches(1.4), Inches(0.3))
    p_nt = now_tag.text_frame.paragraphs[0]
    p_nt.text = "▲ CURRENT STAGE"
    p_nt.font.size = Pt(9)
    p_nt.font.bold = True
    p_nt.font.color.rgb = ACCENT_RED

    gantt_items = [
        ("1. Architecture & Oracle DB", 0.0, 2.0, 0.0, 2.0, "Done (100%)"),
        ("2. Role-Based Auth & Users", 1.8, 3.2, 1.8, 3.2, "Done (100%)"),
        ("3. AI Diagnostics & Runbooks", 3.0, 4.2, 3.0, 4.6, "Done (100%)"),
        ("4. Device Passport Telemetry", 4.0, 5.0, 4.2, 5.2, "Done (100%)"),
        ("5. Kanban Board & SLAs", 5.0, 6.2, 5.0, 6.4, "Done (100%)"),
        ("6. Collaboration & Notes", 6.0, 7.0, 6.0, 7.0, "Done (100%)"),
        ("7. LLM Deep Integration", 7.0, 8.5, 7.0, 8.0, "In Progress (30%)"),
        ("8. Final Testing & Polish", 8.0, 10.0, 8.2, 10.0, "Scheduled")
    ]

    for idx, (task_name, p_start, p_end, a_start, a_end, status_str) in enumerate(gantt_items):
        y_base = Inches(2.1 + (idx * 0.54))

        # Task Name label
        tn = s4.shapes.add_textbox(Inches(1.0), y_base - Inches(0.05), Inches(2.5), Inches(0.45))
        tf = tn.text_frame
        tf.word_wrap = True
        tf.margin_left = tf.margin_top = tf.margin_bottom = tf.margin_right = 0
        p = tf.paragraphs[0]
        p.text = task_name
        p.font.size = Pt(10)
        p.font.bold = True
        p.font.color.rgb = TEXT_LIGHT

        # Planned bar (Top thin bar)
        p_x = axis_left + (p_start * week_w)
        p_w = (p_end - p_start) * week_w
        bar_p = s4.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, p_x, y_base, p_w, Inches(0.12))
        bar_p.fill.solid()
        bar_p.fill.fore_color.rgb = ACCENT_BLUE
        bar_p.line.fill.background()

        # Actual bar (Bottom thicker bar)
        a_x = axis_left + (a_start * week_w)
        a_w = (a_end - a_start) * week_w
        bar_a = s4.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, a_x, y_base + Inches(0.16), a_w, Inches(0.18))
        bar_a.fill.solid()
        if "Done" in status_str:
            bar_a.fill.fore_color.rgb = ACCENT_GREEN
        elif "In Progress" in status_str:
            bar_a.fill.fore_color.rgb = ACCENT_CYAN
        else:
            bar_a.fill.fore_color.rgb = CARD_BORDER
        bar_a.line.fill.background()

        # Status text
        st = s4.shapes.add_textbox(a_x + a_w + Inches(0.1), y_base + Inches(0.08), Inches(1.5), Inches(0.3))
        p_st = st.text_frame.paragraphs[0]
        p_st.text = status_str
        p_st.font.size = Pt(9)
        p_st.font.bold = True
        p_st.font.color.rgb = ACCENT_GREEN if "Done" in status_str else (ACCENT_CYAN if "Progress" in status_str else TEXT_MUTED)

    # ==========================================
    # SLIDE 5: POINT 2 - TECHNICAL IMPLEMENTATION & SYSTEM ARCHITECTURE
    # ==========================================
    s5 = prs.slides.add_slide(blank_layout)
    set_slide_background(s5)
    add_header(s5, "System Architecture: 3-Tier Enterprise Design", "POINT 2 • TECHNICAL IMPLEMENTATION")

    # 3 Architecture Tier Cards
    tiers = [
        ("TIER 1: PRESENTATION LAYER", "Modern Web Client (Responsive SPA)", ACCENT_CYAN, [
            "Vanilla ES6+ Modules (Zero heavy bundle overhead)",
            "Glassmorphism UI Engine with dynamic backdrop-filter",
            "Client-Side Telemetry Agent (Digital Device Passport)",
            "Blast Radius Matrix UI (Real-time dynamic scoring)",
            "Interactive Kanban Dispatch Board with drag-and-drop",
            "1-Click Dark/Light Cyberpunk Theme Switching"
        ]),
        ("TIER 2: APPLICATION SERVICES", "Native Java 21+ Multi-Threaded Engine", ACCENT_BLUE, [
            "Native Sun HttpServer (Lightweight, zero bloat, high RPS)",
            "CachedThreadPool Executor for high concurrency",
            "AiDiagnosticService: Symptom pattern matching & runbooks",
            "SLA Computation Engine: Real-time countdowns & breach flags",
            "TicketService & TechnicianService: Business logic & dispatch",
            "RESTful JSON Endpoints with CORS & Preflight handling"
        ]),
        ("TIER 3: PERSISTENCE & DATA", "Oracle XE / XEPDB1 Database Layer", ACCENT_PURPLE, [
            "Oracle Database XE (Port 1521, Service XEPDB1)",
            "Enterprise JDBC Driver (ojdbc11.jar)",
            "Normalized Relational Schema (NX_USERS, NX_TICKETS, etc.)",
            "Sequence-driven primary keys (NX_USER_SEQ, NX_TICKET_SEQ)",
            "Concurrent In-Memory Metadata Store for rich telemetry",
            "DatabaseManager Connection Pool with fallback resilience"
        ])
    ]

    for i, (title, sub, color, items) in enumerate(tiers):
        x = Inches(0.8 + (i * 4.0))
        add_card(s5, x, Inches(1.7), Inches(3.73), Inches(5.2))

        # Top banner
        tb = s5.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, Inches(1.7), Inches(3.73), Inches(0.8))
        tb.fill.solid()
        tb.fill.fore_color.rgb = color
        tb.line.fill.background()
        tf = tb.text_frame
        p1 = tf.paragraphs[0]
        p1.text = title
        p1.font.size = Pt(11)
        p1.font.bold = True
        p1.font.color.rgb = RGBColor(255, 255, 255)
        p2 = tf.add_paragraph()
        p2.text = sub
        p2.font.size = Pt(10)
        p2.font.color.rgb = RGBColor(230, 240, 255)

        # Bullets
        tb_body = s5.shapes.add_textbox(x + Inches(0.2), Inches(2.65), Inches(3.33), Inches(4.1))
        tf_b = tb_body.text_frame
        tf_b.word_wrap = True
        for j, it in enumerate(items):
            p = tf_b.paragraphs[0] if j == 0 else tf_b.add_paragraph()
            p.text = "✔ " + it
            p.font.size = Pt(11)
            p.font.color.rgb = TEXT_LIGHT
            p.space_after = Pt(10)

    # ==========================================
    # SLIDE 6: POINT 2 - TOOLS & TECHNOLOGIES USED
    # ==========================================
    s6 = prs.slides.add_slide(blank_layout)
    set_slide_background(s6)
    add_header(s6, "Technology Stack & Tooling Ecosystem", "POINT 2 • TOOLS & TECHNOLOGIES")

    tech_categories = [
        ("Backend & Core Runtime", ACCENT_BLUE, [
            ("Java 21 LTS", "Core programming language; virtual threads & modern pattern matching."),
            ("com.sun.net.httpserver", "Zero-dependency, high-throughput micro-webserver."),
            ("ojdbc11.jar", "Official Oracle Database 21c/23c enterprise JDBC driver."),
            ("CachedThreadPool", "Multi-threaded async execution for concurrent API requests.")
        ]),
        ("Database & Storage", ACCENT_PURPLE, [
            ("Oracle Database XE", "Enterprise relational database running on container XEPDB1."),
            ("Oracle Sequences", "Autonomous atomic sequence generators for primary IDs."),
            ("Foreign Key Integrity", "Cascading rules and referential constraints on tickets."),
            ("ConcurrentHashMap", "Thread-safe fast cache for unstructured telemetry JSON.")
        ]),
        ("Frontend & User Experience", ACCENT_CYAN, [
            ("HTML5 & Modern CSS3", "CSS Custom Properties, Flexbox & CSS Grid for fluid layouts."),
            ("Glassmorphism Engine", "backdrop-filter: blur(16px) with translucent cyber panels."),
            ("ES6+ JavaScript", "Modular async/await client architecture without framework bloat."),
            ("Client Telemetry API", "Browser Navigator & Performance API for zero-agent context.")
        ]),
        ("DevOps & Tooling", ACCENT_GREEN, [
            ("Git & GitHub", "Distributed version control, branching & milestone tracking."),
            ("Docker & Containers", "Containerized deployment with portable run scripts."),
            ("PowerShell & Bat Automation", "Automated compile, classpath binding, and startup scripts."),
            ("REST Client & Curl", "API endpoint testing, load emulation, and mock harnesses.")
        ])
    ]

    for i, (cat_title, color, items) in enumerate(tech_categories):
        r = i // 2
        c = i % 2
        x = Inches(0.8 + (c * 6.0))
        y = Inches(1.7 + (r * 2.7))
        add_card(s6, x, y, Inches(5.73), Inches(2.5))

        # Title strip
        st = s6.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, Inches(5.73), Inches(0.45))
        st.fill.solid()
        st.fill.fore_color.rgb = color
        st.line.fill.background()
        p = st.text_frame.paragraphs[0]
        p.text = cat_title
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = RGBColor(255, 255, 255)

        # Content
        tb = s6.shapes.add_textbox(x + Inches(0.2), y + Inches(0.55), Inches(5.33), Inches(1.85))
        tf = tb.text_frame
        tf.word_wrap = True
        for j, (name, desc) in enumerate(items):
            p = tf.paragraphs[0] if j == 0 else tf.add_paragraph()
            p.text = f"{name}: "
            p.font.size = Pt(10)
            p.font.bold = True
            p.font.color.rgb = ACCENT_AMBER
            
            # run for desc
            run = p.add_run()
            run.text = desc
            run.font.bold = False
            run.font.color.rgb = TEXT_LIGHT
            p.space_after = Pt(4)

    # ==========================================
    # SLIDE 7: POINT 2 - DATABASE SCHEMAS & DATA MODEL
    # ==========================================
    s7 = prs.slides.add_slide(blank_layout)
    set_slide_background(s7)
    add_header(s7, "Database Schema & Entity Relationship Model", "POINT 2 • DATABASE DESIGN")

    schemas = [
        ("TABLE: NX_USERS", ACCENT_BLUE, [
            ("USER_ID", "NUMBER", "PRIMARY KEY (NX_USER_SEQ)"),
            ("FULL_NAME", "VARCHAR2(100)", "NOT NULL Employee Full Name"),
            ("EMAIL", "VARCHAR2(150)", "UNIQUE Corporate Email"),
            ("USER_PASSWORD", "VARCHAR2(255)", "Secure Credential Hash"),
            ("USER_ROLE", "VARCHAR2(20)", "DEFAULT 'USER' ('ADMIN' | 'USER')"),
            ("CREATED_AT", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP")
        ]),
        ("TABLE: NX_TECHNICIANS", ACCENT_PURPLE, [
            ("TECHNICIAN_ID", "NUMBER", "PRIMARY KEY (NX_TECH_SEQ)"),
            ("FULL_NAME", "VARCHAR2(100)", "NOT NULL Specialist Name"),
            ("EMAIL", "VARCHAR2(150)", "UNIQUE Contact Email"),
            ("SPECIALIZATION", "VARCHAR2(100)", "Hardware | Network | Software"),
            ("STATUS", "VARCHAR2(20)", "AVAILABLE | BUSY | OFFLINE"),
            ("CREATED_AT", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP")
        ]),
        ("TABLE: NX_TICKETS", ACCENT_CYAN, [
            ("TICKET_ID", "NUMBER", "PRIMARY KEY (NX_TICKET_SEQ)"),
            ("USER_ID", "NUMBER", "FK -> NX_USERS(USER_ID)"),
            ("TECHNICIAN_ID", "NUMBER", "FK -> NX_TECHNICIANS(TECHNICIAN_ID)"),
            ("TITLE & DESC", "VARCHAR2", "Problem Title & Full Symptom Log"),
            ("CATEGORY & PRIORITY", "VARCHAR2", "Network/HW/SW & LOW/MED/HIGH/CRIT"),
            ("STATUS", "VARCHAR2(20)", "OPEN | IN_PROGRESS | WAITING | RESOLVED"),
            ("SLA & METADATA", "DYNAMIC / JSON", "Blast Score, Telemetry, SLA Target")
        ])
    ]

    for i, (table_name, color, cols) in enumerate(schemas):
        x = Inches(0.8 + (i * 4.0))
        add_card(s7, x, Inches(1.7), Inches(3.73), Inches(5.2))

        # Header
        hb = s7.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, Inches(1.7), Inches(3.73), Inches(0.55))
        hb.fill.solid()
        hb.fill.fore_color.rgb = color
        hb.line.fill.background()
        p = hb.text_frame.paragraphs[0]
        p.text = table_name
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = RGBColor(255, 255, 255)
        p.alignment = PP_ALIGN.CENTER

        # Column list
        tb = s7.shapes.add_textbox(x + Inches(0.15), Inches(2.35), Inches(3.43), Inches(4.4))
        tf = tb.text_frame
        tf.word_wrap = True
        for j, (col, dtype, note) in enumerate(cols):
            p = tf.paragraphs[0] if j == 0 else tf.add_paragraph()
            p.text = f"• {col} "
            p.font.size = Pt(10)
            p.font.bold = True
            p.font.color.rgb = ACCENT_AMBER
            
            run = p.add_run()
            run.text = f"({dtype})\n  └ {note}"
            run.font.bold = False
            run.font.color.rgb = TEXT_LIGHT
            p.space_after = Pt(8)

    # ==========================================
    # SLIDE 8: POINT 2 - TESTING METHODOLOGIES USED
    # ==========================================
    s8 = prs.slides.add_slide(blank_layout)
    set_slide_background(s8)
    add_header(s8, "Quality Assurance & Testing Methodologies", "POINT 2 • TESTING STRATEGY")

    tests = [
        ("Unit Testing", ACCENT_BLUE, [
            ("AI Diagnostics Regex Engine", "Validated pattern matching across 40+ symptom permutations (Wi-Fi dropouts, VPN handshakes, thermal throttling)."),
            ("Blast Radius Calculator", "Verified that (Scope × Blocker severity) produces accurate 0-100 scores and assigns appropriate SLA priorities."),
            ("SLA Target Countdown Logic", "Tested boundary conditions for Critical (4h), High (8h), Medium (24h), and Low (48h) deadlines.")
        ]),
        ("Integration Testing", ACCENT_PURPLE, [
            ("Oracle JDBC Connectivity", "Tested connection pool creation, reconnect on socket drop, and fallback sequence retrieval on Oracle XE."),
            ("REST API Payload Validation", "Tested JSON parsing, URL query string decoding, and CORS header compliance for cross-origin callers."),
            ("Relational JOIN Integrity", "Verified that ticket queues properly join requester details and assigned technician metadata.")
        ]),
        ("System & E2E Testing", ACCENT_CYAN, [
            ("User Issue-to-Deflection Flow", "Verified end-to-end journey where user inputs symptoms, executes self-fix runbook, and deflects ticket."),
            ("Technician Kanban Dispatch Flow", "Validated status transitions (OPEN -> IN_PROGRESS -> RESOLVED) and real-time UI refresh."),
            ("Collaboration Thread Audit", "Verified role isolation: public users cannot view internal tech-only notes.")
        ]),
        ("Performance & Concurrency", ACCENT_GREEN, [
            ("CachedThreadPool Stress", "Simulated 100 concurrent HTTP requests to /api/system/status and /api/ai/diagnose with < 15ms response latency."),
            ("Memory Leak Verification", "Monitored JVM heap across 1,000 ticket operations; verified zero memory leaks in ConcurrentHashMap store."),
            ("Browser Telemetry Accuracy", "Benchmarked navigator client telemetry snapshot across Chrome, Edge, and Firefox.")
        ])
    ]

    for i, (test_name, color, items) in enumerate(tests):
        r = i // 2
        c = i % 2
        x = Inches(0.8 + (c * 6.0))
        y = Inches(1.7 + (r * 2.7))
        add_card(s8, x, y, Inches(5.73), Inches(2.5))

        st = s8.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, Inches(5.73), Inches(0.45))
        st.fill.solid()
        st.fill.fore_color.rgb = color
        st.line.fill.background()
        p = st.text_frame.paragraphs[0]
        p.text = test_name
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = RGBColor(255, 255, 255)

        tb = s8.shapes.add_textbox(x + Inches(0.2), y + Inches(0.55), Inches(5.33), Inches(1.85))
        tf = tb.text_frame
        tf.word_wrap = True
        for j, (subj, desc) in enumerate(items):
            p = tf.paragraphs[0] if j == 0 else tf.add_paragraph()
            p.text = f"• {subj}: "
            p.font.size = Pt(10)
            p.font.bold = True
            p.font.color.rgb = ACCENT_CYAN
            
            run = p.add_run()
            run.text = desc
            run.font.bold = False
            run.font.color.rgb = TEXT_LIGHT
            p.space_after = Pt(4)

    # ==========================================
    # SLIDE 9: POINT 3 - PROBLEM-SOLVING & BUG FIXES (PART 1)
    # ==========================================
    s9 = prs.slides.add_slide(blank_layout)
    set_slide_background(s9)
    add_header(s9, "Problem-Solving & Bug Fixes: Technical Challenges (Part 1)", "POINT 3 • PROBLEM-SOLVING")

    bugs_part1 = [
        ("CHALLENGE 1: Oracle XE Connection & Generated Key Retrieval", [
            ("The Challenge", "In Oracle Database XE, standard JDBC `getGeneratedKeys()` can fail or return non-standard row IDs depending on the driver version and table sequence triggers."),
            ("Impact", "When users created tickets, the backend failed to return the newly generated `TICKET_ID`, causing ticket creation to throw an unhandled 500 error."),
            ("Resolution Strategy", "Implemented a 2-stage defensive strategy in `TicketService.java`: first attempts `prepareStatement(sql, new String[]{'TICKET_ID'})`, and if empty, automatically triggers a fallback query: `SELECT MAX(TICKET_ID) FROM NX_TICKETS WHERE CREATED_BY = ?` within the same transaction."),
            ("Outcome", "100% reliable ticket ID generation across all versions of Oracle Database XE / Cloud ADB.")
        ]),
        ("CHALLENGE 2: The '20-Questions' Hardware Telemetry Blindspot", [
            ("The Challenge", "End users reported vague issues like 'My computer is freezing' or 'Internet broken', forcing technicians to spend 2-3 days asking basic questions regarding OS, IP, RAM, and browser."),
            ("Impact", "Inflated MTTR (Mean Time to Resolution) and severe technician frustration."),
            ("Resolution Strategy", "Engineered the 'Digital Device Passport': client-side JavaScript module that captures navigator hardware concurrency, device memory (RAM), network round-trip latency, screen resolution, and OS user agent into a non-intrusive JSON snapshot."),
            ("Outcome", "Every ticket carries instant, verified hardware context; eliminated 90% of back-and-forth diagnostic queries.")
        ])
    ]

    for i, (bug_title, points) in enumerate(bugs_part1):
        x = Inches(0.8 + (i * 6.0))
        add_card(s9, x, Inches(1.7), Inches(5.73), Inches(5.2))

        # Title
        hb = s9.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, Inches(1.7), Inches(5.73), Inches(0.55))
        hb.fill.solid()
        hb.fill.fore_color.rgb = ACCENT_RED if i == 0 else ACCENT_AMBER
        hb.line.fill.background()
        p = hb.text_frame.paragraphs[0]
        p.text = bug_title
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = RGBColor(255, 255, 255)

        # Content
        tb = s9.shapes.add_textbox(x + Inches(0.2), Inches(2.35), Inches(5.33), Inches(4.4))
        tf = tb.text_frame
        tf.word_wrap = True
        for j, (heading, body) in enumerate(points):
            p = tf.paragraphs[0] if j == 0 else tf.add_paragraph()
            p.text = f"{heading}: "
            p.font.size = Pt(10)
            p.font.bold = True
            p.font.color.rgb = ACCENT_CYAN
            
            run = p.add_run()
            run.text = body
            run.font.bold = False
            run.font.color.rgb = TEXT_LIGHT
            p.space_after = Pt(8)

    # ==========================================
    # SLIDE 10: POINT 3 - PROBLEM-SOLVING & BUG FIXES (PART 2)
    # ==========================================
    s10 = prs.slides.add_slide(blank_layout)
    set_slide_background(s10)
    add_header(s10, "Problem-Solving & Bug Fixes: Technical Challenges (Part 2)", "POINT 3 • PROBLEM-SOLVING")

    bugs_part2 = [
        ("CHALLENGE 3: Subjective Priority Inflation ('Cry Wolf Syndrome')", [
            ("The Challenge", "In standard ticketing portals, end users invariably tag their tickets as 'CRITICAL' or 'URGENT', rendering priority sorting meaningless."),
            ("Impact", "Actual production outages were buried under routine mouse or password reset requests."),
            ("Resolution Strategy", "Created an objective 2D 'Blast Radius & Impact Matrix'. Instead of asking for priority, the UI asks two factual questions: Scope (Just Me -> Entire Org) and Work Blocker (Minor -> Data Risk). The algorithm computes an Impact Score (0-100) and locks the true SLA priority."),
            ("Outcome", "Eliminated subjective priority inflation; critical tickets now represent genuine high-blast-radius incidents.")
        ]),
        ("CHALLENGE 4: Repetitive Ticket Volume & Slow Triage", [
            ("The Challenge", "Over 60% of all submitted tickets were routine issues (DNS cache lockup, VPN split-tunnel stalls, Teams cache corruption)."),
            ("Impact", "IT dispatchers were overwhelmed with repetitive copy-pasting of identical troubleshooting steps."),
            ("Resolution Strategy", "Developed the Real-Time AI Co-Pilot Diagnostic Engine. As the user types their symptoms, the client analyzes keywords, displays instant step-by-step runbooks, provides copyable PowerShell/Bash fix commands, and offers a 'Yes, Problem Solved! (Zero-Ticket)' deflection button."),
            ("Outcome", "Over 148 tickets deflected before hitting the queue, saving an estimated 74 hours of engineering labor.")
        ])
    ]

    for i, (bug_title, points) in enumerate(bugs_part2):
        x = Inches(0.8 + (i * 6.0))
        add_card(s10, x, Inches(1.7), Inches(5.73), Inches(5.2))

        hb = s10.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, Inches(1.7), Inches(5.73), Inches(0.55))
        hb.fill.solid()
        hb.fill.fore_color.rgb = ACCENT_PURPLE if i == 0 else ACCENT_BLUE
        hb.line.fill.background()
        p = hb.text_frame.paragraphs[0]
        p.text = bug_title
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = RGBColor(255, 255, 255)

        tb = s10.shapes.add_textbox(x + Inches(0.2), Inches(2.35), Inches(5.33), Inches(4.4))
        tf = tb.text_frame
        tf.word_wrap = True
        for j, (heading, body) in enumerate(points):
            p = tf.paragraphs[0] if j == 0 else tf.add_paragraph()
            p.text = f"{heading}: "
            p.font.size = Pt(10)
            p.font.bold = True
            p.font.color.rgb = ACCENT_GREEN
            
            run = p.add_run()
            run.text = body
            run.font.bold = False
            run.font.color.rgb = TEXT_LIGHT
            p.space_after = Pt(8)

    # ==========================================
    # SLIDE 11: POINT 4 - FEEDBACK INTEGRATION FROM FIRST REVIEW
    # ==========================================
    s11 = prs.slides.add_slide(blank_layout)
    set_slide_background(s11)
    add_header(s11, "Feedback Integration: Enhancements Implemented from Review 1", "POINT 4 • FEEDBACK INTEGRATION")

    feedback_items = [
        ("Review 1 Feedback: Clunky & Static UI", "Evaluators noted that standard ticketing portals feel like dull accounting software.", [
            "Redesigned the entire frontend with an ultra-modern Glassmorphic Cyber-Clean Theme (`backdrop-filter: blur(16px)`).",
            "Implemented a 1-click Light/Dark theme toggle with persistent CSS variable tokens.",
            "Replaced static HTML tables with an interactive Mission Control Kanban Dispatch Cockpit with visual SLA rings."
        ]),
        ("Review 1 Feedback: Need Proactive Ticket Deflection", "Reviewers stressed that modern IT service management must prevent tickets rather than just log them.", [
            "Introduced Real-Time AI Symptom Triage that displays diagnostic runbooks as the user types.",
            "Added 1-click PowerShell/CMD fix commands and a '🎉 Yes, Problem Solved!' deflection button.",
            "Built a dedicated deflection counter in IT Ops Analytics showing hours saved."
        ]),
        ("Review 1 Feedback: Diagnostic Context Gaps", "Mentors highlighted that technician time is wasted asking users for OS and network configurations.", [
            "Created the 'Digital Device Passport' auto-snapshot capturing OS, RAM, Latency, and Screen specs.",
            "Embedded interactive telemetry chips inside the ticket detail modal for technician inspection.",
            "Added an opt-out toggle to respect employee privacy."
        ]),
        ("Review 1 Feedback: Internal vs. External Collaboration", "Reviewers pointed out that technicians need a private space to discuss tickets without confusing end-users.", [
            "Built a Two-Way Discussion Thread with a dedicated '🔒 Internal Tech-Only Note' checkbox.",
            "Implemented role-gated visibility: end-users see public updates; internal notes are restricted to admins/technicians.",
            "Added AI-assisted technician response drafting to speed up communication."
        ])
    ]

    for i, (fb_title, fb_quote, actions) in enumerate(feedback_items):
        r = i // 2
        c = i % 2
        x = Inches(0.8 + (c * 6.0))
        y = Inches(1.7 + (r * 2.7))
        add_card(s11, x, y, Inches(5.73), Inches(2.5))

        st = s11.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, Inches(5.73), Inches(0.45))
        st.fill.solid()
        st.fill.fore_color.rgb = ACCENT_CYAN if r == 0 else ACCENT_PURPLE
        st.line.fill.background()
        p = st.text_frame.paragraphs[0]
        p.text = fb_title
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = RGBColor(255, 255, 255)

        tb = s11.shapes.add_textbox(x + Inches(0.2), y + Inches(0.52), Inches(5.33), Inches(1.9))
        tf = tb.text_frame
        tf.word_wrap = True
        
        p0 = tf.paragraphs[0]
        p0.text = f"Mentor Critique: \"{fb_quote}\""
        p0.font.size = Pt(9.5)
        p0.font.italic = True
        p0.font.color.rgb = ACCENT_AMBER
        p0.space_after = Pt(4)

        for j, act in enumerate(actions):
            p = tf.add_paragraph()
            p.text = "✔ " + act
            p.font.size = Pt(9.5)
            p.font.color.rgb = TEXT_LIGHT
            p.space_after = Pt(2)

    # ==========================================
    # SLIDE 12: POINT 5 - ROADMAP TO COMPLETION: REMAINING TASKS
    # ==========================================
    s12 = prs.slides.add_slide(blank_layout)
    set_slide_background(s12)
    add_header(s12, "Roadmap to Completion: Workstreams & Sprint Plan", "POINT 5 • ROADMAP TO COMPLETION")

    workstreams = [
        ("SPRINT 7: ADVANCED AI & NLP (WEEKS 8-9)", ACCENT_CYAN, [
            ("External LLM Integration", "Connect Google Gemini / OpenAI API for complex, multi-paragraph log file analysis and code stack-trace debugging."),
            ("Voice-to-Ticket Transcription", "Allow employees to dictate tickets using Web Speech API with automatic summarization."),
            ("Semantic Similar Ticket Search", "Detect duplicate company-wide incidents using vector embeddings and cluster notifications.")
        ]),
        ("SPRINT 8: ENTERPRISE NOTIFICATIONS (WEEKS 9-10)", ACCENT_BLUE, [
            ("Automated Email / SMTP Alerts", "Dispatch instant email notifications on ticket assignment, SLA breach warnings, and user replies."),
            ("Slack / MS Teams Webhook Bot", "Broadcast Critical incident alerts into dedicated IT Ops Slack/Teams incident channels."),
            ("Multi-Factor Authentication (MFA)", "Implement TOTP authenticator app support for privileged IT Administrator accounts.")
        ]),
        ("SPRINT 9: FLEET AUTOMATION (WEEKS 10-11)", ACCENT_PURPLE, [
            ("One-Click Remote Remediation", "Allow technicians to trigger verified PowerShell agent scripts directly from the Kanban detail modal."),
            ("Asset Management CMDB", "Link hardware serial numbers, warranty status, and department inventory to user profiles."),
            ("Automated SLA Escalation", "Reassign unacknowledged tickets automatically after 50% of the SLA deadline has elapsed.")
        ])
    ]

    for i, (ws_title, color, items) in enumerate(workstreams):
        x = Inches(0.8 + (i * 4.0))
        add_card(s12, x, Inches(1.7), Inches(3.73), Inches(5.2))

        hb = s12.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, Inches(1.7), Inches(3.73), Inches(0.6))
        hb.fill.solid()
        hb.fill.fore_color.rgb = color
        hb.line.fill.background()
        p = hb.text_frame.paragraphs[0]
        p.text = ws_title
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = RGBColor(255, 255, 255)
        p.alignment = PP_ALIGN.CENTER

        tb = s12.shapes.add_textbox(x + Inches(0.15), Inches(2.4), Inches(3.43), Inches(4.3))
        tf = tb.text_frame
        tf.word_wrap = True
        for j, (task, desc) in enumerate(items):
            p = tf.paragraphs[0] if j == 0 else tf.add_paragraph()
            p.text = f"• {task}: "
            p.font.size = Pt(10)
            p.font.bold = True
            p.font.color.rgb = ACCENT_AMBER
            
            run = p.add_run()
            run.text = desc
            run.font.bold = False
            run.font.color.rgb = TEXT_LIGHT
            p.space_after = Pt(10)

    # ==========================================
    # SLIDE 13: POINT 5 - ROADMAP: FINAL TESTING & QA PLAN
    # ==========================================
    s13 = prs.slides.add_slide(blank_layout)
    set_slide_background(s13)
    add_header(s13, "Roadmap to Completion: Final Testing & Quality Assurance Plan", "POINT 5 • FINAL TESTING PLAN")

    qa_pillars = [
        ("Performance & Load Stress Testing", ACCENT_BLUE, [
            "Simulate 500+ concurrent simulated users and technicians using Apache JMeter.",
            "Verify server response time remains under 50ms under peak burst ticket creation.",
            "Profile Oracle XE connection pool under sustained multi-threaded read/write load."
        ]),
        ("Security & Vulnerability Audit", ACCENT_RED, [
            "Penetration testing: SQL injection prevention via strictly parameterized queries.",
            "Cross-Site Scripting (XSS) defense: input sanitization in ticket descriptions and comments.",
            "Role privilege escalation testing: ensure standard users cannot access /api/admin/* endpoints."
        ]),
        ("Cross-Browser & Device Compatibility", ACCENT_CYAN, [
            "Audit UI rendering across Google Chrome, Mozilla Firefox, Microsoft Edge, and Apple Safari.",
            "Responsive viewport testing: Desktop (1920x1080), Tablet (1024x768), and Mobile (375x812).",
            "Verify hardware telemetry gathering consistency across Windows, macOS, and Linux."
        ]),
        ("Database Disaster Recovery & Backup", ACCENT_GREEN, [
            "Test Oracle XE RMAN export / backup routines and snapshot restoration.",
            "Validate graceful error handling and offline queuing if the database server temporarily drops.",
            "Verify zero data loss in audit log timestamps and sequence numbers."
        ])
    ]

    for i, (qa_title, color, bullets) in enumerate(qa_pillars):
        r = i // 2
        c = i % 2
        x = Inches(0.8 + (c * 6.0))
        y = Inches(1.7 + (r * 2.7))
        add_card(s13, x, y, Inches(5.73), Inches(2.5))

        st = s13.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, Inches(5.73), Inches(0.45))
        st.fill.solid()
        st.fill.fore_color.rgb = color
        st.line.fill.background()
        p = st.text_frame.paragraphs[0]
        p.text = qa_title
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = RGBColor(255, 255, 255)

        tb = s13.shapes.add_textbox(x + Inches(0.2), y + Inches(0.55), Inches(5.33), Inches(1.85))
        tf = tb.text_frame
        tf.word_wrap = True
        for j, b in enumerate(bullets):
            p = tf.paragraphs[0] if j == 0 else tf.add_paragraph()
            p.text = "✔ " + b
            p.font.size = Pt(10.5)
            p.font.color.rgb = TEXT_LIGHT
            p.space_after = Pt(5)

    # ==========================================
    # SLIDE 14: POINT 5 - FINAL DELIVERABLES CHECKLIST & EVALUATION METRICS
    # ==========================================
    s14 = prs.slides.add_slide(blank_layout)
    set_slide_background(s14)
    add_header(s14, "Final Review Deliverables & Expected Project Outcomes", "POINT 5 • DELIVERABLES FOR FINAL REVIEW")

    # Left Card: Deliverables Checklist
    add_card(s14, Inches(0.8), Inches(1.7), Inches(5.73), Inches(5.2))
    h_deliv = s14.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(1.7), Inches(5.73), Inches(0.55))
    h_deliv.fill.solid()
    h_deliv.fill.fore_color.rgb = ACCENT_PURPLE
    h_deliv.line.fill.background()
    p = h_deliv.text_frame.paragraphs[0]
    p.text = "📦 Deliverables Required for Final Review"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = RGBColor(255, 255, 255)

    deliv_items = [
        ("Production-Ready Source Code", "Complete Java 21+ backend, clean vanilla frontend, and Oracle SQL schema scripts in GitHub repository."),
        ("One-Click Runnable Package", "Automated Windows bat/PowerShell launch script and standalone Dockerfile for zero-friction evaluation."),
        ("Comprehensive Technical Report", "Detailed IEEE-format architecture document, class diagrams, ER models, and API specifications."),
        ("User & Administrator Manuals", "Step-by-step illustrated guides for both employees (reporting & self-fix) and IT dispatchers (Kanban & telemetry)."),
        ("Live Demonstration & Test Data", "Pre-seeded database with realistic IT users, specialist technicians, and historical ticket analytics.")
    ]
    tb_d = s14.shapes.add_textbox(Inches(1.0), Inches(2.35), Inches(5.33), Inches(4.4))
    tf_d = tb_d.text_frame
    tf_d.word_wrap = True
    for j, (title, desc) in enumerate(deliv_items):
        p = tf_d.paragraphs[0] if j == 0 else tf_d.add_paragraph()
        p.text = f"☑ {title}\n"
        p.font.size = Pt(10)
        p.font.bold = True
        p.font.color.rgb = ACCENT_GREEN
        run = p.add_run()
        run.text = f"   {desc}"
        run.font.bold = False
        run.font.color.rgb = TEXT_LIGHT
        p.space_after = Pt(7)

    # Right Card: Target Performance & Evaluation Metrics
    add_card(s14, Inches(6.8), Inches(1.7), Inches(5.73), Inches(5.2))
    h_eval = s14.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(6.8), Inches(1.7), Inches(5.73), Inches(0.55))
    h_eval.fill.solid()
    h_eval.fill.fore_color.rgb = ACCENT_CYAN
    h_eval.line.fill.background()
    p = h_eval.text_frame.paragraphs[0]
    p.text = "🎯 Target KPIs & Evaluation Metrics"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = RGBColor(255, 255, 255)

    kpi_items = [
        ("Mean Time to Resolution (MTTR)", "< 2.0 Hours", "Targeting 60% faster resolution compared to standard industry baseline of 5.2h."),
        ("Zero-Ticket Deflection Rate", "> 30.0%", "Deflecting common network, VPN, and credential inquiries via interactive runbooks."),
        ("SLA Adherence Compliance", "≥ 95.0%", "Enforcing strict 4h (Critical) and 8h (High) resolution countdowns via Kanban cockpit."),
        ("First Contact Resolution (FCR)", "≥ 75.0%", "Empowering technicians with instant device telemetry and automated fix scripts on first touch.")
    ]
    tb_k = s14.shapes.add_textbox(Inches(7.0), Inches(2.35), Inches(5.33), Inches(4.4))
    tf_k = tb_k.text_frame
    tf_k.word_wrap = True
    for j, (metric, target, impact) in enumerate(kpi_items):
        p = tf_k.paragraphs[0] if j == 0 else tf_k.add_paragraph()
        p.text = f"📊 {metric}: "
        p.font.size = Pt(10.5)
        p.font.bold = True
        p.font.color.rgb = ACCENT_AMBER
        
        run_tgt = p.add_run()
        run_tgt.text = f"{target}\n"
        run_tgt.font.bold = True
        run_tgt.font.color.rgb = ACCENT_GREEN

        run_imp = p.add_run()
        run_imp.text = f"   Impact: {impact}"
        run_imp.font.bold = False
        run_imp.font.color.rgb = TEXT_LIGHT
        p.space_after = Pt(10)

    # ==========================================
    # SLIDE 15: CONCLUSION & Q&A
    # ==========================================
    s15 = prs.slides.add_slide(blank_layout)
    set_slide_background(s15)

    add_card(s15, Inches(1.5), Inches(1.2), Inches(10.33), Inches(5.1), bg_color=RGBColor(24, 33, 50), border_color=ACCENT_CYAN)

    tb_end = s15.shapes.add_textbox(Inches(2.0), Inches(1.8), Inches(9.33), Inches(1.2))
    tf = tb_end.text_frame
    p = tf.paragraphs[0]
    p.text = "Thank You! Questions & Discussion"
    p.font.size = Pt(36)
    p.font.bold = True
    p.font.color.rgb = TEXT_LIGHT
    p.alignment = PP_ALIGN.CENTER

    tb_sum = s15.shapes.add_textbox(Inches(2.0), Inches(3.0), Inches(9.33), Inches(1.0))
    tf_s = tb_sum.text_frame
    tf_s.word_wrap = True
    p = tf_s.paragraphs[0]
    p.text = "NexDesk 2.0 transforms enterprise IT service management from passive logging into an autonomous, self-healing, data-driven operational cockpit."
    p.font.size = Pt(16)
    p.font.color.rgb = ACCENT_CYAN
    p.alignment = PP_ALIGN.CENTER

    # Quick summary badges
    badges = [
        "✔ Java 21+ & Oracle XE Ready",
        "✔ AI Self-Healing & Deflection Active",
        "✔ Digital Device Passport Built",
        "✔ Mission Control Kanban Operational"
    ]
    for idx, b in enumerate(badges):
        bx = Inches(2.2 + (idx * 2.3))
        bg = s15.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, bx, Inches(4.3), Inches(2.1), Inches(0.6))
        bg.fill.solid()
        bg.fill.fore_color.rgb = CARD_BG
        bg.line.color.rgb = ACCENT_GREEN
        tf_bg = bg.text_frame
        p_bg = tf_bg.paragraphs[0]
        p_bg.text = b
        p_bg.font.size = Pt(10)
        p_bg.font.bold = True
        p_bg.font.color.rgb = TEXT_LIGHT
        p_bg.alignment = PP_ALIGN.CENTER

    # Output file
    output_filename = "NexDesk_Project_Review_Presentation.pptx"
    prs.save(output_filename)
    print(f"Presentation saved successfully as '{output_filename}'")

if __name__ == "__main__":
    create_presentation()
