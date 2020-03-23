package net.projectwhitespace.phoneconnect;

import androidx.annotation.Nullable;

import java.util.Date;

public class Notification {
    public String getTitle() { return title;    }

    public void setTitle(String title) { this.title = title;    }

    public String getMessage() { return message;    }

    public void setMessage(String message) { this.message = message;    }

    public String getKey() { return key;    }

    public void setKey(String key) { this.key = key;    }

    public Date getTime() { return time;    }

    public void setTime(Date time) { this.time = time;   }

    private String title;
    private String message;
    private String key;
    private Date time;

    @Override
    public boolean equals(@Nullable Object obj) {

        if(obj != null) {
            Notification other = (Notification) obj;

            return title.equals(other.title) && message.equals(other.message) && time.equals(other.time);
        }
        return false;
    }
}
