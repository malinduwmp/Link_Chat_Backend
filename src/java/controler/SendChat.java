/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.Chat_Status;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;

/** 
*
 * @author user
 */
@WebServlet(name = "SendChat", urlPatterns = {"/SendChat"})
public class SendChat extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject reponseJson = new JsonObject();
        reponseJson.addProperty("success", false);

        ///SndChat?logged_user_id=1&other_user_id=3&message=hello
        Session session = HibernateUtil.getSessionFactory().openSession();

        //get Parameter 
        String logged_user_id = request.getParameter("logged_user_id");
        String other_user_id = request.getParameter("other_user_id");
        String message = request.getParameter("message");

        //get user 
        User logged_user = (User) session.get(User.class, Integer.parseInt(logged_user_id));

        //get other user
        User other_User = (User) session.get(User.class, Integer.parseInt(other_user_id));

        //save chat 
        Chat chat = new Chat();

        //chat status = 2 : unseen
        Chat_Status chat_Status = (Chat_Status) session.get(Chat_Status.class, 2);
        chat.setChat_Status(chat_Status);

        chat.setDate_time(new Date());
        chat.setFrom_user(logged_user);
        chat.setTo_user(other_User);
        chat.setMessage(message);

        //save in db
        session.save(chat);
        try {
            session.beginTransaction().commit();
            reponseJson.addProperty("success", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //send responce 
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(reponseJson));

    }

}
