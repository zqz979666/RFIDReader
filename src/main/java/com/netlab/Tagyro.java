/**
 * @author Hashira
 * @createdAt 2020.11.23
 * Coarse Implementation of Tagyro
 */

package com.netlab;

import com.impinj.octane.*;
import com.netlab.listener.AntennaChangeListenerImplementation;
import com.netlab.listener.TagOpCompleteListenerImplementation;
import com.netlab.listener.TagReportListenerImplementation;
import com.netlab.listener.TagReportListenerImplementationOfTagyro;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class Tagyro {

    public static String dataPath = "readData.txt";//存储数据路径
    static String hostname = "169.254.1.6";//host
    static Writer writer;//文件写入Writer
    public static String dateOfToday;//获取今日日期
    public static String timeNow;//获取现在的时间
    public static ArrayList<String> tagData;//写入文件的已读取的标签信息

    public static HashMap<String, Integer> tagMap;


    public Tagyro() {
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
        Tagyro rt = new Tagyro();
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
            reader.setTagReportListener(new TagReportListenerImplementationOfTagyro());
            reader.setAntennaChangeListener(new AntennaChangeListenerImplementation());
            System.out.println("set TagReportListener successfully!");

            writer.write("Read Time : " + dateOfToday + "\n");

            reader.start();
            System.out.println("Start inventory...");
            //跳频间隔约为300ms
            Thread.sleep(500);


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