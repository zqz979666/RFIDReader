/**
 * @author Hashira
 * @createdAt 2020.10.28
 * listen the event that antenna state changed
 */
package com.netlab.listener;

import com.impinj.octane.AntennaChangeListener;
import com.impinj.octane.AntennaEvent;
import com.impinj.octane.ImpinjReader;

public class AntennaChangeListenerImplementation implements AntennaChangeListener{

    @Override
    public void onAntennaChanged(ImpinjReader reader, AntennaEvent e) {
        System.out.println("Antenna Status changed, port: " + e.getPortNumber()
                + "state: " + e.getState().toString());
    }
}
