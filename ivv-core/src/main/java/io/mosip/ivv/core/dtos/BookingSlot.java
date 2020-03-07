package io.mosip.ivv.core.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingSlot {
    private String date = "";
    private String from = "";
    private String to = "";
    private boolean available = false;

    public BookingSlot(){

    }

    public BookingSlot(String date, String from, String to){
        this.date = date;
        this.from = from;
        this.to = to;
        this.available = true;
    }
}
