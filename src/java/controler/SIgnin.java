/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import model.Validation;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author user
 */

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1 MB
        maxFileSize = 1024 * 1024 * 5, // 5 MB
        maxRequestSize = 1024 * 1024 * 10 // 10 MB
)

@WebServlet(name = "SIgnin", urlPatterns = {"/SIgnin"})
public class SIgnin extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        JsonObject requestJson = gson.fromJson(request.getReader(), JsonObject.class);
        String mobile = requestJson.get("mobile").getAsString();
        String password = requestJson.get("password").getAsString();

        if (mobile.isEmpty()) {
            //mobile Number is Empty 
            responseJson.addProperty("message", "Please Add Your Mobile");
        } else if (!Validation.isMobileValid(mobile)) {
            //mobile Number is Not Valid
            responseJson.addProperty("message", "Please Add Valid Mobile Number");
        } else if (password.isEmpty()) {
            //Password Name is empty
            responseJson.addProperty("message", "Please Add Your Password");
        } else if (!Validation.isPasswordValid(password)) {
            //password not valid 
            responseJson.addProperty("message", "Password must contain at least one"
                    + " lowercase letter, one uppercase letter, one digit, "
                    + "one special character (@#$%^&+=), and be at least 8 "
                    + "characters long.");
        } else {

            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("mobile", mobile));
            criteria1.add(Restrictions.eq("password", password));

            if (!criteria1.list().isEmpty()) {
//                mobile found 
                User user = (User) criteria1.uniqueResult();

                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Signin Success");
                responseJson.add("user", gson.toJsonTree(user));
            } else {
//              user not found

                responseJson.addProperty("message", "Invalid Credentials!");

            }

            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }

}
