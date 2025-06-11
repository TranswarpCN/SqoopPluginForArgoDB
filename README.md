# SqoopPluginForArgoDB

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/yourusername/SqoopPluginForArgoDB)](https://github.com/yourusername/SqoopPluginForArgoDB/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/yourusername/SqoopPluginForArgoDB)](https://github.com/yourusername/SqoopPluginForArgoDB/network)

## 项目简介

SqoopPluginForArgoDB 是为 ArgoDB 提供的 Sqoop 插件。该项目扩展了 Apache Sqoop 的功能，使其能够高效地将数据从传统关系型数据库迁移到 ArgoDB 分布式数据库中。

## 主要特性

- **高效数据迁移**：优化的数据传输机制，实现高性能数据导入
- **多数据库支持**：兼容主流关系型数据库(MySQL、Oracle等)
- **易用性**：简化的安装和配置流程
- **可靠性**：完善的错误处理和日志记录机制
- **大对象支持**：特殊处理大字符串(Large String)类型数据
- **压缩支持**：支持Snappy/ZLIB/LZF等多种压缩格式

## 项目结构

```
SqoopPluginForArgoDB/
├── src/                    # 源代码目录
│   ├── main/               # 主代码
│   └── test/               # 测试代码
├── docs/                   # 文档
├── lib/                    # 依赖库
└── LICENSE                 # 许可证文件
```

## 快速开始

### 前提条件

- Java 8+
- Apache Sqoop 1.4.7+
- ArgoDB 已安装并运行
- Hadoop 环境

### 安装步骤

1. 克隆项目仓库：
   ```bash
   git clone https://github.com/yourusername/SqoopPluginForArgoDB.git
   ```

2. 编译项目：
   ```bash
   mvn clean package
   ```

3. 将生成的jar包复制到Sqoop的lib目录下

### 使用示例

```bash
sqoop import \
    --connect jdbc:mysql://mysql-host:3306/database \
    --username user \
    --password pass \
    --table source_table \
    --holotable target_table \
    --dblink-url argo-db-url \
    --dblink-user argo-user \
    --dblink-password argo-pass
```

## 详细文档

更多使用说明和配置选项，请参考[文档目录](docs/)。

## 贡献指南

我们欢迎任何形式的贡献！请参阅[贡献指南](CONTRIBUTING.md)了解如何参与项目开发。

## 许可证

本项目采用 [Apache License 2.0](LICENSE) 开源协议。

## 支持与联系

如有任何问题或建议，请通过以下方式联系我们：

- 提交 [GitHub Issues](https://github.com/yourusername/SqoopPluginForArgoDB/issues)
- 参与项目 [Discussions](https://github.com/yourusername/SqoopPluginForArgoDB/discussions)

## 致谢

感谢所有为项目做出贡献的开发者！
