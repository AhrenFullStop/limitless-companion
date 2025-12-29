# Limitless Companion - Mobile App Wireframes

## Introduction
This document outlines the wireframes and user interface specifications for the Limitless Companion Android application.

**Design Aesthetic: "Modern Retro Terminal"**
The design merges the nostalgia of cathode-ray tube (CRT) monitors and command-line interfaces with modern mobile UX patterns.
*   **Vibe:** High-tech, hacker-chic, Fallout-inspired but clean.
*   **Visuals:** Deep dark backgrounds, phosphor green accents, glassy overlays, and ASCII art flourishes.
*   **Interaction:** Snappy, responsive, with "terminal boot" style animations for loading states.
*   **Philosophy:** Function over form, but form is strictly "cyber-minimalism".

## Navigation Structure
The app uses a **Bottom Navigation Bar** for primary top-level destinations.

*   **Destinations:**
    1.  **Record** (Home)
    2.  **History** (Transcripts)
    3.  **Search**
*   **Secondary Navigation:**
    *   **Settings:** Accessed via Top App Bar icon on the Home screen.

---

## 1. Recording Screen (Home)

### Purpose
The primary landing screen. Focuses on the recording state with a high-tech visualizer.

### User Flow
*   **Entry:** App launch -> "Boot sequence" animation (lines of text scrolling fast) -> Main Screen.
*   **Next:** Tap 'History' or 'Search'.

### ASCII Wireframe
```text
┌─────────────────────────────────┐
│  LIMITLESS_OS v1.0   [ONLINE] ⚙ │  <-- Monospace Header
├─────────────────────────────────┤
│                                 │
│        [ 00:04:23:12 ]          │  <-- Monospace Timer with millis
│                                 │
│      . . : : | | | : : . .      │  <-- Retro Audio Visualizer
│      : : | | | | | | | : :      │      (Green bars on black)
│      ' ' : : | | | : : ' '      │
│                                 │
│ < BT: HEADSET_X1 [CONNECTED] >  │  <-- Techy bracketed status
│                                 │
├─────────────────────────────────┤
│  > LIVE_FEED                    │
│                                 │
│  > Remember to email John       │  
│  > about the project timeline_  │   <-- Blinking cursor (6 lines of text fading out to the top)
│                                 │
├─────────────────────────────────┤
│                                 │
│      [  █  STOP_CAPTURE  ]      │  <-- Rectangular, slight rounded corners
│                                 │
│                                 │
├─────────────────────────────────┤
│ [REC]    HISTORY    SEARCH      │  <-- Capitalized, Monospace Nav
└─────────────────────────────────┘
```

### Components
*   **Top Bar:** Minimal. Status indicator is a solid green square `[■]`.
*   **Timer:** `JetBrains Mono` font. Large, glowing green.
*   **Visualizer:** Digital bar graph or oscilloscope style. Green lines on black.
*   **Live Preview:** Looks like a terminal window. Black background, slightly lighter border. Text appears character-by-character (typewriter effect) if performance allows.
*   **Primary Control:**
    *   **Style:** "Glassy" button with a thin green border and glowing text.
    *   **Idle:** `[ > EXECUTE_RECORDING ]`
    *   **Active:** `[ █ TERMINATE ]` or `[ || PAUSE ]`

### Interactions
*   **Boot Sequence:** On first open, show a quick 800ms animation of "System initializing... Audio drivers loaded... Connection established."
*   **Typewriter Effect:** Transcribed text appears as if being typed.

