package com.steps.targe;

class TrustElement {
    String number,name;
    float frequency, intimacy, recency, trust=0f;

    TrustElement(String n,String n2, float f, float i, float r, float t) {
        this.number = n;
        this.name=n2;
        this.frequency = f;
        this.intimacy = i;
        this.recency = r;
        this.trust = t;
    }
    //returns in csv format number,name,frequency,intimacy,recency,trust
    public String getString()
    {
        //the sequence should NEVER EVER be changed
        return this.number+","+this.name+","+this.frequency+","+this.intimacy+","+this.recency+","+this.trust+"\n";
    }
}