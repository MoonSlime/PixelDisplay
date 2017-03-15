package prototype_action;

import com.google.gson.reflect.TypeToken;
import prototype_model.ResponseModel;
import util.GsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SaveIntensityProfile extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String json = request.getParameter("intensityProfile");
        ArrayList<Float> intensityProfile = GsonUtil.deserealize(json, new TypeToken<ArrayList<Float>>(){}.getType());
        String now = new SimpleDateFormat("yyMMddHHmmss").format(new Date());

        String resultMessage = "";
        FileOutputStream os = null;
        try {
            File file = new File("/home/hosting_users/pixeldisplay/intensityProfiles/" + now + ".txt");
            file.createNewFile();
            os = new FileOutputStream(file);

            for (int i = 0; i < intensityProfile.size(); i++) {
                os.write(String.format("%d : %.8f\r\n", i, intensityProfile.get(i)).getBytes());
            }

            resultMessage += GsonUtil.serialize(new ResponseModel(200, "success"));
        } catch (Exception e) {
            resultMessage += GsonUtil.serialize(new ResponseModel(201, e.getClass().getSimpleName()+"/"+e.getMessage()+"/"+Arrays.toString(e.getStackTrace())));
        } finally {
            if (os != null) {
                os.close();
            }
        }

        PrintWriter out = response.getWriter();
        out.println(resultMessage);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}