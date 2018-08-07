package telegram.bot.rules.like;

import java.io.Serializable;
import java.util.ArrayList;

class Like implements Serializable {
    ArrayList<Integer> usersWhoLiked;
    ArrayList<Integer> usersWhoDisLiked;

    Like() {
        this.usersWhoLiked = new ArrayList<>();
        this.usersWhoDisLiked = new ArrayList<>();
    }
}
