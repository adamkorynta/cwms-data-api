package cwms.radar.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;


public class ApiKeyUserPreparer implements ConnectionPreparer {
    private final String apiKey;

    public ApiKeyUserPreparer(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Connection prepare(Connection conn) {
        if (apiKey != null) {
            try (DSLContext dsl = DSL.using(conn, SQLDialect.ORACLE11G);
                PreparedStatement setUser = conn.prepareStatement("begin CWMS_ENV.set_session_user_apikey(?); end;")
                ) {
                setUser.setString(1,apiKey);
                setUser.execute();
            } catch (Exception e) {
                boolean keyNullOrEmpty = apiKey == null || apiKey.isEmpty();
                throw new DataAccessException("Unable to set user session.  "
                        + "user null or empty = " + keyNullOrEmpty, e);
            }
        }

        return conn;
    }
}
