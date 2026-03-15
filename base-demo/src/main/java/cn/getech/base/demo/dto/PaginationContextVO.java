package cn.getech.base.demo.dto;

import cn.getech.base.demo.dto.PaginationPathVO;

/**
 * 分页上下文
 */
public class PaginationContextVO {
    private PaginationPathVO path;
    private String pathId;
    private boolean pathExpired;

    public PaginationContextVO(PaginationPathVO path, String pathId, boolean pathExpired) {
        this.path = path;
        this.pathId = pathId;
        this.pathExpired = pathExpired;
    }

    public PaginationPathVO getPath() {
        return path;
    }

    public void setPath(PaginationPathVO path) {
        this.path = path;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public boolean isPathExpired() {
        return pathExpired;
    }

    public void setPathExpired(boolean pathExpired) {
        this.pathExpired = pathExpired;
    }
}
