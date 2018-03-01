package com.ae.benchmark.models;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by Rakshit on 19-Nov-16.
 */
public class LoadSummary implements Parcelable {
    private String item_code;
    private String item_description;
    private String item_description_ar;
    private String quantity_cases;
    private String quantity_units;
    private String uom;
    private String price;
    private String materialNo;
    private String reasonCode;
    private boolean isAltUOM;
    //Get Instance
    public LoadSummary() {
    }
    //Getter and Setter
    public String getItemCode() {
        return item_code;
    }
    public void setItemCode(String item_code) {
        this.item_code = item_code;
    }
    public String getItemDescription() {
        return item_description;
    }
    public void setItemDescription(String item_description) {
        this.item_description = item_description;
    }
    public String getItem_description_ar() {
        return item_description_ar;
    }
    public void setItem_description_ar(String item_description_ar) {
        this.item_description_ar = item_description_ar;
    }
    public String getQuantityCases() {
        return quantity_cases;
    }
    public void setQuantityCases(String quantity_cases) {
        this.quantity_cases = quantity_cases;
    }
    public String getQuantityUnits() {
        return quantity_units;
    }
    public void setQuantityUnits(String quantity_units) {
        this.quantity_units = quantity_units;
    }
    public String getMaterialNo() {
        return materialNo;
    }
    public void setMaterialNo(String materialNo) {
        this.materialNo = materialNo;
    }
    public String getUom() {
        return uom;
    }
    public void setUom(String uom) {
        this.uom = uom;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }
    public boolean isAltUOM() {
        return isAltUOM;
    }
    public void setIsAltUOM(boolean isAltUOM) {
        this.isAltUOM = isAltUOM;
    }
    public String getReasonCode() {
        return reasonCode;
    }
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }
    public static final Parcelable.Creator<LoadSummary> CREATOR = new Parcelable.Creator<LoadSummary>() {
        @Override
        public LoadSummary createFromParcel(Parcel source) {
            LoadSummary loadSummary = new LoadSummary();
            loadSummary.item_code = source.readString();
            loadSummary.item_description = source.readString();
            loadSummary.quantity_cases = source.readString();
           // loadSummary.item_description_ar = source.readString();
            loadSummary.quantity_units = source.readString();
            loadSummary.materialNo = source.readString();
            loadSummary.uom = source.readString();
            loadSummary.price = source.readString();
            loadSummary.reasonCode = source.readString();
            loadSummary.isAltUOM = source.readByte()!=0;
            return loadSummary;
        }
        @Override
        public LoadSummary[] newArray(int size) {
            return new LoadSummary[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(item_code);
        parcel.writeString(item_description);
       // parcel.writeString(item_description_ar);
        parcel.writeString(quantity_cases);
        parcel.writeString(quantity_units);
        parcel.writeString(materialNo);
        parcel.writeString(uom);
        parcel.writeString(price);
        parcel.writeString(reasonCode);
        parcel.writeByte((byte) (isAltUOM ? 1 : 0));
    }
}
