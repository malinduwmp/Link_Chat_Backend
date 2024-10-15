/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.Chat_Status;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author user
 */
@WebServlet(name = "LoadChat", urlPatterns = {"/LoadChat"})
public class LoadChat extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        ///LoadChat?logged_user_id=1&other_user_id=3
        Session session = HibernateUtil.getSessionFactory().openSession();

        String logged_user_id = request.getParameter("logged_user_id");
        String other_user_id = request.getParameter("other_user_id");

        //get user 
        User logged_user = (User) session.get(User.class, Integer.parseInt(logged_user_id));

        //get other user
        User other_User = (User) session.get(User.class, Integer.parseInt(other_user_id));

        //get chat 
        Criteria criteria1 = session.createCriteria(Chat.class);
        criteria1.add(
                Restrictions.or(
                        Restrictions.and(Restrictions.eq("from_user", logged_user),
                                Restrictions.eq("to_user", other_User)),
                        Restrictions.and(Restrictions.eq("from_user", other_User),
                                Restrictions.eq("to_user", logged_user))
                )
        );

        //sort chat
        criteria1.addOrder(Order.asc("date_time"));

        //get Chat list 
        List<Chat> chat_list = criteria1.list();

        //get chat status =1 seen 
        Chat_Status chat_Status = (Chat_Status) session.get(Chat_Status.class, 1);

        //create chat array 
        JsonArray chatArray = new JsonArray();

        //create Date Format 
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a");

        for (Chat chat : chat_list) {

            //create chat Object 
            JsonObject chatObject = new JsonObject();
            chatObject.addProperty("message", chat.getMessage());
            chatObject.addProperty("dateTime", dateFormat.format(chat.getDate_time()));

            //get chat only from other user  
            if (chat.getFrom_user().getId() == other_User.getId()) {

                chatObject.addProperty("side", "left");

                //get only unseen (chat_status_id = 2) 
                if (chat.getChat_Status().getId() == 2) {
                    //Update chat Status  -> seen
                    chat.setChat_Status(chat_Status);
                    session.update(chat);
                }

            } else {

                //get chat from logged user 
                //add side to chat object 
                chatObject.addProperty("side", "right");
                chatObject.addProperty("status", chat.getChat_Status().getId());//1 = seen 2 = unseen
            }

            //add chat object into chat array 
            chatArray.add(chatObject);

        }
        //update db 
        session.beginTransaction().commit();

        response.setContentType("application/json");
      response.getWriter().write(gson.toJson(chatArray));

    }

}
