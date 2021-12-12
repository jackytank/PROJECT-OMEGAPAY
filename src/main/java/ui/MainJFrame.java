/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import dao.CardDAO;
import dao.TransactionDAO;
import dao.UserDetailDAO;
import dao.UserLoginDAO;
import entity.Card;
import entity.Transaction;
import entity.User_Detail;
import entity.User_Login;
import helper.AuthUser;
import helper.DateHelper;
import helper.ImageHelper;
import helper.JTextFieldLimit;
import helper.MsgHelper;
import helper.SendPhone;
import helper.UtilityHelper;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author balis
 */
public class MainJFrame extends javax.swing.JFrame {
    
    CardDAO cardDAO = new CardDAO();
    TransactionDAO transDAO = new TransactionDAO();
    UserDetailDAO detailDAO = new UserDetailDAO();
    UserLoginDAO loginDAO = new UserLoginDAO();
    
    Object[] cardNames = {"Agribank", "Sacombank", "Techcombank", "MBBank"};
    String[] statusList = {"Silver", "Gold", "Platinum"};
    int cardTableRow = -1;
    Countdown count;
    
    private final String receiptFile = System.getProperty("user.home") + "/Desktop/receipt.pdf";
    private final String[] websites = new String[]{
        "https://www.youtube.com/watch?v=dQw4w9WgXcQ&ab_channel=RickAstley",
        "https://www.youtube.com/watch?v=GxM3wstBcD4&ab_channel=Trendnation",
        "https://www.youtube.com/watch?v=sL1AUp8c8QQ&ab_channel=OfirShoham",
        "https://www.youtube.com/watch?v=l01WQXJjO_A&ab_channel=XeTinht%E1%BA%BF",
        "https://www.youtube.com/watch?v=d3y9e0BZkGU&ab_channel=%C4%90%C6%B0%E1%BB%9Dng2Chi%E1%BB%81u",
        "https://www.youtube.com/watch?v=8GGW93DLC5c&ab_channel=Matts",
        "https://www.youtube.com/watch?v=B_ViNy2X5I8&ab_channel=OfirShoham"
    };
    
