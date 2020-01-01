package net.projectwhitespace.phoneconnect;

public class Settings {

    // Singleton
    private static Settings settings = new Settings();
    private Settings() {};
    public static Settings getInstance(){
        return settings;
    }


    public String SERVER_ID = "";
    public String SERVER_IP = "192.168.1.22";
    public final int SERVER_PORT = 25000;
    public String BROADCAST_IP = "192.168.1.255";
    public final int BROADCAST_PORT = 25001;


    public String KEY = "1234567890";

}