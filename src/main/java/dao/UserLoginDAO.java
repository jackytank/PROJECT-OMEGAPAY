/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.User_Login;
import helper.JDBCHelper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author balis
 */
public class UserLoginDAO extends OmegaPayDAO<User_Login, String> {

    String INSERT_SQL = "INSERT INTO User_Login(OmegaAccount, Username, Password) VALUES(?,?,?)";
    String UPDATE_SQL = "UPDATE User_Login SET Password=? WHERE OmegaAccount=?";
    String SELECT_BY_USERNAME_SQL = "SELECT * FROM User_Login WHERE Username=?";

    @Override
    public void insert(User_Login entity) {
        JDBCHelper.executeUpdate(INSERT_SQL,
                entity.getOmegaAccount(),
                entity.getUsername(),
                entity.getPassword());
    }

    @Override
    public void update(User_Login entity) {
        JDBCHelper.executeUpdate(UPDATE_SQL,
                entity.getPassword(),
                entity.getOmegaAccount());
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User_Login selectByID(String username) {
        List<User_Login> list = this.selectBySQL(SELECT_BY_USERNAME_SQL, username);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<User_Login> selectAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<User_Login> selectBySQL(String SQL, Object... args) {
        List<User_Login> list = new ArrayList<>();
        try {
            ResultSet rs = JDBCHelper.executeQuery(SQL, args);
            while (rs.next()) {
                User_Login user = new User_Login();
                user.setOmegaAccount(rs.getString("OmegaAccount"));
                user.setUsername(rs.getString("Username"));
                user.setPassword(rs.getString("Password"));
                list.add(user);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
