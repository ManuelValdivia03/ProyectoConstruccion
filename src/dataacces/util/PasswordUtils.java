package dataacces.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    private static final int HASH_ROUNDS = 12;

    public static String hashPassword(String plainPassword) {
        if(plainPassword == null || plainPassword.trim().isEmpty()){
            throw new IllegalArgumentException("La contraseña no puede estar vacia");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(HASH_ROUNDS));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try{
            return BCrypt.checkpw(plainPassword, hashedPassword);
        }catch(Exception e){
            return false;
        }
    }
}
