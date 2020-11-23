/**
 * @author Hashira
 * @createdAt 2020.11.16
 * Data struct for antenna position
 */

package com.netlab;


public class Pos {//position类，指示天线的坐标
    public double x;
    public double y;

    public Pos(double x, double y){
        this.x = x;
        this.y = y;
    }
}