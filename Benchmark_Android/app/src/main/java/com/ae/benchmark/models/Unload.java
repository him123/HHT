package com.ae.benchmark.models;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by Rakshit on 18-Jan-17.
 */
public class Unload implements Parcelable{
    private String item_code;
    private String item_category;
    private String material_no;
    private String material_description;
    private String uom;
    private String name;
    private String price;
    private String pic;
    private String cases;
    private String inv_cases;
    private String inv_piece;
    private String reasonCode;
    private String isCheck;
    private boolean isAltUOM;
    public String getReasonCode() {
        return reasonCode;
    }
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }
    public String getMaterial_description() {
        return material_description;
    }
    public void setMaterial_description(String material_description) {
        this.material_description = material_description;
    }
    public String getUom() {
        return uom;
    }
    public void setUom(String uom) {
        this.uom = uom;
    }
    public String getItem_category() {
        return item_category;
    }
    public void setItem_category(String item_category) {
        this.item_category = item_category;
    }
    public String getItem_code() {
        return item_code;
    }
    public void setItem_code(String item_code) {
        this.item_code = item_code;
    }
    public String getMaterial_no() {
        return material_no;
    }
    public void setMaterial_no(String material_no) {
        this.material_no = material_no;
    }


    public String getInv_cases() {
        return inv_cases;
    }
    public void setInv_cases(String inv_cases) {
        this.inv_cases = inv_cases;
    }
    public String getInv_piece() {
        return inv_piece;
    }
    public void setInv_piece(String inv_piece) {
        this.inv_piece = inv_piece;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getCases() {
        return cases;
    }

    public String getIsCheck() {
        return isCheck;
    }

    public void setIsCheck(String isCheck) {
        this.isCheck = isCheck;
    }

    public void setCases(String cases) {
        this.cases = cases;
    }
    public boolean isAltUOM() {
        return isAltUOM;
    }
    public void setIsAltUOM(boolean isAltUOM) {
        this.isAltUOM = isAltUOM;
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(item_code);
        parcel.writeString(item_category);
        parcel.writeString(material_no);
        parcel.writeString(material_description);
        parcel.writeString(uom);
        parcel.writeString(name);
        parcel.writeString(price);
        parcel.writeString(pic);
        parcel.writeString(cases);
        parcel.writeString(inv_cases);
        parcel.writeString(inv_piece);
        parcel.writeString(reasonCode);
        parcel.writeString(isCheck);
        parcel.writeByte((byte) (isAltUOM ? 1 : 0));
    }

    public static final Parcelable.Creator<Unload> CREATOR = new Parcelable.Creator<Unload>() {
        @Override
        public Unload createFromParcel(Parcel source) {
            Unload unload = new Unload();

            unload.item_code = source.readString();
            unload.item_category = source.readString();
            unload.material_no = source.readString();
            unload.material_description = source.readString();
            unload.uom = source.readString();
            unload.name = source.readString();
            unload.price = source.readString();
            unload.pic = source.readString();
            unload.cases = source.readString();
            unload.inv_cases = source.readString();
            unload.inv_piece = source.readString();
            unload.reasonCode = source.readString();
            unload.isCheck = source.readString();
            unload.isAltUOM = source.readByte()!=0;
            return unload;
        }
        @Override
        public Unload[] newArray(int size) {
            return new Unload[size];
        }
    };
}
