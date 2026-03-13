# Maven配置说明

本目录包含Maven的配置文件。

## 配置说明

1. **maven.config** - Maven命令行参数配置
   - `-s` 参数: 指定自定义settings.xml文件路径
     - 当前配置: `D:/soft/apache-maven-3.8.1/conf/settings.xml`
   - `-Dmaven.repo.local` 参数: 指定Maven本地仓库路径
     - 当前配置: `D:/mavenRepo`

2. **jvm.config** - JVM启动参数
   - 指定Maven配置目录: `-Dmaven.conf=D:/soft/apache-maven-3.8.1/conf`
   - 指定Maven本地仓库路径: `-Dmaven.repo.local=D:/mavenRepo`

## 使用方法

当使用Maven Wrapper (mvnw) 或 Maven命令构建项目时，会自动使用这些配置文件中指定的settings.xml和本地仓库路径。

## 其他方式

如果需要在IDEA中设置，可以：
1. 打开 Settings/Preferences → Build, Execution, Deployment → Build Tools → Maven
2. 在 User settings file 中填入: `D:/soft/apache-maven-3.8.1/conf/settings.xml`
3. 在 Local repository 中填入: `D:/mavenRepo`
4. 勾选 Override 复选框
