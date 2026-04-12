package cn.example.flink.demo.function;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.types.Row;
import java.sql.*;

public class RichDatabaseSourceFunction extends RichSourceFunction<Row> {

    private volatile boolean isRunning = true;
    private transient Connection connection = null;

    public void open(Configuration parameters) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3316/ai_demo", "root", "root");
    }

    @Override
    public void run(SourceContext<Row> ctx) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("select order_number, user_name from `order`");
            rs = ps.executeQuery();
            while (rs.next() && isRunning) {
                String orderNo = rs.getString("order_number");
                String userName = rs.getString("user_name");
                ctx.collect(Row.of(String.format("%s：%s", userName, orderNo)));
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

}
