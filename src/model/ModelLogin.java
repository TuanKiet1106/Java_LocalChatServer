package model;

public class ModelLogin {

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ModelLogin(String userName, String password) {
        this.username = userName;
        this.password = password;
    }

    public ModelLogin() {
    }

    private String username;
    private String password;
}