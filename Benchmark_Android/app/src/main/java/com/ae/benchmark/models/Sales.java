package com.ae.benchmark.models;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by eheuristic on 24/12/2016.
 */

public class Sales implements Parcelable {
    private String item_code;
    private String item_category;
    private String material_no;
    private String material_description;
    private String uom;
    private String name;
    private String price;
    private String pricemin;
    private String pricemax;
    private String pricecase;
    private String pricepcs;
    private String pic;
    private String cases;
    private String inv_cases;
    private String inv_piece;
    private String reasonCode;
    private String denominator;
    private boolean isAltUOM;

    public String getDenominator() {
        return denominator;
    }

    public void setDenominator(String denominator) {
        this.denominator = denominator;
    }

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

    public String getPricemin() {
        return pricemin;
    }

    public void setPricemin(String pricemin) {
        this.pricemin = pricemin;
    }

    public String getPricemax() {
        return pricemax;
    }

    public void setPricemax(String pricemax) {
        this.pricemax = pricemax;
    }

    public String getPricecase() {
        return pricecase;
    }

    public void setPricecase(String pricecase) {
        this.pricecase = pricecase;
    }

    public String getPricepcs() {
        return pricepcs;
    }

    public void setPricepcs(String pricepcs) {
        this.pricepcs = pricepcs;
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
        parcel.writeString(pricemin);
        parcel.writeString(pricemax);
        parcel.writeString(pricepcs);
        parcel.writeString(pricecase);
        parcel.writeString(pic);
        parcel.writeString(cases);
        parcel.writeString(inv_cases);
        parcel.writeString(inv_piece);
        parcel.writeString(reasonCode);
        parcel.writeByte((byte) (isAltUOM ? 1 : 0));
    }

    public static final Creator<Sales> CREATOR = new Creator<Sales>() {
        @Override
        public Sales createFromParcel(Parcel source) {
            Sales sale = new Sales();

            sale.item_code = source.readString();
            sale.item_category = source.readString();
            sale.material_no = source.readString();
            sale.material_description = source.readString();
            sale.uom = source.readString();
            sale.name = source.readString();
            sale.price = source.readString();
            sale.pricemin = source.readString();
            sale.pricemax = source.readString();
            sale.pricepcs = source.readString();
            sale.pricecase = source.readString();
            sale.pic = source.readString();
            sale.cases = source.readString();
            sale.inv_cases = source.readString();
            sale.inv_piece = source.readString();
            sale.reasonCode = source.readString();
            sale.isAltUOM = source.readByte()!=0;
            return sale;
        }
        @Override
        public Sales[] newArray(int size) {
            return new Sales[size];
        }
    };
}
