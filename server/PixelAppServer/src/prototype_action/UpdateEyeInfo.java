package prototype_action;

import connect.Database;
import prototype_model.EyeModel;
import prototype_model.ResponseModel;
import prototype_model.UserModel;
import prototype_model_handler.EyeHandler;
import util.GsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class UpdateEyeInfo extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserModel userModel = (UserModel) session.getAttribute("userModel");

        String resultMessage = "";
        if (userModel == null) {
            resultMessage += GsonUtil.serialize(new ResponseModel(201, "not logined"));
        } else {
            String json_eyeModel = request.getParameter("eyeModel");
            EyeModel eyeModel = GsonUtil.deserealize(json_eyeModel, EyeModel.class);
            eyeModel.setUserNum(userModel.getUserNum());

            Database db = null;
            try {
                db = Database.getInstance();
                ResponseModel result = new EyeHandler(db).updateEyeInfo(eyeModel);
                resultMessage += GsonUtil.serialize(result);
            } catch (Exception e) {
                resultMessage += GsonUtil.serialize(new ResponseModel(201, e.getClass().getSimpleName()+"/"+e.getMessage()+"/"+Arrays.toString(e.getStackTrace())));
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }

        PrintWriter out = response.getWriter();
        out.println(resultMessage);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}