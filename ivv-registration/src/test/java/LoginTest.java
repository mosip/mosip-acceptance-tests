import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.registration.methods.Login;
import org.junit.Test;

public class LoginTest {

    @Test
    public void test(){
        Login lg = new Login();
        lg.run(new Scenario.Step());
    }
}
