package co.com.expressdelnorte.expressdelnorte.models;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class User implements Serializable {
    private String username;
    private String password;
    private boolean piscinero;

    public User(String username, String password, boolean piscinero) {
        this.username = username;
        this.password = password;
        this.piscinero = piscinero;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isPiscinero() {
        return piscinero;
    }

    public void save(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput("user", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void delete(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput("user", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(null);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static User get(Context context) {
        try {
            FileInputStream fis = context.openFileInput("user");
            ObjectInputStream is = new ObjectInputStream(fis);
            User user = (User) is.readObject();
            is.close();
            fis.close();
            return user;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
