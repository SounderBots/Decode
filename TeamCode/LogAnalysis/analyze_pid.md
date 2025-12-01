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
*   **Read the Console Output**: The script prints detailed metrics (RMSE, Overshoot, Settling Time) and specific tuning recommendations.

---

## 1. Interpreting the Plots

The analysis tool generates three synchronized plots to help you visualize performance.

### Plot 1: Velocity Tracking (The "What")
*   **Green Dashed Line**: The **Target Velocity** (what you asked the motor to do).
*   **Blue Line**: **Left Motor** Actual Velocity.
*   **Yellow Line**: **Right Motor** Actual Velocity.
*   **Green Background Zone**: **"Ready to Shoot"**. Both motors are within the 5% tolerance band of the target.
*   **Orange Background Zone**: **"Unstable/Premature"**. The motors are technically in range, but moving too fast (high acceleration). Shooting here is risky.
*   **Red Fill**: **Discrepancy**. The gap between the Left and Right motors. A large red gap means the motors are fighting each other, which causes curved shots.

### Plot 2: Error & Recovery (The "How Good")
*   **Green Band**: The **Acceptable Range** (+/- 5% of Target).
*   **Goal**: Keep the error lines inside this green band.
*   **Spikes**: Large spikes are expected when a ring is fired. The critical metric is **Recovery Time**—how fast does the line snap back into the green band?

### Plot 3: Motor Effort (The "Cost")
*   **Red Dotted Lines**: **Saturation Limits** (+1.0 and -1.0).
*   **Red Background**: Indicates **Saturation** (Power > 95%).
*   **Insight**: If you see a Red Background while the Error is high, your motor is physically maxed out. You need to gear down or reduce target speed.

---

## 2. Interactive Features

*   **On-Demand Tooltip**: Hold **Left Ctrl** and hover over the plots to see exact values (Time, Target, Actuals, Errors) at any point.
*   **Synchronized Zoom**: Zooming in on one plot automatically zooms the others to the same time range.

---

## 3. Primary Metrics

This analysis focuses on two main metrics that provide a complete picture of system performance: **RMSE** (Stability) and **Settling Time** (Responsiveness).

### RMSE (Root Mean Square Error)
**Formula:** $\sqrt{\frac{1}{n}\sum_{i=1}^{n}(Target_i - Actual_i)^2}$

*   **What it Represents:** Consistency.
*   **Goal:** Minimize RMSE. A lower RMSE means the ball lands in the same spot every time.

### Settling Time
**Definition:** The time elapsed from the start of a command until the error stays within a small percentage (typically ±2%) of the target value.

*   **What it Represents:** Speed & Recovery.
*   **Goal:** Minimize Settling Time to improve cycle times (shoot faster).

---

## 4. Synchronization Analysis (Dual Motors)

For dual-motor shooters, synchronization is critical. The script calculates:
*   **Max Discrepancy**: The largest speed difference between the two motors.
*   **Warning Threshold**: If the difference is **> 5%**, the script warns of potential curved shots.

---

## 5. Tuning Guide Summary

| Symptom | Metric to Watch | Tuning Action |
| :--- | :--- | :--- |
| **Inconsistent Shots** | High RMSE / Orange Zones | Check for oscillations. Decrease `kP` or increase `kD`. |
| **Curved Shots** | High Discrepancy (Red Fill) | Check mechanical friction differences or tune motors individually. |
| **Slow Spin-Up** | High Settling Time | Increase `kP` or `kV` (Feedforward). |
| **Overshooting Target** | High Overshoot % | Decrease `kP`. |
| **Not Reaching Target** | Steady-State Error / Red Power Zone | Increase `kV` (Velocity FF) or `kI`. If Power is saturated, reduce target speed. |