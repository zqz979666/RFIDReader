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
import java.lang.reflect.Array;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Tagyro {

    public static String dataPath = "readData.txt";//存储数据路径
    static String hostname = "169.254.1.4"; //host
    static Writer writer;//文件写入Writer
    public static String dateOfToday;//获取今日日期
    public static String timeNow;//获取现在的时间
    public static ArrayList<String> tagData;//写入文件的已读取的标签信息
    final static double waveLength = 30000.0/920.625;

    //标签阵列中标签的数量
    public static final int tagNum = 4;
    public static int index = 0;
    //相位哈希表
    public static HashMap<String, Double> phaseMap;
    //PDoA数组
    public static Double[][] PDoA;
    //PDoA最大值
    public static Double[][] PDoAMax;
    //PDoa最小值
    public static Double[][] PDoAMin;
    //相位补偿
    public static Double[][] unwarp;
    //Index Map
    public static HashMap<String, Integer> indexMap;

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
        phaseMap = new HashMap<String, Double>();
        indexMap = new HashMap<String, Integer>();
        PDoA = new Double[tagNum][tagNum];
        PDoAMax = new Double[tagNum][tagNum];
        PDoAMin = new Double[tagNum][tagNum];
        unwarp = new Double[tagNum][tagNum];

        initArrays();


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

    private void initArrays() {
        //初始化数组们
        for(int i = 0 ; i < tagNum ; i++){
            for(int j = 0 ; j < tagNum ; j++){
                PDoA[i][j] = (double)0;
                PDoAMax[i][j] = (double)-10;
                PDoAMin[i][j] = (double)10;
                unwarp[i][j] = (double)0;
            }
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

            //360°旋转 以2°为步长 总共180组数据
            for(int i = 0 ; i < 180 ; i++){
                //记录上一次PDoA矩阵
                Double[][] lastPDoA= new Double[tagNum][tagNum];
                for(int i1 = 0 ; i1 < tagNum ; i1++){
                    for(int j1 = 0 ; j1 < tagNum ; j1++){
                        lastPDoA[i1][j1] = PDoA[i1][j1];
                    }
                }
                //开始查询标签
                reader.start();
                System.out.println("Start inventory...");
                Thread.sleep(4000);//等待3s 查询
                reader.stop();

                //unwarp
                for(int i1 = 0 ; i1 < tagNum ; i1++){
                    for(int j1 = 0 ; j1 < tagNum ; j1++){
                        if(PDoA[i1][j1]-lastPDoA[i1][j1]>Math.PI && lastPDoA[i1][j1]!=0){
                            PDoA[i1][j1] -= Math.PI;
                        }
                        if (PDoA[i1][j1]-lastPDoA[i1][j1]< -1 * Math.PI && lastPDoA[i1][j1]!=0){
                            PDoA[i1][j1] += Math.PI;
                        }
                        //update max and min of PDoA
                        if(PDoA[i1][j1]>PDoAMax[i1][j1]){
                            PDoAMax[i1][j1] = PDoA[i1][j1];
                            PDoAMin[j1][i1] = PDoA[j1][i1];
                        }
                        if(PDoA[i1][j1]<PDoAMin[i1][j1]){
                            PDoAMin[i1][j1] = PDoA[i1][j1];
                            PDoAMax[j1][i1] = PDoA[j1][i1];
                        }
                    }
                }
                System.out.println("group" + i);
                writer.write("max PDoA: \n");
                for(int i1 = 0 ; i1 < tagNum ; i1++){
                    writer.write("[");
                    for(int j1 = 0 ; j1 < tagNum ; j1++){
                        writer.write(PDoAMax[i1][j1] + ",");
                    }
                    writer.write("],\n");
                }
                writer.write("min PDoA: \n");
                for(int i1 = 0 ; i1 < tagNum ; i1++){
                    writer.write("[");
                    for(int j1 = 0 ; j1 < tagNum ; j1++){
                        writer.write(PDoAMin[i1][j1] + ",");
                    }
                    writer.write("],\n");
                }
                writer.write("PDoA: \n");
                for(int i1 = 0 ; i1 < tagNum ; i1++){
                    writer.write("[");
                    for(int j1 = 0 ; j1 < tagNum ; j1++){
                        writer.write(PDoA[i1][j1] + ",");
                    }
                    writer.write("],\n");
                }
                writer.write("effective distance: \n");
                for(int i1 = 0 ; i1 < tagNum ; i1++){
                    writer.write("[");
                    for(int j1 = 0 ; j1 < tagNum ; j1++){
                        double ed = (PDoAMax[i1][j1]-PDoAMin[i1][j1])* waveLength / (8*Math.PI);
                        writer.write(PDoAMax[i1][j1] + ",");
                    }
                    writer.write("],\n");
                }
                writer.flush();
                System.out.println("Press Enter to continue...");
                Scanner s = new Scanner(System.in);
                s.nextLine();
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