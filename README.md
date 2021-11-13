# decision_generator<br>(Desicion Matrix Generator)

## Overview
    Desicion Matrix Generatorは指定のJavaファイルからホワイトボックステストにおけるカバレッジを担保するテストケース表をxlsxに出力するツールです

## Specification
### Usage
#### Parameters

| name | alias | default | explain | required |
---- | ---- | ---- | ---- | ---- 
| -T | --target | - | 解析対象リソースのトップディレクトリ(例:c:/hoge/fuga/target) | Y |
| -M | --mode | C1 | 網羅度の指定(C0/C1/C2/MCC) ※2021/11/13 現在 C1のみ対応  | N |
| -D | --directory | (カレントディレクトリ) | 生成ファイルの出力先ディレクトリ | N |
| -F | --file | decision_matrix | 生成ファイル名 | N |

#### Execute

#### Details




    指定したモードによってどのカバレッジ種別のテストケースを作成するか指定が可能
    (C1のみ対応 2021/11/13 )

    指定したディレクトリ配下のファイルを再帰的に探索