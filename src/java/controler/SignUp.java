/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.User_Status;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.annotation.MultipartConfig;
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
    fileSizeThreshold = 1024 * 1024,  // 1 MB
    maxFileSize = 1024 * 1024 * 15,   // 15 MB
    maxRequestSize = 1024 * 1024 * 20 // 20 MB
)
@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
//        JsonObject requestJson = gson.fromJson(request.getReader(), JsonObject.class);
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        String mobile = request.getParameter("mobile");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String password = request.getParameter("password");
        Part avertarImage = request.getPart("avertarImage");

        if (mobile.isEmpty()) {
            //mobile Number is Empty 
            responseJson.addProperty("message", "Please Add Your Mobile");
        } else if (!Validation.isMobileValid(mobile)) {
            //mobile Number is Not Valid
            responseJson.addProperty("message", "Please Add Valid Mobile Number");
        } else if (firstName.isEmpty()) {
            //First Name is empty
            responseJson.addProperty("message", "Please Add Your First Name");
        } else if (lastName.isEmpty()) {
            //Last Name is empty
            responseJson.addProperty("message", "Please Add Your Last Name");
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

            if (!criteria1.list().isEmpty()) {
//                mobile number is alrady used
                   responseJson.addProperty("message", "Mobile number is alrady used");
            } else {
//                 Mobile number is not use
                User user = new User();
                user.setFirst_name(firstName);
                user.setLast_name(lastName);
                user.setMobile(mobile);
                user.setPassword(password);
                user.setRegisterd_date_time(new Date());

                //Get User Status2 = Offilne
                User_Status user_Status = (User_Status) session.get(User_Status.class, 2);
                user.setUser_Status(user_Status);

                session.save(user);
                session.beginTransaction().commit();

                //check uploaded image
                if (avertarImage != null) {
                    //image selected
                    String serverPath = request.getServletContext().getRealPath("");
                    String aveatarImagePath = serverPath + File.separator + "AvatarImage" + File.separator + mobile + ".png";
                    File file = new File(aveatarImagePath);
                    Files.copy(avertarImage.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

                }

                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Registration Complete");
            }

            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));

    }
    
}
