/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Transaction;
import helper.JDBCHelper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author balis
 */
public class TransactionDAO extends OmegaPayDAO<Transaction, Integer> {

    String INSERT_SQL = "INSERT INTO [Transaction](TransactionDate, FromAccount, ToAccount, Amount, Note) VALUES(?,?,?,?,?)";
    String SELECT_ALL_SQL = "SELECT * FROM [Transaction]";
    String SELECT_BY_ID_SQL = "SELECT * FROM [Transaction] WHERE TransactionID=?";

    @Override
    public void insert(Transaction entity) {
        JDBCHelper.executeUpdate(INSERT_SQL,
                entity.getTransactionDate(),
                entity.getFromAccount(),
                entity.getToAccount(),
                entity.getAmount(),
                entity.getNote());
    }

    @Override
    public void update(Transaction entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Transaction selectByID(Integer id) {
        List<Transaction> list = this.selectBySQL(SELECT_BY_ID_SQL, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Transaction> selectAll() {
        return this.selectBySQL(SELECT_ALL_SQL);
    }

    @Override
    public List<Transaction> selectBySQL(String SQL, Object... args) {
        List<Transaction> list = new ArrayList<>();
        try {
            ResultSet rs = JDBCHelper.executeQuery(SQL, args);
            while (rs.next()) {
                Transaction entity = new Transaction();
                entity.setTransactionID(rs.getInt("TransactionID"));
                entity.setTransactionDate(rs.getDate("TransactionDate"));
                entity.setFromAccount(rs.getString("FromAccount"));
                entity.setToAccount(rs.getString("ToAccount"));
                entity.setAmount(rs.getFloat("Amount"));
                entity.setNote(rs.getString("Note"));
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
