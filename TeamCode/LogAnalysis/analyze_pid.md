# Analysis Methodology for PID Tuning

This document explains how to use the `analyze_pid.py` tool and interpret its results to tune your PIDF controllers.

## Quick Start

Run the analysis on your latest log file:
```powershell
python analyze_pid.py
```

Or specify a specific log file:
```powershell
python analyze_pid.py "path/to/log.csv"
```

*   **Hold `Left Ctrl`** to see the interactive tooltip.
*   **Zoom** on one plot to zoom all of them.
*   **Read the Console Output**: The script prints detailed metrics (Loop Frequency, Shot Latency, Stability) and highlights potential issues in red.

---

## 1. Shot Selection & Filtering

If the log file contains many shots (>3) or is very long (>60s), the tool will pause and ask you to select which shots to analyze.

*   **Enter `all`**: Plots the entire log in one continuous timeline.
*   **Enter Indices (e.g., `1, 3-5`)**: Opens **separate windows** for each selected shot (up to 3). This is ideal for comparing specific shots side-by-side without the clutter of the full log.

---

## 2. Interpreting the Plots

The analysis tool generates synchronized plots to help you visualize performance.

### Plot 1: Velocity Tracking (The "What")
*   **Green Dashed Line**: The **Target Velocity** (what you asked the motor to do).
*   **Blue/Yellow Lines**: **Actual Velocity** of your motors (e.g., Left/Right).
*   **Green Vertical Line**: **"Ready Signal"**. When the robot logic believed it was ready to shoot.
*   **Purple Vertical Line**: **"Shoot Command"**. When the trigger was actually pulled.
*   **Red Vertical Line**: **"Actual Shot"**. The detected moment the motor power ramped up to fire the ring.
*   **Orange Background Zone**: **"Unstable/Premature"**. The shot occurred while the motor was accelerating too fast (unstable).
*   **Red Fill**: **Discrepancy**. The gap between the Left and Right motors during the shot. A large red gap means the motors are fighting each other.

### Plot 2: Motor Power (The "Cost")
*(Only appears if power data is available)*
*   **Red Dotted Lines**: **Saturation Limits** (+1.0 and -1.0).
*   **Insight**: If the power hits these lines (saturates) while the velocity is still struggling to reach the target, you need to gear down or reduce target speed.

---

## 3. Interactive Features

*   **On-Demand Tooltip**: Hold **Left Ctrl** and hover over the plots to see exact values (Time, Target, Actuals, Errors) at any point.
*   **Synchronized Zoom**: Zooming in on one plot automatically zooms the others to the same time range.

---

## 4. Primary Metrics

This analysis focuses on metrics critical for shooting consistency:

### Loop Frequency
*   **What it is**: How often the control loop runs (in ms).
*   **Goal**: Consistent low loop times (< 20ms). Spikes > 50ms can cause stuttering.

### Shot Latency
*   **Definition**: The time delay between the code saying `IsShooting = true` and the motor actually reacting (Power Ramp).
*   **Goal**: Minimize latency for responsive firing. High latency might indicate mechanical backlash or code delays.

### Shot Stability
*   **Definition**: Checks if the motor velocity was stable (low acceleration) and within tolerance when the shot occurred.
*   **Status**:
    *   **OK**: Shot taken while stable and ready.
    *   **NOT READY**: Shot taken before error was within tolerance.
    *   **UNSTABLE**: Shot taken while velocity was changing rapidly.

---

## 5. Tuning Guide Summary

| Symptom | Metric to Watch | Tuning Action |
| :--- | :--- | :--- |
| **Inconsistent Shots** | "UNSTABLE" status / Orange Zones | Wait longer before shooting, or tune `kD` to dampen oscillations. |
| **Curved Shots** | High Discrepancy (Red Fill) | Check mechanical friction differences or tune motors individually. |
| **Slow Spin-Up** | High Latency | Increase `kP` or `kV` (Feedforward). |
| **Not Reaching Target** | Steady Error in Tooltip | Increase `kV` (Velocity FF) or `kI`. |
| **Motor Stalling** | Power Plot Saturated (at 1.0) | Reduce target speed or change gear ratio. |
