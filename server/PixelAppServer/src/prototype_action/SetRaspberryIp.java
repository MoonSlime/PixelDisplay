package prototype_action;

import connect.Database;
import prototype_model.ResponseModel;
import util.GsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;

public class SetRaspberryIp extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String ip = request.getParameter("ip");

        Database db = null;
        String resultMessage = "";
        try {
            db = Database.getInstance();
            int affectedRowsnum = db.update(
                    "UPDATE GOD SET ip=? WHERE ip_key=1",
                    ip
            );

            if (affectedRowsnum <= 0) {
                resultMessage += GsonUtil.serialize(new ResponseModel(201, "There is no affected rows num."));
            } else {
                resultMessage += GsonUtil.serialize(new ResponseModel(200, "success"));
            }
        } catch (SQLException e) {
            resultMessage += GsonUtil.serialize(new ResponseModel(202, e.getClass().getSimpleName()+"/"+e.getMessage()+"/"+ Arrays.toString(e.getStackTrace())));
        } finally {
            if (db != null) {
                db.close();
            }
        }

        PrintWriter out = response.getWriter();
        out.println(resultMessage);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}