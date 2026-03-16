package cn.getech.base.demo.dto;

import cn.hutool.core.util.StrUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import java.util.Base64;
import static cn.getech.base.demo.constant.FieldValueConstant.CUSTOMER_CURSOR_SEPARATOR;

/**
 * 游标信息封装
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompositeCursorDto implements Serializable {
    private static final long serialVersionUID = 1L;

    // 主排序字段值（score/crteaTime）
    private String primaryValue;

    // 次排序字段值（id）
    private Long secondaryValue;

    // 排序类型（DESC/ASC）
    private String sortDirection = "DESC";

    /**
     * 编码游标，对这个采用base64编码：primaryValue|secondaryValue|sortDirection
     */
    public String encodeCursor() {
        try {
            String cursor = new StringBuilder()
                    .append(primaryValue).append(CUSTOMER_CURSOR_SEPARATOR)
                    .append(secondaryValue).append(CUSTOMER_CURSOR_SEPARATOR)
                    .append(sortDirection)
                    .toString();
            return Base64.getEncoder().encodeToString(cursor.getBytes());
        } catch (Exception e) {
            log.error("编码游标失败", e);
            return null;
        }
    }

    /**
     * 解码游标
     */
    public static CompositeCursorDto decodeCursor(String cursor) {
        if (StrUtil.isBlank(cursor)) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String cursorData = new String(decodedBytes);

            String[] parts = cursorData.split("\\" + CUSTOMER_CURSOR_SEPARATOR);
            if (parts.length != 3) {
                return null;
            }

            CompositeCursorDto cursorDto = new CompositeCursorDto();
            cursorDto.setPrimaryValue(parts[0]);
            cursorDto.setSecondaryValue(Long.parseLong(parts[1]));
            cursorDto.setSortDirection(parts[2]);
            return cursorDto;
        } catch (Exception e) {
            log.error("解码游标失败，cursor: {}", cursor, e);
            return null;
        }
    }

}
