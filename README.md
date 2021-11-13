# decision_generator<br>(Desicion Matrix Generator)

## Overview
---

    Desicion Matrix Generatorは指定のJavaファイルからホワイトボックステストにおけるカバレッジを担保するテストケース表をxlsxに出力するツールです

## Specification
---
### Usage

#### Parameters

| name | alias | default | explain | required |
---- | ---- | ---- | ---- | ---- 
| -T | --target | - | 解析対象リソースのトップディレクトリ(例:c:/hoge/fuga/target) | Y |
| -M | --mode | C1 | 網羅度の指定(C0/C1/C2/MCC) ※2021/11/13 現在 C1のみ対応  | N |
| -D | --directory | (カレントディレクトリ) | 生成ファイルの出力先ディレクトリ | N |
| -F | --file | decision_matrix | 生成ファイル名(生成時に.xlsx が付与されます) | N |

#### Execute

java コマンドでjarを実行します

```
C:\hoge> java -jar DecisionMatrixGenerator.jar
 -T "C:\target\directory"^
 -M C1
 -D "C:\output\directory"^
 -F example
```

実行例

![execute_example](https://github.com/miwa-m/decision_generator/blob/readme_contents/readme_contents/execute_example.PNG?raw=true)

#### Details

- 処理仕様について

    対象ファイルには、<br>
    -Tで指定したディレクトリ内の配下のディレクトリ内のファイル、ディレクトリ配下のファイル... も含まれます

    パラメータで網羅度の指定ができるが、現在はC1のみ対応

- 出力ファイルについて

    出力されるxlsxファイルには、-Tに指定したディレクトリ配下に存在するjavaファイル毎にシートが作成されます。<br>
    シート内には、対応するjavaファイルに内に定義されているクラス、その内部に定義されているメソッド毎にテストケースが出力されます。<br>

- 出力内容について

    (C1の為)各判定文が、真/偽となるための、判定文を構成する条件の真偽値を出力します。<br>
    各判定文が真の時、そのブロック内での各判定文に対しても同様に解析を行い条件の真偽値を出力します。<br>

    - ブロックについて
        
        if/else if/else<br>
        for/while/do while<br>
        (switch内の)case/default<br>
        try/catch/finally<br>
        を1ブロックとしています。<br>

        各ブロック内への遷移(判定文が真)が発生する為の各条件の真偽の組み合わせは<br>
        if / else if : 判定文が真となるような条件の真偽の組み合わせ<br>
        else : 対応するif/else ifがそれぞれ偽となる組み合わせ<br>
        for / while / do while : 繰り返しの判定が真となる条件の真偽の組み合わせ<br>
        case : caseで指定された値に一致<br>
        default : 対応するswitch内のcaseのいずれかに一致しない<br>
        try : 対応するcatch内で指定されている例外が発生しない<br>
        catch : 指定されている例外のいずれかが発生する<br>
        finally : tryと同様<br>
        となるように出力しています

![explain1](https://github.com/miwa-m/decision_generator/blob/readme_contents/readme_contents/explain_01.PNG?raw=true)

![explain2](https://github.com/miwa-m/decision_generator/blob/readme_contents/readme_contents/explain_02.PNG?raw=true)
    
![explain3](https://github.com/miwa-m/decision_generator/blob/readme_contents/readme_contents/explain_03.PNG?raw=true)

    - その他

        あるブロックからみて、上に存在するブロック内にreturnやthrowがあったケースに関しては考慮されていません<br>
        (⇒場合によっては各条件の真偽を揃えてテストを実行すると、あるブロックに処理が到達しない)
        三項演算子･lambdaには非対応となっています<br>
        (上記いずれも残課題になります)