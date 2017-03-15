package nodi_action;

import connect.Database;
import model.ResponseModel;
import model.EyeModel;
import model.UserModel;
import nodi_model_handler.EyeHandler;
import nodi_model_handler.UserHandler;
import util.GsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class UpdateInfo extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserModel userModel = (UserModel) session.getAttribute("userModel");

        String resultMessage = "";
        if (userModel == null) {
            resultMessage += GsonUtil.serialize(new ResponseModel(201, "not logined"));
        } else {
            Integer age = new Integer(request.getParameter("age"));
            Integer sex = new Integer(request.getParameter("sex"));
            String stringEyeModel = request.getParameter("eyeModel");

            userModel.setAge(age);
            userModel.setSex(sex);

            EyeModel eyeModel = GsonUtil.deserealize(stringEyeModel, EyeModel.class);
            eyeModel.setUserNum(userModel.getUserNum());

            Database db = null;
            try {
                db = Database.getInstance();

                UserHandler userHandler = new UserHandler(db);
                EyeHandler eyeHandler = new EyeHandler(db);

                db.transaction_start();
                userHandler.update(userModel);

                ResponseModel result = eyeHandler.insertEye(eyeModel);
                db.transaction_finish();

                resultMessage += GsonUtil.serialize(result);
            } catch (Exception e) {
                resultMessage += GsonUtil.serialize(new ResponseModel(201, e.getMessage()+"\n"+Arrays.toString(e.getStackTrace())));
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