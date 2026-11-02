package org.team7525.subsystem.genericRoller;

import org.team7525.subsystem.SubsystemStates;

public enum RollerSystemStates implements SubsystemStates {
    BRAKE,
    COAST,
    VOLTAGE_CONTROL,
    CLOSED_LOOP
}
