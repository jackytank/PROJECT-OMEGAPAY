/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import com.formdev.flatlaf.FlatLightLaf;
import dao.CardDAO;
import dao.TransactionDAO;
import entity.Card;
import entity.Transaction;
import helper.AuthUser;
import helper.DateHelper;
import helper.MsgHelper;
import java.awt.Color;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author balis
 */
public class Tri_JFrame extends javax.swing.JFrame {

    CardDAO cardDAO = new CardDAO();
    TransactionDAO transDAO = new TransactionDAO();
    Object[] cardNames = {"Agribank", "Sacombank", "Techcombank", "MBBank"};
    int cardTableRow = -1;

    /**
     * Creates new form NewJFrame
     */
    public Tri_JFrame() {
        initComponents();
    }

    private void initCard() {
        fillCardComboBox();
        fillCardTable();
    }

    private void openAddCardDialog() {
        new addCardJDialog(this, rootPaneCheckingEnabled).setVisible(true);
    }

    private void deleteCard() {
        if (MsgHelper.confirm(this, "Do you want to remove card: " + txtCardNumber.getText())) {
            int id = Integer.parseInt(lblCardID.getText());
            cardDAO.delete(id);
            fillCardTable();
            clearCardForm();
            MsgHelper.alert(this, "Remove card successfully!");
        }
    }

    private void clearCardForm() {
        lblCardID.setText("");
        txtCardBalance.setText("");
        txtCardHolder.setText("");
        txtCardNumber.setText("");
        txtExpirationDate.setText("");
        txtBillingAddress.setText("");
        cboCardNames.setSelectedIndex(0);
        lblATMCardName.setText("");
        lblATMCardNumber.setText("");
        lblATMHolderName.setText("");
        lblATMExpiry.setText("");
        cardTableRow = -1;
    }

