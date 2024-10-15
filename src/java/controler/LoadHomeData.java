/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.User;
import entity.User_Status;
import java.io.File;
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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author user
 */
@WebServlet(name = "LoadHomeData", urlPatterns = {"/LoadHomeData"})
public class LoadHomeData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

          Gson gson = new Gson();
            JsonObject responsJson = new JsonObject();
            responsJson.addProperty("success", false);
            responsJson.addProperty("message", "Unable to Procress Your Request");
        try {
          

            Session session = HibernateUtil.getSessionFactory().openSession();

            //Get user id from request parameter
            String userId = request.getParameter("id");

            //Get user object
            User user = (User) session.get(User.class, Integer.parseInt(userId));

            //GEt user status = 1 (online)
            User_Status user_Status = (User_Status) session.get(User_Status.class, 1);

            //update user status 
            user.setUser_Status(user_Status);
            session.update(user);

            //Get Other Users 
            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.ne("id", user.getId()));

            List<User> otherUsersList = criteria1.list();

            //Remove Password

             JsonArray jsonChatArray= new JsonArray();
            for (User otheUser : otherUsersList) {

                //get Last Conversation
                Criteria criteria2 = session.createCriteria(Chat.class);
                criteria2.add(
                        Restrictions.or(
                                Restrictions.and(
                                        Restrictions.eq("from_user", user),
                                        Restrictions.eq("to_user", otheUser)
                                ),
                                Restrictions.and(
                                        Restrictions.eq("from_user", otheUser),
                                        Restrictions.eq("to_user", user)
                                )
                        )
                );

                criteria2.addOrder(Order.desc("id"));
                criteria2.setMaxResults(1);

                
                //Ceeat chat item json to send frontend data
                JsonObject chatItem = new JsonObject();
                chatItem.addProperty("other_user_id", otheUser.getId());
                chatItem.addProperty("other_user_mobile",otheUser.getMobile());
                chatItem.addProperty("other_user_name", otheUser.getFirst_name() + " " + otheUser.getLast_name());
                chatItem.addProperty("other_user_status", otheUser.getUser_Status().getId()); //1=online 2=offline
                 
                //check Avertar Image 
                String servePath = request.getServletContext().getRealPath("");
                String otherUserAvertarImagePath = servePath + File.separator + "AvatarImage" + File.separator +otheUser.getMobile() + ".png";
                
                File otherUserAvertarImageFile = new File(otherUserAvertarImagePath);
                
                if(otherUserAvertarImageFile.exists()){
                //avatar image found
                    chatItem.addProperty("avatar_image_found",true);
                   
                }else{
                //avatar image not found  
                
                    chatItem.addProperty("avatar_image_found", false);
                    chatItem.addProperty("other_user_avartar_settings", otheUser.getFirst_name().charAt(0) + "" + otheUser.getLast_name().charAt(0));
                
                }
                
                //get Chat list
                List<Chat> dbChatList = criteria2.list();
               SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy, MMM dd hh:mm:ss a");


                if (dbChatList.isEmpty()) {
                    //no conversation
                    chatItem.addProperty("message", "Let's Start new convercation");
                    chatItem.addProperty("dateTime",dateFormat.format(user.getRegisterd_date_time()));
                    chatItem.addProperty("chat_status_id",2);//1=seen ,2 =notseen 
                    
                } else {
                    //load convosation      
                    chatItem.addProperty("message", dbChatList.get(0).getMessage());
                    chatItem.addProperty("dateTime", dateFormat.format(dbChatList.get(0).getDate_time()));
                    chatItem.addProperty("chat_status_id",dbChatList.get(0).getChat_Status().getId()) ;
                    
                }

//            get Last Conversation
               jsonChatArray.add(chatItem);
               // otheUser.setPassword(null);
            }

//            Send User 
            responsJson.addProperty("success", true);
            responsJson.addProperty("message", "Success");
            
//            responsJson.add("user", gson.toJsonTree(user));
            responsJson.add("jsonChatArray",gson.toJsonTree(jsonChatArray));
            
            
            session.beginTransaction().commit();
            session.close(); 
 
        } catch (NumberFormatException | HibernateException e) {
        }
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(responsJson));
    }

}
