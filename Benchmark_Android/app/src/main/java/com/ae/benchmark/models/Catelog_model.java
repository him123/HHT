package com.ae.benchmark.models;

/**
 * Created by Dell on 11-02-2018.
 */

public class Catelog_model {

    String titel,item_id,count_no,net_weight,barcode,shelf_life,Ingredints,nutrition_facts,Energy,carbohydrate,potassium,fat,calcuim,curde_fibre,protin,sodium,vitamin,qty = "1",flawer,price,productTotal,img_url;
    Boolean IsSelected = false;

    public String getItem_id() {
        return item_id;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProductTotal() {
        return productTotal;
    }

    public void setProductTotal(String productTotal) {
        this.productTotal = productTotal;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getCount_no() {
        return count_no;
    }

    public String getFlawer() {
        return flawer;
    }

    public void setFlawer(String flawer) {
        this.flawer = flawer;
    }

    public void setCount_no(String count_no) {
        this.count_no = count_no;
    }

    public String getNet_weight() {
        return net_weight;
    }

    public void setNet_weight(String net_weight) {
        this.net_weight = net_weight;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public Boolean getSelected() {
        return IsSelected;
    }

    public void setSelected(Boolean selected) {
        IsSelected = selected;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getShelf_life() {
        return shelf_life;
    }

    public void setShelf_life(String shelf_life) {
        this.shelf_life = shelf_life;
    }

    public String getIngredints() {
        return Ingredints;
    }

    public void setIngredints(String ingredints) {
        Ingredints = ingredints;
    }

    public String getNutrition_facts() {
        return nutrition_facts;
    }

    public void setNutrition_facts(String nutrition_facts) {
        this.nutrition_facts = nutrition_facts;
    }

    public String getEnergy() {
        return Energy;
    }

    public void setEnergy(String energy) {
        Energy = energy;
    }

    public String getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(String carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public String getPotassium() {
        return potassium;
    }

    public void setPotassium(String potassium) {
        this.potassium = potassium;
    }

    public String getFat() {
        return fat;
    }

    public void setFat(String fat) {
        this.fat = fat;
    }

    public String getCalcuim() {
        return calcuim;
    }

    public void setCalcuim(String calcuim) {
        this.calcuim = calcuim;
    }

    public String getCurde_fibre() {
        return curde_fibre;
    }

    public void setCurde_fibre(String curde_fibre) {
        this.curde_fibre = curde_fibre;
    }

    public String getProtin() {
        return protin;
    }

    public void setProtin(String protin) {
        this.protin = protin;
    }

    public String getSodium() {
        return sodium;
    }

    public void setSodium(String sodium) {
        this.sodium = sodium;
    }

    public String getVitamin() {
        return vitamin;
    }

    public void setVitamin(String vitamin) {
        this.vitamin = vitamin;
    }
}
