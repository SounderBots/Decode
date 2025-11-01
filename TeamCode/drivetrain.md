Pedro pathing readme
===

Use
---

from command factory,
use startMove for first move (move from 0, 0, could be changed), following moves, use move to. 

Coordinate
---
![Coordinate](https://pedropathing.com/docs/fieldcoordinates-dark.png)

Note: robot is facing **RIGHT** at *bottom left* corner

move forward -> +x

move left -> +y

turn anticlockwise (look down) -> degree increase

How to tune
---

1. Select Tuning in teleop menu
2. click on init
3. there will be a menu in telemetry
4. use driver controller d pad to select item, use right shoulder button to select menu/item
5. after selected on item, start the action
6. Panels could be accessed by visiting http://192.168.43.1:8001
7. in case robot disconnected/rebooted itself, reconnect to robot wifi, then run following to reconnect

```bash
adb kill-server
adb start-server
adb connect 192.168.43.1:5555
```

to verify connect succeed or not

```
adb devices
```

or oneliner

```bash
adb kill-server&&adb start-server&&sleep 3&&adb connect 192.168.43.1:5555&&adb devices
```

Tuning steps
---

File to change running steps: org/firstinspires/ftc/teamcode/pedroPathing/Constants.java

1. Four wheels are: "FL", "FR", "BL", "BR". the odometry name is "pinpoint", those names are hardcoded in driveConstants and localizerConstants
2. Make sure the odometry direction are right [reference](https://pedropathing.com/docs/pathing/tuning/localization/pinpoint). 

    2.1 Use Tuning/Manual/Translational Tuner

    2.2 push robot forward, make sure x increase

    2.3 push robot left, make sure y increase

    2.4 if not correct, change driveConstants

3. Follow [Instruction](https://pedropathing.com/docs/pathing/tuning/automatic) to set xVelocity, yVelocity, forwardZeroPowerAcceleration and lateralZeroPowerAcceleration
4. Translational PIDF (changes should save to followerConstants) [Instruction](https://pedropathing.com/docs/pathing/tuning/pids/translational)
   
    4.1 set p, i, d, f in pannel to 0, and increase f until robot about to move

    4.2 set p, push the robot sideways until robot could correct to original position with min overshoot (typical value: 0.x)

    4.3 set d, push the robot sideways until overshoot is acceptable

5. Heading PIDF (changes should save to followerConstants) [Instruction](https://pedropathing.com/docs/pathing/tuning/pids/heading)
   
    5.1 set p, i, d, f in pannel to 0, and increase f until robot about to move

    5.2 set p, turn the robot until robot could correct to original heading with min overshoot (typical value: 0.x ~ 1.x)

    5.3 set d, turn the robot until overshoot is acceptable

6. Driving PIDF (changes should save to followerConstants) [Instruction](https://pedropathing.com/docs/pathing/tuning/pids/drive)

7. Centripetal Scaling (changes should save to followerConstants) [Instruction](https://pedropathing.com/docs/pathing/tuning/pids/centripetal)
    
    7.1 if turn too much, decrease

    7.2 if turn too less, increase
