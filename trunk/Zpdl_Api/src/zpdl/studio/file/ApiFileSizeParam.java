package zpdl.studio.file;

public class ApiFileSizeParam {
    private int count;
    private long size;

    public ApiFileSizeParam() {
        count = 0;
        size = 0;
    }

    public int getCount() {
        return count;
    }

    public long getSize() {
        return size;
    }

    public void plusCount(int c) {
        count += c;
    }

    public void plusSize(long s) {
        size += s;
    }

    public void plus(ApiFileSizeParam sc) {
        this.count += sc.getCount();
        this.size += sc.getSize();
    }
}
