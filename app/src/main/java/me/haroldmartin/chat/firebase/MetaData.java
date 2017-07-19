package me.haroldmartin.chat.firebase;

public class MetaData {
    public static final String META = "meta";
    public static final String DATA = "data";

    protected int total = 0;
    protected String id;

    public MetaData() {
    }

    public MetaData(String id, int total) {
        this.id = id;
        this.total = total;
    }

    public int getTotal() {
        return total;
    }
    public String getId() {
        return id;
    }
}
