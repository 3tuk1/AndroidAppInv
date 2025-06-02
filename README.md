# 在庫管理アプリ (Inventory Management App)

Androidネイティブ（Java）で開発された在庫管理アプリケーションです。食品やその他のアイテムの在庫を効率的に管理し、消費パターンを追跡・分析することで、無駄を減らし、計画的な購買をサポートします。

## 概要

このアプリケーションは、日々の在庫管理を簡素化し、よりスマートな消費生活を支援するために開発されました。バーコードスキャン、レシート読み取り、賞味期限認識などの便利な機能を通じて、アイテムの登録や管理の手間を軽減します。また、消費履歴や分析ダッシュボードを通じて、ユーザーの消費傾向を可視化し、節約や計画的な購買に役立つ情報を提供します。

## 主な機能

本アプリケーションは、以下の主要な機能を提供します。

* **在庫管理:**
    * アイテムの登録（名前、数量、カテゴリ、賞味期限、場所、画像）
    * 在庫アイテムの一覧表示（グリッド形式）
    * アイテム情報の編集・更新
    * アイテムの削除（長押し操作による）
    * 数量が0になったアイテムの自動非表示管理
* **カメラ連携:**
    * バーコードスキャンによるアイテム情報取得・登録
    * レシート読み取りによるアイテム情報の一括登録補助
    * 賞味期限スキャンによる日付入力補助
    * アイテム画像の撮影・登録
* **消費履歴:**
    * アイテムの消費・追加履歴の記録・表示
    * 消費理由（通常使用、廃棄など）の記録
* **データ分析:**
    * 分析ダッシュボードの提供
    * カレンダー形式での日別消費予測表示
    * 消費傾向の表示（日別、週別、月別）
    * カテゴリ別消費傾向の表示
    * 廃棄率の表示
* **買い物リスト:**
    * 在庫状況や消費予測に基づいた買い物リストの自動生成
    * 購入推奨理由の表示
    * 買い物リストアイテムの編集（購入数量の変更）
    * 買い物リストからのアイテム削除
* **設定:**
    * 重複バーコードの検出と修正ユーティリティ
* **ナビゲーション:**
    * 下部タブナビゲーション（在庫、貯金、分析、設定）

## 技術スタック

* **プログラミング言語:** Java
* **アーキテクチャ:** ViewModel, Repository, Room (MVVMに近い構造)
* **データベース:** Room Persistence Library
* **カメラ:** CameraX
* **機械学習:** Google ML Kit (Barcode Scanning, Text Recognition)
* **カレンダーUI:** Kizitonwose CalendarView
* **UIフレームワーク:** Android SDK, AndroidX Libraries, Material Components for Android
* **ビルドシステム:** Gradle

## セットアップ

### 前提条件

* Android Studio (最新版を推奨)
* Android SDK (プロジェクトの `minSdkVersion` と `targetSdkVersion` に合わせてください。これらは `build.gradle (Module :app)` ファイルに定義されていますが、提供されたファイルには含まれていません。)
* Java Development Kit (JDK) 17
* Gradle (プロジェクトで指定されたバージョン。 `gradle-wrapper.properties` によると Gradle 8.10.2 を使用)

### ビルド手順

1.  **リポジトリをクローンします:**
    ```bash
    git clone [https://github.com/3tuk1/AndroidAppInv.git](https://github.com/3tuk1/AndroidAppInv.git)
    ```
2.  **Android Studio でプロジェクトを開きます:**
    * Android Studio を起動します。
    * 「Open an existing Android Studio project」または「ファイル > 開く...」を選択します。
    * クローンしたリポジトリのルートディレクトリ (`AndroidAppInv`) を選択します。
3.  **SDKパスの設定 (必要な場合):**
    * `local.properties` ファイルがローカル環境のAndroid SDKパスを正しく指しているか確認してください。 通常、Android Studioが自動で設定します。
4.  **Gradle Sync とビルド:**
    * Android Studio がプロジェクトを開くと、自動的に Gradle Sync が実行されます。
    * 完了後、メニューの「ビルド > プロジェクトのビルド」または緑色の再生ボタン（▶️）でアプリケーションをビルド・実行します。

## データベース構造の概要

このアプリケーションは Room Persistence Library を使用して以下のエンティティを管理しています。

* **main_items:** 在庫アイテムの基本情報（ID、数量、カテゴリID、名前、賞味期限）
* **item_images:** アイテムの画像パス情報
* **locations:** アイテムの保管場所情報
* **barcodes:** アイテムに紐づくバーコード情報
* **categories:** アイテムのカテゴリマスタ
* **hidden_items:** 数量が0になったアイテムなど、一時的に非表示にするアイテムのID
* **history:** アイテムの入出庫履歴（消費、追加、削除など）
* **item_analytics_data:** アイテムごとの分析データ（消費ペース、在庫切れ予測日など）

詳細なスキーマは `app/schemas/com.inv.inventryapp.room.AppDatabase/1.json` を参照してください。

## 必要な権限

このアプリケーションは、以下の権限を必要とします。

* `android.permission.CAMERA`: バーコードスキャン、レシート読み取り、賞味期限スキャン、写真撮影のため。
* `android.permission.READ_EXTERNAL_STORAGE` (Android 12L (API 32) 以下): 画像の読み込みのため。
* `android.permission.WRITE_EXTERNAL_STORAGE` (Android 10 (API 29) より前): 画像の保存のため。
* `android.permission.READ_MEDIA_IMAGES` (Android 13 (API 33) 以降): 画像の読み込みのため。
* `android.permission.READ_MEDIA_VISUAL_USER_SELECTED` (Android 14 (API 34) 以降): ユーザーが選択した画像のみへのアクセスのため。

## 貢献方法

貢献を歓迎します！バグ報告や機能提案は、GitHub Issues を通じてお願いします。プルリクエストを作成する場合は、以下の手順に従ってください。

1.  このリポジトリをフォークします。
2.  新しいブランチを作成します (`git checkout -b feature/YourFeature`)。
3.  変更をコミットします (`git commit -m 'Add some YourFeature'`)。
4.  ブランチにプッシュします (`git push origin feature/YourFeature`)。
5.  プルリクエストを開きます。

## ライセンス