### States
*   **Idle:** Static cursor `_`.
*   **Recording:** Active visualizer, incrementing timer.
*   **Connecting:** Text: `> ESTABLISHING_LINK...` with a spinning ASCII character `| / - \`.

---

## 2. Transcript Screen (History)

### Purpose
Review past logs. Designed to look like a system log file or database entry list.

### User Flow
*   **Entry:** Tap 'HISTORY'.

### ASCII Wireframe
```text
┌─────────────────────────────────┐
│  /VAR/LOGS/TRANSCRIPTS      [▼] │  <-- Filter Dropdown
├─────────────────────────────────┤
│  > DATE: 2025-12-28             │  <-- Section Header
│ ┌─────────────────────────────┐ │
│ │ 10:00:00 [SESSION_ID_42]    │ │
│ │ DUR: 45m | LOC: WORK        │ │
│ │                             │ │
│ │ > Speaker_1: The budget...  │ │
│ │                             │ │
│ │ [!] ACTION: EMAIL_JOHN      │ │  <-- Blue color for actions
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 09:15:00 [SESSION_ID_41]    │ │
│ │ > Speaker_0: Note to self   │ │
│ └─────────────────────────────┘ │
│                                 │
│  > DATE: 2025-12-27             │
│ ┌─────────────────────────────┐ │
│ │ 16:30:00 [SESSION_ID_40]    │ │
│ │ ...                         │ │
│ └─────────────────────────────┘ │
├─────────────────────────────────┤
│  REC     [HISTORY]  SEARCH      │
└─────────────────────────────────┘
```

### Components
*   **Cards:** Dark grey glass (`#1A1A1A` with blur). Thin green border `1px`.
*   **Action Badges:** Blue text to stand out against the green theme.
*   **Headers:** Terminal style paths `> DIR: TODAY`.

### Interactions
*   **Tap Card:** "Expands" with a slide-down animation like opening a folder.
*   **Scroll:** Kinetic scrolling but with a "snap" feel to items.

---

## 3. Search Screen

### Purpose
Query the database.

### ASCII Wireframe
```text
┌─────────────────────────────────┐
│  QUERY_DATABASE                 │
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ > find "budget meeting"_    │ │  <-- Input looks like command line
│ └─────────────────────────────┘ │
│                                 │
│ [LAST_7_DAYS] [SPEAKER] [TYPE]  │  <-- Rectangular chips
│                                 │
├─────────────────────────────────┤
│  > SEARCH_RESULTS: 3 FOUND      │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 2025-01-12 @ 14:00          │ │
│ │ ...we need to cut the       │ │
│ │ >> BUDGET << by 10%...      │ │  <-- Inverted colors for match
│ └─────────────────────────────┘ │
│                                 │
├─────────────────────────────────┤
│  REC      HISTORY   [SEARCH]    │
└─────────────────────────────────┘
```

### Components
*   **Search Input:** No magnifying glass icon. Just a prompt `>`.
*   **Highlighting:** Instead of bold, use "Inverse Video" style (Black text on Green background) for matched terms.
*   **Loading:** "Scanning sectors..." progress bar.

---

## 4. Settings Screen

### Purpose
System configuration.

### ASCII Wireframe
```text
┌─────────────────────────────────┐
│ < SYSTEM_CONFIG                 │
├─────────────────────────────────┤
│  // SERVER_STATUS               │
│  URL: https://api.myserver      │
│  PING: 45ms [OK]                │
│                                 │
│  // AUDIO_DRIVERS               │
│  INPUT: [ DEFAULT_MIC       ]   │
│  QUALITY: [ HIGH_FIDELITY   ]   │
│  ACTION_DETECT: [ ENABLED ]     │  <-- Toggle looks like [ON]/[OFF]
│                                 │
│  // DATA_MANAGEMENT             │
│  > MANAGE_KEYS                  │
│  > EXPORT_DUMP (.JSON)          │
│  > PURGE_HISTORY                │
│                                 │
│  // ABOUT                       │
│  LIMITLESS_OS v1.0.0            │
└─────────────────────────────────┘
```

### Components
*   **Headers:** Comment style `// HEADER`.
*   **Toggles:** Text-based switches `[ ON ]` / `[ OFF ]` that toggle color (Green/Grey).

---

## 5. Action Notification

