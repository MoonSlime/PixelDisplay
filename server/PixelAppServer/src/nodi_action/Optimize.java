package nodi_action;

import connect.Database;
import model.EyeModel;
import model.ResponseModel;
import model.UserModel;
import nodi_model_handler.EyeHandler;
import util.GsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class Optimize extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserModel userModel = (UserModel) session.getAttribute("userModel");

        String resultMessage = "";
        if (userModel == null) {
            resultMessage += GsonUtil.serialize(new ResponseModel(201, "not logined"));
        } else {
            Database db = null;
            try {
                db = Database.getInstance();
                EyeHandler eyeHandler = new EyeHandler(db);

                EyeModel lastEyeModel = (EyeModel) eyeHandler.getLastEyeInfo(userModel);
                lastEyeModel.setUserNum(userModel.getUserNum());
                lastEyeModel.setOptimized(true);

                ResponseModel result = eyeHandler.optimize(lastEyeModel);
                resultMessage += GsonUtil.serialize(result);
            } catch (Exception e) {
                resultMessage += GsonUtil.serialize(new ResponseModel(201, e.getMessage()+"\n"+ Arrays.toString(e.getStackTrace())));
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
