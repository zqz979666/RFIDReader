/**
 * @author Hashira
 * @createdAt 2020.11.24
 * listener of Tagyro
 */
package com.netlab.listener;

import com.impinj.octane.*;
import com.netlab.Tagyro;

import java.text.DecimalFormat;
import java.util.List;

public class TagReportListenerImplementationOfTagyro implements TagReportListener {

    String record="";
    DecimalFormat df;
    long interval=0;
    long initTime = 0;
    boolean isInitTime = false;

    public TagReportListenerImplementationOfTagyro(){
        System.out.println("TagReportListener Initializing...");
        df = new DecimalFormat("#0.0000");
    }

    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();

        //判断Tag中的属性
        for (Tag t : tags) {
            System.out.print(" EPC: " + t.getEpc().toString());

            //输出标签信息到控制台
            {
                if (reader.getName() != null) {
                    System.out.print(" Reader_name: " + reader.getName());
                } else {
                    System.out.print(" Reader_ip: " + reader.getAddress());
                }

                if (t.isAntennaPortNumberPresent()) {
                    System.out.print(" antenna: " + t.getAntennaPortNumber());
                }

                if (t.isFirstSeenTimePresent()) {
                    System.out.print(" first: " + t.getFirstSeenTime().ToString());
                }

                if (t.isLastSeenTimePresent()) {
                    System.out.print(" last: " + t.getLastSeenTime().ToString());
                }

                if (t.isSeenCountPresent()) {
                    System.out.print(" count: " + t.getTagSeenCount());
                }

                if (t.isRfDopplerFrequencyPresent()) {
                    System.out.print(" doppler: " + t.getRfDopplerFrequency());
                }

                if (t.isPeakRssiInDbmPresent()) {
                    System.out.print(" peak_rssi: " + t.getPeakRssiInDbm());
                }

                if (t.isChannelInMhzPresent()) {
                    System.out.print(" chan_MHz: " + t.getChannelInMhz());
                }

                if (t.isRfPhaseAnglePresent()) {
                    System.out.print(" phase angle: " + t.getPhaseAngleInRadians());
                }

                if (t.isFastIdPresent()) {
                    System.out.print("\n     fast_id: " + t.getTid().toHexString());
                }
                System.out.println("");
            }

            //记录初始时间
            synchronized (this) {
                if(!isInitTime) {
                    isInitTime = true;
                    initTime = t.getFirstSeenTime().getLocalDateTime().getTime();
                }
            }
            interval = t.getLastSeenTime().getLocalDateTime().getTime() - initTime;//Δt，单位ms
            //生成记录
            // Id Interval Phase
            record = t.getTid().toHexString() + "  "
                    + interval + " "
                    + df.format(t.getPhaseAngleInRadians()) + " ";
            if(!record.equals("")){
                System.out.println("record to file...");
                //System.out.println(record);
                Tagyro.tagData.add(record + '\n');
                //向全局数组中写入数据
                String tagId = t.getTid().toHexString();
                //若表中没有项 则添加 若有 则更新
                if(!Tagyro.phaseMap.containsKey(tagId)){
                    synchronized (this) {
                        Tagyro.indexMap.put(tagId,Tagyro.index++);
                        System.out.println("ID: "+tagId+" index: " + (Tagyro.index-1));
                    }
                }
                Tagyro.phaseMap.put(tagId,t.getPhaseAngleInRadians());
                //获取本tag在二维数组中的index
                int index = Tagyro.indexMap.get(tagId);
                for(int i = 0 ; i < Tagyro.tagNum ; i++){
                    Tagyro.PDoA[index][i] = t.getPhaseAngleInRadians();
                }
                for(String id: Tagyro.phaseMap.keySet()){
                    int ind = Tagyro.indexMap.get(id);
                    Tagyro.PDoA[ind][index] = Tagyro.phaseMap.get(id) - t.getPhaseAngleInRadians();
                    Tagyro.PDoA[index][ind] = t.getPhaseAngleInRadians() - Tagyro.phaseMap.get(id);
                }

            }

        }
    }

}
