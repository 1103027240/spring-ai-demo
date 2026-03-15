package cn.getech.base.demo.dto;

import java.util.Set;

/**
 * 游标信息
 */
public class CursorInfoVO {
    private Set<Long> returnedIds;
    private Double cursorMinScore;
    private Double cursorMaxScore;
    private Long lastCursorId;

    public CursorInfoVO(Set<Long> returnedIds, Double cursorMinScore, Double cursorMaxScore, Long lastCursorId) {
        this.returnedIds = returnedIds;
        this.cursorMinScore = cursorMinScore;
        this.cursorMaxScore = cursorMaxScore;
        this.lastCursorId = lastCursorId;
    }

    public Set<Long> getReturnedIds() {
        return returnedIds;
    }

    public void setReturnedIds(Set<Long> returnedIds) {
        this.returnedIds = returnedIds;
    }

    public Double getCursorMinScore() {
        return cursorMinScore;
    }

    public void setCursorMinScore(Double cursorMinScore) {
        this.cursorMinScore = cursorMinScore;
    }

    public Double getCursorMaxScore() {
        return cursorMaxScore;
    }

    public void setCursorMaxScore(Double cursorMaxScore) {
        this.cursorMaxScore = cursorMaxScore;
    }

    public Long getLastCursorId() {
        return lastCursorId;
    }

    public void setLastCursorId(Long lastCursorId) {
        this.lastCursorId = lastCursorId;
    }
}
