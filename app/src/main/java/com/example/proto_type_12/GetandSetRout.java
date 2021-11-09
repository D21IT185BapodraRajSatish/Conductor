package com.example.proto_type_12;

public class GetandSetRout
{
    private String first_Point,last_Point,stp_Name;
    private int rout_Number;
    private  double kilometre;

    public GetandSetRout()
    {
    }

    public String getFirst_Point() {
        return first_Point;
    }

    public void setFirst_Point(String first_Point) {
        this.first_Point = first_Point;
    }

    public String getLast_Point() {
        return last_Point;
    }

    public void setLast_Point(String last_Point) {
        this.last_Point = last_Point;
    }

    public String getStp_Name() {
        return stp_Name;
    }

    public void setStp_Name(String stp_Name) {
        this.stp_Name = stp_Name;
    }

    public int getRout_Number() {
        return rout_Number;
    }

    public void setRout_Number(int rout_Number) {
        this.rout_Number = rout_Number;
    }

    public double getKilometre() {
        return kilometre;
    }

    public void setKilometre(double kilometre) {
        this.kilometre = kilometre;
    }
}
