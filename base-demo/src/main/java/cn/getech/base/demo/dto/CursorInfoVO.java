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

    private Double cursorLastExactScore;  // 最后一条数据的精确分数
    private Long cursorLastExactId;       // 最后一条数据的精确ID

    public CursorInfoVO(Set<Long> returnedIds, Double cursorMinScore, Double cursorMaxScore, Long lastCursorId) {
        this.returnedIds = returnedIds;
        this.cursorMinScore = cursorMinScore;
        this.cursorMaxScore = cursorMaxScore;
        this.lastCursorId = lastCursorId;
        this.cursorLastExactScore = null;
        this.cursorLastExactId = null;
    }

    public CursorInfoVO(Set<Long> returnedIds, Double cursorMinScore, Double cursorMaxScore, Long lastCursorId,
                       Double cursorLastExactScore, Long cursorLastExactId) {
        this.returnedIds = returnedIds;
        this.cursorMinScore = cursorMinScore;
        this.cursorMaxScore = cursorMaxScore;
        this.lastCursorId = lastCursorId;
        this.cursorLastExactScore = cursorLastExactScore;
        this.cursorLastExactId = cursorLastExactId;
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

    public Double getCursorLastExactScore() {
        return cursorLastExactScore;
    }

    public void setCursorLastExactScore(Double cursorLastExactScore) {
        this.cursorLastExactScore = cursorLastExactScore;
    }

    public Long getCursorLastExactId() {
        return cursorLastExactId;
    }

    public void setCursorLastExactId(Long cursorLastExactId) {
        this.cursorLastExactId = cursorLastExactId;
    }
}
