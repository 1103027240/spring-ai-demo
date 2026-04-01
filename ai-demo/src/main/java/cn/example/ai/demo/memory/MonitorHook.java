package cn.example.ai.demo.memory;

import io.agentscope.core.hook.*;
import io.agentscope.core.message.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.List;

/**
 * Agent监控
 */
@Slf4j
@Component
public class MonitorHook implements Hook {

    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        if (event instanceof PreCallEvent preCallEvent) {
            log.info("Agent started，name：{}", preCallEvent.getAgent().getName());
        }

        else if (event instanceof PostCallEvent postCallEvent) {
            Msg finalMessage = postCallEvent.getFinalMessage();
            log.info("Agent finished，name：{}, textContent：{}", postCallEvent.getAgent().getName(), finalMessage.getTextContent());
        }

        else if (event instanceof ErrorEvent errorEvent) {
            Throwable error = errorEvent.getError();
            log.info("Agent error，name：{}, message：{}", errorEvent.getAgent().getName(), error.getMessage());
        }

        else if (event instanceof PreReasoningEvent preReasoningEvent) {
            List<Msg> messages = preReasoningEvent.getInputMessages();
            messages.add(0, Msg.builder()
                    .role(MsgRole.SYSTEM)
                    .content(List.of(TextBlock.builder().text("逐步思考...").build()))
                    .build());

            preReasoningEvent.setInputMessages(messages);
            log.info("Agent PreReasoningEvent，name：{}， inputMessages：{}", preReasoningEvent.getAgent().getName(), messages);
        }

        else if (event instanceof ReasoningChunkEvent reasoningChunkEvent) {
            Msg incrementalChunk = reasoningChunkEvent.getIncrementalChunk();
            log.info("Agent ReasoningChunkEvent，name：{}, textContent：{}", reasoningChunkEvent.getAgent().getName(), incrementalChunk.getTextContent());
        }

        else if (event instanceof PostReasoningEvent postReasoningEvent) {
            Msg reasoningMessage = postReasoningEvent.getReasoningMessage();
            log.info("Agent PostReasoningEvent，name：{}, textContent：{}", postReasoningEvent.getAgent().getName(), reasoningMessage.getTextContent());
        }

        else if (event instanceof PreActingEvent preActingEvent) {
            ToolUseBlock toolUse = preActingEvent.getToolUse();
            log.info("Agent PreActingEvent，name：{}，toolName：{}，toolInput：{}", preActingEvent.getAgent().getName(), toolUse.getName(), toolUse.getInput());
        }

        else if (event instanceof ActingChunkEvent actingChunkEvent) {
            ToolResultBlock toolResult = actingChunkEvent.getChunk();
            log.info("Agent ActingChunkEvent，name：{}, toolName：{}，toolOutput：{}", actingChunkEvent.getAgent().getName(), toolResult.getName(), toolResult.getOutput());
        }

        else if (event instanceof PostActingEvent postActingEvent) {
            ToolResultBlock toolResult = postActingEvent.getToolResult();
            log.info("Agent PostActingEvent，name：{}，toolName：{}，toolOutput：{}", postActingEvent.getAgent().getName(), toolResult.getName(), toolResult.getOutput());
        }

        return Mono.just(event);
    }

}
