package com.instanza.i18nmanager.entity;


/*




CREATE TABLE i18n_item
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    source_key VARCHAR(100),
    android_key VARCHAR(200),
    ios_key VARCHAR(200),
    android_formatted INT DEFAULT 0 NOT NULL,
    projects VARCHAR(500),
    created BIGINT,
    updated BIGINT,
    value_en LONGTEXT,
    value_ar LONGTEXT,
    value_es LONGTEXT,
    value_pt LONGTEXT,
    value_ru LONGTEXT,
    value_pl LONGTEXT,
    value_fa LONGTEXT,
    value_zh LONGTEXT,
    value_ms LONGTEXT,
    value_nl LONGTEXT,
    value_th LONGTEXT,
    value_tr LONGTEXT,
    value_uk LONGTEXT,
    value_vi LONGTEXT,
    value_fr LONGTEXT,
    value_de LONGTEXT,
    value_it LONGTEXT,
    value_ja LONGTEXT,
    value_hi LONGTEXT,
    value_hu LONGTEXT,
    value_id LONGTEXT,
    value_ko LONGTEXT,
    value_nb LONGTEXT,
    value_ca LONGTEXT,
    value_hr LONGTEXT,
    value_cs LONGTEXT,
    value_da LONGTEXT,
    value_fi LONGTEXT,
    value_el LONGTEXT,
    value_he LONGTEXT,
    value_ro LONGTEXT,
    value_sk LONGTEXT
);





*/




//i18n_item
public class I18nItem {

    private static final String EMPTY_STRING = "";

    private Integer id;
    private String source_key = EMPTY_STRING;
    private String android_key = EMPTY_STRING;
    private Integer android_formatted = 0;
    private String ios_key = EMPTY_STRING;

    //在哪些项目中使用
    private String projects = EMPTY_STRING; //#1#2#23321#2232#
    private Long created;
    private Long updated;


    private String value_en = EMPTY_STRING;
    private String value_ar = EMPTY_STRING;
    private String value_es = EMPTY_STRING;
    private String value_pt = EMPTY_STRING;
    private String value_ru = EMPTY_STRING;
    private String value_pl = EMPTY_STRING;
    private String value_fa = EMPTY_STRING;
    private String value_zh = EMPTY_STRING;
    private String value_ms = EMPTY_STRING;
    private String value_nl = EMPTY_STRING;
    private String value_th = EMPTY_STRING;
    private String value_tr = EMPTY_STRING;
    private String value_uk = EMPTY_STRING;
    private String value_vi = EMPTY_STRING;
    private String value_fr = EMPTY_STRING;
    private String value_de = EMPTY_STRING;
    private String value_it = EMPTY_STRING;
    private String value_ja = EMPTY_STRING;
    private String value_hi = EMPTY_STRING;
    private String value_hu = EMPTY_STRING;
    private String value_id = EMPTY_STRING;
    private String value_ko = EMPTY_STRING;
    private String value_nb = EMPTY_STRING;
    private String value_ca = EMPTY_STRING;
    private String value_hr = EMPTY_STRING;
    private String value_cs = EMPTY_STRING;
    private String value_da = EMPTY_STRING;
    private String value_fi = EMPTY_STRING;
    private String value_el = EMPTY_STRING;
    private String value_he = EMPTY_STRING;
    private String value_ro = EMPTY_STRING;
    private String value_sk = EMPTY_STRING;


    public I18nItem() {

    }


    public Integer getAndroid_formatted() {
        return android_formatted;
    }

    public void setAndroid_formatted(Integer android_formatted) {
        this.android_formatted = android_formatted;
    }

    public String getSource_key() {
        return source_key;
    }

    public void setSource_key(String source_key) {
        this.source_key = source_key;
    }

    public String getAndroid_key() {
        return android_key;
    }

    public void setAndroid_key(String android_key) {
        this.android_key = android_key;
    }

    public String getIos_key() {
        return ios_key;
    }

