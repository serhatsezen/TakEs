package app.tez.com.takes.block.Models;

/**
 * Created by serhat on 3.05.2018.
 */

public class PostDTO {

    String name;
    String description;
    String image;

    public PostDTO(String name, String description, String image ) {
        this.name=name;
        this.description=description;
        this.image=image;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

}
