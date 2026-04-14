package cn.example.flink.demo.strategy;

import cn.example.flink.demo.param.UserVisitorDto;
import org.apache.flink.api.common.eventtime.*;

/**
 * 自定义生成水位线
 */
public class CustomerWatermarkStrategy implements WatermarkStrategy<UserVisitorDto> {

    private final long interval = 3000; //延迟3s
    private volatile long maxTimestamp = Long.MIN_VALUE + interval + 1;

    @Override
    public WatermarkGenerator<UserVisitorDto> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return new WatermarkGenerator<>() {
            @Override
            public void onEvent(UserVisitorDto dto, long eventTimestamp, WatermarkOutput output) {
                maxTimestamp = Math.max(maxTimestamp, dto.getTimestamp());
            }

            @Override
            public void onPeriodicEmit(WatermarkOutput output) {
                output.emitWatermark(new Watermark(maxTimestamp - interval - 1));
            }
        };
    }

    @Override
    public TimestampAssigner<UserVisitorDto> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
        return (userVisitorDto, recordTimestamp) -> userVisitorDto.getTimestamp();
    }

}
