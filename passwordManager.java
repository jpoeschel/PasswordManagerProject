import java.security.SecureRandom;
import java.util.*;
import java.lang.Object;
import java.security.spec.KeySpec;
import java.math.BigInteger;
import java.io.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class passwordManager {
    //class variables
    public static String hashedUser = "";  //hashed username for master account
    public static String hashedPass = ""; //hashed password for master account
    public static String passwordFileName = "storage.txt"; //text file name where all data will be stored
    public static SecretKeySpec skeySpec; //used for encryption/decryption key

    public static ArrayList<Account> accounts = new ArrayList<>(); //arraylist holding each account object with encrypted information

    //inner class to create account objects storing only encrypted values
    public static class Account {
        protected String email;
        protected String username;
        protected String password;
        protected String website;

        public Account(String e, String u, String p, String w){
          email = e;
          username = u;
          password = p;
          website = w;
        }
    }

    public static void main(String args[])
    {
        boolean userExists = false;
        try{
          userExists = readPasswordFile();
        }catch(FileNotFoundException e){
          System.out.println("Error "+e);
        }

        if(!userExists){
          masterInit();
        }
        login();

        Scanner menu1options = new Scanner(System.in);
        String menuInput = "";

        while(true){

          System.out.println("Add account - 1");
          System.out.println("Check for Existing Account - 2");
          System.out.println("Exit - 3");
          menuInput = menu1options.nextLine();

          if(menuInput.equals("1")){
            addAccount();
          }
          else if(menuInput.equals("2")){
            checkExisting();
          }
          else if(menuInput.equals("3")){
            System.exit(0);
          }
          else{
            System.out.println("Invalid input, enter option 1, 2, or 3");
          }
        }
    }
    //asks for user input to create new object, encrypts and updates ArrayList
    //update storage called at end.
    public static void addAccount() {
        String email = "";
        String username = "";
        String password = "";
        String website = "";
        Scanner add = new Scanner(System.in);

        while(true){
          System.out.println("Enter associated email");
          email = add.nextLine();
          if(email.length() > 0 && email.length() < 50){
            break;
          }
          else{
            System.out.println("Invalid Input, input length must be bewtween 1-50");
          }
        }
        while(true){
          System.out.println("Enter account username");
          username = add.nextLine();
          if(username.length() > 0 && username.length() < 50){
            break;
          }
          else{
            System.out.println("Invalid input, input length must be bewtween 1-50");
          }
        }
        while(true){
          System.out.println("Enter account password");
          System.out.println("Generated Password: " + generateRandom() + " (optional)");
          password = add.nextLine();
          if(password.length() > 0 && password.length() < 50){
            break;
          }
          else{
            System.out.println("Invalid Input, input length must be bewtween 1-50");
          }
        }
        while(true){
          System.out.println("Enter associated website");
          website = add.nextLine();
          if(website.length() > 0 && website.length() < 50){
            break;
          }
          else{
            System.out.println("Invalid input, input length must be bewtween 1-50");
          }
        }

        Account newAccount = new Account(encrypt(email), encrypt(username), encrypt(password), encrypt(website));
        accounts.add(newAccount);
        try {
          updateStorage();
        } catch(Exception e) {
          System.out.println("Error "+e);
        }
    }

    public static void checkExisting() {
        if(accounts.size() == 0){
          System.out.println("No Accounts Created");
          return;
        }
        Scanner websiteSearch = new Scanner(System.in);
        String searchInput = "";
        while(true){
          System.out.println("Enter website for account search");
          searchInput = websiteSearch.nextLine();
          if(searchInput.length() > 0 && searchInput.length() < 50){
            break;
          }
          else{
            System.out.println("Invalid input, input length must be bewtween 1-50");
          }
        }

        for(Account a : accounts){
          if(decrypt(a.website).equals(searchInput)){
            //acount found, display other menu
            System.out.println("Account found");
            //call function that displays next options
            secondaryOptions(a);
            return;
          }
        }
        System.out.println("Account not found");
    }

    public static void secondaryOptions(Account a) {
      Scanner menu2options = new Scanner(System.in);
      String menu2Input = "";
      while(true){
        System.out.println("Display Account Email, Username and Password - 1");
        System.out.println("Change Password - 2");
        System.out.println("Remove Account - 3");
        System.out.println("Exit - 4");
        menu2Input = menu2options.nextLine();
        if(menu2Input.equals("1")){
          System.out.println("Email: " + decrypt(a.email));
          System.out.println("Username: "+ decrypt(a.username));
          System.out.println("Password: "+ decrypt(a.password));
        }
        else if(menu2Input.equals("2")){
          changePassword(a);
        }
        else if(menu2Input.equals("3")){
          System.out.println("Are you sure? Y/n");
          Scanner uSure = new Scanner(System.in);
          String uSureString = uSure.nextLine();
          while(true){
            if(!uSureString.equals("Y") && !uSureString.equals("n")){
              System.out.println("Invalid Option");
              System.out.println("Are you sure? Y/n");
              uSureString = uSure.nextLine();

            }
            if(uSureString.equals("Y")){

              accounts.remove(a);
              try {
                updateStorage();
                readPasswordFile();
              } catch(Exception e) {
                System.out.println("Error: "+ e);
              }
              break;
            }
            if(uSureString.equals("n")){
              break;
            }
         }
         break;
        }
        else if(menu2Input.equals("4")){
          break;
        }
        else{
          System.out.println("Invalid input, enter option 1, 2, 3, or 4");
        }
      }
    }
    public static void changePassword(Account a){
      System.out.println("Generated Password:" + generateRandom() + " (optional)");
      Scanner newPass = new Scanner(System.in);
      System.out.println("Enter New Password");
      a.password = encrypt(newPass.nextLine());
      try {
        updateStorage();
      } catch(Exception e) {
        System.out.println("Error "+e);
      }
      System.out.println("Account Updated");
    }
    public static void login(){
      Scanner login = new Scanner(System.in);
      String inputUser = "";
      int inputUserLength = 0;
      String inputPass = "";
      int inputPassLength = 0;

      System.out.println("Enter Username");

      while(!inputUser.equals(hashedUser) || inputUserLength > 20){
        inputUser = login.nextLine();
        inputUserLength = inputUser.length();
        inputUser = hash(inputUser);  // Read user input
        if(!inputUser.equals(hashedUser)){
          System.out.println("Invalid username");
          System.out.println("Enter Username");
        }
      }
      Console c = System.console();
      char[] pw = {};
      while(!inputPass.equals(hashedPass) || inputPassLength > 16){
        if(c != null) {
          pw = c.readPassword("Passwpord: "); //hides user terminal input
        }
        inputPass = new String(pw);
        if(hash(inputPass).equals(hashedPass)){
          try {
            skeySpec = new SecretKeySpec(inputPass.getBytes("UTF-8"), "AES");
          } catch(Exception e) {
            System.out.println("Error "+e);
          }
        }
        inputPassLength = inputPass.length();
        inputPass = hash(inputPass);
        if(!inputPass.equals(hashedPass)){
          System.out.println("Invalid Password");
        }
      }
    }
    public static void masterInit(){
      Scanner createMaster = new Scanner(System.in);  // Create a Scanner object
      System.out.println("Enter master account username");
      String userName = "";

      while(userName.length() > 20 || userName.length() < 1){
        System.out.println("Username cannot be longer than 20 characters");
        System.out.println("Username cannot be empty");
        userName = createMaster.nextLine();  // Read user input
      }

      String enterkey = "default";
      String pass = generateRandom();
      System.out.println("Generated master password: " + pass);
      System.out.println("Write down and securely protect master password");
      hashedUser = hash(userName);
      userName = null;
      hashedPass = hash(pass);
      pass = null;
      System.gc();
      try {
        updateStorage();
      } catch(Exception e) {
        System.out.println("Error "+e);
      }
      while(!enterkey.isEmpty()){
        System.out.println("Press enter when ready to clear display and go to login");
        enterkey = createMaster.nextLine();
      }
      System.out.print("\033[H\033[2J");
      System.out.flush();
    }

    public static String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted) {
      try {
          Cipher cipher = Cipher.getInstance("AES");
          cipher.init(Cipher.DECRYPT_MODE, skeySpec);

          byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

          return new String(original);
      } catch (Exception ex) {
          ex.printStackTrace();
      }
      return null;
    }

    public static String hash(String password) {
      try {
        byte[] salt = {4,32,5,87,5,0,19,41,29,88,0,9,13,34,51,2};
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hashVal = factory.generateSecret(spec).getEncoded();
        BigInteger b =new BigInteger(1, hashVal);
        String s = b.toString(16);
        return s;
      } catch (Exception ex) {
          ex.printStackTrace();
      }
      return null;
    }

    public static String generateRandom() {
      try {
        String acceptable = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890`~!@#$%^&*()_+-={}|[]\\:;\"<>?,./'";
        StringBuilder val = new StringBuilder("");
        SecureRandom secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG", "SUN");
        for(int i = 0; i < 16; i++){
          val.append(acceptable.charAt(secureRandomGenerator.nextInt(acceptable.length())));
        }
        return val.toString();
      } catch (Exception ex) {
          ex.printStackTrace();
      }
      return null;
    }

    public static boolean readPasswordFile() throws FileNotFoundException{
        accounts = new ArrayList<>();
        Scanner passwordFile = new Scanner(new File(passwordFileName));
        String e = "";
        String n = "";
        String p = "";
        String w = "";

        passwordFile.useDelimiter("\n");
        if(!passwordFile.hasNext()){
          return false;
        }else{
          hashedUser = passwordFile.next();
          hashedPass = passwordFile.next();
          while(passwordFile.hasNext()){
            e = passwordFile.next();
            n = passwordFile.next();
            p = passwordFile.next();
            w = passwordFile.next();
            Account a = new Account(e,n,p,w);
            accounts.add(a);
          }
        }
        return true;
    }

    public static void updateStorage() throws IOException {
      PrintWriter writer = new PrintWriter(passwordFileName);
      writer.print("");
      writer.close();
      File output = new File(passwordFileName);
      FileOutputStream fos = new FileOutputStream(output);
      BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos));
      buffer.write(hashedUser);
      buffer.newLine();
      buffer.write(hashedPass);
      buffer.newLine();
      for(Account a : accounts){
          buffer.write(a.email);
          buffer.newLine();
          buffer.write(a.username);

          buffer.newLine();
          buffer.write(a.password);

          buffer.newLine();
          buffer.write(a.website);

          buffer.newLine();

      }
      buffer.close();
    }
}
