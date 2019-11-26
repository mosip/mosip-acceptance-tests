package io.mosip.ivv.core.utils;

public class CoreStructures {
    public static class Slot{

        public String date = "";
        public String from = "";
        public String to = "";
        public boolean available = false;

        public Slot(){

        }

        public Slot(String date, String from, String to){
            this.date = date;
            this.from = from;
            this.to = to;
            this.available = true;
        }
    }
}
