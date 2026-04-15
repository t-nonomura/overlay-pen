# 最前面ペン

Android 12 以上で使える、画面に重ねて描ける注釈ツールです。  
他のアプリを開いたまま、線やマークを書き込み、ポイント共有や操作案内に使えます。

別の PC や新しいスレッドから再開するときは、まず `AGENT.md` を確認してください。

## Current Status

- `app/` に Android アプリ実装がある
- `MainActivity` で権限案内と開始 / 停止導線を提供している
- `OverlayService` で foreground service と overlay window を管理している
- 描画、色変更、太さ変更、透明度変更、線種切り替え、消しゴム、アンドゥ、リドゥ、全消しに対応している
- `Hide` で compact な `Tools` チップへ切り替えられる
- 設定パネルはヘッダー操作を残したまま、下側へ少しはみ出す位置まで移動できる
- クローズドテストとストア公開準備を進行中

## Implemented Features

- フローティングボタン表示とドラッグ移動
- 全画面キャンバス描画
- ペン / 消しゴム / アンドゥ / リドゥ / 全消し
- 色、太さ、透明度、線種の調整
- `Hide` による compact 表示
- 最小化チップ上の `ON / OFF` による compact 描画切り替え
- セッション終了時の描画クリア

## Known Gaps

- Android 12 から 15 の実機差分検証は継続中
- `HIDE_OVERLAY_WINDOWS` を使う一部アプリでは overlay が非表示になる可能性がある
- ストア審査向けの説明文、動画、ポリシー整備を進めている

## Release Strategy

- ファーストリリースは無料版の最小構成とする
- Billing や購入 UI は V1 には含めない
- 将来の premium 候補は、追加色、追加ペン種類、保存、共有など
- premium 候補は無料版から独立して導入できる設計を維持する

## Current Direction

- Platform: Android first
- Tech direction: Native Android, Kotlin, View based custom canvas
- Delivery style: closed testing -> polish -> public release
- Recommended V1 OS range: Android 12 to Android 15

## Repository Structure

- `app/`: Android アプリ本体
- `docs/PROJECT.md`: プロジェクト方針と役割
- `docs/v1-requirements.md`: V1 要件
- `docs/technical-design.md`: 実装方針と構成
- `docs/feasibility-study.md`: platform 制約と設計判断
- `docs/logs/`: 判断ログと handoff
- `app/src/test/`: 単体テスト

## Build Notes

- Android Studio と Android SDK が必要
- Java 17 を使用
- Minimum OS は Android 12 (`minSdk = 31`)

## Important Caveat

`TYPE_APPLICATION_OVERLAY` は Android 12 以降で touch pass-through に制約があります。  
そのため、表示専用状態では `LayoutParams.alpha` を `0.78` に固定し、下のアプリ操作を優先する設計を採用しています。詳しくは `docs/feasibility-study.md` を参照してください。