    private final com.itextpdf.text.Font courier20 = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 20);
    private final com.itextpdf.text.Font courier16 = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 16);
    private final com.itextpdf.text.Font courier12 = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 12);

    /**
     * Creates new form MainJFrame
     */
    public MainJFrame() {
        initComponents();
        setLocationRelativeTo(null);
        updateUserStatus();
        initDashboard();
        initTransfer();
        initSaving();
        initAccount();
        initCard();
        setTitle("OMEGAPAY");
    }

    //-------------------------- Dashboard Section ---------------------------
    protected void initDashboard() {
        User_Detail user_Detail = detailDAO.selectByID(AuthUser.user.getOmegaAccount());
        fillCardsCombobox();
        fillTransactionTable();
        setDashboardForm(user_Detail);
    }
    
    private void fillTransactionTable() {
        DefaultTableModel disableCellEdit = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblLastTransaction.setModel(disableCellEdit);
        DefaultTableModel model = (DefaultTableModel) tblLastTransaction.getModel();
        model.setRowCount(0);
        model.setColumnCount(0);
        model.setColumnIdentifiers(new Object[]{"Datetime", "From Account", "To Account", "Amount", "Note"});
        try {
            List<Transaction> list = transDAO.selectAll();
            for (Transaction e : list) {
                boolean checkFromAcc = e.getFromAccount().equals(AuthUser.user.getOmegaAccount());
                boolean checkToAcc = e.getToAccount().equals(AuthUser.user.getOmegaAccount());
                if (checkFromAcc || checkToAcc) {
                    model.addRow(new Object[]{DateHelper.toString(e.getTransactionDate(), "hh:mm dd-MM-yyyy"),
                        e.getFromAccount(), e.getToAccount(), UtilityHelper.toVND(e.getAmount()), e.getNote()});
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void fillCardsCombobox() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) cboCards.getModel();
        model.removeAllElements();
        List<Card> list = cardDAO.selectByOmegaAccount(AuthUser.user.getOmegaAccount());
        if (list != null) {
            for (Card e : list) {
                model.addElement(e.getCardID());
            }
        }
    }
    
    private void fillCard() {
        if (cboCards.getItemCount() != 0) {
            Card card = cardDAO.selectByID((Integer) cboCards.getSelectedItem());
            if (card != null) {
                lblCardHolder.setText(card.getCardHolderName().toUpperCase());
                lblExpiry.setText(DateHelper.toString(card.getExpirationDate(), "MM-yy"));
                lblCardNumber.setText(card.getCardNumber().replaceAll("(.{" + 4 + "})", "$0 ").trim());
                lblCardname.setText(card.getCardName());
            }
        }
    }
    
    private void setDashboardForm(User_Detail user) {
        lblOmegaBalance.setText(UtilityHelper.toVND(user.getOmegaBalance()));
    }
    
    private void exportExcel() {
        try {
            JFileChooser jfc = new JFileChooser(System.getProperty("user.home") + "/Desktop");
            jfc.showSaveDialog(this);
            File saveFile = jfc.getSelectedFile();
            if (saveFile != null) {
                saveFile = new File(saveFile.toString() + ".xlsx");
                Workbook wb = new HSSFWorkbook();
                Sheet sheet = wb.createSheet("LastTransaction");
                Font defaultFont = wb.createFont();
                defaultFont.setBold(true);
                Row rowCol = sheet.createRow(0);
                for (int i = 0; i < tblLastTransaction.getColumnCount(); i++) {
                    Cell cell = rowCol.createCell(i);
                    cell.setCellValue(tblLastTransaction.getColumnName(i));
                }
                for (int j = 0; j < tblLastTransaction.getRowCount(); j++) {
                    Row row = sheet.createRow(j + 1);
                    for (int a = 0; a < tblLastTransaction.getColumnCount(); a++) {
                        Cell cell = row.createCell(a);
                        if (tblLastTransaction.getValueAt(j, a) != null) {
                            cell.setCellValue(tblLastTransaction.getValueAt(j, a).toString());
                        }
                    }
                }
                FileOutputStream fos = new FileOutputStream(new File(saveFile.toString()));
                wb.write(fos);
                wb.close();
                fos.close();
                if (MsgHelper.confirm(this, "Export excel successfully! Do you want to open it?")) {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(saveFile);
                }
            }
        } catch (HeadlessException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void exportPDF() {
        //1st way using built in JTable print method
        try {
            tblLastTransaction.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("TRANSACTION HISTORY"), new MessageFormat("THANK FOR USING OMEGAPAY!"));
        } catch (PrinterException ex) {
            Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        //2nd way using IText library

//        try {
//            JFileChooser chooser = new JFileChooser(System.getProperty("user.home") + "/Desktop");
//            int tmp = chooser.showSaveDialog(this);
//            File file = null;
//            if (tmp == JFileChooser.APPROVE_OPTION) {
//                file = chooser.getSelectedFile();
//                Document doc = new Document(PageSize.A4);
//                PdfWriter.getInstance(doc, new FileOutputStream(file + ".pdf"));
//                int colCount = tblLastTransaction.getColumnCount();
//                int rowCount = tblLastTransaction.getRowCount();
//                doc.open();
//                PdfPTable table = new PdfPTable(colCount);
//
//                //add title
//                Paragraph title = new Paragraph("TRANSACTION HISTORY", courier20);
//                Paragraph currentDate = new Paragraph(String.valueOf(new Date()), courier12);
//                title.setAlignment(Element.ALIGN_CENTER);
//                currentDate.setAlignment(Element.ALIGN_CENTER);
//                doc.add(title);
//                addEmptyLine(doc, 1);
//
//                //add header row
//                for (int i = 0; i < colCount; i++) {
//                    table.addCell(tblLastTransaction.getColumnName(i));
//                }
//                doc.add(table);
//
//                //add table contents
//                for (int j = 0; j < rowCount; j++) {
//                    table = new PdfPTable(colCount);
//                    for (int k = 0; k < colCount; k++) {
//                        table.addCell(String.valueOf(tblLastTransaction.getValueAt(j, k)));
//                    }
//                    doc.add(table);
//                }
//                doc.close();
//                if (MsgHelper.confirm(this, "Export PDF successfully! Do you want to open it?")) {
//                    Desktop desktop = Desktop.getDesktop();
//                    desktop.open(new File(file.toString() + ".pdf"));
//                }
//            }
//        } catch (HeadlessException | FileNotFoundException | DocumentException e) {
//            throw new RuntimeException(e);
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
    }

    //-------------------------- Card Section ---------------------------
    protected void initCard() {
        fillCardComboBox();
        fillCardTable();
        txtCurrentPIN.setDocument(new JTextFieldLimit(6));
        txtNewPIN.setDocument(new JTextFieldLimit(6));
        txtRetypePIN.setDocument(new JTextFieldLimit(6));
        clearCardForm();
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
    
    private void fillCardTable() {
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
            if (list != null) {
                for (Card c : list) {
                    Object[] row = {c.getCardID(), c.getCardNumber(), UtilityHelper.toVND(c.getCardBalance()), c.getCardName()};
                    model.addRow(row);
                }
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
        txtCardBalance.setText(UtilityHelper.toVND(e.getCardBalance()));
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

    //-------------------------- TransferSection ---------------------------
    protected void initTransfer() {
        User_Detail userDetail = detailDAO.selectByID(AuthUser.user.getOmegaAccount());
        lblCurrentOmegaBalance.setText(UtilityHelper.toVND(userDetail.getOmegaBalance()));
    }
    
    private void transferMoney() {
        Float amount = UtilityHelper.toFloat(txtAmount.getText());
        String toAccount = txtToAccount.getText();
        User_Login user_Login = loginDAO.selectByID(AuthUser.user.getUsername());
        User_Detail userDetail = detailDAO.selectByID(AuthUser.user.getOmegaAccount());
        User_Detail checkDestination = detailDAO.selectByID(toAccount);
        try {
            if (toAccount.equals(AuthUser.user.getOmegaAccount())) {
                MsgHelper.alert(this, "You can't send money to yourself!");
            } else if (checkDestination == null) {
                MsgHelper.alert(this, "Destination account is not exist!");
            } else if (amount > userDetail.getOmegaBalance()) {
                MsgHelper.alert(this, "Omega balance is not enough to transfer!");
            } else if ((toAccount.length() == 0) || (amount == 0)) {
                MsgHelper.alert(this, "Amount and account destination must not be empty!");
            } else if (amount < 50000) {
                MsgHelper.alert(this, "Minimum transfer is 50.000 VND");
            } else {
                SendPhone.send(userDetail.getPhone());
                String userInput = MsgHelper.promptInput(this, "The verification code was sent to your phone, enter the code to continue..");
                if (SendPhone.isCodeValid(userInput, user_Login)) {
                    Transaction tran = getTransferForm();
                    transDAO.insert(tran);
                    updateBalanceAfterTransfer(amount);
                    updateUserStatus();
                    initTransfer();
                    initDashboard();
                    initAccount();
                    initSaving();
                    if (MsgHelper.confirm(this, "Transfer successfully! Do you want to print and view receipt?")) {
                        printReceiptPDF();
                        clearTransferForm();
                    }
                } else {
                    MsgHelper.alert(this, "Your verification code is not correct!");
                }
                
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void updateUserStatus() {
        User_Detail user_Detail = detailDAO.selectByID(AuthUser.user.getOmegaAccount());
        if (user_Detail.getOmegaBalance() < 50000000) {
            user_Detail.setStatus(statusList[0]);
            detailDAO.update(user_Detail);
        } else if (user_Detail.getOmegaBalance() < 100000000) {
            user_Detail.setStatus(statusList[1]);
            detailDAO.update(user_Detail);
        } else {
            user_Detail.setStatus(statusList[2]);
            detailDAO.update(user_Detail);
        }
    }
    
    private void restrictNumericValueOnly(KeyEvent ke, JTextField txt) {
        if (!Character.isDigit(ke.getKeyChar())) {
            txt.setText("");
        }
    }
    
    private void updateBalanceAfterTransfer(float amount) {
        User_Detail fromAccount = detailDAO.selectByID(AuthUser.user.getOmegaAccount());
        User_Detail toAccount = detailDAO.selectByID(txtToAccount.getText());
        //save 2 accounts balance to 2 variable
        float fromBalance = fromAccount.getOmegaBalance();
        float toBalance = toAccount.getOmegaBalance();
        //subtract and add amount 
        fromAccount.setOmegaBalance(fromBalance - amount);
        toAccount.setOmegaBalance(toBalance + amount);
        //update to database
        detailDAO.updateBalance(fromAccount);
        detailDAO.updateBalance(toAccount);
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
    
    private void createNewFileIfNotExist(String fileStr) {
        try {
            File file = new File(fileStr);
            if (file.exists()) {
                file.delete();
            } else {
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void printReceiptPDF() {
        try {
            createNewFileIfNotExist(receiptFile);
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(receiptFile));
            document.open();
            //add title, content, footer
            addTitle(document);
            addContent(document);
            addFooter(document);
            document.close();

            //openfile
            Desktop desktop = Desktop.getDesktop();
            desktop.open(new File(receiptFile));
        } catch (DocumentException | FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void addTitle(Document document) throws DocumentException, IOException {
        addEmptyLine(document, 1);
        //Add OmegaPay logo to center
        Image img = Image.getInstance("photo/omegaSmall.png");
        img.setAlignment(Element.ALIGN_CENTER);
        document.add(img);
        //Add title to center
        Paragraph title = new Paragraph("OMEGAPAY BANKING RECEIPT", courier20);
        Paragraph currentDate = new Paragraph(String.valueOf(new Date()), courier12);
        title.setAlignment(Element.ALIGN_CENTER);
        currentDate.setAlignment(Element.ALIGN_CENTER);
        
        document.add(title);
        document.add(currentDate);
        addEmptyLine(document, 2);
        addDashLine(document);
        addEmptyLine(document, 2);
    }
    
    private void addContent(Document document) throws DocumentException {
        document.add(new Paragraph("Txn ID      : " + generateTxnID(), courier16));
        addEmptyLine(document, 2);
        document.add(new Paragraph("From Account: " + AuthUser.user.getOmegaAccount(), courier16));
        addEmptyLine(document, 2);
        document.add(new Paragraph("To Account  : " + txtToAccount.getText(), courier16));
        addEmptyLine(document, 2);
        document.add(new Paragraph("Amount      : " + txtAmount.getText() + " VND", courier16));
        addEmptyLine(document, 2);
        document.add(new Paragraph("Note        : " + txtNote.getText(), courier16));
        addEmptyLine(document, 2);
        addDashLine(document);
    }
    
    private void addFooter(Document document) throws DocumentException, IOException {
        addEmptyLine(document, 1);
        Paragraph paragraph = new Paragraph();
        paragraph.setFont(courier16);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        paragraph.add("THANK YOU FOR USING OUR APP\n");
        paragraph.add("FOR FURTHER INFORMATION SCAN THE QR CODE\n");
        document.add(paragraph);
        //add QR code image
        createQRCode(websites[new Random().nextInt(websites.length)], 200, 200);
        Image qrImage = Image.getInstance("photo/qrcode.png");
        qrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(qrImage);
    }

    //method for adding linebreak
    private void addEmptyLine(Document document, int number) throws DocumentException {
        for (int i = 0; i < number; i++) {
            document.add(new Paragraph("\n"));
        }
    }
    
    private void addDashLine(Document document) throws DocumentException {
        document.add(new LineSeparator());
    }
    
    private void createQRCode(String data, int height, int width) {
        try {
            QRCodeWriter qRCodeWriter = new QRCodeWriter();
            BitMatrix matrix = qRCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            //Write to file
            String outputFile = "photo/qrcode.png";
            Path savePath = FileSystems.getDefault().getPath(outputFile);
            MatrixToImageWriter.writeToPath(matrix, "PNG", savePath);
        } catch (WriterException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // generate random Txn ID with length 10
    private String generateTxnID() {
        return "" + (10000 + new Random().nextInt(90000)) + (10000 + new Random().nextInt(90000));
    }

    //-------------------------- Saving Section ---------------------------
    protected void initSaving() {
        //fill user balance and profit 
        User_Detail user_Detail = detailDAO.selectByID(AuthUser.user.getOmegaAccount());
        setSavingForm("", "0", user_Detail.getOmegaBalance(), 0);
        
    }
    
    private void setSavingForm(String savingName, String savingDuration, float balance, float profit) {
        lblSavingName.setText(savingName.toUpperCase());
        lblSavingDuration.setText(savingDuration);
        lblSavingDuration.setToolTipText(savingDuration + "");
        lblUserBalance.setText(UtilityHelper.toVND(balance));
        lblUserBalance.setToolTipText(String.valueOf(balance));
        lblUserProfit.setText(UtilityHelper.toVND(profit));
        lblUserProfit.setToolTipText(String.valueOf(profit));
        lblUserTotal.setText(UtilityHelper.toVND(balance + profit));
        lblUserTotal.setToolTipText(String.valueOf(balance + profit));
    }
    
    class Countdown extends Thread {
        
        String savingName;
        
        public Countdown(String savingName) {
            this.savingName = savingName;
        }
        
        @Override
        public void run() {
            User_Login user_Login = loginDAO.selectByID(AuthUser.user.getUsername());
            User_Detail user_Detail = detailDAO.selectByID(AuthUser.user.getOmegaAccount());
            float duration = 0F, interest = 0F;
            
            SendPhone.send(user_Detail.getPhone());
            String userInput = MsgHelper.promptInput(MainJFrame.this, "The verification code was sent to your phone, enter the code to continue..");
            if (SendPhone.isCodeValid(userInput, user_Login)) {
                MsgHelper.alert(null, "Successfully verified!");
                if (savingName.equals("alpha")) {
                    duration = 19;
                    interest = 1.1F;
                } else if (savingName.equals("epsilon")) {
                    duration = 30;
                    interest = 2.6F;
                } else if (savingName.equals("delta")) {
                    duration = Integer.MAX_VALUE;
                    interest = 0.1F;
                } else if (savingName.equals("omicron")) {
                    duration = 15;
                    interest = 3F;
                } else if (savingName.equals("sigma")) {
                    duration = 10;
                    interest = 1F;
                } else if (savingName.equals("iota")) {
                    duration = Integer.MAX_VALUE;
                    interest = 0.13F;
                }

                // formula: increase = Increase ÷ Original Number × 100
                float balance = user_Detail.getOmegaBalance();
                float increase = 0;
                for (int i = 1; i <= duration; i++) {
                    try {
                        Thread.sleep(1000);
                        //interest rate per second
                        increase += balance * interest / 100;
                        setSavingForm(savingName, "" + i, balance, increase);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } else {
                MsgHelper.alert(null, "Your verification code is not correct!");
            }
        }
    }
    
    private void stopThread(Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }
    
    private void redeem() {
        stopThread(count);
        User_Detail user_Detail = detailDAO.selectByID(AuthUser.user.getOmegaAccount());
        float balance = Float.parseFloat(lblUserTotal.getToolTipText());
        user_Detail.setOmegaBalance(balance);
        detailDAO.updateBalance(user_Detail);
        updateUserStatus();
        initAccount();
        initDashboard();
        initTransfer();
        initSaving();
        MsgHelper.alert(this, "Redeem successfully!!");
    }
    
    private void subscribeSaving(String savingName) {
        if (MsgHelper.confirm(this, "Do you want to subscribe to " + savingName + " plan?")) {
            count = null;
            count = new Countdown(savingName);
            count.start();
        }
    }

//-------------------------- Account Section ---------------------------
    protected void initAccount() {
        User_Detail user_Detail = detailDAO.selectByID(AuthUser.user.getOmegaAccount());
        this.defaultEditable();
        this.setAccountForm(user_Detail);
    }
    
    private void choosePhoto() {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home") + "/Desktop");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            ImageHelper.saveFile(file);
            ImageIcon icon = ImageHelper.readFile(file.getName(), lblPhoto);
            lblPhoto.setIcon(icon);
            lblPhoto.setToolTipText(file.getName());
        }
    }
    
    private void updateUserDetail() {
        User_Detail entity = getAccountForm();
        if (isAccountFormValid()) {
            try {
                detailDAO.update(entity);
                initAccount();
                MsgHelper.alert(this, "Update successfully!");
            } catch (Exception e) {
                MsgHelper.alert(this, "Update failed!");
            }
        }
    }
    
    private boolean isAccountFormValid() {
        boolean isValid = true;
        String error = "";
        if (txtFirstname.getText().equals("")) {
            error += "First name can not be empty!\n";
            isValid = false;
        }
        if (txtLastname.getText().equals("")) {
            error += "Last name can not be empty!\n";
            isValid = false;
        }
        if (!txtEmail.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            error += "Email is invalid!\n";
            isValid = false;
        }
        if (txtPhone.getText().length() < 10) {
            error += "Phone must have the length of 10!\n";
            isValid = false;
        }
        if (txtBirthday.getDate() == null) {
            error += "Birthday is invalid!\n";
            isValid = false;
        }
        if (txtAddress.getText().equals("")) {
            error += "Address can not be empty!\n";
            isValid = false;
        }
        if (!error.equals("")) {
            MsgHelper.alert(this, error);
        }
        return isValid;
    }
    
    private void setAccountForm(User_Detail e) {
        txtOmegaAccount.setText(e.getOmegaAccount());
        txtFirstname.setText(e.getFirstName());
        txtLastname.setText(e.getLastName());
        txtEmail.setText(e.getEmail());
        txtPhone.setText(e.getPhone());
        rdoMale.setSelected(e.getGender());
        rdoFemale.setSelected(!e.getGender());
        txtBirthday.setDate(e.getBirthday());
        txtAddress.setText(e.getAddress());
        txtDayCreated.setText(DateHelper.toString(e.getDayCreated(), "dd-MM-yyyy"));
        txtStatus.setText(e.getStatus());
        if (e.getPhoto() != null) {
            lblPhoto.setToolTipText(e.getPhoto());
            lblPhoto.setIcon(ImageHelper.readFile(e.getPhoto(), lblPhoto));
        }
        txtOmegaBalance.setText(UtilityHelper.toVND(e.getOmegaBalance()));
        lblBalance.setToolTipText(String.valueOf(e.getOmegaBalance()));
    }
    
    private User_Detail getAccountForm() {
        User_Detail entity = new User_Detail();
        entity.setOmegaAccount(txtOmegaAccount.getText());
        entity.setFirstName(txtFirstname.getText());
        entity.setLastName(txtLastname.getText());
        entity.setEmail(txtEmail.getText());
        entity.setPhone(txtPhone.getText());
        entity.setGender(rdoMale.isSelected());
        entity.setBirthday(txtBirthday.getDate());
        entity.setAddress(txtAddress.getText());
        entity.setDayCreated(DateHelper.toDate(txtDayCreated.getText(), "dd-MM-yyyy"));
        entity.setStatus(txtStatus.getText());
        entity.setPhoto(lblPhoto.getToolTipText());
        entity.setOmegaBalance(Float.parseFloat(lblBalance.getToolTipText()));
        return entity;
    }
    
    private void defaultEditable() {
        txtOmegaAccount.setEnabled(false);
        txtFirstname.setEnabled(false);
        txtLastname.setEnabled(false);
        txtAddress.setEnabled(false);
        txtEmail.setEnabled(false);
        txtPhone.setEnabled(false);
        txtBirthday.setEnabled(false);
        txtOmegaBalance.setEnabled(false);
        rdoFemale.setEnabled(false);
        rdoMale.setEnabled(false);
        txtDayCreated.setEnabled(false);
        txtStatus.setEnabled(false);
    }
    
    private void updateEditable() {
        //Can edit all except OmegaAccount, OmegaBalance, DayCreated and Status
        txtOmegaAccount.setEnabled(false);
        txtFirstname.setEnabled(true);
        txtLastname.setEnabled(true);
        txtAddress.setEnabled(true);
        txtEmail.setEnabled(true);
        txtPhone.setEnabled(true);
        txtBirthday.setEnabled(true);
        txtOmegaBalance.setEnabled(false);
        rdoFemale.setEnabled(true);
        rdoMale.setEnabled(true);
        txtDayCreated.setEnabled(false);
        txtStatus.setEnabled(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        genderGroup = new javax.swing.ButtonGroup();
        MainJPanel = new javax.swing.JPanel();
        leftJPanel = new javax.swing.JPanel();
        pnlHome = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pnlTransfer = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pnlSaving = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
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
        lblExpiry = new javax.swing.JLabel();
        lblCVVTitle = new javax.swing.JLabel();
        lblCardHolder = new javax.swing.JLabel();
        lblCardname = new javax.swing.JLabel();
        lblCardNumber = new javax.swing.JLabel();
        lblCardSection = new javax.swing.JLabel();
        lblLastTransaction = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblLastTransaction = new javax.swing.JTable();
        lblOverview = new javax.swing.JLabel();
        btnTransferMoney = new javax.swing.JButton();
        cboCards = new javax.swing.JComboBox<>();
        btnPDF = new javax.swing.JButton();
        btnExcel = new javax.swing.JButton();
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
        lblCurrentOmegaBalance = new javax.swing.JLabel();
        btnTransfer = new javax.swing.JButton();
        pnlSavingSection = new javax.swing.JPanel();
        pnlSavingList = new javax.swing.JPanel();
        pnlUpper = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        lblAPY = new javax.swing.JLabel();
        lblDuration = new javax.swing.JLabel();
        pnlSavingDiv0 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        lblName1 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        btnAlpha = new javax.swing.JButton();
        pnlSavingDiv1 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        lblName2 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        btnEpsilon = new javax.swing.JButton();
        pnlSavingDiv2 = new javax.swing.JPanel();
        jLabel49 = new javax.swing.JLabel();
        lblName3 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        btnDelta = new javax.swing.JButton();
        pnlSavingDiv3 = new javax.swing.JPanel();
        jLabel56 = new javax.swing.JLabel();
        lblName4 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        btnOmicron = new javax.swing.JButton();
        pnlSavingDiv4 = new javax.swing.JPanel();
        jLabel60 = new javax.swing.JLabel();
        lblName5 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        btnSigma = new javax.swing.JButton();
        pnlSavingDiv5 = new javax.swing.JPanel();
        jLabel64 = new javax.swing.JLabel();
        lblName6 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        btnIota = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        lblUserBalance = new javax.swing.JLabel();
        lblUserProfit = new javax.swing.JLabel();
        lblUserTotal = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        lblSavingName = new javax.swing.JLabel();
        lblSavingDuration = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        btnConfirm = new javax.swing.JButton();
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
        lblBalance = new javax.swing.JLabel();
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

        leftJPanel.add(pnlHome, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 130, 192, -1));

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

        leftJPanel.add(pnlTransfer, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 210, 192, -1));

        pnlSaving.setBackground(new java.awt.Color(255, 255, 255));
        pnlSaving.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlSaving.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlSavingMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlSavingMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlSavingMouseExited(evt);
            }
        });

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_stack_of_money_30px_1.png"))); // NOI18N

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("SAVING");

        javax.swing.GroupLayout pnlSavingLayout = new javax.swing.GroupLayout(pnlSaving);
        pnlSaving.setLayout(pnlSavingLayout);
        pnlSavingLayout.setHorizontalGroup(
            pnlSavingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingLayout.createSequentialGroup()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 15, Short.MAX_VALUE))
        );
        pnlSavingLayout.setVerticalGroup(
            pnlSavingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
        );

        leftJPanel.add(pnlSaving, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 290, -1, -1));

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

        leftJPanel.add(pnlAccount, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 370, -1, -1));

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

        leftJPanel.add(pnlCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 450, -1, -1));

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

        leftJPanel.add(pnlLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 530, -1, -1));

        lblOmegaPayTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblOmegaPayTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblOmegaPayTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblOmegaPayTitle.setText("OMEGAPAY");
        leftJPanel.add(lblOmegaPayTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 150, 30));

        lblUserName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUserName.setForeground(new java.awt.Color(255, 255, 255));
        lblUserName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblUserName.setText("USER: TO MINH TRI");
        leftJPanel.add(lblUserName, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, 190, -1));

        lblOmegaSymbol.setFont(new java.awt.Font("Segoe UI", 1, 50)); // NOI18N
        lblOmegaSymbol.setForeground(new java.awt.Color(255, 255, 255));
        lblOmegaSymbol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblOmegaSymbol.setText("Ω");
        leftJPanel.add(lblOmegaSymbol, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 20, 90, 50));

        rightJPanel.setBackground(new java.awt.Color(204, 204, 204));
        rightJPanel.setLayout(new java.awt.CardLayout());

        pnlHomeSection.setBackground(new java.awt.Color(255, 255, 255));
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

        lblExpiry.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblExpiry.setForeground(new java.awt.Color(255, 255, 255));
        lblExpiry.setText("11/25");
        pnlHomeSection.add(lblExpiry, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 470, 40, -1));

        lblCVVTitle.setFont(new java.awt.Font("Credit Card", 0, 12)); // NOI18N
        lblCVVTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblCVVTitle.setText("EXPIRY");
        pnlHomeSection.add(lblCVVTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 470, 40, -1));

        lblCardHolder.setFont(new java.awt.Font("Credit Card", 2, 11)); // NOI18N
        lblCardHolder.setForeground(new java.awt.Color(255, 255, 255));
        lblCardHolder.setText("TO MINH TRI");
        pnlHomeSection.add(lblCardHolder, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 470, 120, -1));

        lblCardname.setFont(new java.awt.Font("Courier New", 1, 14)); // NOI18N
        lblCardname.setForeground(new java.awt.Color(255, 255, 255));
        lblCardname.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCardname.setText("ARGIBANK");
        pnlHomeSection.add(lblCardname, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 360, 220, -1));

        lblCardNumber.setFont(new java.awt.Font("Credit Card", 0, 12)); // NOI18N
        lblCardNumber.setForeground(new java.awt.Color(255, 255, 255));
        lblCardNumber.setText("0375 9485 2934 2834");
        pnlHomeSection.add(lblCardNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 440, 200, 20));

        lblCardSection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/card_1.png"))); // NOI18N
        pnlHomeSection.add(lblCardSection, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 340, 260, -1));

        lblLastTransaction.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblLastTransaction.setForeground(new java.awt.Color(255, 255, 255));
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
        lblOverview.setForeground(new java.awt.Color(255, 255, 255));
        lblOverview.setText("OVERVIEW");
        pnlHomeSection.add(lblOverview, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 100, -1));

        btnTransferMoney.setBackground(new java.awt.Color(0, 112, 186));
        btnTransferMoney.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnTransferMoney.setForeground(new java.awt.Color(255, 255, 255));
        btnTransferMoney.setText("TRANSFER MONEY");
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
        cboCards.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboCardsActionPerformed(evt);
            }
        });
        pnlHomeSection.add(cboCards, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 310, 130, 20));

        btnPDF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_pdf_20px_1.png"))); // NOI18N
        btnPDF.setText("PDF");
        btnPDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPDFActionPerformed(evt);
            }
        });
        pnlHomeSection.add(btnPDF, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 30, 90, -1));

        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_file_excel_20px.png"))); // NOI18N
        btnExcel.setText("Excel");
        btnExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcelActionPerformed(evt);
            }
        });
        pnlHomeSection.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 30, 90, -1));

        rightJPanel.add(pnlHomeSection, "card2");

        pnlTransferSection.setBackground(new java.awt.Color(255, 255, 255));
        pnlTransferSection.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tabTransfer.setBackground(new java.awt.Color(255, 255, 255));
        tabTransfer.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tabTransfer.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        pnlOmegapayAcc.setBackground(new java.awt.Color(255, 255, 255));
        pnlOmegapayAcc.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtAmount.setText("100000");
        txtAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtAmountKeyTyped(evt);
            }
        });
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

        lblCurrentOmegaBalance.setForeground(new java.awt.Color(51, 255, 51));
        lblCurrentOmegaBalance.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCurrentOmegaBalance.setText("304.000 VND");
        pnlOmegapayAcc.add(lblCurrentOmegaBalance, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 100, 110, -1));

        tabTransfer.addTab("TO OMEGAPAY ACCOUNT", pnlOmegapayAcc);

        pnlTransferSection.add(tabTransfer, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 130, 390, 320));

        btnTransfer.setBackground(new java.awt.Color(0, 112, 186));
        btnTransfer.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnTransfer.setForeground(new java.awt.Color(255, 255, 255));
        btnTransfer.setText("TRANSFER");
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

        pnlSavingSection.setBackground(new java.awt.Color(255, 255, 255));
        pnlSavingSection.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlSavingList.setBackground(new java.awt.Color(255, 255, 255));
        pnlSavingList.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        pnlUpper.setBackground(new java.awt.Color(238, 0, 51));
        pnlUpper.setForeground(new java.awt.Color(255, 255, 255));

        lblName.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblName.setForeground(new java.awt.Color(255, 255, 255));
        lblName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblName.setText("Name");

        lblAPY.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblAPY.setForeground(new java.awt.Color(255, 255, 255));
        lblAPY.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAPY.setText("Interest / sec");

        lblDuration.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDuration.setForeground(new java.awt.Color(255, 255, 255));
        lblDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDuration.setText("Duration");

        javax.swing.GroupLayout pnlUpperLayout = new javax.swing.GroupLayout(pnlUpper);
        pnlUpper.setLayout(pnlUpperLayout);
        pnlUpperLayout.setHorizontalGroup(
            pnlUpperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUpperLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(lblAPY)
                .addGap(50, 50, 50)
                .addComponent(lblDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(140, Short.MAX_VALUE))
        );
        pnlUpperLayout.setVerticalGroup(
            pnlUpperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUpperLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlUpperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(lblAPY)
                    .addComponent(lblDuration))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pnlSavingDiv0.setBackground(new java.awt.Color(255, 255, 255));
        pnlSavingDiv0.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_1_30px.png"))); // NOI18N

        lblName1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblName1.setText("ALPHA");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(51, 255, 51));
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setText("1.1%");

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("19s");

        btnAlpha.setBackground(new java.awt.Color(238, 0, 51));
        btnAlpha.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnAlpha.setForeground(new java.awt.Color(255, 255, 255));
        btnAlpha.setText("Subscribe");
        btnAlpha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlphaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSavingDiv0Layout = new javax.swing.GroupLayout(pnlSavingDiv0);
        pnlSavingDiv0.setLayout(pnlSavingDiv0Layout);
        pnlSavingDiv0Layout.setHorizontalGroup(
            pnlSavingDiv0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv0Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblName1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54)
                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(btnAlpha)
                .addGap(33, 33, 33))
        );
        pnlSavingDiv0Layout.setVerticalGroup(
            pnlSavingDiv0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv0Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlSavingDiv0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlSavingDiv0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnAlpha)
                        .addComponent(jLabel26)
                        .addComponent(jLabel24)
                        .addComponent(lblName1)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pnlSavingDiv1.setBackground(new java.awt.Color(255, 255, 255));
        pnlSavingDiv1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_2_30px.png"))); // NOI18N

        lblName2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblName2.setText("EPSILON");

        jLabel40.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(51, 255, 51));
        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel40.setText("2.6%");

        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel43.setText("30s");

        btnEpsilon.setBackground(new java.awt.Color(238, 0, 51));
        btnEpsilon.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnEpsilon.setForeground(new java.awt.Color(255, 255, 255));
        btnEpsilon.setText("Subscribe");
        btnEpsilon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEpsilonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSavingDiv1Layout = new javax.swing.GroupLayout(pnlSavingDiv1);
        pnlSavingDiv1.setLayout(pnlSavingDiv1Layout);
        pnlSavingDiv1Layout.setHorizontalGroup(
            pnlSavingDiv1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblName2, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(jLabel40)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnEpsilon)
                .addGap(33, 33, 33))
        );
        pnlSavingDiv1Layout.setVerticalGroup(
            pnlSavingDiv1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlSavingDiv1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlSavingDiv1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnEpsilon)
                        .addComponent(jLabel43)
                        .addComponent(jLabel40)
                        .addComponent(lblName2)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pnlSavingDiv2.setBackground(new java.awt.Color(255, 255, 255));
        pnlSavingDiv2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));

        jLabel49.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_3_30px.png"))); // NOI18N

        lblName3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblName3.setText("DELTA");

        jLabel54.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel54.setForeground(new java.awt.Color(51, 255, 51));
        jLabel54.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel54.setText("0.1%");

        jLabel55.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel55.setText("Flexible");

        btnDelta.setBackground(new java.awt.Color(238, 0, 51));
        btnDelta.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnDelta.setForeground(new java.awt.Color(255, 255, 255));
        btnDelta.setText("Subscribe");
        btnDelta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeltaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSavingDiv2Layout = new javax.swing.GroupLayout(pnlSavingDiv2);
        pnlSavingDiv2.setLayout(pnlSavingDiv2Layout);
        pnlSavingDiv2Layout.setHorizontalGroup(
            pnlSavingDiv2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel49)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblName3, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel54, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72)
                .addComponent(jLabel55, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDelta)
                .addGap(33, 33, 33))
        );
        pnlSavingDiv2Layout.setVerticalGroup(
            pnlSavingDiv2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlSavingDiv2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlSavingDiv2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnDelta)
                        .addComponent(jLabel55)
                        .addComponent(jLabel54)
                        .addComponent(lblName3)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pnlSavingDiv3.setBackground(new java.awt.Color(255, 255, 255));
        pnlSavingDiv3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));

        jLabel56.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_4_30px.png"))); // NOI18N

        lblName4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblName4.setText("OMICRON");

        jLabel58.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel58.setForeground(new java.awt.Color(51, 255, 51));
        jLabel58.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel58.setText("3%");

        jLabel59.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel59.setText("15s");

        btnOmicron.setBackground(new java.awt.Color(238, 0, 51));
        btnOmicron.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnOmicron.setForeground(new java.awt.Color(255, 255, 255));
        btnOmicron.setText("Subscribe");
        btnOmicron.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOmicronActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSavingDiv3Layout = new javax.swing.GroupLayout(pnlSavingDiv3);
        pnlSavingDiv3.setLayout(pnlSavingDiv3Layout);
        pnlSavingDiv3Layout.setHorizontalGroup(
            pnlSavingDiv3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel56)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblName4, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel58, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72)
                .addComponent(jLabel59, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnOmicron)
                .addGap(33, 33, 33))
        );
        pnlSavingDiv3Layout.setVerticalGroup(
            pnlSavingDiv3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlSavingDiv3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel56, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlSavingDiv3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnOmicron)
                        .addComponent(jLabel59)
                        .addComponent(jLabel58)
                        .addComponent(lblName4)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pnlSavingDiv4.setBackground(new java.awt.Color(255, 255, 255));
        pnlSavingDiv4.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));

        jLabel60.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_5_30px.png"))); // NOI18N

        lblName5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblName5.setText("SIGMA");

        jLabel62.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel62.setForeground(new java.awt.Color(51, 255, 51));
        jLabel62.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel62.setText("1%");

        jLabel63.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel63.setText("10s");

        btnSigma.setBackground(new java.awt.Color(238, 0, 51));
        btnSigma.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnSigma.setForeground(new java.awt.Color(255, 255, 255));
        btnSigma.setText("Subscribe");
        btnSigma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSigmaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSavingDiv4Layout = new javax.swing.GroupLayout(pnlSavingDiv4);
        pnlSavingDiv4.setLayout(pnlSavingDiv4Layout);
        pnlSavingDiv4Layout.setHorizontalGroup(
            pnlSavingDiv4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel60)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblName5, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel62, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72)
                .addComponent(jLabel63, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSigma)
                .addGap(33, 33, 33))
        );
        pnlSavingDiv4Layout.setVerticalGroup(
            pnlSavingDiv4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlSavingDiv4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlSavingDiv4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSigma)
                        .addComponent(jLabel63)
                        .addComponent(jLabel62)
                        .addComponent(lblName5)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pnlSavingDiv5.setBackground(new java.awt.Color(255, 255, 255));
        pnlSavingDiv5.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));

        jLabel64.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icons8_6_30px.png"))); // NOI18N

        lblName6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblName6.setText("IOTA");

        jLabel66.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel66.setForeground(new java.awt.Color(51, 255, 51));
        jLabel66.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel66.setText("0.13%");

        jLabel67.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel67.setText("Flexible");

        btnIota.setBackground(new java.awt.Color(238, 0, 51));
        btnIota.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnIota.setForeground(new java.awt.Color(255, 255, 255));
        btnIota.setText("Subscribe");
        btnIota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIotaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSavingDiv5Layout = new javax.swing.GroupLayout(pnlSavingDiv5);
        pnlSavingDiv5.setLayout(pnlSavingDiv5Layout);
        pnlSavingDiv5Layout.setHorizontalGroup(
            pnlSavingDiv5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv5Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel64)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblName6, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel66, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72)
                .addComponent(jLabel67, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnIota)
                .addGap(33, 33, 33))
        );
        pnlSavingDiv5Layout.setVerticalGroup(
            pnlSavingDiv5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingDiv5Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlSavingDiv5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel64, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlSavingDiv5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnIota)
                        .addComponent(jLabel67)
                        .addComponent(jLabel66)
                        .addComponent(lblName6)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlSavingListLayout = new javax.swing.GroupLayout(pnlSavingList);
        pnlSavingList.setLayout(pnlSavingListLayout);
        pnlSavingListLayout.setHorizontalGroup(
            pnlSavingListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlUpper, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlSavingDiv0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlSavingDiv1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlSavingDiv2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlSavingDiv3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlSavingDiv4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlSavingDiv5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlSavingListLayout.setVerticalGroup(
            pnlSavingListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSavingListLayout.createSequentialGroup()
                .addComponent(pnlUpper, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSavingDiv0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSavingDiv1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSavingDiv2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSavingDiv3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSavingDiv4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSavingDiv5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 15, Short.MAX_VALUE))
        );

        pnlSavingSection.add(pnlSavingList, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel16.setText("BALANCE");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(51, 255, 51));
        jLabel17.setText("PROFIT");

        jLabel68.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel68.setText("TOTAL");

        lblUserBalance.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUserBalance.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUserBalance.setText("3.000.000 VND");

        lblUserProfit.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUserProfit.setForeground(new java.awt.Color(51, 255, 51));
        lblUserProfit.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUserProfit.setText("97.000 VND");

        lblUserTotal.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUserTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUserTotal.setText("3.097.000 VND");

        lblSavingName.setBackground(new java.awt.Color(238, 0, 51));
        lblSavingName.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        lblSavingName.setText("ALPHA");

        lblSavingDuration.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblSavingDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSavingDuration.setText("19s");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(lblSavingName, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblSavingDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblUserBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblUserProfit, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel68, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblUserTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator2))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSavingName)
                    .addComponent(lblSavingDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(lblUserBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(lblUserProfit, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel68)
                    .addComponent(lblUserTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        pnlSavingSection.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 130, 350, 280));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("OMEGA SAVING");
        pnlSavingSection.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 10, 350, -1));
        pnlSavingSection.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 60, 290, -1));

        btnConfirm.setBackground(new java.awt.Color(238, 0, 51));
        btnConfirm.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnConfirm.setForeground(new java.awt.Color(255, 255, 255));
        btnConfirm.setText("REDEEM");
        btnConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfirmActionPerformed(evt);
            }
        });
        pnlSavingSection.add(btnConfirm, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 420, 150, 30));

        rightJPanel.add(pnlSavingSection, "card4");

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

        lblBalance.setText("Omega Balance");

        txtOmegaBalance.setText("304.000 VND");

        genderGroup.add(rdoMale);
        rdoMale.setSelected(true);
        rdoMale.setText("Male");

        genderGroup.add(rdoFemale);
        rdoFemale.setText("Female");

        txtBirthday.setDateFormatString("yyyy-MM-dd");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlUp, javax.swing.GroupLayout.DEFAULT_SIZE, 898, Short.MAX_VALUE)
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
                        .addComponent(lblBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                                    .addComponent(lblBalance)
                                    .addComponent(txtOmegaBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(21, 21, 21)
                        .addComponent(jLabel39))
                    .addComponent(txtBirthday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 26, Short.MAX_VALUE))
        );

        btnUpdate.setText("UPDATE");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        lblPhoto.setBackground(new java.awt.Color(255, 255, 255));
        lblPhoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPhoto.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblPhoto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblPhotoMouseClicked(evt);
            }
        });

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
                .addComponent(lblPhoto, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(pnlMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAccountSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUpdate)
                    .addComponent(btnEdit))
                .addGap(92, 92, 92))
        );

        rightJPanel.add(pnlAccountSection, "card5");

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
        pnlCard2.add(lblATMCardName, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 40, 200, -1));

        lblATMCardNumber.setFont(new java.awt.Font("Credit Card", 0, 12)); // NOI18N
        lblATMCardNumber.setForeground(new java.awt.Color(255, 255, 255));
        lblATMCardNumber.setText("0375 9485 2934 2834");
        pnlCard2.add(lblATMCardNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 120, 200, 20));

        lblCardSection2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/card_1.png"))); // NOI18N
        pnlCard2.add(lblCardSection2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 260, -1));

        cboCardNames.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cboCardNames.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Agribank", " " }));
        cboCardNames.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cboCardNames.setEnabled(false);
        cboCardNames.setFocusable(false);
        cboCardNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboCardNamesActionPerformed(evt);
            }
        });

        txtExpirationDate.setEditable(false);
        txtExpirationDate.setText("1-11-2025");

        jLabel19.setText("Card name");

        txtCardNumber.setEditable(false);
        txtCardNumber.setText("650492834958");

        jLabel44.setText("Card number");

        txtCardHolder.setEditable(false);
        txtCardHolder.setText("To Minh Tri");

        jLabel45.setText("Cardholder's name");

        txtBillingAddress.setEditable(false);
        txtBillingAddress.setText("No.69 St. Truong Chinh, HCM");

        jLabel46.setText("Billing address");

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
        lblCardID.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCardID.setText("1");

        lblCardOrder1.setFont(new java.awt.Font("Courier New", 1, 14)); // NOI18N
        lblCardOrder1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCardOrder1.setText("Card");

        javax.swing.GroupLayout pnlCardDetail1Layout = new javax.swing.GroupLayout(pnlCardDetail1);
        pnlCardDetail1.setLayout(pnlCardDetail1Layout);
        pnlCardDetail1Layout.setHorizontalGroup(
            pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCardDetail1Layout.createSequentialGroup()
                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(lblRemoveCard, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addGap(200, 200, 200)
                        .addComponent(btnPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(219, 219, 219))
            .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addGap(109, 109, 109)
                        .addComponent(lblCardOrder1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCardID, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(lblAddNew, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlCard2, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                            .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(cboCardNames, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtExpirationDate, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtCardNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel45, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtCardHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel46, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtBillingAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25))
        );
        pnlCardDetail1Layout.setVerticalGroup(
            pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                                .addGroup(pnlCardDetail1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblCardID, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblCardOrder1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(213, 213, 213))
                            .addComponent(pnlCard2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(62, 62, 62)
                        .addComponent(lblAddNew)
                        .addGap(20, 20, 20)
                        .addComponent(lblRemoveCard))
                    .addGroup(pnlCardDetail1Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboCardNames, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                        .addComponent(txtCardHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel46)
                        .addGap(6, 6, 6)
                        .addComponent(txtBillingAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(60, 60, 60)
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addContainerGap(21, Short.MAX_VALUE))
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
                .addContainerGap(69, Short.MAX_VALUE))
        );

        rightJPanel.add(pnlCardSection, "card6");

        javax.swing.GroupLayout MainJPanelLayout = new javax.swing.GroupLayout(MainJPanel);
        MainJPanel.setLayout(MainJPanelLayout);
        MainJPanelLayout.setHorizontalGroup(
            MainJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MainJPanelLayout.createSequentialGroup()
                .addComponent(leftJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rightJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 922, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
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
            .addComponent(MainJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pnlHomeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlHomeMouseEntered
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(230, 230, 230));
    }//GEN-LAST:event_pnlHomeMouseEntered

    private void pnlHomeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlHomeMouseExited
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(255, 255, 255));
    }//GEN-LAST:event_pnlHomeMouseExited

    private void btnTransferMoneyMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTransferMoneyMouseEntered
        JButton b = (JButton) evt.getSource();
        b.setBackground(new Color(0, 79, 153));
    }//GEN-LAST:event_btnTransferMoneyMouseEntered

    private void btnTransferMoneyMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTransferMoneyMouseExited
        JButton b = (JButton) evt.getSource();
        b.setBackground(new Color(0, 112, 186));
    }//GEN-LAST:event_btnTransferMoneyMouseExited

    private void pnlTransferMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlTransferMouseClicked
        if (pnlTransfer == evt.getSource()) {
            pnlHomeSection.setVisible(false);
            pnlTransferSection.setVisible(true);
            pnlSavingSection.setVisible(false);
            pnlAccountSection.setVisible(false);
            pnlCardSection.setVisible(false);
        }
    }//GEN-LAST:event_pnlTransferMouseClicked

    private void pnlHomeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlHomeMouseClicked
        if (pnlHome == evt.getSource()) {
            pnlHomeSection.setVisible(true);
            pnlTransferSection.setVisible(false);
            pnlSavingSection.setVisible(false);
            pnlAccountSection.setVisible(false);
            pnlCardSection.setVisible(false);
        }
    }//GEN-LAST:event_pnlHomeMouseClicked

    private void pnlTransferMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlTransferMouseEntered
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(230, 230, 230));
    }//GEN-LAST:event_pnlTransferMouseEntered

    private void pnlTransferMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlTransferMouseExited
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(255, 255, 255));
    }//GEN-LAST:event_pnlTransferMouseExited

    private void btnTransferMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTransferMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTransferMouseEntered

    private void btnTransferMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTransferMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTransferMouseExited

    private void btnTransferMoneyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransferMoneyActionPerformed
        pnlHomeSection.setVisible(false);
        pnlTransferSection.setVisible(true);
        pnlSavingSection.setVisible(false);
        pnlAccountSection.setVisible(false);
        pnlCardSection.setVisible(false);
    }//GEN-LAST:event_btnTransferMoneyActionPerformed

    private void pnlCardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlCardMouseClicked
        pnlHomeSection.setVisible(false);
        pnlTransferSection.setVisible(false);
        pnlSavingSection.setVisible(false);
        pnlAccountSection.setVisible(false);
        pnlCardSection.setVisible(true);

    }//GEN-LAST:event_pnlCardMouseClicked

    private void pnlAccountMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlAccountMouseClicked
        pnlHomeSection.setVisible(false);
        pnlTransferSection.setVisible(false);
        pnlSavingSection.setVisible(false);
        pnlAccountSection.setVisible(true);
        pnlCardSection.setVisible(false);
    }//GEN-LAST:event_pnlAccountMouseClicked

    private void pnlAccountMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlAccountMouseEntered
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(230, 230, 230));
    }//GEN-LAST:event_pnlAccountMouseEntered

    private void pnlAccountMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlAccountMouseExited
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(255, 255, 255));
    }//GEN-LAST:event_pnlAccountMouseExited

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

    private void pnlCardMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlCardMouseEntered
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(230, 230, 230));
    }//GEN-LAST:event_pnlCardMouseEntered

    private void pnlCardMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlCardMouseExited
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(255, 255, 255));
    }//GEN-LAST:event_pnlCardMouseExited

    private void lblAddMoneyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAddMoneyMouseClicked
        new addMoneyJDialog(this, rootPaneCheckingEnabled).setVisible(true);
    }//GEN-LAST:event_lblAddMoneyMouseClicked

    private void btnSavePINActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSavePINActionPerformed
        changePIN();
    }//GEN-LAST:event_btnSavePINActionPerformed

    private void cboCardNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboCardNamesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboCardNamesActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        if (this.cardTableRow < tblCardList.getRowCount() - 1) {
            cardTableRow++;
            this.displayClickedCard();
        }
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        if (this.cardTableRow > 0) {
            cardTableRow--;
            this.displayClickedCard();
        }
    }//GEN-LAST:event_btnPrevActionPerformed

    private void tblCardListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCardListMouseClicked
        if (evt.getClickCount() == 2) {
            this.cardTableRow = tblCardList.getSelectedRow();
            this.displayClickedCard();
        }
    }//GEN-LAST:event_tblCardListMouseClicked

    private void lblAddNewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAddNewMouseClicked
        openAddCardDialog();
    }//GEN-LAST:event_lblAddNewMouseClicked

    private void lblAddNewMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAddNewMouseEntered
        JLabel label = (JLabel) evt.getSource();
        label.setForeground(new Color(204, 255, 102));
    }//GEN-LAST:event_lblAddNewMouseEntered

    private void lblAddNewMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAddNewMouseExited
        JLabel label = (JLabel) evt.getSource();
        label.setForeground(new Color(0, 255, 0));
    }//GEN-LAST:event_lblAddNewMouseExited

    private void lblRemoveCardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRemoveCardMouseClicked
        deleteCard();
    }//GEN-LAST:event_lblRemoveCardMouseClicked

    private void btnTransferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransferActionPerformed
        transferMoney();
    }//GEN-LAST:event_btnTransferActionPerformed

    private void lblPhotoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblPhotoMouseClicked
        choosePhoto();
    }//GEN-LAST:event_lblPhotoMouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        updateEditable();
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateUserDetail();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void txtAmountKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAmountKeyTyped
        restrictNumericValueOnly(evt, txtAmount);
    }//GEN-LAST:event_txtAmountKeyTyped

    private void cboCardsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboCardsActionPerformed
        fillCard();
    }//GEN-LAST:event_cboCardsActionPerformed

    private void btnPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPDFActionPerformed
        exportPDF();
    }//GEN-LAST:event_btnPDFActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        exportExcel();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void pnlSavingMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlSavingMouseClicked
        pnlHomeSection.setVisible(false);
        pnlTransferSection.setVisible(false);
        pnlSavingSection.setVisible(true);
        pnlAccountSection.setVisible(false);
        pnlCardSection.setVisible(false);
    }//GEN-LAST:event_pnlSavingMouseClicked

    private void pnlSavingMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlSavingMouseEntered
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(230, 230, 230));
    }//GEN-LAST:event_pnlSavingMouseEntered

    private void pnlSavingMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlSavingMouseExited
        JPanel homeJPanel = (JPanel) evt.getSource();
        homeJPanel.setBackground(new Color(255, 255, 255));
    }//GEN-LAST:event_pnlSavingMouseExited

    private void btnAlphaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlphaActionPerformed
        subscribeSaving("alpha");
    }//GEN-LAST:event_btnAlphaActionPerformed

    private void btnEpsilonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEpsilonActionPerformed
        subscribeSaving("epsilon");
    }//GEN-LAST:event_btnEpsilonActionPerformed

    private void btnOmicronActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOmicronActionPerformed
        subscribeSaving("omicron");
    }//GEN-LAST:event_btnOmicronActionPerformed

    private void btnSigmaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSigmaActionPerformed
        subscribeSaving("sigma");
    }//GEN-LAST:event_btnSigmaActionPerformed

    private void btnDeltaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeltaActionPerformed
        subscribeSaving("delta");
    }//GEN-LAST:event_btnDeltaActionPerformed

    private void btnIotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIotaActionPerformed
        subscribeSaving("iota");
    }//GEN-LAST:event_btnIotaActionPerformed

    private void btnConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmActionPerformed
        redeem();
    }//GEN-LAST:event_btnConfirmActionPerformed

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
            java.util.logging.Logger.getLogger(MainJFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
                new MainJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel MainJPanel;
    private javax.swing.JButton btnAlpha;
    private javax.swing.JButton btnConfirm;
    private javax.swing.JButton btnDelta;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnEpsilon;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnIota;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnOmicron;
    private javax.swing.JButton btnPDF;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnSavePIN;
    private javax.swing.JButton btnSigma;
    private javax.swing.JButton btnTransfer;
    private javax.swing.JButton btnTransferMoney;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cboCardNames;
    private javax.swing.JComboBox<String> cboCards;
    private javax.swing.ButtonGroup genderGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
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
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblAPY;
    private javax.swing.JLabel lblATMCardName;
    private javax.swing.JLabel lblATMCardNumber;
    private javax.swing.JLabel lblATMExpiry;
    private javax.swing.JLabel lblATMHolderName;
    private javax.swing.JLabel lblAccountInfoTitle;
    private javax.swing.JLabel lblAddMoney;
    private javax.swing.JLabel lblAddNew;
    private javax.swing.JLabel lblBalance;
    private javax.swing.JLabel lblCVVTitle;
    private javax.swing.JLabel lblCVVTitle1;
    private javax.swing.JLabel lblCardBalance;
    private javax.swing.JLabel lblCardHolder;
    private javax.swing.JLabel lblCardID;
    private javax.swing.JLabel lblCardNumber;
    private javax.swing.JLabel lblCardOrder1;
    private javax.swing.JLabel lblCardSection;
    private javax.swing.JLabel lblCardSection2;
    private javax.swing.JLabel lblCardname;
    private javax.swing.JLabel lblCurrentOmegaBalance;
    private javax.swing.JLabel lblDuration;
    private javax.swing.JLabel lblExpiry;
    private javax.swing.JLabel lblLastTransaction;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblName1;
    private javax.swing.JLabel lblName2;
    private javax.swing.JLabel lblName3;
    private javax.swing.JLabel lblName4;
    private javax.swing.JLabel lblName5;
    private javax.swing.JLabel lblName6;
    private javax.swing.JLabel lblOmegaBalance;
    private javax.swing.JLabel lblOmegaPayTitle;
    private javax.swing.JLabel lblOmegaSymbol;
    private javax.swing.JLabel lblOverview;
    private javax.swing.JLabel lblPhoto;
    private javax.swing.JLabel lblRemoveCard;
    private javax.swing.JLabel lblSavingDuration;
    private javax.swing.JLabel lblSavingName;
    private javax.swing.JLabel lblUserBalance;
    private javax.swing.JLabel lblUserName;
    private javax.swing.JLabel lblUserProfit;
    private javax.swing.JLabel lblUserTotal;
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
    private javax.swing.JPanel pnlSaving;
    private javax.swing.JPanel pnlSavingDiv0;
    private javax.swing.JPanel pnlSavingDiv1;
    private javax.swing.JPanel pnlSavingDiv2;
    private javax.swing.JPanel pnlSavingDiv3;
    private javax.swing.JPanel pnlSavingDiv4;
    private javax.swing.JPanel pnlSavingDiv5;
    private javax.swing.JPanel pnlSavingList;
    private javax.swing.JPanel pnlSavingSection;
    private javax.swing.JPanel pnlTransfer;
    private javax.swing.JPanel pnlTransferSection;
    private javax.swing.JPanel pnlUp;
    private javax.swing.JPanel pnlUpper;
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
