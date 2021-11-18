/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.User_Detail;
import helper.JDBCHelper;
import java.util.List;

/**
 *
 * @author balis
 */
public class UserDetailDAO extends OmegaPayDAO<User_Detail, String> {

    String INSERT_SQL = "INSERT INTO User_Detail(OmegaAccount, FirstName, LastName, Email, "
            + "Phone, Gender, Birthday, Address, DayCreated, Status, Photo, OmegaBalance) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User_Detail selectByID(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<User_Detail> selectAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<User_Detail> selectBySQL(String SQL, Object... args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