### ASCII Wireframe
```text
┌───────────────────────────────────────┐
│  [!] ACTION_REQUIRED                  │
│                                       │
│  DETECTED: REMINDER                   │
│  "Email John about the slides"        │
│                                       │
│  [ REJECT ]        [ >> EXECUTE ]     │
└───────────────────────────────────────┘
```

---

## 6. Onboarding Flow

### ASCII Wireframe (Server Setup)
```text
┌─────────────────────────────────┐
│  INITIAL_SETUP                  │
│                                 │
│  > ESTABLISH_CONNECTION         │
│                                 │
│  SERVER_URL:                    │
│  [ https://...                ] │
│                                 │
│  API_KEY:                       │
│  [ ************************** ] │
│                                 │
│      [ INITIATE_HANDSHAKE ]     │
│                                 │
│  STATUS: WAITING_FOR_INPUT...   │
│                                 │
└─────────────────────────────────┘
```

---

## 7. Design System: "Terminal Modern"

### Design Principles
1.  **Cyber-Minimalism:** If it's not data or a control, delete it.
2.  **The "Glow":** Essential elements should feel like they are emitting light (phosphor).
3.  **Glass & Grid:** Use subtle transparency to layer depth, but align everything strictly to a grid.
4.  **Kinetic Typography:** Text shouldn't just appear; it should type out, slide in, or blink.

### Color Palette
We use a strict high-contrast dark mode palette.

**Base Colors:**
*   `--terminal-bg`: `#050505` (Deepest Black - App Background)
*   `--terminal-surface`: `#0F1110` (Dark Gray - Cards/Modals)
*   `--terminal-glass`: `#0F1110` (85% Opacity) + `Blur(10px)`

**Accents:**
*   `--phosphor-green`: `#00FF41` (Primary - Text, Borders, Active States)
*   `--phosphor-dim`: `#008F11` (Secondary - Inactive elements, subtle lines)
*   `--phosphor-glow`: `box-shadow: 0 0 10px #00FF41` (For active buttons/inputs)

**Functional:**
*   `--system-action`: `#00BFFF` (Deep Sky Blue - Action Items)
*   `--system-alert`: `#FFB000` (Amber - Warnings, Errors)
*   `--system-error`: `#FF3333` (Red - Critical Failures, Delete)
*   `--text-primary`: `#E0E0E0` (Off-white - Main reading text)
*   `--text-muted`: `#666666` (Comments, inactive labels)

### Typography
Mixing Monospace for "System" elements with a clean Sans for "Content".

*   **Primary Font (System/Headers/Data):** `JetBrains Mono` or `Roboto Mono`
    *   *Usage:* Timers, Buttons, Nav, Headers, Code snippets.
*   **Secondary Font (Reading/Content):** `Inter` or `Roboto`
    *   *Usage:* Long transcript bodies (for readability), Settings descriptions.

### Component Library Specs

**1. The "Terminal Button"**
*   Background: Transparent or very low opacity Green (`#00FF4110`).
*   Border: `1px solid var(--phosphor-green)`.
*   Radius: `4dp` (Slightly rounded, modern touch).
*   Text: Uppercase, Monospace, `var(--phosphor-green)`.
*   *Active State:* Background fills solid Green, Text turns Black.

**2. The "Glass Card"**
*   Background: `var(--terminal-glass)`.
*   Border: `1px solid #333` (Subtle) or `1px solid var(--phosphor-dim)` (Active).
*   Radius: `12dp` (Modern feel).
*   Padding: `16dp`.

**3. Loading Indicators**
*   **Boot Bar:** `[██████░░░░] 60%`
*   **Spinner:** ASCII `| / - \` cycling loop.

**4. Icons**
*   Use standard Material Symbols but styled:
    *   Stroke: 1.5px (Thin).
    *   Color: `var(--phosphor-green)`.
    *   No fill.

### Assets & Imagery
*   **Logo:** ASCII art representation of an infinite loop.
*   **Empty States:** ASCII art illustrations (e.g., a tumbleweed or empty box).
