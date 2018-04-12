package sample.util;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bson.Document;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

public class UserData {
    private static MongoDatabase database = DbConnect.getInstance().getDatabase();
    private static MongoCollection userCollection = database.getCollection("users");

    private ObservableList<UserProfile> userProfiles;
    private static UserData instance = new UserData();

    public static UserData getInstance() {
        return instance;
    }

    public ObservableList<UserProfile> getUserProfiles() {
        return userProfiles;
    }

    public void addUserProfile (UserProfile userProfile){
        userProfiles.add(userProfile);
    }

    public void loadUserProfiles(){
        userProfiles = FXCollections.observableArrayList();
        }

    public static UserProfile getUserData(String email){
        UserProfile user = new UserProfile();
        FindIterable<Document> findIterable = userCollection.find(com.mongodb.client.model.Filters.eq("_id",email));
        for (Document document : findIterable){
            String name = document.get("name").toString().trim();
            String email1 = document.get("_id").toString().trim();
            byte[] en_passphrase = decodeBase64(document.get("encryptedPassphrase").toString().trim());
            byte[] salt = decodeBase64(document.get("salt").toString().trim());
            user = new UserProfile(name,email1,en_passphrase,salt);
            UserData.getInstance().addUserProfile(user);
        }
        return user;
    }

    public void saveUserProfile(UserProfile userProfile){
        String encryptedPassphrase = encodeBase64String(userProfile.getEncryptedPassphrase());
        String salt = encodeBase64String(userProfile.getSalt());
        Document doc = new Document("_id",userProfile.getEmail())
                .append("name",userProfile.getName())
                .append("encryptedPassphrase",encryptedPassphrase)
                .append("salt",salt)
                .append("files", new Document("file","file1").append("date","date1").append("location","path1").append("algorithm","AES"));
        userCollection.insertOne(doc);
    }

    public static boolean validateName(String name) {
        String regex = "^[\\p{L} .'-]+$";
        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

    public static boolean validateEmail(String email) {
        String regex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.find();
    }

    public static boolean validatePassphrase(String passphrase, String passphrase_re){
        boolean flag;
        if (passphrase.equals(passphrase_re)){
            String regex = "^"
                            + "(?=.*\\d)"
                            + "(?=.*[a-z])"
                            + "(?=.*[A-Z])"
                            + "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])"
                            + "."
                            + "{6,15}"
                            + "$";
            /**
             * 	- contains at least 1 digit
             * 	- contains at least 1 lowercase letter
             * 	- contains at least 1 uppercase letter
             * 	- contains at least 1 of the special
             * 		characters !@#$%^&*()_+\-=[]{};':"\|,.<>/?
             * 	- is at least 6 character long
             * 	- is at most 15 characters long
             * 	(note : for best practices you could even set
             * 	the max length to a higher value )
             */
            Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(passphrase);
            flag = matcher.find();
        }else{
            flag = false;
        }
        return flag;
    }

    public  boolean authenticateUser(String email, String passphrase) throws InvalidKeySpecException, NoSuchAlgorithmException {

        UserProfile user = UserData.getUserData(email);
        return KeyHandler.getInstance().authenticate(passphrase,user.getEncryptedPassphrase(),user.getSalt());
    }
}