    private void fillCardComboBox() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) cboCardNames.getModel();
        model.removeAllElements();
        for (var e : cardNames) {
            model.addElement(e);
        }
    }

    public void fillCardTable() {
        DefaultTableModel disableCellEdit = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCardList.setModel(disableCellEdit);
        DefaultTableModel model = (DefaultTableModel) tblCardList.getModel();
        model.setRowCount(0);
        model.setColumnCount(0);
        model.setColumnIdentifiers(new Object[]{"CardID", "CardNumber", "CardBalance", "Bank"});

        try {
            List<Card> list = cardDAO.selectByOmegaAccount(AuthUser.user.getOmegaAccount());
            for (Card c : list) {
                Object[] row = {c.getCardID(), c.getCardNumber(), String.format("%,.0f", c.getCardBalance()) + " VND", c.getCardName()};
                model.addRow(row);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Card getCardForm() {
        Card card = new Card();
        card.setCardID(Integer.parseInt(lblCardID.getText()));
        card.setBillingAddress(txtBillingAddress.getText());
        card.setCardBalance(Float.parseFloat(lblCardBalance.getToolTipText()));
        card.setCardNumber(txtCardNumber.getText());
        card.setExpirationDate(DateHelper.toDate(txtExpirationDate.getText(), "yyyy-MM-dd"));
        card.setCardName((String) cboCardNames.getSelectedItem());
        card.setCardHolderName(txtCardHolder.getText());
        card.setPIN(new String(txtCurrentPIN.getPassword()));
        card.setOmegaAccount(AuthUser.user.getOmegaAccount());
        return card;
    }

    private void setCardForm(Card e) {
        //set card form info
        lblCardID.setText(String.valueOf(e.getCardID()));
        cboCardNames.setSelectedItem(e.getCardName());
        txtExpirationDate.setText(DateHelper.toString(e.getExpirationDate(), "dd-MM-yyyy"));
        txtCardNumber.setText(e.getCardNumber());
        txtCardHolder.setText(e.getCardHolderName());
        txtBillingAddress.setText(e.getBillingAddress());
        txtCardBalance.setText(String.format("%,.0f", e.getCardBalance()) + " VND");
        lblCardBalance.setToolTipText(String.valueOf(e.getCardBalance()));
        lblCardSection2.setToolTipText(e.getPIN());

        //set atm card info
        lblATMHolderName.setText(e.getCardHolderName().toUpperCase());
        // inserting a whitespace every 4 character. Ex: 222233331111 -> 2222 3333 111
        lblATMCardNumber.setText(e.getCardNumber().replaceAll("(.{" + 4 + "})", "$0 ").trim());
        lblATMExpiry.setText(DateHelper.toString(e.getExpirationDate(), "MM-yy"));
        lblATMCardName.setText(e.getCardName());
    }

    private void displayClickedCard() {
        int cardID = (int) tblCardList.getValueAt(this.cardTableRow, 0);
        Card card = cardDAO.selectByID(cardID);
        this.setCardForm(card);
        tabCard.setSelectedIndex(0);
    }

    private void changePIN() {
        String cardPIN = lblCardSection2.getToolTipText();
        String userTypePIN = new String(txtCurrentPIN.getPassword());
        String newPIN = new String(txtNewPIN.getPassword());
        String retypePIN = new String(txtRetypePIN.getPassword());
        String alertString = "";
        if (cardPIN == null) {
            alertString = "Choose a card to change PIN!\n";
        } else if (!cardPIN.equals(userTypePIN)) {
            alertString += "Card PIN is incorrect!\n";
        } else if (!newPIN.equals(retypePIN)) {
            alertString += "Retype PIN did not match new PIN!\n";
        } else {
            Card card = getCardForm();
            card.setPIN(newPIN);
            cardDAO.updatePIN(card);
            fillCardTable();
            displayClickedCard();
            alertString += "Change PIN successfully!\n";
        }
        if (!alertString.equals("")) {
            MsgHelper.alert(this, alertString);
        }
    }

    //-------------- TransferSection ------------
    private void transferMoney() {
        Transaction tran = getTransferForm();
        try {
            if (txtToAccount.getText().equals(AuthUser.user.getOmegaAccount())) {
                MsgHelper.alert(this, "You can't send money to yourself!");
            } else {
                transDAO.insert(tran);
                clearTransferForm();
                MsgHelper.alert(this, "Transfer successfully!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void clearTransferForm() {
        txtToAccount.setText("");
        txtAmount.setText("");
        txtAmount.setText("");
    }

    private Transaction getTransferForm() {
        Transaction tran = new Transaction();
        tran.setTransactionDate(new Date());
        tran.setFromAccount(AuthUser.user.getOmegaAccount());
        tran.setToAccount(txtToAccount.getText());
        tran.setAmount(Float.parseFloat(txtAmount.getText()));
        tran.setNote(txtNote.getText());
        return tran;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        MainJPanel = new javax.swing.JPanel();
        leftJPanel = new javax.swing.JPanel();
        pnlHome = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pnlTransfer = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pnlAccount = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        pnlCard = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        pnlLogout = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lblOmegaPayTitle = new javax.swing.JLabel();
        lblUserName = new javax.swing.JLabel();
        lblOmegaSymbol = new javax.swing.JLabel();
        rightJPanel = new javax.swing.JPanel();
        pnlHomeSection = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        lblOmegaBalance = new javax.swing.JLabel();
        lblAddMoney = new javax.swing.JLabel();
        lblCVVNum = new javax.swing.JLabel();
        lblCVVTitle = new javax.swing.JLabel();
        lblCardName = new javax.swing.JLabel();
        lblCardVietelPay = new javax.swing.JLabel();
        lblCardNumber = new javax.swing.JLabel();
        lblCardSection = new javax.swing.JLabel();
        lblLastTransaction = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblLastTransaction = new javax.swing.JTable();
        lblOverview = new javax.swing.JLabel();
        btnTransferMoney = new javax.swing.JButton();
        cboCards = new javax.swing.JComboBox<>();
        pnlTransferSection = new javax.swing.JPanel();
        tabTransfer = new javax.swing.JTabbedPane();
        pnlOmegapayAcc = new javax.swing.JPanel();
        txtAmount = new javax.swing.JTextField();
        txtToAccount = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtNote = new javax.swing.JTextArea();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        btnTransfer = new javax.swing.JButton();
        pnlAccountSection = new javax.swing.JPanel();
        pnlMain = new javax.swing.JPanel();
        pnlUp = new javax.swing.JPanel();
        lblAccountInfoTitle = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        txtLastname = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtPhone = new javax.swing.JTextField();
        txtOmegaAccount = new javax.swing.JTextField();
        txtDayCreated = new javax.swing.JTextField();
        txtAddress = new javax.swing.JTextField();
        txtStatus = new javax.swing.JTextField();
        txtFirstname = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        txtOmegaBalance = new javax.swing.JTextField();
        rdoMale = new javax.swing.JRadioButton();
        rdoFemale = new javax.swing.JRadioButton();
        txtBirthday = new com.toedter.calendar.JDateChooser();
        btnUpdate = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        lblPhoto = new javax.swing.JLabel();
        pnlCardSection = new javax.swing.JPanel();
        pnlCardDetail = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel42 = new javax.swing.JLabel();
        tabCard = new javax.swing.JTabbedPane();
        pnlCardDetail1 = new javax.swing.JPanel();
        pnlCard2 = new javax.swing.JPanel();
        lblATMExpiry = new javax.swing.JLabel();
        lblCVVTitle1 = new javax.swing.JLabel();
        lblATMHolderName = new javax.swing.JLabel();
        lblATMCardName = new javax.swing.JLabel();
        lblATMCardNumber = new javax.swing.JLabel();
        lblCardSection2 = new javax.swing.JLabel();
        cboCardNames = new javax.swing.JComboBox<>();
        txtExpirationDate = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtCardNumber = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        txtCardHolder = new javax.swing.JTextField();
        jLabel45 = new javax.swing.JLabel();
        txtBillingAddress = new javax.swing.JTextField();
        jLabel46 = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        lblAddNew = new javax.swing.JLabel();
        lblRemoveCard = new javax.swing.JLabel();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        lblCardID = new javax.swing.JLabel();
        lblCardOrder1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblCardList = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        lblCardBalance = new javax.swing.JLabel();
        txtCardBalance = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel51 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        btnSavePIN = new javax.swing.JButton();
        txtCurrentPIN = new javax.swing.JPasswordField();
        txtNewPIN = new javax.swing.JPasswordField();
        txtRetypePIN = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        MainJPanel.setBackground(new java.awt.Color(255, 255, 255));

        leftJPanel.setBackground(new java.awt.Color(238, 0, 51));
        leftJPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlHome.setBackground(new java.awt.Color(255, 255, 255));
        pnlHome.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlHome.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlHomeMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlHomeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlHomeMouseExited(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_home_30px.png"))); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("DASHBOARD");

        javax.swing.GroupLayout pnlHomeLayout = new javax.swing.GroupLayout(pnlHome);
        pnlHome.setLayout(pnlHomeLayout);
        pnlHomeLayout.setHorizontalGroup(
            pnlHomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHomeLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlHomeLayout.setVerticalGroup(
            pnlHomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        leftJPanel.add(pnlHome, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 153, 192, -1));

        pnlTransfer.setBackground(new java.awt.Color(255, 255, 255));
        pnlTransfer.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlTransfer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlTransferMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlTransferMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlTransferMouseExited(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_data_transfer_30px.png"))); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText("TRANSFER");

        javax.swing.GroupLayout pnlTransferLayout = new javax.swing.GroupLayout(pnlTransfer);
        pnlTransfer.setLayout(pnlTransferLayout);
        pnlTransferLayout.setHorizontalGroup(
            pnlTransferLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTransferLayout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );
        pnlTransferLayout.setVerticalGroup(
            pnlTransferLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
        );

        leftJPanel.add(pnlTransfer, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 240, 192, -1));

        pnlAccount.setBackground(new java.awt.Color(255, 255, 255));
        pnlAccount.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlAccount.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlAccountMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlAccountMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlAccountMouseExited(evt);
            }
        });

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_user_30px.png"))); // NOI18N

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("ACCOUNT");

        javax.swing.GroupLayout pnlAccountLayout = new javax.swing.GroupLayout(pnlAccount);
        pnlAccount.setLayout(pnlAccountLayout);
        pnlAccountLayout.setHorizontalGroup(
            pnlAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccountLayout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 15, Short.MAX_VALUE))
        );
        pnlAccountLayout.setVerticalGroup(
            pnlAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
        );

        leftJPanel.add(pnlAccount, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 321, -1, -1));

        pnlCard.setBackground(new java.awt.Color(255, 255, 255));
        pnlCard.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlCardMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlCardMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlCardMouseExited(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_mastercard_credit_card_30px.png"))); // NOI18N

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel6.setText("CARDS");

        javax.swing.GroupLayout pnlCardLayout = new javax.swing.GroupLayout(pnlCard);
        pnlCard.setLayout(pnlCardLayout);
        pnlCardLayout.setHorizontalGroup(
            pnlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardLayout.createSequentialGroup()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 15, Short.MAX_VALUE))
        );
        pnlCardLayout.setVerticalGroup(
            pnlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
        );

        leftJPanel.add(pnlCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 400, -1, -1));

        pnlLogout.setBackground(new java.awt.Color(255, 255, 255));
        pnlLogout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlLogoutMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlLogoutMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlLogoutMouseExited(evt);
            }
        });

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_logout_rounded_left_30px.png"))); // NOI18N

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel10.setText("LOG OUT");

        javax.swing.GroupLayout pnlLogoutLayout = new javax.swing.GroupLayout(pnlLogout);
        pnlLogout.setLayout(pnlLogoutLayout);
        pnlLogoutLayout.setHorizontalGroup(
            pnlLogoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLogoutLayout.createSequentialGroup()
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 15, Short.MAX_VALUE))
        );
        pnlLogoutLayout.setVerticalGroup(
            pnlLogoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        leftJPanel.add(pnlLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 480, -1, -1));

        lblOmegaPayTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblOmegaPayTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblOmegaPayTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblOmegaPayTitle.setText("OMEGAPAY");
        leftJPanel.add(lblOmegaPayTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 150, 30));

        lblUserName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUserName.setForeground(new java.awt.Color(255, 255, 255));
        lblUserName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblUserName.setText("USER: TO MINH TRI");
        leftJPanel.add(lblUserName, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 620, 190, -1));

        lblOmegaSymbol.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        lblOmegaSymbol.setForeground(new java.awt.Color(255, 255, 255));
        lblOmegaSymbol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblOmegaSymbol.setText("Ω");
        leftJPanel.add(lblOmegaSymbol, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 20, 90, 50));

        rightJPanel.setBackground(new java.awt.Color(204, 204, 204));
        rightJPanel.setLayout(new java.awt.CardLayout());

        pnlHomeSection.setBackground(new java.awt.Color(250, 250, 250));
        pnlHomeSection.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(204, 204, 204), null));

        jLabel13.setBackground(new java.awt.Color(255, 255, 255));
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_lock_30px.png"))); // NOI18N

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("BALANCE");
        jLabel14.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        lblOmegaBalance.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblOmegaBalance.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblOmegaBalance.setText("304.000 VND");

        lblAddMoney.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_add_20px.png"))); // NOI18N
        lblAddMoney.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAddMoneyMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblAddMoney)
                        .addGap(0, 7, Short.MAX_VALUE))
                    .addComponent(lblOmegaBalance, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblAddMoney, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(lblOmegaBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );

        pnlHomeSection.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, 260, 170));

        lblCVVNum.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCVVNum.setForeground(new java.awt.Color(255, 255, 255));
        lblCVVNum.setText("11/25");
        pnlHomeSection.add(lblCVVNum, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 470, 40, -1));

        lblCVVTitle.setFont(new java.awt.Font("Credit Card", 0, 12)); // NOI18N
        lblCVVTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblCVVTitle.setText("EXPIRY");
        pnlHomeSection.add(lblCVVTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 470, 40, -1));

        lblCardName.setFont(new java.awt.Font("Credit Card", 2, 11)); // NOI18N
        lblCardName.setForeground(new java.awt.Color(255, 255, 255));
        lblCardName.setText("TO MINH TRI");
        pnlHomeSection.add(lblCardName, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 470, 120, -1));

        lblCardVietelPay.setFont(new java.awt.Font("Credit Card", 0, 14)); // NOI18N
        lblCardVietelPay.setForeground(new java.awt.Color(255, 255, 255));
        lblCardVietelPay.setText("ARGIBANK");
        pnlHomeSection.add(lblCardVietelPay, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 360, 80, -1));

        lblCardNumber.setFont(new java.awt.Font("Credit Card", 0, 12)); // NOI18N
        lblCardNumber.setForeground(new java.awt.Color(255, 255, 255));
        lblCardNumber.setText("0375 9485 2934 2834");
        pnlHomeSection.add(lblCardNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 440, 200, 20));

        lblCardSection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/card_1.png"))); // NOI18N
        pnlHomeSection.add(lblCardSection, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 340, 260, -1));

        lblLastTransaction.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblLastTransaction.setText("LAST TRANSACTION");
        pnlHomeSection.add(lblLastTransaction, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 40, 130, 20));

        tblLastTransaction.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Date", "From Account", "To Account", "Amount", "Note"
            }
        ));
        jScrollPane1.setViewportView(tblLastTransaction);

        pnlHomeSection.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 67, 570, 520));

        lblOverview.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblOverview.setText("OVERVIEW");
        pnlHomeSection.add(lblOverview, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 100, -1));

        btnTransferMoney.setBackground(new java.awt.Color(0, 112, 186));
        btnTransferMoney.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnTransferMoney.setForeground(new java.awt.Color(255, 255, 255));
        btnTransferMoney.setText("TRANSFER MONEY");
        btnTransferMoney.setBorder(null);
        btnTransferMoney.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnTransferMoneyMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnTransferMoneyMouseExited(evt);
            }
        });
        btnTransferMoney.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransferMoneyActionPerformed(evt);
            }
        });
        pnlHomeSection.add(btnTransferMoney, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 550, 170, 40));

        cboCards.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cboCards.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Card_1", "Card_2", "Card_3", "Card_4" }));
        cboCards.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlHomeSection.add(cboCards, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 310, 130, 20));

        rightJPanel.add(pnlHomeSection, "card2");

        pnlTransferSection.setBackground(new java.awt.Color(250, 250, 250));
        pnlTransferSection.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tabTransfer.setBackground(new java.awt.Color(255, 255, 255));
        tabTransfer.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tabTransfer.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        pnlOmegapayAcc.setBackground(new java.awt.Color(255, 255, 255));
        pnlOmegapayAcc.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtAmount.setText("100000");
        pnlOmegapayAcc.add(txtAmount, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 370, 40));

        txtToAccount.setText("111299893443");
        pnlOmegapayAcc.add(txtToAccount, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 370, 40));

        jLabel20.setText("Omega Account");
        pnlOmegapayAcc.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 120, -1));

        jLabel21.setText("Note");
        pnlOmegapayAcc.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 100, -1));

        jLabel41.setText("Amount (VND)");
        pnlOmegapayAcc.add(jLabel41, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, 100, -1));

        txtNote.setColumns(20);
        txtNote.setRows(5);
        txtNote.setText("test transfer");
        jScrollPane2.setViewportView(txtNote);

        pnlOmegapayAcc.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 370, 70));

        jLabel25.setForeground(new java.awt.Color(51, 255, 51));
        jLabel25.setText("Current: ");
        pnlOmegapayAcc.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 100, 50, -1));

        jLabel26.setForeground(new java.awt.Color(51, 255, 51));
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("304.000 VND");
        pnlOmegapayAcc.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 100, 110, -1));

        tabTransfer.addTab("TO OMEGAPAY ACCOUNT", pnlOmegapayAcc);

        pnlTransferSection.add(tabTransfer, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 130, 390, 320));

        btnTransfer.setBackground(new java.awt.Color(0, 112, 186));
        btnTransfer.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnTransfer.setForeground(new java.awt.Color(255, 255, 255));
        btnTransfer.setText("TRANSFER");
        btnTransfer.setBorder(null);
        btnTransfer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnTransferMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnTransferMouseExited(evt);
            }
        });
        btnTransfer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransferActionPerformed(evt);
            }
        });
        pnlTransferSection.add(btnTransfer, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 470, 170, 40));

        rightJPanel.add(pnlTransferSection, "card3");

        pnlAccountSection.setBackground(new java.awt.Color(250, 250, 250));

        pnlMain.setBackground(new java.awt.Color(255, 255, 255));
        pnlMain.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        pnlUp.setBackground(new java.awt.Color(238, 0, 51));
        pnlUp.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblAccountInfoTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblAccountInfoTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblAccountInfoTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAccountInfoTitle.setText("ACOUNT INFORMATION");
        pnlUp.add(lblAccountInfoTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 10, 285, -1));

        jLabel29.setText("First name");

        jLabel30.setText("Last name");

        jLabel31.setText("Email");

        jLabel32.setText("Phone");

        jLabel33.setText("Omega Account");

        jLabel35.setText("Day created");

        jLabel36.setText("Address");

        jLabel37.setText("Gender");

        jLabel38.setText("Status");

        txtLastname.setText("To Minh");

        txtEmail.setText("abc@gmail.com");

        txtPhone.setText("0844993847");

        txtOmegaAccount.setText("690078902233");

        txtDayCreated.setText("11/11/2021");

        txtAddress.setText("No.23 St. Nguyen Van Cu");

        txtStatus.setText("Platinum");

        txtFirstname.setText("Tri");

        jLabel39.setText("Birthday");

        jLabel40.setText("Omega Balance");

        txtOmegaBalance.setText("304.000 VND");

        rdoMale.setSelected(true);
        rdoMale.setText("Male");

        rdoFemale.setText("Female");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlUp, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBirthday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFirstname, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPhone))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEmail))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtLastname))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rdoMale)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(rdoFemale)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAddress))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtStatus))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtOmegaAccount))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDayCreated, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtOmegaBalance)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addComponent(pnlUp, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(jLabel33)
                    .addComponent(txtOmegaAccount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFirstname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel30)
                                    .addComponent(txtLastname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel31)
                                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel32)
                                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel37)
                                    .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(rdoMale)
                                        .addComponent(rdoFemale))))
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel35)
                                    .addComponent(txtDayCreated, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel36)
                                    .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel38)
                                    .addComponent(txtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel40)
                                    .addComponent(txtOmegaBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(21, 21, 21)
                        .addComponent(jLabel39))
                    .addComponent(txtBirthday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 26, Short.MAX_VALUE))
        );

        btnUpdate.setText("UPDATE");

        btnEdit.setText("Edit");

        lblPhoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPhoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/photo/elonmusk.png"))); // NOI18N

        javax.swing.GroupLayout pnlAccountSectionLayout = new javax.swing.GroupLayout(pnlAccountSection);
        pnlAccountSection.setLayout(pnlAccountSectionLayout);
        pnlAccountSectionLayout.setHorizontalGroup(
            pnlAccountSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccountSectionLayout.createSequentialGroup()
                .addGroup(pnlAccountSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlAccountSectionLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlAccountSectionLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(pnlAccountSectionLayout.createSequentialGroup()
                .addGap(385, 385, 385)
                .addComponent(lblPhoto, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlAccountSectionLayout.setVerticalGroup(
            pnlAccountSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAccountSectionLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(lblPhoto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(pnlMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAccountSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUpdate)
                    .addComponent(btnEdit))
                .addGap(92, 92, 92))
        );

        rightJPanel.add(pnlAccountSection, "card4");

        pnlCardSection.setBackground(new java.awt.Color(255, 255, 255));

        jPanel4.setBackground(new java.awt.Color(238, 0, 51));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel42.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(255, 255, 255));
        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel42.setText("CARD DETAILS");
        jPanel4.add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 10, 200, -1));

        pnlCard2.setToolTipText("");
        pnlCard2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblATMExpiry.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblATMExpiry.setForeground(new java.awt.Color(255, 255, 255));
        lblATMExpiry.setText("11/25");
        pnlCard2.add(lblATMExpiry, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 150, 40, -1));

        lblCVVTitle1.setFont(new java.awt.Font("Credit Card", 0, 12)); // NOI18N
        lblCVVTitle1.setForeground(new java.awt.Color(255, 255, 255));
        lblCVVTitle1.setText("EXPIRY");
        pnlCard2.add(lblCVVTitle1, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 150, 40, -1));

        lblATMHolderName.setFont(new java.awt.Font("Credit Card", 2, 11)); // NOI18N
        lblATMHolderName.setForeground(new java.awt.Color(255, 255, 255));
        lblATMHolderName.setText("TO MINH TRI");
        pnlCard2.add(lblATMHolderName, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 150, 120, -1));

        lblATMCardName.setFont(new java.awt.Font("Courier New", 1, 14)); // NOI18N
        lblATMCardName.setForeground(new java.awt.Color(255, 255, 255));
        lblATMCardName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblATMCardName.setText("ARGIBANK");
        pnlCard2.add(lblATMCardName, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 40, 150, -1));

        lblATMCardNumber.setFont(new java.awt.Font("Credit Card", 0, 12)); // NOI18N
        lblATMCardNumber.setForeground(new java.awt.Color(255, 255, 255));
        lblATMCardNumber.setText("0375 9485 2934 2834");
        pnlCard2.add(lblATMCardNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 120, 200, 20));

        lblCardSection2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/card_1.png"))); // NOI18N
        pnlCard2.add(lblCardSection2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 260, -1));

        cboCardNames.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cboCardNames.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Agribank", " " }));
        cboCardNames.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cboCardNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboCardNamesActionPerformed(evt);
            }
        });

        txtExpirationDate.setText("1-11-2025");

        jLabel19.setText("Card name");

        txtCardNumber.setText("650492834958");

        jLabel44.setText("Card number");

        txtCardHolder.setText("To Minh Tri");

        jLabel45.setText("Cardholder's name");

        txtBillingAddress.setText("No.69 St. Truong Chinh, HCM");

        jLabel46.setText("Billing address");

        btnSave.setBackground(new java.awt.Color(238, 0, 51));
        btnSave.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSave.setForeground(new java.awt.Color(255, 255, 255));
        btnSave.setText("SAVE");

        lblAddNew.setBackground(new java.awt.Color(238, 0, 51));
        lblAddNew.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblAddNew.setForeground(new java.awt.Color(51, 255, 51));
        lblAddNew.setText("Add new card");
        lblAddNew.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblAddNewMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblAddNewMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblAddNewMouseExited(evt);
            }
        });

        lblRemoveCard.setBackground(new java.awt.Color(238, 0, 51));
        lblRemoveCard.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblRemoveCard.setForeground(new java.awt.Color(238, 0, 51));
        lblRemoveCard.setText("Remove card");
        lblRemoveCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRemoveCardMouseClicked(evt);
            }
        });

        btnPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_left_20px.png"))); // NOI18N
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_right_20px.png"))); // NOI18N
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        jLabel27.setText("Expiration date");

        lblCardID.setFont(new java.awt.Font("Credit Card", 2, 14)); // NOI18N
        lblCardID.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCardID.setText("1");

        lblCardOrder1.setFont(new java.awt.Font("Courier New", 1, 14)); // NOI18N
        lblCardOrder1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCardOrder1.setText("Card");

        javax.swing.GroupLayout pnlCardDetail1Layout = new javax.swing.GroupLayout(pnlCardDetail1);
        pnlCardDetail1.setLayout(pnlCardDetail1Layout);
        pnlCardDetail1Layout.setHorizontalGroup(
            pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlCardDetail1Layout.createSequentialGroup()
                        .addGap(109, 109, 109)
                        .addComponent(lblCardOrder1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCardID, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(139, 139, 139)
                        .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(cboCardNames, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                            .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                                    .addGap(30, 30, 30)
                                    .addComponent(lblAddNew, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(pnlCard2, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(28, 28, 28)
                            .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtExpirationDate, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtCardNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel45, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtCardHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtBillingAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                            .addGap(30, 30, 30)
                            .addComponent(lblRemoveCard, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(203, 203, 203)
                            .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                            .addGap(200, 200, 200)
                            .addComponent(btnPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(20, 20, 20)
                            .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(15, 15, 15))
        );
        pnlCardDetail1Layout.setVerticalGroup(
            pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboCardNames, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCardID, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCardOrder1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel27)
                        .addGap(6, 6, 6)
                        .addComponent(txtExpirationDate, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel44)
                        .addGap(6, 6, 6)
                        .addComponent(txtCardNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel45)
                        .addGap(6, 6, 6)
                        .addComponent(txtCardHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlCard2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel46)
                        .addGap(6, 6, 6)
                        .addComponent(txtBillingAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(lblAddNew)))
                .addGap(5, 5, 5)
                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(lblRemoveCard))
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(54, 54, 54)
                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnPrev)
                    .addComponent(btnNext))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        tabCard.addTab("Card", pnlCardDetail1);

        tblCardList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblCardList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCardListMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblCardList);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE))
        );

        tabCard.addTab("List", jPanel2);

        javax.swing.GroupLayout pnlCardDetailLayout = new javax.swing.GroupLayout(pnlCardDetail);
        pnlCardDetail.setLayout(pnlCardDetailLayout);
        pnlCardDetailLayout.setHorizontalGroup(
            pnlCardDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardDetailLayout.createSequentialGroup()
                .addGroup(pnlCardDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tabCard))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnlCardDetailLayout.setVerticalGroup(
            pnlCardDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardDetailLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(tabCard, javax.swing.GroupLayout.PREFERRED_SIZE, 533, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jPanel8.setBackground(new java.awt.Color(238, 0, 51));

        lblCardBalance.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblCardBalance.setForeground(new java.awt.Color(255, 255, 255));
        lblCardBalance.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCardBalance.setText("CARD BALANCE");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblCardBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCardBalance, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addContainerGap())
        );

        txtCardBalance.setEditable(false);
        txtCardBalance.setFont(new java.awt.Font("Courier New", 1, 18)); // NOI18N
        txtCardBalance.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCardBalance.setText("100000000");

        jLabel50.setText("Current card's balance");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(txtCardBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(jLabel50)
                .addGap(18, 18, 18)
                .addComponent(txtCardBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(76, Short.MAX_VALUE))
        );

        jPanel9.setBackground(new java.awt.Color(238, 0, 51));

        jLabel51.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel51.setText("CHANGE PIN");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(65, Short.MAX_VALUE)
                .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel51, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel47.setText("Current PIN");

        jLabel48.setText("New PIN");

        jLabel52.setText("Re Enter PIN");

        btnSavePIN.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSavePIN.setText("SAVE");
        btnSavePIN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSavePINActionPerformed(evt);
            }
        });

        txtCurrentPIN.setFont(new java.awt.Font("Courier New", 1, 18)); // NOI18N
        txtCurrentPIN.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCurrentPIN.setText("445566");

        txtNewPIN.setFont(new java.awt.Font("Courier New", 1, 18)); // NOI18N
        txtNewPIN.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtNewPIN.setText("445566");

        txtRetypePIN.setFont(new java.awt.Font("Courier New", 1, 18)); // NOI18N
        txtRetypePIN.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtRetypePIN.setText("445566");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCurrentPIN, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNewPIN, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtRetypePIN, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(77, 77, 77)
                        .addComponent(btnSavePIN, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel47)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCurrentPIN, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel48)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNewPIN, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel52)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtRetypePIN, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSavePIN, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlCardSectionLayout = new javax.swing.GroupLayout(pnlCardSection);
        pnlCardSection.setLayout(pnlCardSectionLayout);
        pnlCardSectionLayout.setHorizontalGroup(
            pnlCardSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardSectionLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(pnlCardDetail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCardSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlCardSectionLayout.setVerticalGroup(
            pnlCardSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardSectionLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(pnlCardSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlCardDetail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlCardSectionLayout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(55, Short.MAX_VALUE))
        );

        rightJPanel.add(pnlCardSection, "card5");

        javax.swing.GroupLayout MainJPanelLayout = new javax.swing.GroupLayout(MainJPanel);
        MainJPanel.setLayout(MainJPanelLayout);
        MainJPanelLayout.setHorizontalGroup(
            MainJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MainJPanelLayout.createSequentialGroup()
                .addComponent(leftJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rightJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        MainJPanelLayout.setVerticalGroup(
            MainJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(leftJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(rightJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(MainJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pnlHomeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlHomeMouseClicked
        if (pnlHome == evt.getSource()) {
            pnlHomeSection.setVisible(true);
            pnlTransferSection.setVisible(false);
            pnlAccountSection.setVisible(false);
            pnlCardSection.setVisible(false);
        }
    }//GEN-LAST:event_pnlHomeMouseClicked

    private void pnlHomeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlHomeMouseEntered
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(230, 230, 230));
    }//GEN-LAST:event_pnlHomeMouseEntered

    private void pnlHomeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlHomeMouseExited
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(255, 255, 255));
    }//GEN-LAST:event_pnlHomeMouseExited

    private void pnlTransferMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlTransferMouseClicked
        if (pnlTransfer == evt.getSource()) {
            pnlHomeSection.setVisible(false);
            pnlTransferSection.setVisible(true);
            pnlAccountSection.setVisible(false);
            pnlCardSection.setVisible(false);
        }
    }//GEN-LAST:event_pnlTransferMouseClicked

    private void pnlTransferMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlTransferMouseEntered
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(230, 230, 230));
    }//GEN-LAST:event_pnlTransferMouseEntered

    private void pnlTransferMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlTransferMouseExited
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(255, 255, 255));
    }//GEN-LAST:event_pnlTransferMouseExited

    private void pnlAccountMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlAccountMouseClicked
        pnlHomeSection.setVisible(false);
        pnlTransferSection.setVisible(false);
        pnlAccountSection.setVisible(true);
    }//GEN-LAST:event_pnlAccountMouseClicked

    private void pnlAccountMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlAccountMouseEntered
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(230, 230, 230));
    }//GEN-LAST:event_pnlAccountMouseEntered

    private void pnlAccountMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlAccountMouseExited
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(255, 255, 255));
    }//GEN-LAST:event_pnlAccountMouseExited

    private void pnlCardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlCardMouseClicked
        pnlHomeSection.setVisible(false);
        pnlTransferSection.setVisible(false);
        pnlAccountSection.setVisible(false);
        pnlCardSection.setVisible(true);
    }//GEN-LAST:event_pnlCardMouseClicked

    private void pnlCardMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlCardMouseEntered
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(230, 230, 230));
    }//GEN-LAST:event_pnlCardMouseEntered

    private void pnlCardMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlCardMouseExited
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(255, 255, 255));
    }//GEN-LAST:event_pnlCardMouseExited

    private void pnlLogoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlLogoutMouseClicked
        AuthUser.clear();
        this.dispose();
        new LoginJFrame().setVisible(true);
    }//GEN-LAST:event_pnlLogoutMouseClicked

    private void pnlLogoutMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlLogoutMouseEntered
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(230, 230, 230));
    }//GEN-LAST:event_pnlLogoutMouseEntered

    private void pnlLogoutMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlLogoutMouseExited
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(255, 255, 255));
    }//GEN-LAST:event_pnlLogoutMouseExited

    private void lblAddMoneyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAddMoneyMouseClicked
        new addMoneyJDialog(this, rootPaneCheckingEnabled).setVisible(true);
    }//GEN-LAST:event_lblAddMoneyMouseClicked

    private void btnTransferMoneyMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTransferMoneyMouseEntered
        JButton b = (JButton) evt.getSource();
        b.setBackground(new Color(0, 79, 153));
    }//GEN-LAST:event_btnTransferMoneyMouseEntered

    private void btnTransferMoneyMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTransferMoneyMouseExited
        JButton b = (JButton) evt.getSource();
        b.setBackground(new Color(0, 112, 186));
    }//GEN-LAST:event_btnTransferMoneyMouseExited

    private void btnTransferMoneyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransferMoneyActionPerformed
        pnlHomeSection.setVisible(false);
        pnlTransferSection.setVisible(true);
        pnlAccountSection.setVisible(false);
        pnlCardSection.setVisible(false);
    }//GEN-LAST:event_btnTransferMoneyActionPerformed

    private void btnTransferMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTransferMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTransferMouseEntered

    private void btnTransferMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTransferMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTransferMouseExited

    private void btnTransferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransferActionPerformed
        transferMoney();
    }//GEN-LAST:event_btnTransferActionPerformed

    private void cboCardNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboCardNamesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboCardNamesActionPerformed

    private void lblAddNewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAddNewMouseClicked
        openAddCardDialog();
    }//GEN-LAST:event_lblAddNewMouseClicked

    private void lblAddNewMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAddNewMouseEntered
        JLabel label = (JLabel) evt.getSource();
        label.setForeground(new Color(204, 255, 102));
    }//GEN-LAST:event_lblAddNewMouseEntered

    private void lblAddNewMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAddNewMouseExited
        JLabel label = (JLabel) evt.getSource();
        label.setForeground(new Color(51, 255, 51));
    }//GEN-LAST:event_lblAddNewMouseExited

    private void lblRemoveCardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRemoveCardMouseClicked
        deleteCard();
    }//GEN-LAST:event_lblRemoveCardMouseClicked

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        if (this.cardTableRow > 0) {
            cardTableRow--;
            this.displayClickedCard();
        }
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        if (this.cardTableRow < tblCardList.getRowCount() - 1) {
            cardTableRow++;
            this.displayClickedCard();
        }
    }//GEN-LAST:event_btnNextActionPerformed

    private void tblCardListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCardListMouseClicked
        if (evt.getClickCount() == 2) {
            this.cardTableRow = tblCardList.getSelectedRow();
            this.displayClickedCard();
        }
    }//GEN-LAST:event_tblCardListMouseClicked

    private void btnSavePINActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSavePINActionPerformed
        changePIN();
    }//GEN-LAST:event_btnSavePINActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Tri_JFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tri_JFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tri_JFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tri_JFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        FlatLightLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF");
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Tri_JFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel MainJPanel;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSavePIN;
    private javax.swing.JButton btnTransfer;
    private javax.swing.JButton btnTransferMoney;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cboCardNames;
    private javax.swing.JComboBox<String> cboCards;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblATMCardName;
    private javax.swing.JLabel lblATMCardNumber;
    private javax.swing.JLabel lblATMExpiry;
    private javax.swing.JLabel lblATMHolderName;
    private javax.swing.JLabel lblAccountInfoTitle;
    private javax.swing.JLabel lblAddMoney;
    private javax.swing.JLabel lblAddNew;
    private javax.swing.JLabel lblCVVNum;
    private javax.swing.JLabel lblCVVTitle;
    private javax.swing.JLabel lblCVVTitle1;
    private javax.swing.JLabel lblCardBalance;
    private javax.swing.JLabel lblCardID;
    private javax.swing.JLabel lblCardName;
    private javax.swing.JLabel lblCardNumber;
    private javax.swing.JLabel lblCardOrder1;
    private javax.swing.JLabel lblCardSection;
    private javax.swing.JLabel lblCardSection2;
    private javax.swing.JLabel lblCardVietelPay;
    private javax.swing.JLabel lblLastTransaction;
    private javax.swing.JLabel lblOmegaBalance;
    private javax.swing.JLabel lblOmegaPayTitle;
    private javax.swing.JLabel lblOmegaSymbol;
    private javax.swing.JLabel lblOverview;
    private javax.swing.JLabel lblPhoto;
    private javax.swing.JLabel lblRemoveCard;
    private javax.swing.JLabel lblUserName;
    private javax.swing.JPanel leftJPanel;
    private javax.swing.JPanel pnlAccount;
    private javax.swing.JPanel pnlAccountSection;
    private javax.swing.JPanel pnlCard;
    private javax.swing.JPanel pnlCard2;
    private javax.swing.JPanel pnlCardDetail;
    private javax.swing.JPanel pnlCardDetail1;
    private javax.swing.JPanel pnlCardSection;
    private javax.swing.JPanel pnlHome;
    private javax.swing.JPanel pnlHomeSection;
    private javax.swing.JPanel pnlLogout;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlOmegapayAcc;
    private javax.swing.JPanel pnlTransfer;
    private javax.swing.JPanel pnlTransferSection;
    private javax.swing.JPanel pnlUp;
    private javax.swing.JRadioButton rdoFemale;
    private javax.swing.JRadioButton rdoMale;
    private javax.swing.JPanel rightJPanel;
    private javax.swing.JTabbedPane tabCard;
    private javax.swing.JTabbedPane tabTransfer;
    private javax.swing.JTable tblCardList;
    private javax.swing.JTable tblLastTransaction;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtBillingAddress;
    private com.toedter.calendar.JDateChooser txtBirthday;
    private javax.swing.JTextField txtCardBalance;
    private javax.swing.JTextField txtCardHolder;
    private javax.swing.JTextField txtCardNumber;
    private javax.swing.JPasswordField txtCurrentPIN;
    private javax.swing.JTextField txtDayCreated;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtExpirationDate;
    private javax.swing.JTextField txtFirstname;
    private javax.swing.JTextField txtLastname;
    private javax.swing.JPasswordField txtNewPIN;
    private javax.swing.JTextArea txtNote;
    private javax.swing.JTextField txtOmegaAccount;
    private javax.swing.JTextField txtOmegaBalance;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JPasswordField txtRetypePIN;
    private javax.swing.JTextField txtStatus;
    private javax.swing.JTextField txtToAccount;
    // End of variables declaration//GEN-END:variables
}