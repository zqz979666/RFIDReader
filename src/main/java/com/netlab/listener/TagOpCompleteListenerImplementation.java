/**
 * @author Hashira
 * @createdAt 2020.10.28
 * listen the event that Tag Op complete
 */
package com.netlab.listener;

import com.impinj.octane.*;

public class TagOpCompleteListenerImplementation implements TagOpCompleteListener{


    @Override
    public void onTagOpComplete(ImpinjReader reader, TagOpReport results) {
        System.out.println("Tag Operation Complete!");
        for(TagOpResult r : results.getResults()) {
            System.out.print("  EPC: " + r.getTag().getEpc().toHexString());
            if(r instanceof TagReadOpResult) {
                TagReadOpResult rr = (TagReadOpResult) r;
                System.out.print(" READ: id: " + rr.getOpId());
                System.out.print(" sequence: " + rr.getSequenceId());
                System.out.print(" result: " + rr.getResult().toString());
                if (rr.getResult() == ReadResultStatus.Success) {
                    System.out.print(" data:" + rr.getData().toHexWordString());
                } else {
                    System.out.print(" data: read failed or no data!");
                }
            }

            if(r instanceof TagWriteOpResult) {
                TagWriteOpResult wr = (TagWriteOpResult) r;
                System.out.print(" WRITE: id: " + wr.getOpId());
                System.out.print(" sequence: " + wr.getSequenceId());
                System.out.print(" result: " + wr.getResult().toString());
                System.out.print(" word_written: " + wr.getNumWordsWritten());
            }

            System.out.println("");
        }
    }
}
