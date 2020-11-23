/**
 * @author Hashira
 * @createdAt 2020.11.23
 * Run this file to read more tags in given times
 */

package com.netlab;

import com.impinj.octane.*;
import com.netlab.listener.AntennaChangeListenerImplementation;
import com.netlab.listener.TagOpCompleteListenerImplementation;
import com.netlab.listener.TagReportListenerImplementation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class ReadTagsSpeed {

    public static String dataPath = "readData.txt";//存储数据路径
    static String hostname = "169.254.1.6";//host
    static Writer writer;//文件写入Writer
    public static String dateOfToday;//获取今日日期
    public static String timeNow;//获取现在的时间
    public static ArrayList<String> tagData;//写入文件的已读取的标签信息

    public static HashMap<String, Integer> tagMap;

    public ReadTagsSpeed() {
        //在构造函数中初始化field
        //获取今日日期并储存到字符串中
        LocalDateTime ldt = LocalDateTime.now();
        System.out.println("Current Time is : " + ldt);
        System.out.println("Start processing...");

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter dfTime = DateTimeFormatter.ofPattern("HHmmss");
        dateOfToday = df.format(ldt);
        timeNow = dfTime.format(ldt);

        tagData = new ArrayList<String>();
        tagMap = new HashMap<String, Integer>();


        try {
            File dir0 = new File("Data");
            if(!dir0.exists()){
                dir0.mkdirs();
            }
            File dir = new File("Data/" + dateOfToday + "Data");
            if(!dir.exists()){
                dir.mkdirs();
            }
            File dataFile = new File("Data/" + dateOfToday + "Data/" + timeNow + "-" +dataPath);
            if(!dataFile.exists()){
                if(dataFile.createNewFile()){
                    System.out.println("Create Data File...");
                } else {
                    System.out.println("Create file failed...");
                }
            }
            writer = new FileWriter(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]){

        //初始化
        ReadTagsSpeed rts = new ReadTagsSpeed();
        ReaderInit ri = new ReaderInit();

        try {

            //初始化Reader
            System.out.println("trying to initialize the Reader...");
            ri.InitReader();
            System.out.println("complete initialization!");

            //创建reader实例
            ImpinjReader reader = new ImpinjReader();
            System.out.println("connecting to reader of " + hostname);
            reader.connect(hostname);
            System.out.println("connected!");
            //设置TagReportListener 当有TagReport时在其中处理
            reader.setTagReportListener(new TagReportListenerImplementation());
            reader.setAntennaChangeListener(new AntennaChangeListenerImplementation());
            System.out.println("set TagReportListener successfully!");

            writer.write("Read Time : " + dateOfToday + "\n");
            writer.write("Epc  t   Channel  PeakRSSI PA    SC DopplerFreq" + "\n");
            writer.flush();

            //创建一个新的Op序列
            TagOpSequence seq = new TagOpSequence();
            seq.setOps(new ArrayList<TagOp>());//Op序列
            seq.setExecutionCount((short)2);//执行两次
            seq.setSequenceStopTrigger(SequenceTriggerType.ExecutionCount);
            seq.setState(SequenceState.Active);//启用
            seq.setId(1);//Op序列的Id

            //创建一个新的读操作
            TagReadOp readOp = new TagReadOp();
            readOp.setMemoryBank(MemoryBank.User);
            readOp.setWordCount((short)2);//读取两个word
            readOp.setWordPointer((short)0);//从0开始读取
            seq.getOps().add(readOp);

            //创建一个新的写操作
            TagWriteOp writeOp = new TagWriteOp();
            String myName = "0123abcd";
            writeOp.setMemoryBank(MemoryBank.User);
            writeOp.setWordPointer((short) 0);
            writeOp.setData(TagData.fromByteArray(myName.getBytes()));
            seq.getOps().add(writeOp);

            //设置seq的目标Tag，若未设置则对所有Tag生效
            seq.setTargetTag(new TargetTag());//指定要读取的标签，这里还是指定为21160220
            seq.getTargetTag().setBitPointer(BitPointers.Epc);
            seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
            seq.getTargetTag().setData("21160220");

            //添加操作到序列中
            reader.addOpSequence(seq);
            reader.setTagOpCompleteListener(new TagOpCompleteListenerImplementation());

            //获取当前Instance
            long beginTime = System.currentTimeMillis();

            //查询时间设置为1s，然后每次增加1s
            long invTime = 1000;
            //while(System.currentTimeMillis() - beginTime < 10500) {
            while(invTime <= 10000  ) {
                //启动Reader，开始读取
                reader.start();
                System.out.println("Start inventory...");
                //给reader 20s来读取tag
                Thread.sleep(invTime);

                //停止Reader，停止读取
                reader.stop();
                System.out.println("Stop inventory...time: " + invTime);
                writer.write("inventory time: " + invTime + "\n");
                invTime += 1000;

                //遍历tagData中的数据，按行写入
                for(String td : tagData) {
                    writer.write(td);
                    //统计TagData
                    if(td.indexOf(" ")!= -1) {
                        String epc = td.substring(0, td.indexOf(" "));
                        //若不存在epc，则为第一次探查到
                        if(!tagMap.containsKey(epc)){
                            tagMap.put(epc, 1);
                        } else {
                            //若存在，则count++
                            int count = tagMap.get(epc);
                            count += 1;
                            tagMap.put(epc, count);
                        }
                    }
                }
                System.out.println("Tag data batch saved!");
                //清空tagData，留给下一帧读取的信息使用
                tagData.clear();
                writer.flush();

                //遍历tagMap
                System.out.println("Tags that have been inventoried: ");
                for (String key : tagMap.keySet()){
                    Integer value = tagMap.get(key);
                    System.out.println("Epc: " + key + " inventory count: " + value);
                }

                //休眠5s后进行下一轮查询
                Thread.sleep(5000);
            }

            System.out.println("Read Complete, ready to close connection...");
            reader.disconnect();
            System.out.println("Disconnected.");

            writer.close();
            System.out.println("resource released...");

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
