package net.projectwhitespace.phoneconnect;

public class Settings {

    // Singleton
    private static Settings settings = new Settings();
    private Settings() {};
    public static Settings getInstance(){
        return settings;
    }


    public String PCip = "192.168.1.22";
    public String KEY = "1234567890";

}