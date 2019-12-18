package net.projectwhitespace.phoneconnect;

import androidx.annotation.Nullable;

import java.util.Date;

public class Notification {
    public String title;
    public String message;
    public String key;
    public Date time;

    @Override
    public boolean equals(@Nullable Object obj) {

        Notification other = (Notification)obj;

        return title.equals(other.title) && message.equals(other.message) && time.equals(other.time);
    }
}
