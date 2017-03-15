package prototype_action;

import connect.Database;
import prototype_model.ResponseModel;
import prototype_model.UserModel;
import prototype_model_handler.UserHandler;
import util.GsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class Join extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String json_userModel = request.getParameter("userModel");
        UserModel userModel = GsonUtil.deserealize(json_userModel, UserModel.class);

        String resultMessage = "";
        Database db = null;
        try {
            db = Database.getInstance();
            ResponseModel result = new UserHandler(db).join(userModel);
            resultMessage += GsonUtil.serialize(result);
        } catch (Exception e) {
            resultMessage += GsonUtil.serialize(new ResponseModel(201, e.getClass().getSimpleName()+"/"+e.getMessage()+"/"+Arrays.toString(e.getStackTrace())));
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