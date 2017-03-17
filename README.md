nablarch-test-support
======================

[![Build Status](https://travis-ci.org/tamura-orz/nablarch-test-support.svg?branch=develop)](https://travis-ci.org/tamura-orz/nablarch-test-support)

Nablarchモジュールのテストで必要な共通処理をまとめています。

既存のテストで使用しているnablarch-all-test及びnablarch-toolboxの処理を切り出しています。
テストを移行する際には本モジュールを参照するようにし、足りなければ処理を新しく切り出して追加してください。
その場合、切り出し元には処理を残し、本モジュールに追加したクラスのパッケージを適宜変更してください。

    本モジュールに切り出したクラスの一部を挙げます。
    パッケージ名を変更する際に参考にして下さい。
    
    test.support.db.helper.DatabaseTestRunner -> nablarch.test.support.db.helper.DatabaseTestRunner
    nablarch.tool.Hereis -> nablarch.test.support.tool.Hereis

## 依存ライブラリ

本モジュールのコンパイルまたはテストには、下記ライブラリを手動でローカルリポジトリへインストールする必要があります。

ライブラリ          |ファイル名       |グループID     |アーティファクトID   |バージョン   |
:-------------------|:----------------|:--------------|:--------------------|:------------|
Oracle JDBC Driver  |ojdbc6.jar       |com.oracle     |ojdbc6               |11.2.0.2.0   |
Oracle UCP for JDBC |ucp.jar          |com.oracle     |ucp                  |11.2.0.3.0   |

上記ライブラリは、下記コマンドでインストールしてください。


```
mvn install:install-file -Dfile=<ファイル名> -DgroupId=<グループID> -DartifactId=<アーティファクトID> -Dversion=<バージョン> -Dpackaging=jar
```
