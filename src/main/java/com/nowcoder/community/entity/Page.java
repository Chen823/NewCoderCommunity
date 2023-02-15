package com.nowcoder.community.entity;

public class Page {
    // 当前页码
    private int curPage = 1;
    // 显示上限
    private int limit = 10;
    // 数据总数
    private int total;
    // 查询路径
    private String path;

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        if(curPage >= 1) {
            this.curPage = curPage;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        if(total >= 0) {
            this.total = total;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页起始行
     * @return
     */
    public int getOffset(){
        return (curPage - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotalPage(){
        return total % limit == 0 ? total/limit : total/limit + 1;
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getStartPage(){
        return curPage - 2 >= 1 ? curPage - 2 : 1;
    }

    /**
     *
     * @return
     */
    public int getEndPage(){
        return curPage + 2 <= total ? curPage + 2 : total;
    }
}
