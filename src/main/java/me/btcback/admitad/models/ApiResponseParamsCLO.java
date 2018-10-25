package me.btcback.admitad.models;

public class ApiResponseParamsCLO {

    int count;
    int limit;
    int offset;

    public ApiResponseParamsCLO(int count, int limit, int offset) {
        this.count = count;
        this.limit = limit;
        this.offset = offset;
    }


    public int getCount() {
        return count;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }


}
