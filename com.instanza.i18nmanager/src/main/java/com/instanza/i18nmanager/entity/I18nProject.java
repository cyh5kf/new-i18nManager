package com.instanza.i18nmanager.entity;

/*


CREATE TABLE i18n_project
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name VARCHAR(200),
    type VARCHAR(20),
    languages VARCHAR(1024),
    created BIGINT,
    updated BIGINT
);





*/



//i18n_project
public class I18nProject {
    private int id;
    private String name;
    private String type;//单选:Desktop,Mobile,Web
    private String languages; //多选:#en#cn#es##

    private Long created;
    private Long updated;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }
}
