---
layout: default
title: Generators
parent: User
---

# Generators

### Avro

Description:

Class: ca.breakpoints.datamaker.generator.AvroGenerator

Configuration:

- Compress content
    - Type: BOOLEAN
    - Default value: False
    - Possible values: True, False
- Codec
    - Type: STRING
    - Default value: null
    - Possible values: null, deflate, bzip2, xz, zstandard, snappy

### Bytes

Description:

Class: ca.breakpoints.datamaker.generator.BytesGenerator

Configuration:


### Csv

Description:

Class: ca.breakpoints.datamaker.generator.CsvGenerator

Configuration:

- File encoding
    - Type: STRING
    - Default value: UTF-8
    - Possible values:
- Delimiter
    - Type: STRING
    - Default value: ,
    - Possible values:
- Quote char
    - Type: STRING
    - Default value: "
    - Possible values:
- Escape char
    - Type: STRING
    - Default value: "
    - Possible values:
- Line ending
    - Type: STRING
    - Default value:

    - Possible values:
- Null value
    - Type: STRING
    - Default value:
    - Possible values:
- Quote all
    - Type: BOOLEAN
    - Default value: False
    - Possible values: True, False

### Excel

Description:

Class: ca.breakpoints.datamaker.generator.ExcelGenerator

Configuration:

- Date format
    - Type: STRING
    - Default value: yyyy/m/d h:mm
    - Possible values:

### Json

Description:

Class: ca.breakpoints.datamaker.generator.JsonGenerator

Configuration:

- Line ending
    - Type: STRING
    - Default value:

    - Possible values:

### Parquet

Description:

Class: ca.breakpoints.datamaker.generator.ParquetGenerator

Configuration:

- Compress content
    - Type: BOOLEAN
    - Default value: False
    - Possible values: True, False
- Codec
    - Type: STRING
    - Default value: UNCOMPRESSED
    - Possible values: UNCOMPRESSED, SNAPPY, GZIP, LZO, BROTLI, LZ4, ZSTD

### Passthrough

Description:

Class: ca.breakpoints.datamaker.generator.PassthroughGenerator

Configuration:


### Pdf

Description:

Class: ca.breakpoints.datamaker.generator.PdfGenerator

Configuration:

- Template
    - Type: STRING
    - Default value:
    - Possible values:
- Number of pages
    - Type: NUMERIC
    - Default value:
    - Possible values:
- Font
    - Type: STRING
    - Default value: TIMES_ROMAN
    - Possible values: COURIER, HELVETICA, TIMES_ROMAN, SYMBOL, ZAPFDINGBATS, UNDEFINED
- Output data in a table
    - Type: BOOLEAN
    - Default value: False
    - Possible values:
- Document author
    - Type: STRING
    - Default value:
    - Possible values:
- Document creator
    - Type: STRING
    - Default value:
    - Possible values:
- Document title
    - Type: EXPRESSION
    - Default value: #dataset.name
    - Possible values:
- Document subject
    - Type: EXPRESSION
    - Default value:
    - Possible values:
- Keywords
    - Type: STRING
    - Default value:
    - Possible values:

### Sql

Description:

Class: ca.breakpoints.datamaker.generator.SqlGenerator

Configuration:

- Managed table
    - Type: BOOLEAN
    - Default value: False
    - Possible values: True, False
- Line ending
    - Type: STRING
    - Default value:

    - Possible values:
- SQL Dialect
    - Type: STRING
    - Default value: SQL_1999
    - Possible values: SQL_1999, SQL_2006, PL_pgSQL, Transact_SQL, PL_SQL, SQL_SERVER, POSTGRES, MYSQL, HIVE

### Template Data

Description:

Class: ca.breakpoints.datamaker.generator.TemplateDataGenerator

Configuration:

- Freemarker template
    - Type: STRING
    - Default value: Dataset: ${dataset.name}  
      <#list fieldValueList as fieldValueList >
      <#list fieldValueList as fieldValue>	${fieldValue.field.name}: ${fieldValue.value}
      </#list>
      =======================
      </#list>
    - Possible values:

### Text

Description:

Class: ca.breakpoints.datamaker.generator.TextGenerator

Configuration:

- Element separator
    - Type: STRING
    - Default value:
    - Possible values:
- Key value separator
    - Type: STRING
    - Default value: =
    - Possible values:
- Output keys
    - Type: BOOLEAN
    - Default value: false
    - Possible values:

### Xml

Description:

Class: ca.breakpoints.datamaker.generator.XmlGenerator

Configuration:

- Root element
    - Type: STRING
    - Default value:
    - Possible values:
- Encoding
    - Type: STRING
    - Default value: UTF-8
    - Possible values:
- Pretty print
    - Type: BOOLEAN
    - Default value: False
    - Possible values: True, False
- Version
    - Type: STRING
    - Default value: 1.0
    - Possible values: 