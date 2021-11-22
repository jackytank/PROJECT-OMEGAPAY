/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import entity.Card;
import helper.JDBCHelper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author balis
 */
public class CardDAO extends OmegaPayDAO<Card, Integer> {

    String INSERT_SQL = "INSERT INTO Card(OmegaAccount, CardNumber, PIN, ExpirationDate, "
            + "CardHolderName, BillingAddress, CardBalance, CardName) VALUES(?,?,?,?,?,?,?,?)";
    String UPDATE_CARD_BALANCE_SQL = "UPDATE Card SET CardBalance=? WHERE CardID=?";
    String UPDATE_PIN_SQL = "UPDATE Card SET PIN=? WHERE CardID=?";
    String DELETE_SQL = "DELETE FROM Card WHERE CardID=?";
    String SELECT_ALL_SQL = "SELECT * FROM Card";
    String SELECT_BY_ID_SQL = "SELECT * FROM Card WHERE CardID=?";
    String SELECT_BY_OMEGA = "SELECT * FROM Card WHERE OmegaAccount=?";

    @Override
    public void insert(Card entity) {
        JDBCHelper.executeUpdate(INSERT_SQL,
                entity.getOmegaAccount(),
                entity.getCardNumber(),
                entity.getPIN(),
                entity.getExpirationDate(),
                entity.getCardHolderName(),
                entity.getBillingAddress(),
                entity.getCardBalance(),
                entity.getCardName());
    }

    @Override
    public void update(Card entity) {

    }

    public void updateBalance(Card entity) {
        JDBCHelper.executeUpdate(UPDATE_CARD_BALANCE_SQL,
                entity.getCardBalance(),
                entity.getCardID());
    }

    public void updatePIN(Card entity) {
        JDBCHelper.executeUpdate(UPDATE_PIN_SQL,
                entity.getPIN(),
                entity.getCardID());
    }

    @Override
    public void delete(Integer id) {
        JDBCHelper.executeUpdate(DELETE_SQL, id);
    }

    @Override
    public Card selectByID(Integer id) {
        List<Card> list = selectBySQL(SELECT_BY_ID_SQL, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Card> selectAll() {
        return this.selectBySQL(SELECT_ALL_SQL);
    }

    public List<Card> selectByOmegaAccount(String id) {
        List<Card> list = selectBySQL(SELECT_BY_OMEGA, id);
        if (list.isEmpty()) {
            return null;
        }
        return list;
    }

    @Override
    public List<Card> selectBySQL(String SQL, Object... args) {
        List<Card> list = new ArrayList<>();
        try {
            ResultSet rs = JDBCHelper.executeQuery(SQL, args);
            while (rs.next()) {
                Card entity = new Card();
                entity.setCardID(rs.getInt("CardID"));
                entity.setOmegaAccount(rs.getString("OmegaAccount"));
                entity.setCardNumber(rs.getString("CardNumber"));
                entity.setPIN(rs.getString("PIN"));
                entity.setExpirationDate(rs.getDate("ExpirationDate"));
                entity.setCardHolderName(rs.getString("CardHolderName"));
                entity.setBillingAddress(rs.getString("BillingAddress"));
                entity.setCardBalance(rs.getFloat("CardBalance"));
                entity.setCardName(rs.getString("CardName"));
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
