/**
 * @author Hashira
 * @createdAt 2020.10.28
 * listen the event that reader send tag report
 */
package com.netlab.listener;

import com.impinj.octane.*;
import com.netlab.Pos;
import com.netlab.ReadTags;

import java.text.DecimalFormat;
import java.util.List;

public class TagReportListenerImplementation implements TagReportListener {

    String record="";
    DecimalFormat df;
    long interval=0;
    long initTime = 0;
    boolean isInitTime = false;

    public TagReportListenerImplementation(){
        System.out.println("TagReportListener Initializing...");
        df = new DecimalFormat("#0.0000");
    }

    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();

        //判断Tag中的属性
        for (Tag t : tags) {
            System.out.print(" EPC: " + t.getEpc().toString());

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

                System.out.print(" model: " +
                        t.getModelDetails().getModelName());

                System.out.print(" epcsize: " +
                        t.getModelDetails().getEpcSizeBits());

                System.out.print(" usermemsize: " +
                        t.getModelDetails().getUserMemorySizeBits());
            }
            System.out.println("");

            synchronized (this) {
                if(!isInitTime) {
                    isInitTime = true;
                    initTime = t.getFirstSeenTime().getLocalDateTime().getTime();
                }
            }
            interval = t.getLastSeenTime().getLocalDateTime().getTime() - initTime;//Δt，单位ms
            //生成记录
            // Epc Interval RSSI peakRSSI Phase TagSeenCount RfDopplerFrequency
            record = t.getEpc().toHexString() + "  "
                    + interval + " "
                    + df.format(t.getChannelInMhz()) + " "
                    + df.format(t.getPeakRssiInDbm()) + " "
                    + df.format(t.getPhaseAngleInRadians()) + " "
                    + t.getTagSeenCount() + " "
                    + df.format(t.getRfDopplerFrequency());
            if(!record.equals("")){
                System.out.println("record to file...");
                //System.out.println(record);
                ReadTags.tagData.add(record + '\n');
            }

            //若为天线1的数据
            //设定N为100，即天线每秒读30次左右，读10s
            if(t.getAntennaPortNumber()==1 && ReadTags.antenna1Time.size()<300){
                ReadTags.antenna1Time.add((double)(interval + initTime)/1000);//时间数组，单位s
                ReadTags.antenna1Phase.add(t.getPhaseAngleInRadians());//相位数组
                double v = 0.05;//给定速度，单位m/s
                double y = 0.7;//y轴坐标固定 , (0, 70cm)
                double x = -1 * v * interval;//两个天线的初始x都为0
                ReadTags.antenna1Pos.add(new Pos(x, y));//添加位置，元素为坐标
            }


            //若为天线2的数据
            if(t.getAntennaPortNumber()==2 && ReadTags.antenna2Time.size()<300){
                ReadTags.antenna2Time.add((double)(interval + initTime)/1000);
                ReadTags.antenna2Phase.add(t.getPhaseAngleInRadians());//相位数组
                double v = 0.05;//给定速度，单位m/s
                double y = -0.8;//y轴坐标固定 , (0, -80cm)
                double x = -1 * v * interval;
                ReadTags.antenna2Pos.add(new Pos(x, y));//添加位置，元素为坐标
            }

        }
    }
}
