/**
 * @author Hashira
 * @createdAt 2020.10.28
 * used to Initialize impinj reader with certain properties
 */

package com.netlab;

import com.impinj.octane.*;

import java.io.File;
import java.io.IOException;

public class ReaderInit {

    ImpinjReader reader;

    void InitReader() throws Exception {
        try {
            String hostname = "169.254.1.6";
            String propertyPath = hostname + ".xml";

            if(hostname == null) {
                throw new Exception("Please specify the '"+ hostname + "' property");
            }

            reader = new ImpinjReader();

            //连接reader
            System.out.println("trying to connect reader...");
            reader.connect(hostname);
            System.out.println("connect successfully!");

            //获取天线设置
            //若存在当前已保存设置，则读取 / 否则进行datetimeOfToday+"Data/"+dataPath新的设置
            Settings settings;
            File propertyFile = new File(propertyPath);
            if(propertyFile.isFile() && propertyFile.exists()){
                settings = Settings.load(propertyPath);
                System.out.println("load exist settings successfully");
            } else {
                System.out.println("alter the default settings...");
                settings = reader.queryDefaultSettings();

                //更改默认设置
                settings.setRfMode(0);//MaxThroughput
                settings.setSearchMode(SearchMode.DualTarget);
                settings.setSession(1);//设置会话1
                settings.setTagPopulationEstimate(20);//预计只有一个标签

                //设置标签过滤
                TagFilter tf1 = settings.getFilters().getTagFilter1();//获取Filter1
                tf1.setBitPointer(BitPointers.Epc);//以Epc来过滤标签
                tf1.setBitCount(32);//总共比较32bit，这是Epc的位数：32bit/8位16进制
                tf1.setFilterOp(TagFilterOp.Match);//匹配后才执行
                tf1.setTagMask("21160220");//Epc掩码 16进制字符串
                tf1.setMemoryBank(MemoryBank.Epc);
                settings.getFilters().setMode(TagFilterMode.None);//仅Filter1生效

                //设置report格式
                ReportConfig tr = settings.getReport();
                //默认setting下individual Report只包含Epc
//                tr.setIncludeAntennaPortNumber(true);
//                tr.setIncludeChannel(true);
//                tr.setIncludeFirstSeenTime(true);
//                tr.setIncludeLastSeenTime(true);
//                tr.setIncludePeakRssi(true);
//                tr.setIncludeSeenCount(true);
//                tr.setIncludeFastId(true);
//                tr.setIncludePhaseAngle(true);
//                tr.setIncludeDopplerFrequency(true);
//                tr.setIncludePcBits(true);
                tr.setMode(ReportMode.Individual);//单个报告

                //设置天线属性
                AntennaConfigGroup acg = settings.getAntennas();
                acg.disableAll();//首先禁用所有天线
                acg.setIsMaxRxSensitivity(false);//设置为非最大灵敏度
                acg.setIsMaxTxPower(false);//设置为非最大传输功率
                acg.setTxPowerinDbm(30);//设置传输功率为30Dbm

                AntennaConfig ac1 = acg.getAntenna(1);//获取第一个端口的天线Config
                ac1.setEnabled(true);//仅将第一个端口天线置为可用
                ac1.setPortName("Test Antenna");
                ac1.setIsMaxRxSensitivity(true);
                //ac1.setIsMaxTxPower(true);

                //保存设置
                try {
                    settings.save(propertyPath);
                    System.out.println("save settings successfully!");
                } catch(IOException e){
                    System.out.println("failed to save settings!");
                }

            }

            //应用设置
            reader.applySettings(settings);
            System.out.println("apply settings successfully!");


        } catch (OctaneSdkException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            reader.disconnect();
            System.out.println("disconnect from reader...");
        }
    }
}