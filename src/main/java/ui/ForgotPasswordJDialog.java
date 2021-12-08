/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import com.formdev.flatlaf.FlatLightLaf;
import dao.UserDetailDAO;
import dao.UserLoginDAO;
import entity.User_Detail;
import entity.User_Login;
import helper.MsgHelper;
import helper.SendGmail;
import helper.SendPhone;
import helper.UtilityHelper;
import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author balis
 */
public class ForgotPasswordJDialog extends javax.swing.JDialog {

    UserDetailDAO detailDAO = new UserDetailDAO();
    UserLoginDAO loginDAO = new UserLoginDAO();

    /**
     * Creates new form ForgotPasswordJDialog
     */
    public ForgotPasswordJDialog(JDialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
        init();
        setLocationRelativeTo(parent);
        setTitle("Forgot password");
    }

    private void init() {
        int selected = UtilityHelper.selectedTab;
        if (selected == 0) {
            tabs.setSelectedIndex(0);
        } else {
            tabs.setSelectedIndex(1);
        }
    }

    private void sendSMS() {
        // isFormValid(false) is for validating SMS form
        if (isSMSFormValid()) {
            User_Login user_Login = loginDAO.selectByID(txtUsernameSMS.getText());
            SendPhone.send(txtPhoneSMS.getText());
            String userInput = MsgHelper.promptInput(this, "The verification code was sent to your phone, enter the code to continue..");
            if (SendPhone.isCodeValid(userInput, user_Login)) {
                MsgHelper.alert(this, "Successfully verified! Your password is: " + user_Login.getPassword());
            } else {
                MsgHelper.alert(this, "Your verification code is not correct!");
            }
            this.dispose();
        }
    }

    private boolean isSMSFormValid() {
        boolean isValid = true;
        String error = "";
        User_Detail userDetail = detailDAO.selectByID(txtOmegaAccountSMS.getText());
        User_Login userLogin = loginDAO.selectByID(txtUsernameSMS.getText());
        if (userDetail == null) {
            error += "Omega Account is not valid\n";
            error += "Phone is not valid\n";
            isValid = false;
        }
        if (userLogin == null) {
            error += "Username is not valid\n";
            isValid = false;
        }
        if (userDetail != null) {
            if (!userDetail.getPhone().equals(txtPhoneSMS.getText())) {
                error += "Phone is not valid\n";
                isValid = false;
            }
        }
        if (!error.equals("")) {
            MsgHelper.alert(this, error);
        }
        return isValid;
    }

    // ----------------------------- Email section ----------------------------
    private void sendEmail() {
        if (isEmailFormValid()) {
            String code = UtilityHelper.randomString(6);
            Thread t = new Thread() {
                @Override
                public void run() {
                    SendGmail.sendMail(txtEmail.getText(), "Verification Code", "Your verification Code is : " + code);
                }
            };
            t.start();
            String userInput = MsgHelper.promptInput(this, "The verification code was sent to your email, enter the code to continue..");
            verifyEmailCode(userInput, code);
            this.dispose();
        }
    }

    private void verifyEmailCode(String input, String EmailCode) {
        if (input.equals(EmailCode)) {
            User_Login userLogin = loginDAO.selectByID(txtUsernameEmail.getText());
            MsgHelper.alert(this, "Your email is successfully verify!\n"
                    + "Your password is: " + userLogin.getPassword());
        } else {
            MsgHelper.alert(this, "Email Code is not valid!!");
        }
    }

    private boolean isEmailFormValid() {
        boolean isValid = true;
        String error = "";
        User_Detail userDetail = detailDAO.selectByID(txtOmegaAccountEmail.getText());
        User_Login userLogin = loginDAO.selectByID(txtUsernameEmail.getText());
        if (userDetail == null) {
            error += "Omega Account is not valid\n";
            isValid = false;
        } else if (userLogin == null) {
            error += "Username is not valid\n";
            isValid = false;
        } else if (userDetail != null) {
            if (!txtEmail.getText().equals(userDetail.getEmail())) {
                error += "Email is not valid\n";
                isValid = false;
            }
        }
        if (!error.equals("")) {
            MsgHelper.alert(this, error);
        }
        return isValid;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMain = new javax.swing.JPanel();
        pnlUp = new javax.swing.JPanel();
        lblRestorePassTitle = new javax.swing.JLabel();
        tabs = new javax.swing.JTabbedPane();
        tabEmail = new javax.swing.JPanel();
        txtOmegaAccountEmail = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtUsernameEmail = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnSendToEmail = new javax.swing.JButton();
        tabSMS = new javax.swing.JPanel();
        txtOmegaAccountSMS = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtUsernameSMS = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtPhoneSMS = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        btnSendToPhone = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlMain.setBackground(new java.awt.Color(255, 255, 255));

        pnlUp.setBackground(new java.awt.Color(238, 0, 51));
        pnlUp.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblRestorePassTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblRestorePassTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblRestorePassTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRestorePassTitle.setText("FORGOT PASSWORD");
        pnlUp.add(lblRestorePassTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 285, -1));

