import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Statement;


/**
 * Created by Claudius on 2017/6/9.
 */
public class FindComentTest {
    private  FindComent findComent;

    @Before
    public void init(){
        findComent = new FindComent();
    }

    @Test
    public void getConnection() throws Exception {
        Connection connection =  findComent.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("insert into comment  VALUES  (2,'test')");
    }

}