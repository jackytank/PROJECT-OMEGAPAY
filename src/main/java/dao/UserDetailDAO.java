/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.User_Detail;
import helper.JDBCHelper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author balis
 */
public class UserDetailDAO extends OmegaPayDAO<User_Detail, String> {

    String INSERT_SQL = "INSERT INTO User_Detail(OmegaAccount, FirstName, LastName, Email, "
            + "Phone, Gender, Birthday, Address, DayCreated, Status, Photo, OmegaBalance) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
    String UPDATE_SQL = "UPDATE User_Detail SET FirstName=?, LastName=?, Email=?, Phone=?, Gender=?, Birthday=?,"
            + " Address=?, DayCreated=?, Status=?, Photo=?, OmegaBalance=? WHERE OmegaAccount=?";
    String UPDATE_BALANCE_SQL = "UPDATE User_Detail SET OmegaBalance=? WHERE OmegaAccount=?";
    String DELETE_SQL = "DELETE FROM User_Detail WHERE OmegaAccount=?";
    String SELECT_ALL = "SELECT * FROM User_Detail";
    String SELECT_BY_OMEGA = "SELECT * FROM User_Detail WHERE OmegaAccount=?";

    @Override
    public void insert(User_Detail entity) {
        JDBCHelper.executeUpdate(INSERT_SQL,
                entity.getOmegaAccount(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getGender(),
                entity.getBirthday(),
                entity.getAddress(),
                entity.getDayCreated(),
                entity.getStatus(),
                entity.getPhoto(),
                entity.getOmegaBalance());
    }

    @Override
    public void update(User_Detail entity) {
        JDBCHelper.executeUpdate(UPDATE_SQL,
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getGender(),
                entity.getBirthday(),
                entity.getAddress(),
                entity.getDayCreated(),
                entity.getStatus(),
                entity.getPhoto(),
                entity.getOmegaBalance(),
                entity.getOmegaAccount()
        );
    }

    public void updateBalance(User_Detail entity) {
        JDBCHelper.executeUpdate(UPDATE_BALANCE_SQL,
                entity.getOmegaBalance(),
                entity.getOmegaAccount());
    }

    @Override
    public void delete(String id) {
        JDBCHelper.executeUpdate(DELETE_SQL, id);
    }

    @Override
    public User_Detail selectByID(String omegaAccount) {
        List<User_Detail> list = this.selectBySQL(SELECT_BY_OMEGA, omegaAccount);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<User_Detail> selectAll() {
        return this.selectBySQL(SELECT_ALL);
    }

    @Override
    public List<User_Detail> selectBySQL(String SQL, Object... args) {
        List<User_Detail> list = new ArrayList<>();
        try {
            ResultSet rs = JDBCHelper.executeQuery(SQL, args);
            while (rs.next()) {
                User_Detail entity = new User_Detail();
                entity.setOmegaAccount(rs.getString("OmegaAccount"));
                entity.setFirstName(rs.getString("FirstName"));
                entity.setLastName(rs.getString("LastName"));
                entity.setEmail(rs.getString("Email"));
                entity.setPhone(rs.getString("Phone"));
                entity.setGender(rs.getBoolean("Gender"));
                entity.setBirthday(rs.getDate("Birthday"));
                entity.setAddress(rs.getString("Address"));
                entity.setDayCreated(rs.getDate("DayCreated"));
                entity.setStatus(rs.getString("Status"));
                entity.setPhoto(rs.getString("Photo"));
                entity.setOmegaBalance(rs.getFloat("OmegaBalance"));
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
