/**
 * @author Hashira
 * @createdAt 2020.11.16
 * listen the event that Reader Stop inventory
 */

package com.netlab.listener;
import com.impinj.octane.*;

import java.awt.*;

public class ReaderStopListenerImplementation implements ReaderStopListener {

    @Override
    public void onReaderStop(ImpinjReader reader, ReaderStopEvent e) {
        Toolkit.getDefaultToolkit().beep();
        System.out.println("Stop inventory!");
    }
}
