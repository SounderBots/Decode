# System Identification & Feedforward Tuning

This document explains how to use the `derive_coefficients.py` tool to mathematically derive the physical Feedforward coefficients (`kS`, `kV`, `kA`) for your motors.

## Quick Start

Run the analysis on your latest log file:
```powershell
python derive_coefficients.py
```

Or specify a specific log file:
```powershell
python derive_coefficients.py "path/to/log.csv"
```

*   **Hold `Left Ctrl`** to see the interactive tooltip.
*   **Read the Console Output**: The script prints the derived coefficients and specific "What + Goal" insights.

---

## 1. The Physics (Feedforward)

We model the motor voltage (Power) required to achieve a motion state using this equation:

$$ V_{applied} = kS \cdot \text{sgn}(v) + kV \cdot v + kA \cdot a $$

*   **kS (Static Friction)**: The power needed just to break friction and start moving.
*   **kV (Velocity Constant)**: The power needed to maintain a specific velocity (overcoming back-EMF and viscous friction).
*   **kA (Acceleration Constant)**: The power needed to accelerate the mass of the mechanism ($F=ma$).

---

## 2. Interpreting the Plots

The tool generates a column of 3 plots for *each* motor found in the log.

### Plot 1: Velocity Profile (The "Test")
*   **Blue Line**: Actual Velocity.
*   **Green Dashed Line**: Target Velocity.
*   **Red Background**: **Saturation Zone**. Indicates where the motor was at max power (>95%).
    *   *Insight*: If velocity lags behind target inside a red zone, it is a **physical limit**, not a tuning issue.

### Plot 2: Feedforward Fit (The "Physics")
*   **Scatter Points**: Raw data points of Power vs. Velocity.
*   **Orange Line**: The mathematical "best fit" line.
    *   **Slope**: Represents `kV`. Steeper slope = more power needed for speed.
    *   **Y-Intercept**: Represents `kS`. Higher intercept = more friction.
*   **Purple Dotted Line**: **Theoretical Max Velocity**.
    *   *Insight*: If your target velocity is to the right of this line, the motor physically cannot reach it.

### Plot 3: Model Validation (The "Proof")
*   **Blue Line**: Actual Power used by the motor.
*   **Red Dashed Line**: Predicted Power (calculated using the derived `kS`, `kV`, `kA`).
*   **Green Fill**: **Good Fit**. The model predicts the power accurately.
*   **Red Fill**: **Poor Fit**. The model failed to predict the power.
    *   *Insight*: Large red zones during acceleration suggest `kA` is inaccurate or the mechanism has non-linear friction.

---

## 3. Actionable Insights ("What + Goal")

The script analyzes the coefficients and fit quality to give you specific goals.

| Insight | Meaning | Goal |
| :--- | :--- | :--- |
| **Poor Model Fit ($R^2 < 0.6$)** | The data is too noisy or doesn't cover enough range. | **Run a Ramp Test**: Slowly accelerate from 0 to Max Power over 3-5 seconds. |
| **Negative kS** | The math suggests "negative friction" (impossible). | **Check Reversal**: Your motor direction might be reversed relative to the encoder. |
| **Motor Saturation** | Predicted power at max speed > 1.0. | **Reduce Speed**: Your target velocity is physically impossible. |
| **Low Headroom** | Predicted power > 0.9. | **Caution**: You have very little power left for PID to correct errors. |

---

## 4. How to Run a "Ramp Test"

To get the best data for this tool:
1.  Create an OpMode that sets motor power directly (Open Loop).
2.  Linearly ramp the power from **0.0 to 1.0** over **3 to 5 seconds**.
3.  (Optional) Ramp back down to 0.
4.  Log `Timestamp`, `ActualTPS`, and `MotorPower`.
