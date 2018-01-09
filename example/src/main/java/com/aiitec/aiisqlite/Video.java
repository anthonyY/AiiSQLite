package com.aiitec.aiisqlite;

import com.aiitec.openapi.db.annotation.Column;
import com.aiitec.openapi.db.annotation.Unique;

/**
 * @author Anthony
 * @version 1.0
 *          createTime 2018/1/9.
 */

public class Video {

    @Unique
    @Column("audio_id")
    int audioId = 0;
    String title ;
    int audio_type = 0;
    int study_num = 0;
    int image = 0;
    String imagePath;
    String squareListImg;
    String audio_synopsis;
    String play_path;
    String play_length ;
    String timestamp ;
    long time = 0;
    int is_auditions = 0;
    int courseId = 0;
    String path;

    public int getAudioId() {
        return audioId;
    }

    public void setAudioId(int audioId) {
        this.audioId = audioId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAudio_type() {
        return audio_type;
    }

    public void setAudio_type(int audio_type) {
        this.audio_type = audio_type;
    }

    public int getStudy_num() {
        return study_num;
    }

    public void setStudy_num(int study_num) {
        this.study_num = study_num;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getSquareListImg() {
        return squareListImg;
    }

    public void setSquareListImg(String squareListImg) {
        this.squareListImg = squareListImg;
    }

    public String getAudio_synopsis() {
        return audio_synopsis;
    }

    public void setAudio_synopsis(String audio_synopsis) {
        this.audio_synopsis = audio_synopsis;
    }

    public String getPlay_path() {
        return play_path;
    }

    public void setPlay_path(String play_path) {
        this.play_path = play_path;
    }

    public String getPlay_length() {
        return play_length;
    }

    public void setPlay_length(String play_length) {
        this.play_length = play_length;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getIs_auditions() {
        return is_auditions;
    }

    public void setIs_auditions(int is_auditions) {
        this.is_auditions = is_auditions;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
