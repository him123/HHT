package com.ae.benchmark.models;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by Rakshit on 29-Dec-16.
 */
public class OrderRequest implements Parcelable {
    private String itemCode;
    private String itemCategory;
    private String itemName;
    private String materialNo;
    private String cases;
    private String units;
    private String price;
    private String pricemin;
    private String pricemax;
    private String pricecase;
    private String pricepcs;
    private String uom;
    private String denominator;
    private boolean isAltUOM;
    private String itemNameAr;

    public String getItemNameAr() {
        return itemNameAr;
    }
    public void setItemNameAr(String itemNameAr) {
        this.itemNameAr = itemNameAr;
    }

    public static final Creator<OrderRequest> CREATOR = new Creator<OrderRequest>() {
        @Override
        public OrderRequest createFromParcel(Parcel source) {
            OrderRequest loadRequest = new OrderRequest();
            loadRequest.itemCode = source.readString();
            loadRequest.itemCategory = source.readString();
            loadRequest.materialNo = source.readString();
            loadRequest.cases = source.readString();
            loadRequest.units = source.readString();
            loadRequest.price = source.readString();
            loadRequest.pricemin = source.readString();
            loadRequest.pricemax = source.readString();
            loadRequest.pricepcs = source.readString();
            loadRequest.pricecase = source.readString();
            loadRequest.uom = source.readString();
            loadRequest.itemName = source.readString();
            loadRequest.isAltUOM = source.readByte()!=0;
            loadRequest.itemNameAr = source.readString();
            loadRequest.denominator = source.readString();
            return loadRequest;
        }
        @Override
        public OrderRequest[] newArray(int size) {
            return new OrderRequest[size];
        }
    };
    public String getCases() {
        return cases;
    }
    public void setCases(String cases) {
        this.cases = cases;
    }
    public String getItemCategory() {
        return itemCategory;
    }
    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }
    public String getItemCode() {
        return itemCode;
    }
    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }
    public String getMaterialNo() {
        return materialNo;
    }
    public void setMaterialNo(String materialNo) {
        this.materialNo = materialNo;
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

    public String getUnits() {
        return units;
    }
    public void setUnits(String units) {
        this.units = units;
    }
    public String getUom() {
        return uom;
    }
    public void setUom(String uom) {
        this.uom = uom;
    }
    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public boolean isAltUOM() {
        return isAltUOM;
    }
    public void setIsAltUOM(boolean isAltUOM) {
        this.isAltUOM = isAltUOM;
    }

    public String getDenominator() {
        return denominator;
    }

    public void setDenominator(String denominator) {
        this.denominator = denominator;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(itemCode);
        parcel.writeString(itemCategory);
        parcel.writeString(materialNo);
        parcel.writeString(cases);
        parcel.writeString(units);
        parcel.writeString(price);
        parcel.writeString(pricemin);
        parcel.writeString(pricemax);
        parcel.writeString(pricepcs);
        parcel.writeString(pricecase);
        parcel.writeString(uom);
        parcel.writeString(itemName);
        parcel.writeByte((byte) (isAltUOM ? 1 : 0));
        parcel.writeString(itemNameAr);
        parcel.writeString(denominator);
    }
}
