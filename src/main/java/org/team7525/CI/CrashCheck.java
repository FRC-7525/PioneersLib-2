package org.team7525.CI;

import java.util.concurrent.atomic.AtomicReference;
import org.wpilib.framework.IterativeRobotBase;
import org.wpilib.hardware.hal.DriverStationJNI;
import org.wpilib.hardware.hal.HAL;
import org.wpilib.hardware.hal.RobotMode;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.simulation.DriverStationSim;

/**
 * This class is used to check if the robot code will crash when running in different modes. It will
 * run the robot code in each mode for a set amount of time and check if it crashes. If it does, it
 * will print out the stack trace and exit the program.
 */
public class CrashCheck extends IterativeRobotBase {

    private static IterativeRobotBase robot;

    // AtomicReference is used to ensure that the state is updated atomically, preventing race
    // conditions in a multi-threaded environment.
    private final AtomicReference<CrashCheckStates> currentState =
            new AtomicReference<>(CrashCheckStates.DISABLED);
    private final AtomicReference<CrashCheckStates> lastState =
            new AtomicReference<>(CrashCheckStates.UTILITY);

    private long startTime = 0;
    private boolean
            m_robotMainOverridden; // "Holy I hate this notation so much" - OTTO THE BENEVOLENT
    private boolean lastStateSet = false;

    private enum CrashCheckStates {
        DISABLED(
                "DISABLED",
                () -> {
                    getRobot().robotPeriodic();
                    getRobot().disabledPeriodic();
                },
                () -> {
                    getRobot().disabledInit();
                },
                () -> {
                    getRobot().disabledExit();
                }),
        TELEOP(
                "TELEOP",
                () -> {
                    getRobot().robotPeriodic();
                    getRobot().teleopPeriodic();
                },
                () -> {
                    getRobot().teleopInit();
                },
                () -> {
                    getRobot().teleopExit();
                }),
        AUTONOMOUS(
                "AUTONOMOUS",
                () -> {
                    getRobot().robotPeriodic();
                    getRobot().autonomousPeriodic();
                },
                () -> {
                    getRobot().autonomousInit();
                },
                () -> {
                    getRobot().autonomousExit();
                }),
        UTILITY(
                "UTILITY",
                () -> {
                    getRobot().robotPeriodic();
                    getRobot().utilityPeriodic();
                },
                () -> {
                    getRobot().utilityInit();
                },
                () -> {
                    getRobot().utilityExit();
                });

        private String state;
        private Runnable periodic;
        private Runnable init;
        private Runnable exit;

        CrashCheckStates(String state, Runnable periodic, Runnable init, Runnable exit) {
            this.state = state;
            this.periodic = periodic;
            this.init = init;
            this.exit = exit;
        }

        public String getStateString() {
            return state;
        }

        public void periodic() {
            periodic.run();
        }

        public void init() {
            init.run();
        }

        public void exit() {
            exit.run();
        }
    }

    public CrashCheck(IterativeRobotBase robot) {
        super(0.02);
        CrashCheck.robot = robot;
        HAL.initialize();

        DriverStationSim.setEnabled(false);
        DriverStationSim.setRobotMode(RobotMode.TELEOPERATED);

        Thread.setDefaultUncaughtExceptionHandler(
                (thread, throwable) -> {
                    System.exit(1); // Exit the program if an uncaught exception occurs
                });
    }

    public static IterativeRobotBase getRobot() {
        return robot;
    }

    private void updateState() {
        if (lastState.get() != currentState.get() && lastStateSet) {
            if (lastState != null) {
                lastState.get().exit();
                System.out.println(lastState.get().getStateString() + " ran propperly.");
            }
            currentState.get().init();
        }
        lastState.set(currentState.get());
        lastStateSet = true;
        currentState.get().periodic();
    }

    private void setMode(CrashCheckStates newState) {
        if (currentState.get() != newState) {
            currentState.set(newState);
            switch (newState) {
                case DISABLED:
                    DriverStationSim.setEnabled(false);
                    break;
                case TELEOP:
                    DriverStationSim.setEnabled(true);
                    DriverStationSim.setRobotMode(RobotMode.TELEOPERATED);
                    break;
                case AUTONOMOUS:
                    DriverStationSim.setEnabled(true);
                    DriverStationSim.setRobotMode(RobotMode.AUTONOMOUS);
                    break;
                case UTILITY:
                    DriverStationSim.setEnabled(true);
                    DriverStationSim.setRobotMode(RobotMode.UTILITY);
                    break;
            }
        }
    }

    @Override
    public void startCompetition() {
        System.out.println("********** Robot program startup complete **********");
        // Accesses JVM runtime to get the start time in nanoseconds.
        // "This is a massive skill issue ngl" - OTTO THE BENEVOLENT
        startTime = System.nanoTime();

        while (!m_robotMainOverridden) {
            double testTime =
                    (System.nanoTime() - startTime)
                            / 1e9; // Convert nanoseconds to seconds. TODO: THIS WILL BREAK IN @)@&
            // DUE TO NO MORE NANOSECONDS IN JAVA 21

            DriverStationSim.notifyNewData();

            // State Transitions
            if (testTime < 5.0) {
                setMode(CrashCheckStates.DISABLED);
            } else if (testTime < 10.0) {
                setMode(CrashCheckStates.TELEOP);
            } else if (testTime < 15.0) {
                setMode(CrashCheckStates.AUTONOMOUS);
            } else if (testTime < 20.0) {
                setMode(CrashCheckStates.UTILITY);
            } else {
                System.out.println(currentState.get().getStateString() + " ran propperly.");
                System.out.println("Test completed after " + testTime + " seconds");
                break;
            }

            updateState();

            if (currentState.get() == CrashCheckStates.DISABLED) {
                DriverStationJNI.observeUserProgram(DriverStationJNI.nativeGetControlWord());
            } else if (currentState.get() == CrashCheckStates.TELEOP) {
                DriverStationJNI.observeUserProgram(DriverStationJNI.nativeGetControlWord());
            } else if (currentState.get() == CrashCheckStates.AUTONOMOUS) {
                DriverStationJNI.observeUserProgram(DriverStationJNI.nativeGetControlWord());
            } else if (currentState.get() == CrashCheckStates.UTILITY) {
                DriverStationJNI.observeUserProgram(DriverStationJNI.nativeGetControlWord());
            }

            NetworkTableInstance.getDefault().flushLocal();

            // This the periodic thingy or sum or other
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("********** Competition Ended **********");
    }

    @Override
    public void endCompetition() {
        m_robotMainOverridden = true;
    }
}
