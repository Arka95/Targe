package com.steps.targe;

/**
 * Created by Arka Bhowmik on 7/16/2016.
 */
 class Person {
    private String name, number;
    private Integer callin, callout;
    private Integer durin, durout;
    private Long lastcallstamp;
    private double trust;

    public Person(String number, String name, Double trust,Long lcs) {
        this.name = name;
        this.number = number;
        this.trust = trust;
        this.callin = this.callout = 0;
        this.durin = this.durout = 0;
        this.lastcallstamp=lcs;
    }
    public void display (){
        System.out.println(number+" "+callin+" "+callout+" "+durin+" "+durout+" "+lastcallstamp);
    }
    public Long getLastcallstamp(){return lastcallstamp; }

    public void setLastCallStamp(long c){lastcallstamp=c;}

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public Integer getCin() {
        return callin;
    }

    public Integer getCout() {
        return callout;
    }

    public Integer getDin() {
        return durin;
    }

    public Integer getDout() {
        return durout;
    }

    public Double getTrust(){return trust;}

    public void setName(String nam) {
        name=nam;
    }

    public void setNumber(String num) {
        number=num;
    }

    public void setCin(Integer cin) {
        callin=cin;
    }

    public void setCout(Integer cout) {
        callout=cout;
    }

    public void setDin(Integer din) {
        durin=din;
    }

    public void setDout(Integer dot) {
        durout=dot;
    }

    public void setTrust(double t){trust=t;}

}