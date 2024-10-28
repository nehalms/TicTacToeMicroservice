package com.example.iNoteGames.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DbPlayer {

    private String _id;
    private String userId;
    private String userName;
    private TttStats tttStats;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public TttStats getTttStats() {
        return tttStats;
    }

    public void setTttStats(TttStats tttStats) {
        this.tttStats = tttStats;
    }
}
