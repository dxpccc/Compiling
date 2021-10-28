# Compiling
***
单词名称|类别编码|单词值
-------|-------|------
标识符|1|内部字符串
无符号整数|2|整数值
布尔值|3|0/1
字符串常量|4|字符串
INT|5|int
MAIN|6|main
RETURN|7|return
(|8|(
)|9|)
{|10|{
}|11|}
;|12|;

### 产生式
CompUnit

### FIRST集和FOLLOW集
|          | FIRST  | FOLLOW |
| -------- | ------ | ------ |
| CompUnit | int    | #      |
| FuncDef  | int    | #      |
| FuncType | int    | main   |
| Ident    | main   | （     |
| Block    | {      | #      |
| Stmt     | return | }      |

### LL(1)分析表
|          | (    | )    | int                                 | main           | {                  | }    | return                   | Number | ;    |
| -------- | ---- | ---- | ----------------------------------- | -------------- | ------------------ | ---- | ------------------------ | ------ | ---- |
| CompUnit |      |      | CompUnit$\to$FuncDef                |                |                    |      |                          |        |      |
| FuncDef  |      |      | FuncDef$\to$FuncType Ident () Block |                |                    |      |                          |        |      |
| FuncType |      |      | FuncType$\to$int                    |                |                    |      |                          |        |      |
| Ident    |      |      |                                     | Ident$\to$main |                    |      |                          |        |      |
| Block    |      |      |                                     |                | Block$\to${ Stmt } |      |                          |        |      |
| Stmt     |      |      |                                     |                |                    |      | Stmt$\to$return Number ; |        |      |

