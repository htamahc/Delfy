package sample.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.bson.Document;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.StyleClassDecoration;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import sample.util.KeyHandler;
import sample.util.UserData;
import sample.util.UserProfile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

public class LogInController implements Initializable{
    public static ArrayList<Document> userProfiles;
    private double xOffset = 0;
    private double yOffset = 0;
    @FXML
    private JFXTextField textFieldName, textFieldEmail, textEmailSignIn;
    @FXML
    private JFXButton buttonSignInPane, buttonRegisterPane, buttonSignIn,buttonRegister;
    @FXML
    private AnchorPane paneSignIn, paneRegister;
    @FXML
    private JFXPasswordField passwordFieldKey , passwordFieldKey_re, passwordFieldSignIn ;

    @FXML
    private void handlePane( MouseEvent event) {

        if (event.getSource()==buttonRegisterPane){
            paneRegister.toFront();
        }else if (event.getSource()==buttonSignInPane){
            paneSignIn.toFront();
        }
    }

    public void handleSignIn (MouseEvent event) {
        try{
//            ValidationSupport validation = new ValidationSupport();
//            validation.registerValidator(textEmailSignIn, Validator.createEmptyValidator("User ID is required"));
//            Decorator.addDecoration(textEmailSignIn,new StyleClassDecoration("warning"));
            String email = textEmailSignIn.getText().trim();
            String logInPassphrase = passwordFieldSignIn.getText().trim();
//            if (!UserData.getInstance().authenticateUser(email,logInPassphrase)){
//                Alert alert = new Alert(Alert.AlertType.ERROR,"userID or passphrase incorrect");
//                alert.setHeaderText(null);
//                alert.showAndWait();
//                textEmailSignIn.clear();
//                passwordFieldSignIn.clear();
//
//            }else {
//                    Stage stage;
//                    Parent root;
//
//                    stage=(Stage) buttonSignIn.getScene().getWindow();
//                    root = FXMLLoader.load(getClass().getResource("/sample/userInterface/mainWindow.fxml"));
//                    Scene scene = new Scene(root);
//                    stage.setScene(scene);
//                    stage.show();
            if (UserData.getInstance().authenticateUser(email,logInPassphrase)){
                Stage stage;
                Parent root;

                stage=(Stage) buttonSignIn.getScene().getWindow();
                root = FXMLLoader.load(getClass().getResource("/sample/userInterface/mainWindow.fxml"));
                root.setOnMousePressed(e -> {
                    xOffset = e.getSceneX();
                    yOffset = e.getSceneY();
                });
                root.setOnMouseDragged(e -> {
                    stage.setX(e.getScreenX() - xOffset);
                    stage.setY(e.getScreenY() - yOffset);
                });
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
                }
                else {
                Alert alert = new Alert(Alert.AlertType.ERROR,"passphrase is incorrect");
                alert.setHeaderText(null);
                alert.showAndWait();
                passwordFieldSignIn.clear();
            }

        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR,"userID or passphrase incorrect");
            alert.setHeaderText(null);
            alert.showAndWait();
            textEmailSignIn.clear();
            passwordFieldSignIn.clear();

        }

    }
    public void handleRegister (MouseEvent event) {
        try{
            String name = textFieldName.getText().trim() ;
            String email = textFieldEmail.getText().trim();
            String passphrase = passwordFieldKey.getText().trim();
            String passphrase_re = passwordFieldKey_re.getText().trim();

            if (!UserData.validateName(name)){
                Alert alert = new Alert(Alert.AlertType.ERROR,"User name is not valid");
                alert.showAndWait();

                textFieldName.clear();

            }else if (!UserData.validateEmail(email)){
                Alert alert = new Alert(Alert.AlertType.ERROR,"User e-mail is not valid");
                alert.showAndWait();
                textFieldEmail.clear();
            }else if(!UserData.validatePassphrase(passphrase,passphrase_re)){
                Alert alert = new Alert(Alert.AlertType.ERROR,"passphrase mismatch or the passphrase is not valid");
                alert.showAndWait();
                passwordFieldKey.clear();
                passwordFieldKey_re.clear();
            }else{
                register();
                paneSignIn.toFront();
                textFieldName.clear();
                textFieldEmail.clear();
                passwordFieldKey.clear();
                passwordFieldKey_re.clear();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void register(){
        try{
            String name = textFieldName.getText().trim() ;
            String email = textFieldEmail.getText().trim();
            String passphrase = passwordFieldKey.getText().trim();
            byte[] salt = KeyHandler.getInstance().generateSalt();
            byte[] encryptedPassphrase = KeyHandler.getInstance().getEncryptedPassphrase(passphrase,salt);
            UserProfile user = new UserProfile(name, email, encryptedPassphrase, salt);
            UserData.getInstance().addUserProfile(user);
            UserData.getInstance().saveUserProfile(user);
            ObservableList<UserProfile> userProfiles = UserData.getInstance().getUserProfiles();

            Iterator<UserProfile> iterator = userProfiles.iterator();
            while (iterator.hasNext()){
                UserProfile profile = iterator.next();
                System.out.println("\n\n"+profile.getName() +"\n"+profile.getEmail()+"\n"+(profile.getEncryptedPassphrase().toString())+"\n"+(profile.getSalt().toString()));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UserData.getInstance().loadUserProfiles();
    }
}
