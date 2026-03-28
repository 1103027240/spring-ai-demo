# 构建阶段 - OpenJDK 21
FROM maven:3.9-openjdk-21-slim AS builder

WORKDIR /build

# 复制整个项目
COPY . .

# 只构建 base-demo 模块
RUN mvn clean package -B -pl base-demo -am -DskipTests

# 运行阶段 - OpenJDK 21
FROM openjdk:21-slim

WORKDIR /app

# 复制构建产物
COPY --from=builder /build/base-demo/target/app.jar app.jar

# 暴露端口
EXPOSE 8030

# JVM参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
