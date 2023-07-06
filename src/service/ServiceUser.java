package service;

import connection.DatabaseConnection;
import model.ModelMessage;
import model.ModelRegister;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.ModelClient;
import model.ModelLogin;
import model.ModelUserAccount;

public class ServiceUser {

    public ServiceUser() {
        this.con = DatabaseConnection.getInstance().getConnection();
    }

    public ModelMessage register(ModelRegister data) {
        //  Check user exit
        ModelMessage message = new ModelMessage();
        try {
            PreparedStatement p = con.prepareStatement(CHECK_USER);
            p.setString(1, data.getUserName());
            ResultSet r = p.executeQuery();
            if (r.first()) {
                message.setAction(false);
                message.setMessage("User Already Exited");
            } else {
                message.setAction(true);
            }
            r.close();
            p.close();
            if (message.isAction()) {
                //  Insert User Register
                con.setAutoCommit(false);
                p = con.prepareStatement(INSERT_USER, PreparedStatement.RETURN_GENERATED_KEYS);
                p.setString(1, data.getUserName());
                p.setString(2, data.getPassword());
                p.execute();
                r = p.getGeneratedKeys();
                r.first();
                int userID = r.getInt(1);
                r.close();
                p.close();
                // Create User Account
                p = con.prepareStatement(INSERT_USER_ACCOUNT);
                p.setInt(1, userID);
                p.setString(2, data.getUserName());
                p.execute();
                p.close();
                con.commit();
                con.setAutoCommit(true);
                message.setAction(true);
                message.setMessage("Ok");
                message.setData(new ModelUserAccount(userID, data.getUserName(), "", "", true));
            }
        } catch (SQLException e) {
            System.out.println(e);
            message.setAction(false);
            message.setMessage("Server Error");
            try {
                if (con.getAutoCommit() == false) {
                    con.rollback();
                    con.setAutoCommit(true);
                }
            } catch (SQLException e1) {
                
            }
        }
        return message;
    }
    
    public ModelUserAccount login(ModelLogin login) throws SQLException {
        ModelUserAccount data = null;
        PreparedStatement p = con.prepareStatement(LOGIN);
        p.setString(1, login.getUserName());
        p.setString(2, login.getPassword());
        ResultSet r = p.executeQuery();
        if (r.first()) {
            int userID = r.getInt(1);
            String userName = r.getString(2);
            String gender = r.getString(3);
            String image = r.getString(4);
            data = new ModelUserAccount(userID, userName, gender, image, true);
        }
        r.close();
        p.close();
        return data;
    }
    
    public List<ModelUserAccount> getUser(int exitUser) throws SQLException {
        List<ModelUserAccount> list = new ArrayList<>();
        PreparedStatement p = con.prepareStatement(SELECT_USER_ACCOUNT);
        p.setInt(1, exitUser);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int userID = r.getInt(1);
            String userName = r.getString(2);
            String gender = r.getString(3);
            String image = r.getString(4);
            list.add(new ModelUserAccount(userID, userName, gender, image, checkUserStatus(userID)));
        }
        r.close();
        p.close();
        
        return list;
    }
    
    private boolean checkUserStatus(int userID) {
        List<ModelClient> clients = Service.getInstance(null).getListClient();
        for (ModelClient c : clients) {
            if (c.getUser().getUserID() == userID) {
                return true;
            }
        }
        return false;
    }

    //  SQL
    private final String LOGIN = "select UserID, user_account.Username, Gender, ImageString from `user` join user_account using (UserID) where `user`.Username=BINARY(?) and `user`.`Password`=BINARY(?) and user_account.`Status`='1'";
    private final String SELECT_USER_ACCOUNT = "select UserID, Username, Gender, ImageString from user_account where user_account.`Status`='1' and UserID<>?";
    private final String INSERT_USER = "insert into user (Username, Password) values (?,?)";
    private final String INSERT_USER_ACCOUNT = "insert into user_account (UserID, Username) values (?,?)";
    private final String CHECK_USER = "select UserID from user where Username = ? limit 1";
    //  Instance
    private final Connection con;
}