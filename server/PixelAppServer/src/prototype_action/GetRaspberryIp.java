package prototype_action;

import connect.Database;
import prototype_model.ResponseModel;
import prototype_model.ValueModel;
import util.GsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;

public class GetRaspberryIp extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Database db = null;
        String resultMessage = "";
        try {
            db = Database.getInstance();
            String ip = (String) db.value("SELECT ip FROM GOD WHERE ip_key=1", "ip");
            resultMessage += GsonUtil.serialize(new ValueModel(200, "success", ip));
        } catch (SQLException e) {
            resultMessage += GsonUtil.serialize(new ResponseModel(201, e.getClass().getSimpleName()+"/"+e.getMessage()+"/"+Arrays.toString(e.getStackTrace())));
        } finally {
            if (db != null) {
                db.close();
            }
        }

        PrintWriter out = response.getWriter();
        out.println(resultMessage);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}