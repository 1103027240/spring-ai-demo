package cn.example.ai.demo.service.writer;

import org.jspecify.annotations.NonNull;
import java.io.IOException;
import java.io.Writer;

/**
 * 不自动关闭的 Writer 包装器
 * 用于防止底层 Writer 被外部库（如 Jackson）自动关闭
 */
public class NonClosingWriter extends Writer {

    private final Writer delegate;

    public NonClosingWriter(Writer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(char @NonNull [] buff, int off, int len) throws IOException {
        delegate.write(buff, off, len);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        // 只刷新，不关闭
        delegate.flush();
    }

    /**
     * 真正关闭底层 Writer
     */
    public void reallyClose() throws IOException {
        delegate.close();
    }

}
