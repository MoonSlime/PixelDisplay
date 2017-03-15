package nodi_action;

import model.ResponseModel;
import model.UserModel;
import util.GsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class Logout extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserModel userModel = (UserModel) session.getAttribute("userModel");

        String resultMessage = "";
        if (userModel == null) {
            resultMessage += GsonUtil.serialize(new ResponseModel(201, "not logined"));
        } else {
            session.removeAttribute("userModel");
            session.invalidate();
            resultMessage += GsonUtil.serialize(new ResponseModel(200, "success"));
        }

        PrintWriter out = response.getWriter();
        out.println(resultMessage);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}