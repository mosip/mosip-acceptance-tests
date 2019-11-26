package temp;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestAV {
    @Test
    public void main(){
        try {
            System.out.println(InetAddress.getLocalHost().getHostName().toLowerCase());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