    public void setIos_key(String ios_key) {
        this.ios_key = ios_key;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public String getValue_en() {
        return value_en;
    }

    public void setValue_en(String value_en) {
        this.value_en = value_en;
    }

    public String getValue_es() {
        return value_es;
    }

    public void setValue_es(String value_es) {
        this.value_es = value_es;
    }

    public String getValue_pt() {
        return value_pt;
    }

    public void setValue_pt(String value_pt) {
        this.value_pt = value_pt;
    }

    public String getValue_ru() {
        return value_ru;
    }

    public void setValue_ru(String value_ru) {
        this.value_ru = value_ru;
    }

    public String getValue_ar() {
        return value_ar;
    }

    public void setValue_ar(String value_ar) {
        this.value_ar = value_ar;
    }

    public String getValue_de() {
        return value_de;
    }

    public void setValue_de(String value_de) {
        this.value_de = value_de;
    }

    public String getValue_fa() {
        return value_fa;
    }

    public void setValue_fa(String value_fa) {
        this.value_fa = value_fa;
    }

    public String getValue_fr() {
        return value_fr;
    }

    public void setValue_fr(String value_fr) {
        this.value_fr = value_fr;
    }

    public String getValue_hi() {
        return value_hi;
    }

    public void setValue_hi(String value_hi) {
        this.value_hi = value_hi;
    }

    public String getValue_hu() {
        return value_hu;
    }

    public void setValue_hu(String value_hu) {
        this.value_hu = value_hu;
    }

    public String getValue_id() {
        return value_id;
    }

    public void setValue_id(String value_id) {
        this.value_id = value_id;
    }

    public String getValue_it() {
        return value_it;
    }

    public void setValue_it(String value_it) {
        this.value_it = value_it;
    }

    public String getValue_ja() {
        return value_ja;
    }

    public void setValue_ja(String value_ja) {
        this.value_ja = value_ja;
    }

    public String getValue_ko() {
        return value_ko;
    }

    public void setValue_ko(String value_ko) {
        this.value_ko = value_ko;
    }

    public String getValue_ms() {
        return value_ms;
    }

    public void setValue_ms(String value_ms) {
        this.value_ms = value_ms;
    }

    public String getValue_nl() {
        return value_nl;
    }

    public void setValue_nl(String value_nl) {
        this.value_nl = value_nl;
    }

    public String getValue_pl() {
        return value_pl;
    }

    public void setValue_pl(String value_pl) {
        this.value_pl = value_pl;
    }


    public String getValue_th() {
        return value_th;
    }

    public void setValue_th(String value_th) {
        this.value_th = value_th;
    }

    public String getValue_tr() {
        return value_tr;
    }

    public void setValue_tr(String value_tr) {
        this.value_tr = value_tr;
    }

    public String getValue_uk() {
        return value_uk;
    }

    public void setValue_uk(String value_uk) {
        this.value_uk = value_uk;
    }

    public String getValue_vi() {
        return value_vi;
    }

    public void setValue_vi(String value_vi) {
        this.value_vi = value_vi;
    }

    public String getValue_zh() {
        return value_zh;
    }

    public void setValue_zh(String value_zh) {
        this.value_zh = value_zh;
    }

    public String getValue_cs() {
        return value_cs;
    }

    public void setValue_cs(String value_cs) {
        this.value_cs = value_cs;
    }

    public String getValue_hr() {
        return value_hr;
    }

    public void setValue_hr(String value_hr) {
        this.value_hr = value_hr;
    }

    public String getValue_da() {
        return value_da;
    }

    public void setValue_da(String value_da) {
        this.value_da = value_da;
    }

    public String getValue_fi() {
        return value_fi;
    }

    public void setValue_fi(String value_fi) {
        this.value_fi = value_fi;
    }

    public String getValue_el() {
        return value_el;
    }

    public void setValue_el(String value_el) {
        this.value_el = value_el;
    }

    public String getValue_ro() {
        return value_ro;
    }

    public void setValue_ro(String value_ro) {
        this.value_ro = value_ro;
    }

    public String getValue_sk() {
        return value_sk;
    }

    public void setValue_sk(String value_sk) {
        this.value_sk = value_sk;
    }

    public String getValue_ca() {
        return value_ca;
    }

    public void setValue_ca(String value_ca) {
        this.value_ca = value_ca;
    }

    public String getValue_he() {
        return value_he;
    }

    public void setValue_he(String value_he) {
        this.value_he = value_he;
    }

    public String getValue_nb() {
        return value_nb;
    }

    public void setValue_nb(String value_nb) {
        this.value_nb = value_nb;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