        tabs.setBackground(new java.awt.Color(255, 255, 255));

        tabEmail.setBackground(new java.awt.Color(255, 255, 255));

        txtOmegaAccountEmail.setText("690078902233");

        jLabel1.setText("Enter Omega Account");

        txtUsernameEmail.setText("To Minh Tri");

        jLabel2.setText("Enter Username");

        txtEmail.setText("tritmps15506@fpt.edu.vn");

        jLabel3.setText("Enter email to receive password");

        btnSendToEmail.setBackground(new java.awt.Color(238, 0, 51));
        btnSendToEmail.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnSendToEmail.setForeground(new java.awt.Color(255, 255, 255));
        btnSendToEmail.setText("SEND TO EMAIL");
        btnSendToEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendToEmailActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tabEmailLayout = new javax.swing.GroupLayout(tabEmail);
        tabEmail.setLayout(tabEmailLayout);
        tabEmailLayout.setHorizontalGroup(
            tabEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabEmailLayout.createSequentialGroup()
                .addContainerGap(69, Short.MAX_VALUE)
                .addGroup(tabEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUsernameEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtOmegaAccountEmail)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSendToEmail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(61, 61, 61))
        );
        tabEmailLayout.setVerticalGroup(
            tabEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabEmailLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtUsernameEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtOmegaAccountEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(btnSendToEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        tabs.addTab("Email", tabEmail);

        tabSMS.setBackground(new java.awt.Color(255, 255, 255));

        txtOmegaAccountSMS.setText("690078902233");

        jLabel4.setText("Enter Omega Account");

        txtUsernameSMS.setText("To Minh Tri");

        jLabel5.setText("Enter Username");

        txtPhoneSMS.setText("0396069932");

        jLabel6.setText("Enter phonenumber to receive password");

        btnSendToPhone.setBackground(new java.awt.Color(238, 0, 51));
        btnSendToPhone.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnSendToPhone.setForeground(new java.awt.Color(255, 255, 255));
        btnSendToPhone.setText("SEND TO PHONE");
        btnSendToPhone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendToPhoneActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tabSMSLayout = new javax.swing.GroupLayout(tabSMS);
        tabSMS.setLayout(tabSMSLayout);
        tabSMSLayout.setHorizontalGroup(
            tabSMSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabSMSLayout.createSequentialGroup()
                .addContainerGap(69, Short.MAX_VALUE)
                .addGroup(tabSMSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtPhoneSMS, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(txtUsernameSMS, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtOmegaAccountSMS)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSendToPhone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(61, 61, 61))
        );
        tabSMSLayout.setVerticalGroup(
            tabSMSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabSMSLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtUsernameSMS, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtOmegaAccountSMS, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPhoneSMS, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(btnSendToPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        tabs.addTab("SMS", tabSMS);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlUp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tabs)
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addComponent(pnlUp, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(tabs))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSendToEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendToEmailActionPerformed
        sendEmail();
    }//GEN-LAST:event_btnSendToEmailActionPerformed

    private void btnSendToPhoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendToPhoneActionPerformed
        sendSMS();
    }//GEN-LAST:event_btnSendToPhoneActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        FlatLightLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF");
        }
        //</editor-fold>
        FlatLightLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF");
        }
        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ForgotPasswordJDialog dialog = new ForgotPasswordJDialog(new JDialog(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSendToEmail;
    private javax.swing.JButton btnSendToPhone;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel lblRestorePassTitle;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlUp;
    private javax.swing.JPanel tabEmail;
    private javax.swing.JPanel tabSMS;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtOmegaAccountEmail;
    private javax.swing.JTextField txtOmegaAccountSMS;
    private javax.swing.JTextField txtPhoneSMS;
    private javax.swing.JTextField txtUsernameEmail;
    private javax.swing.JTextField txtUsernameSMS;
    // End of variables declaration//GEN-END:variables
}
