package net.agnusvox.lydl1;

/**
 * Created by ken on 6/9/2017.
 */

public class PrfProgram {
    private int pid;
    private String title;
    private String picture;
    private boolean liked;

    public PrfProgram(int pid,String title,String picture, boolean liked) {
        this.pid = pid;
        this.title = title;
        this.picture = picture;
        this.liked = liked;
    }
    public int getPid(){
        return pid;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public boolean getLiked () { return liked; }
    public int getLikedInt() { return liked ? 1 : 0; }
    public void setLiked (boolean liked ) { this.liked = liked; }

    public String getPicture(){ return picture; }
    public void setPicture(String picture){
        this.picture = picture;
    }

    public void toggleLiked(){
        if ( this.liked == true) {
            this.liked = false;
        } else {
            this.liked = true;
        }
    }
}
